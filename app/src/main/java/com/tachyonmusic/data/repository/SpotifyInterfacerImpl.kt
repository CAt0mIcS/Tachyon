package com.tachyonmusic.data.repository

import android.app.Activity
import android.content.Intent
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.Repeat
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import com.tachyonmusic.TachyonApplication
import com.tachyonmusic.core.ArtworkType
import com.tachyonmusic.core.RepeatMode
import com.tachyonmusic.core.data.RemoteArtwork
import com.tachyonmusic.core.data.playback.SpotifyPlaylist
import com.tachyonmusic.core.data.playback.SpotifySong
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.domain.model.PlaylistEntity
import com.tachyonmusic.database.domain.model.SongEntity
import com.tachyonmusic.database.domain.repository.DataRepository
import com.tachyonmusic.database.domain.repository.PlaylistRepository
import com.tachyonmusic.database.domain.repository.SettingsRepository
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.domain.repository.MediaBrowserController
import com.tachyonmusic.domain.repository.SpotifyInterfacer
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.playback_layers.toSpotifySong
import com.tachyonmusic.util.Duration
import com.tachyonmusic.util.IListenable
import com.tachyonmusic.util.Listenable
import com.tachyonmusic.util.delay
import com.tachyonmusic.util.ms
import com.tachyonmusic.util.runOnUiThreadAsync
import kaaes.spotify.webapi.android.SpotifyApi
import kaaes.spotify.webapi.android.models.Track
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import retrofit.RetrofitError
import java.net.URI

