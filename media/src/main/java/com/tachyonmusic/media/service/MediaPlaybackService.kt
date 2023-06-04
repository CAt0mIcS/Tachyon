package com.tachyonmusic.media.service

import android.os.Bundle
import android.widget.Toast
import androidx.media3.cast.CastPlayer
import androidx.media3.cast.SessionAvailabilityListener
import androidx.media3.common.*
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.util.EventLogger
import androidx.media3.session.*
import com.google.android.gms.cast.framework.CastContext
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.tachyonmusic.core.ArtworkType
import com.tachyonmusic.core.PlaybackParameters
import com.tachyonmusic.core.RepeatMode
import com.tachyonmusic.core.data.RemoteArtwork
import com.tachyonmusic.core.data.constants.MetadataKeys
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.CustomizedSong
import com.tachyonmusic.database.domain.repository.DataRepository
import com.tachyonmusic.database.domain.repository.RecentlyPlayed
import com.tachyonmusic.database.domain.repository.SettingsRepository
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.media.R
import com.tachyonmusic.media.core.*
import com.tachyonmusic.media.data.*
import com.tachyonmusic.media.domain.AudioEffectController
import com.tachyonmusic.media.domain.CastWebServerController
import com.tachyonmusic.media.domain.CustomPlayer
import com.tachyonmusic.media.domain.use_case.AddNewPlaybackToHistory
import com.tachyonmusic.media.domain.use_case.SaveRecentlyPlayed
import com.tachyonmusic.media.util.*
import com.tachyonmusic.playback_layers.domain.GetPlaylistForPlayback
import com.tachyonmusic.playback_layers.domain.PlaybackRepository
import com.tachyonmusic.util.future
import com.tachyonmusic.util.ms
import com.tachyonmusic.util.runOnUiThread
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

/**
 * DOCUMENTATION
 * https://developer.android.com/guide/topics/media/media3
 */

@AndroidEntryPoint
class MediaPlaybackService : MediaLibraryService(), Player.Listener {

    private lateinit var exoPlayer: ExoPlayer
    private lateinit var currentPlayer: CustomPlayer

    @Inject
    lateinit var browserTree: BrowserTree

    @Inject
    lateinit var playbackRepository: PlaybackRepository

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var saveRecentlyPlayed: SaveRecentlyPlayed

    @Inject
    lateinit var addNewPlaybackToHistory: AddNewPlaybackToHistory

    @Inject
    lateinit var dataRepository: DataRepository

    @Inject
    lateinit var audioEffectController: AudioEffectController

    @Inject
    lateinit var getPlaylistForPlayback: GetPlaylistForPlayback

    @Inject
    lateinit var castWebServerController: CastWebServerController

    @Inject
    lateinit var log: Logger

    private val ioScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private lateinit var mediaSession: MediaLibrarySession

    private val castPlayer: CastPlayer? by lazy {
        try {
            val castContext = CastContext.getSharedInstance(this)
            CastPlayer(castContext, CastMediaItemConverter(castWebServerController)).apply {
                setSessionAvailabilityListener(CastSessionAvailabilityListener())
                addListener(this@MediaPlaybackService)
            }
        } catch (e: Exception) {
            log.debug(
                "Cast is not available on this device. " +
                        "Exception thrown when attempting to obtain CastContext. " + e.message
            )
            null
        }
    }

