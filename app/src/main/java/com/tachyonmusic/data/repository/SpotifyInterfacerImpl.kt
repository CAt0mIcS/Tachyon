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
import com.tachyonmusic.core.data.playback.SpotifySong
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.database.domain.model.PlaylistEntity
import com.tachyonmusic.database.domain.model.SongEntity
import com.tachyonmusic.database.domain.repository.DataRepository
import com.tachyonmusic.database.domain.repository.PlaylistRepository
import com.tachyonmusic.database.domain.repository.SettingsRepository
import com.tachyonmusic.database.domain.repository.SongRepository
import com.tachyonmusic.domain.repository.SpotifyInterfacer
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.util.Duration
import com.tachyonmusic.util.delay
import com.tachyonmusic.util.ms
import com.tachyonmusic.util.runOnUiThreadAsync
import kaaes.spotify.webapi.android.SpotifyApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

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

    init {
        ioScope.launch {
            val accessToken = dataRepository.getData().spotifyAccessToken
            if (accessToken.isNotEmpty())
                runOnUiThreadAsync { connectToSpotify(accessToken) }
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

    private fun connectToSpotify(accessToken: String) {
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
            val playlists = api!!.service.myPlaylists.items.map {
                val playlistTracks = api!!.service.getPlaylistTracks(
                    CLIENT_ID,
                    it.id
                ).items.map { playlistTrack ->
                    val track = playlistTrack.track
                    SongEntity(
                        MediaId(track.uri),
                        track.name,
                        track.artists.firstOrNull()?.name ?: "Unknown Artist",
                        track.duration_ms.ms,
                        if (track.album.images.firstOrNull() != null) ArtworkType.REMOTE else ArtworkType.NO_ARTWORK,
                        track.album.images.firstOrNull()?.url
                    )
                }

                songRepository.addAll(playlistTracks)

                PlaylistEntity(
                    it.name,
                    MediaId(it.uri),
                    playlistTracks.map { track -> track.mediaId })
            }
            playlistRepository.addAll(playlists)

            spotifyAppRemote?.playerApi?.subscribeToPlayerState()?.setEventCallback { data ->
                _currentPlayback.update {
                    SpotifySong(
                        MediaId(data.track.uri),
                        data.track.name,
                        data.track.artists.firstOrNull()?.name ?: "Unknown Artist",
                        data.track.duration.ms
                    )
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

    companion object {
        private const val AUTHORIZE_REQUEST_CODE = 4382
        private const val CLIENT_ID = "2a708447488345f3b1d0452821e269af"
        private const val REDIRECT_URI = "https://com.tachyonmusic/callback"
    }
}