class SpotifyInterfacerImpl(
    private val application: TachyonApplication,
    private val songRepository: SongRepository,
    private val playlistRepository: PlaylistRepository,
    private val settingsRepository: SettingsRepository,
    private val dataRepository: DataRepository,
    private val log: Logger
) : SpotifyInterfacer, IListenable<MediaBrowserController.EventListener> by Listenable() {
    private var spotifyAppRemote: SpotifyAppRemote? = null
    private var api: SpotifyApi? = null

    private val ioScope = application.coroutineScope + Dispatchers.IO

    override val isAuthorized: Boolean
        get() = spotifyAppRemote != null && api != null

    private var userCountry: String = "UNKNOWN"

    init {
        ioScope.launch {
            if (!isAuthorized) {
                val accessToken = dataRepository.getData().spotifyAccessToken
                if (accessToken.isNotEmpty())
                    runOnUiThreadAsync { connectToSpotify(accessToken) }
            }

            // TODO: Can we do this in [onSpotifyConnected -> subscribeToPlayerState]?
            val updateInterval = settingsRepository.getSettings().audioUpdateInterval
            while (true) {
                currentPosition =
                    spotifyAppRemote?.playerApi?.playerState?.await()?.data?.playbackPosition?.ms
                delay(updateInterval)
            }
        }
    }

    override fun authorize(activity: Activity) {
        val builder = AuthorizationRequest.Builder(
            CLIENT_ID,
            AuthorizationResponse.Type.TOKEN,
            REDIRECT_URI
        )

        builder.setScopes(
            arrayOf(
                "app-remote-control",
                "user-read-playback-state",
                "user-modify-playback-state",
                "user-read-currently-playing",
                "playlist-read-private",
                "playlist-read-collaborative",
                "user-read-private" // is user premium
            )
        )
        val request = builder.build()

        AuthorizationClient.openLoginActivity(activity, AUTHORIZE_REQUEST_CODE, request)
    }

    override fun onAuthorization(requestCode: Int, resultCode: Int, intent: Intent?) {
        // Check if result comes from the correct activity
        if (requestCode == AUTHORIZE_REQUEST_CODE) {
            val response = AuthorizationClient.getResponse(resultCode, intent)
            when (response.type) {
                AuthorizationResponse.Type.TOKEN -> {
                    ioScope.launch {
                        dataRepository.update(spotifyAccessToken = response.accessToken)
                    }
                    connectToSpotify(response.accessToken)
                }

                AuthorizationResponse.Type.ERROR -> {
                    onAuthorizationError(response)
                }

                else -> {}
            }
        }
    }

    /**************************************************************************
     ********** PLAYBACK CONTROL
     *************************************************************************/
    override var currentPosition: Duration? = 0.ms
        private set

    private val _currentPlayback = MutableStateFlow<SpotifySong?>(null)
    override val currentPlayback = _currentPlayback.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying = _isPlaying.asStateFlow()

    private val _repeatMode = MutableStateFlow<RepeatMode>(RepeatMode.All)
    override val repeatMode = _repeatMode.asStateFlow()

    private val jobLock = Any()
    private var awaitingPlaybackStart: CompletableJob? = null

    override fun play(uri: String, index: Int?) {
        spotifyAppRemote?.playerApi?.play(uri)

        synchronized(jobLock) {
            awaitingPlaybackStart = Job()
        }

        ioScope.launch {
            awaitingPlaybackStart?.invokeOnCompletion {
                spotifyAppRemote?.playerApi?.skipToIndex(uri, index ?: return@invokeOnCompletion)
            }
        }
    }

    override fun resume() {
        awaitingPlaybackStart.invokeOnCompletionOrNull {
            spotifyAppRemote?.playerApi?.resume()
        }
    }

    override fun pause() {
        awaitingPlaybackStart.invokeOnCompletionOrNull {
            spotifyAppRemote?.playerApi?.pause()
        }
    }

    override fun seekTo(pos: Duration) {
        awaitingPlaybackStart.invokeOnCompletionOrNull {
            spotifyAppRemote?.playerApi?.seekTo(pos.inWholeMilliseconds)
        }
    }

    override fun seekTo(playlistUri: String, index: Int, pos: Duration?) {
        awaitingPlaybackStart.invokeOnCompletionOrNull {
            spotifyAppRemote?.playerApi?.skipToIndex(playlistUri, index)
            spotifyAppRemote?.playerApi?.seekTo(
                pos?.inWholeMilliseconds ?: return@invokeOnCompletionOrNull
            )
        }
    }

    override fun setRepeatMode(repeatMode: RepeatMode) {
        when (repeatMode) {
            is RepeatMode.All -> {
                spotifyAppRemote?.playerApi?.setRepeat(Repeat.ALL)
                spotifyAppRemote?.playerApi?.setShuffle(false)
            }

            is RepeatMode.One -> {
                spotifyAppRemote?.playerApi?.setRepeat(Repeat.ONE)
                spotifyAppRemote?.playerApi?.setShuffle(false)
            }

            is RepeatMode.Shuffle -> {
                spotifyAppRemote?.playerApi?.setRepeat(Repeat.ALL)
                spotifyAppRemote?.playerApi?.setShuffle(true)
            }

            is RepeatMode.Off -> {
                spotifyAppRemote?.playerApi?.setRepeat(Repeat.OFF)
                spotifyAppRemote?.playerApi?.setShuffle(false)
            }
        }

    }

    override suspend fun searchTracks(query: String) = withContext(Dispatchers.IO) {
        api!!.service.searchTracks(query).tracks.items.map {
            it.toSpotifySong(userCountry)
        }
    }

    override suspend fun searchPlaylists(query: String) = withContext(Dispatchers.IO) {
        api!!.service.searchPlaylists(query).playlists.items.map {
            SpotifyPlaylist(
                it.name,
                MediaId(it.uri),
                api!!.service.getPlaylistTracks(CLIENT_ID, it.id).items.map { track ->
                    track.track.toSpotifySong(userCountry)
                }.toMutableList()
            )
        }
    }


    override suspend fun disconnect() = withContext(Dispatchers.IO) {
        dataRepository.update(spotifyAccessToken = "")
        api?.setAccessToken("")
        api = null
        SpotifyAppRemote.disconnect(spotifyAppRemote)
        spotifyAppRemote = null
    }


    private fun connectToSpotify(accessToken: String) {
        log.debug("Connecting to spotify with accessToken: $accessToken")

        val connectionParams = ConnectionParams.Builder(CLIENT_ID)
            .setRedirectUri(REDIRECT_URI)
            .showAuthView(true)
            .build()

        SpotifyAppRemote.connect(
            application,
            connectionParams,
            object : Connector.ConnectionListener {
                override fun onConnected(appRemote: SpotifyAppRemote) {
                    spotifyAppRemote = appRemote
                    api = SpotifyApi()
                    api!!.setAccessToken(accessToken)
                    log.info("Spotify connected successfully")
                    onSpotifyConnected()
                }

                override fun onFailure(throwable: Throwable) {
                    log.error(throwable.message.toString())
                }
            })
    }

    private fun onAuthorizationError(response: AuthorizationResponse) {
        TODO(response.error)
    }

    private fun onSpotifyConnected() {
        ioScope.launch {
            // TODO: When does this Unauthorized exception occur?
            try {
                api!!.service.me
            } catch (e: RetrofitError) {
                log.error("Retrofit error occurred: " + e.message.toString())
                runOnUiThreadAsync { authorize(application.mainActivity!!) }
                return@launch
            }

            if (api?.service?.me?.product != "premium") {
                log.info("Disconnecting from Spotify due to invalid product state ${api?.service?.me?.product}")
                disconnect()
                // TODO: Show user toast that they need to have premium
                return@launch
            }

            userCountry = api!!.service!!.me!!.country

            val playlists = api!!.service.myPlaylists.items.map {
                val playlistTracks = api!!.service.getPlaylistTracks(
                    CLIENT_ID,
                    it.id
                ).items.map { playlistTrack ->
                    playlistTrack.track.toSongEntity(userCountry)
                }

                songRepository.addAll(playlistTracks)

                PlaylistEntity(
                    it.name,
                    MediaId(it.uri),
                    playlistTracks.map { track -> track.mediaId })
            }
            playlistRepository.addAll(playlists)

            spotifyAppRemote?.playerApi?.subscribeToPlayerState()?.setEventCallback { data ->
                if (currentPlayback.value?.mediaId?.source != data.track.uri)
                    if (correctInvalidRepeatMode(
                            data.playbackOptions.repeatMode,
                            data.playbackOptions.isShuffling
                        )
                    ) return@setEventCallback

                ioScope.launch {
                    val updatedEntity =
                        api!!.service.getTrack(data.track?.id)?.toSongEntity(userCountry)
                    val updatedSong = updatedEntity?.toSpotifySong()
                    var dispatchControl = false

                    synchronized(jobLock) {
                        awaitingPlaybackStart?.complete()
                        awaitingPlaybackStart = null
                    }

                    if (updatedEntity != null && updatedEntity.mediaId != currentPlayback.value?.mediaId) {
                        /**
                         * Add it in case playback was started in spotify before opening app
                         * If it's not added it won't show in history or the mini player since
                         * the mediaId it points to doesn't exist in the database
                         */
                        songRepository.addAll(listOf(updatedEntity))

                        /**
                         * Only pass control to [SpotifyMediaBrowserController] if it's the first
                         * item in history
                         */
                        if (dataRepository.getData().recentlyPlayedMediaId == updatedEntity.mediaId) {
                            dispatchControl = true
                        }

                        invokeEvent {
                            it.onMediaItemTransition(
                                updatedSong,
                                MediaBrowserController.PlaybackLocation.Spotify
                            )
                        }
                    }

                    _currentPlayback.update { updatedSong }
                    _isPlaying.update { !data.isPaused }
                    _repeatMode.update {
                        RepeatMode.fromSpotify(
                            data.playbackOptions.repeatMode,
                            data.playbackOptions.isShuffling
                        )
                    }

                    if (dispatchControl && !data.isPaused)
                        invokeEvent { it.onControlDispatched(MediaBrowserController.PlaybackLocation.Spotify) }
                }
            }
        }
    }

    /**
     * We don't have shuffle and repeat as separate things and you can only shuffle if you have
     * [Repeat.ALL] enabled
     *
     * @return true if repeat mode was corrected to app-supported one, false otherwise
     */
    private fun correctInvalidRepeatMode(repeatMode: Int, isShuffling: Boolean): Boolean {
        if ((repeatMode == Repeat.OFF || repeatMode == Repeat.ONE) && isShuffling) {
            setRepeatMode(RepeatMode.Shuffle)
            _repeatMode.update { RepeatMode.Shuffle }
            return true
        }
        return false
    }


    companion object {
        private const val AUTHORIZE_REQUEST_CODE = 4382
        private const val CLIENT_ID = "2a708447488345f3b1d0452821e269af"
        private const val REDIRECT_URI = "https://com.tachyonmusic/callback"
    }
}