    override fun onCreate() {
        super.onCreate()

        runBlocking {
            exoPlayer = buildExoPlayer(!settingsRepository.getSettings().ignoreAudioFocus)
        }

        currentPlayer = CustomPlayerImpl(
            if (castPlayer?.isCastSessionAvailable == true)
                castPlayer!!
            else
                exoPlayer,
            log
        ).apply {
            registerEventListener(CustomPlayerEventListener())
        }

        audioEffectController.controller = object : AudioEffectController.PlaybackController {
            override fun onNewPlaybackParameters(params: PlaybackParameters) {
                exoPlayer.playbackParameters = PlaybackParameters(
                    params.speed,
                    params.pitch
                )
                exoPlayer.volume = params.volume
                castPlayer?.playbackParameters = exoPlayer.playbackParameters
                castPlayer?.volume = exoPlayer.volume
            }

            override fun onReverbToggled(enabled: Boolean, effectId: Int) {
                if (enabled) {
                    currentPlayer.setAuxEffectInfo(AuxEffectInfo(effectId, 1F))
                } else {
                    val effectInfo = AuxEffectInfo(AuxEffectInfo.NO_AUX_EFFECT_ID, 0F)
                    exoPlayer.setAuxEffectInfo(effectInfo)
                }

                // TODO: Set aux effects for [CastPlayer]
            }
        }
        audioEffectController.updateAudioSessionId(exoPlayer.audioSessionId)

//        settingsRepository.observe().onEach {
//            switchPlayer(exoPlayer, buildExoPlayer(!it.ignoreAudioFocus))
//        }.launchIn(ioScope)

        setMediaNotificationProvider(MediaNotificationProvider(this))

        exoPlayer.addListener(this)
        castPlayer?.addListener(this)

        mediaSession =
            MediaLibrarySession.Builder(this, currentPlayer, MediaLibrarySessionCallback()).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession =
        mediaSession

    override fun onDestroy() {
        super.onDestroy()
        ioScope.coroutineContext.cancelChildren()

        audioEffectController.release()

        exoPlayer.release()
        castPlayer?.release()
        mediaSession.release()
    }

    private inner class MediaLibrarySessionCallback : MediaLibrarySession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult = supportedCommands

        override fun onPostConnect(session: MediaSession, controller: MediaSession.ControllerInfo) {
            val playback = currentPlayer.mediaMetadata.playback
            log.info("Dispatching StateUpdateEvent with $playback and playWhenReady=${currentPlayer.playWhenReady}")
            mediaSession.dispatchMediaEvent(
                StateUpdateEvent(
                    playback,
                    currentPlayer.playWhenReady
                )
            )

            mediaSession.dispatchMediaEvent(AudioSessionIdChangedEvent(exoPlayer.audioSessionId))

            mediaSession.setCustomLayout(
                controller,
                buildCustomNotificationLayout(
                    if (currentPlayer.repeatMode == Player.REPEAT_MODE_OFF) RepeatMode.All
                    else currentPlayer.coreRepeatMode
                )
            )
        }

        override fun onGetLibraryRoot(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<MediaItem>> {
            val maximumRootChildLimit = params?.extras?.getInt(
                MediaConstants.EXTRAS_KEY_ROOT_CHILDREN_LIMIT, 4
            ) ?: 4
            browserTree.maximumRootChildLimit = maximumRootChildLimit

            /**
             * Define app globals how media is displayed in Android Auto
             */
            val extras = LibraryParams.Builder().apply {
                setExtras(Bundle().apply {
                })

                setOffline(true)
                setSuggested(true)
                setRecent(false)
            }.build()

            return Futures.immediateFuture(LibraryResult.ofItem(browserTree.root, extras))
        }

        override fun onGetChildren(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> = future(Dispatchers.IO) {
            log.debug("Started onGetChildren")
            val items = browserTree.get(parentId, page, pageSize)
            if (items != null)
                return@future LibraryResult.ofItemList(items, null)
            LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE)
        }

        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>
        ): ListenableFuture<List<MediaItem>> = future(Dispatchers.IO) {
            mediaItems.mapNotNull {
                it.buildUpon()
                    .setUri(it.mediaMetadata.playback?.uri ?: return@mapNotNull null)
                    .build()
            }
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            when (val event = customCommand.toMediaBrowserEvent(args)) {
                is SetRepeatModeEvent -> handleSetRepeatModeEvent(event)
                is SetTimingDataEvent -> handleSetTimingDataEvent(event)
            }

            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }

        override fun onSetMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>,
            startIndex: Int,
            startPositionMs: Long
        ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
            return if (mediaItems.size == 1 && startIndex == C.INDEX_UNSET) {
                future(Dispatchers.IO) {
                    val mediaId = MediaId.deserializeIfValid(mediaItems.first().mediaId)
                    val playlist = if (mediaId?.isLocalPlaylist == true) {
                        playbackRepository.getPlaylists().find { it.mediaId == mediaId }
                    } else {
                        getPlaylistForPlayback(mediaId)
                    } ?: return@future MediaSession.MediaItemsWithStartPosition(
                        mediaItems,
                        startIndex,
                        startPositionMs
                    )

                    val playlistMediaItems = playlist.playbacks.toMediaItems()
                    runOnUiThread {
                        currentPlayer.setMediaItems(playlistMediaItems)
                        currentPlayer.prepare()
                    }

                    MediaSession.MediaItemsWithStartPosition(
                        playlistMediaItems, playlist.currentPlaylistIndex, startPositionMs
                    )
                }

            } else
                super.onSetMediaItems(
                    mediaSession,
                    controller,
                    mediaItems,
                    startIndex,
                    startPositionMs
                )
        }

        private fun handleSetRepeatModeEvent(event: SetRepeatModeEvent) {
            exoPlayer.coreRepeatMode = event.repeatMode
            // castPlayer?.coreRepeatMode = event.repeatMode // TODO: Set repeat mode for cast player
            mediaSession.setCustomLayout(buildCustomNotificationLayout(event.repeatMode))
        }

