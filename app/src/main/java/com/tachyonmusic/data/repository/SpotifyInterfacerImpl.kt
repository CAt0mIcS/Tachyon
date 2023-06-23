package com.tachyonmusic.data.repository

import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.ImageUri
import com.spotify.protocol.types.Repeat
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import com.tachyonmusic.TachyonApplication
import com.tachyonmusic.core.ArtworkType
import com.tachyonmusic.core.RepeatMode
import com.tachyonmusic.core.data.EmbeddedArtwork
import com.tachyonmusic.core.data.RemoteArtwork
import com.tachyonmusic.core.data.playback.SpotifyPlaylist
import com.tachyonmusic.core.data.playback.SpotifySong
import com.tachyonmusic.core.domain.Artwork
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.domain.model.PlaylistEntity
import com.tachyonmusic.database.domain.model.SongEntity
import com.tachyonmusic.database.domain.repository.DataRepository
import com.tachyonmusic.database.domain.repository.PlaylistRepository
import com.tachyonmusic.database.domain.repository.SettingsRepository
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.domain.repository.SpotifyInterfacer
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.permission.toSong
import com.tachyonmusic.util.Duration
import com.tachyonmusic.util.delay
import com.tachyonmusic.util.ms
import com.tachyonmusic.util.runOnUiThreadAsync
import kaaes.spotify.webapi.android.SpotifyApi
import kaaes.spotify.webapi.android.models.Track
import kotlinx.coroutines.Dispatchers
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
) : SpotifyInterfacer {
    private var spotifyAppRemote: SpotifyAppRemote? = null
    private var api: SpotifyApi? = null

    private val ioScope = application.coroutineScope + Dispatchers.IO

    override val isAuthorized: Boolean
        get() = spotifyAppRemote != null && api != null

    init {
        ioScope.launch {
            if (!isAuthorized) {
                val accessToken = dataRepository.getData().spotifyAccessToken
                if (accessToken.isNotEmpty())
                    runOnUiThreadAsync { connectToSpotify(accessToken) }
            }
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
                "playlist-read-collaborative"
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

    override fun play(uri: String, index: Int?) {
        spotifyAppRemote?.playerApi?.play(uri)
        spotifyAppRemote?.playerApi?.skipToIndex(uri, index ?: return)
    }

    override fun resume() {
        spotifyAppRemote?.playerApi?.resume()
    }

    override fun pause() {
        spotifyAppRemote?.playerApi?.pause()
    }

    override fun seekTo(pos: Duration) {
        spotifyAppRemote?.playerApi?.seekTo(pos.inWholeMilliseconds)
    }

    override fun seekTo(playlistUri: String, index: Int, pos: Duration) {
        spotifyAppRemote?.playerApi?.skipToIndex(playlistUri, index)
        spotifyAppRemote?.playerApi?.seekTo(pos.inWholeMilliseconds)
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
        }

    }

    override suspend fun searchTracks(query: String) = withContext(Dispatchers.IO) {
        api!!.service.searchTracks(query).tracks.items.map {
            it.toSpotifySong()
        }
    }

    override suspend fun searchPlaylists(query: String) = withContext(Dispatchers.IO) {
        api!!.service.searchPlaylists(query).playlists.items.map {
            SpotifyPlaylist(
                it.name,
                MediaId(it.uri),
                api!!.service.getPlaylistTracks(CLIENT_ID, it.id).items.map { track ->
                    track.track.toSpotifySong()
                }.toMutableList()
            )
        }
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

            val playlists = api!!.service.myPlaylists.items.map {
                val playlistTracks = api!!.service.getPlaylistTracks(
                    CLIENT_ID,
                    it.id
                ).items.map { playlistTrack ->
                    playlistTrack.track.toSongEntity()
                }

                songRepository.addAll(playlistTracks)

                PlaylistEntity(
                    it.name,
                    MediaId(it.uri),
                    playlistTracks.map { track -> track.mediaId })
            }
            playlistRepository.addAll(playlists)

            spotifyAppRemote?.playerApi?.subscribeToPlayerState()?.setEventCallback { data ->
                ioScope.launch {
                    val updatedSong = data.track?.toSpotifySong(getImage(data.track?.imageUri))
                    _currentPlayback.update { updatedSong }
                }

                _isPlaying.update { !data.isPaused }
                _repeatMode.update {
                    when (data.playbackOptions.repeatMode) {
                        Repeat.ALL -> if (data.playbackOptions.isShuffling) RepeatMode.Shuffle else RepeatMode.All
                        else -> RepeatMode.One  // Default to RepeatMode.One
                    }
                }
            }
        }
    }

    private suspend fun getImage(image: ImageUri?) = withContext(Dispatchers.IO) {
        if (image == null)
            return@withContext null

        try {
            EmbeddedArtwork(
                spotifyAppRemote?.imagesApi?.getImage(image)?.await()?.data
                    ?: return@withContext null,
                Uri.parse(image.raw)
            )
        } catch (e: RetrofitError) {
            log.error("Network error while trying to get artwork image for ${image.raw.toString()}")
            error(e.stackTraceToString())
        }
    }


    companion object {
        private const val AUTHORIZE_REQUEST_CODE = 4382
        private const val CLIENT_ID = "2a708447488345f3b1d0452821e269af"
        private const val REDIRECT_URI = "https://com.tachyonmusic/callback"
    }
}


private fun Track.toSpotifySong(): SpotifySong = toSongEntity().let {
    it.toSong(true, RemoteArtwork(URI(it.artworkUrl))) as SpotifySong
}

private fun Track.toSongEntity() = SongEntity(
    MediaId(uri),
    name,
    artists.firstOrNull()?.name ?: "Unknown Artist",
    duration_ms.ms,
    if (album.images.firstOrNull() != null) ArtworkType.REMOTE else ArtworkType.NO_ARTWORK,
    album.images.firstOrNull()?.url
)

private fun com.spotify.protocol.types.Track.toSpotifySong(artwork: Artwork?): SpotifySong =
    SpotifySong(
        MediaId(uri),
        name,
        artists.firstOrNull()?.name ?: "Unknown Artist",
        duration.ms
    ).apply {
        this.artwork = artwork
    }