private fun Track.toSpotifySong(userCountry: String): SpotifySong =
    toSongEntity(userCountry).let { entity ->
        SpotifySong(
            entity.mediaId,
            entity.title,
            entity.artist,
            entity.duration,
            entity.isHidden
        ).let {
            it.isPlayable = true
            it.artwork = RemoteArtwork(URI(entity.artworkUrl))
            it
        }
    }

private fun Track.toSongEntity(userCountry: String) = SongEntity(
    MediaId(uri),
    name,
    artists.firstOrNull()?.name ?: "Unknown Artist",
    duration_ms.ms,
    isHidden = true,
    isPlayable = userCountry in available_markets,
    if (album.images.firstOrNull() != null) ArtworkType.REMOTE else ArtworkType.NO_ARTWORK,
    album.images.firstOrNull()?.url
)

private val com.spotify.protocol.types.Track.id: String
    get() = uri.substring(uri.indexOfLast { it == ':' } + 1)


private fun RepeatMode.Companion.fromSpotify(repeatMode: Int, isShuffling: Boolean) =
    when (repeatMode) {
        Repeat.OFF -> RepeatMode.Off
        Repeat.ALL -> if (isShuffling) RepeatMode.Shuffle else RepeatMode.All
        Repeat.ONE -> RepeatMode.One
        else -> TODO("Invalid spotify repeat mode $repeatMode and shuffle $isShuffling")
    }

private fun CompletableJob?.invokeOnCompletionOrNull(action: () -> Unit) {
    if (this == null)
        action()
    else
        invokeOnCompletion { action() }
}