        private fun handleSetTimingDataEvent(event: SetTimingDataEvent) {
            currentPlayer.updateTimingDataOfCurrentPlayback(event.timingData)
        }
    }


    private inner class CastSessionAvailabilityListener : SessionAvailabilityListener {
        override fun onCastSessionAvailable() {
            castWebServerController.start(currentPlayer.mediaItems.map { it.localConfiguration!!.uri })
            currentPlayer.setPlayer(castPlayer!!)
        }

        override fun onCastSessionUnavailable() {
            currentPlayer.setPlayer(exoPlayer)
            castWebServerController.stop()
            Player.COMMAND_ADJUST_DEVICE_VOLUME
        }

    }


    /**************************************************************************
     ********** [Player.Listener]
     *************************************************************************/
    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        val playback = mediaItem?.mediaMetadata?.playback ?: return

        // TODO: Update equalizer
        when (playback) {
            is CustomizedSong -> {
                if (playback.bassBoostEnabled) {
                    audioEffectController.bassEnabled = true
                    audioEffectController.bass = playback.bassBoost
                }

                if (playback.virtualizerEnabled) {
                    audioEffectController.virtualizerEnabled = true
                    audioEffectController.virtualizerStrength = playback.virtualizerStrength
                }

                audioEffectController.playbackParams =
                    playback.playbackParameters ?: PlaybackParameters(
                        speed = 1f,
                        pitch = 1f,
                        volume = 1f
                    )

                if (playback.equalizerEnabled) {
                    audioEffectController.equalizerEnabled = true

                    playback.equalizerBands?.forEach { equalizerBand ->
                        // TODO: Do we need all this information to differentiate different bands?
                        audioEffectController.getBandIndex(
                            equalizerBand.lowerBandFrequency,
                            equalizerBand.upperBandFrequency,
                            equalizerBand.centerFrequency
                        )?.let { band ->
                            audioEffectController.setBandLevel(band, equalizerBand.level)
                        }
                    }
                }

                if (playback.reverbEnabled) {
                    audioEffectController.reverbEnabled = true
                    audioEffectController.reverb = playback.reverb
                }
            }
            else -> {
                audioEffectController.bass = null
                audioEffectController.virtualizerStrength = null
                audioEffectController.playbackParams = PlaybackParameters(
                    speed = 1f,
                    pitch = 1f,
                    volume = 1f
                )
                audioEffectController.reverb = null
                audioEffectController.equalizerEnabled = false
            }
        }

        if (reason != Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED) {
            ioScope.launch {
                addNewPlaybackToHistory(playback)
            }
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        if (!isPlaying) {
            val playback = currentPlayer.currentMediaItem?.mediaMetadata?.playback ?: return
            val currentPos = currentPlayer.currentPosition.ms
            ioScope.launch {
                saveRecentlyPlayed(
                    RecentlyPlayed(
                        playback.mediaId,
                        currentPos,
                        playback.duration,
                        ArtworkType.getType(playback),
                        if (playback.artwork is RemoteArtwork)
                            (playback.artwork as RemoteArtwork).uri.toURL()
                                .toString() else null
                    )
                )
            }
        }
    }

    override fun onPositionDiscontinuity(
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: Int
    ) {
        if (reason == Player.DISCONTINUITY_REASON_SEEK || reason == Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT) {
            ioScope.launch {
                val data = dataRepository.getData()
                dataRepository.update(
                    recentlyPlayed = RecentlyPlayed(
                        mediaId = data.recentlyPlayedMediaId ?: return@launch,
                        position = newPosition.contentPositionMs.ms,
                        duration = data.recentlyPlayedDuration,
                        artworkType = data.recentlyPlayedArtworkType,
                        artworkUrl = data.recentlyPlayedArtworkUrl
                    )
                )
            }
        }
    }

    override fun onPlayerError(error: PlaybackException) {
        var message = R.string.generic_error
        log.error("Player error: ${error.errorCodeName} (${error.errorCode}): ${error.message}")
        if (error.errorCode == PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS
            || error.errorCode == PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND
        ) {
            message = R.string.error_media_not_found
        }
        Toast.makeText(
            applicationContext,
            message,
            Toast.LENGTH_LONG
        ).show()
    }


    /**************************************************************************
     ********** HELPER FUNCTIONS
     *************************************************************************/
    private fun buildExoPlayer(handleAudioFocus: Boolean): ExoPlayer =
        ExoPlayer.Builder(this).apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                handleAudioFocus
            )
            setHandleAudioBecomingNoisy(true)
        }.build().apply {
            // TODO: Debug only
            addAnalyticsListener(EventLogger())
        }

    private fun buildCustomNotificationLayout(repeatMode: RepeatMode) = listOf(
        CommandButton.Builder().apply {
            setIconResId(repeatMode.icon)
            setDisplayName("Repeat Mode")
            setSessionCommand(
                SessionCommand(SetRepeatModeEvent.action, Bundle().apply {
                    putInt(MetadataKeys.RepeatMode, repeatMode.next.id)
                })
            )
            setEnabled(true)
        }.build()
    )

    private inner class CustomPlayerEventListener : CustomPlayer.Listener {
        override fun onTimingDataUpdated(controller: TimingDataController?) {
            mediaSession.dispatchMediaEvent(TimingDataUpdatedEvent(controller))
        }
    }
}
