package com.tachyonmusic.media.service

import android.app.PendingIntent
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.media3.cast.CastPlayer
import androidx.media3.cast.SessionAvailabilityListener
import androidx.media3.common.*
import androidx.media3.common.util.UnstableApi
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
import com.tachyonmusic.core.data.playback.LocalPlaylist
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.Remix
import com.tachyonmusic.database.domain.repository.DataRepository
import com.tachyonmusic.database.domain.repository.PlaylistRepository
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
import com.tachyonmusic.media.domain.use_case.SearchStoredPlaybacks
import com.tachyonmusic.media.util.*
import com.tachyonmusic.playback_layers.domain.GetPlaylistForPlayback
import com.tachyonmusic.playback_layers.domain.PlaybackRepository
import com.tachyonmusic.playback_layers.domain.PredefinedPlaylistsRepository
import com.tachyonmusic.playback_layers.predefinedRemixPlaylistMediaId
import com.tachyonmusic.playback_layers.predefinedSongPlaylistMediaId
import com.tachyonmusic.util.future
import com.tachyonmusic.util.ms
import com.tachyonmusic.util.runOnUiThread
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.lang.Integer.max
import javax.inject.Inject

/**
 * DOCUMENTATION
 * https://developer.android.com/guide/topics/media/media3
 */

@UnstableApi
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

    @Inject
    lateinit var predefinedPlaylistsRepository: PredefinedPlaylistsRepository

    @Inject
    lateinit var playlistRepository: PlaylistRepository

    @Inject
    lateinit var searchStoredPlaybacks: SearchStoredPlaybacks

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

        audioEffectController.playbackParams.onEach { params ->
            exoPlayer.playbackParameters = androidx.media3.common.PlaybackParameters(
                params.speed,
                params.pitch
            )
            exoPlayer.volume = params.volume
            castPlayer?.playbackParameters = exoPlayer.playbackParameters
            castPlayer?.volume = exoPlayer.volume
        }.launchIn(ioScope + Dispatchers.Main)

        audioEffectController.reverbEnabled.onEach { enabled ->
            if (enabled) {
                currentPlayer.setAuxEffectInfo(
                    AuxEffectInfo(
                        audioEffectController.reverbAudioEffectId!!,
                        1F
                    )
                )
            } else {
                val effectInfo = AuxEffectInfo(AuxEffectInfo.NO_AUX_EFFECT_ID, 0F)
                exoPlayer.setAuxEffectInfo(effectInfo)
            }
        }.launchIn(ioScope + Dispatchers.Main)

        // TODO: Set aux effects for [CastPlayer]

        audioEffectController.updateAudioSessionId(exoPlayer.audioSessionId)

//        settingsRepository.observe().onEach {
//            switchPlayer(exoPlayer, buildExoPlayer(!it.ignoreAudioFocus))
//        }.launchIn(ioScope)

        exoPlayer.addListener(this)
        castPlayer?.addListener(this)

        mediaSession = with(
            MediaLibrarySession.Builder(this, currentPlayer, MediaLibrarySessionCallback())
        ) {
            setId(packageName)
            packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
                setSessionActivity(
                    PendingIntent.getActivity(
                        this@MediaPlaybackService,
                        0,
                        sessionIntent,
                        PendingIntent.FLAG_IMMUTABLE
                    )
                )
            }
            build()
        }
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

            log.info("Dispatching StateUpdateEvent with $playback, playWhenReady=${currentPlayer.playWhenReady}, and repeatMode=${currentPlayer.coreRepeatMode}")
            mediaSession.dispatchMediaEvent(
                StateUpdateEvent(
                    playback,
                    getCurrentPlaylist(),
                    currentPlayer.playWhenReady,
                    currentPlayer.coreRepeatMode
                )
            )

            mediaSession.dispatchMediaEvent(AudioSessionIdChangedEvent(exoPlayer.audioSessionId))

            mediaSession.setCustomLayout(
                controller,
                buildCustomNotificationLayout(currentPlayer.coreRepeatMode)
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
                    putBoolean("android.media.browse.SEARCH_SUPPORTED", true)
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

        override fun onSearch(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            query: String,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<Void>> = future(Dispatchers.IO) {
            val items = executeSearch(query, params?.extras ?: Bundle())
            mediaSession.notifySearchResultChanged(browser, query, items.size, params)
            LibraryResult.ofVoid()
        }

        override fun onGetSearchResult(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            query: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> = future(Dispatchers.IO) {
            val items = executeSearch(query, params?.extras ?: Bundle())
            val fromIndex = max((page - 1) * pageSize, items.size - 1)
            val toIndex = max(fromIndex + pageSize, items.size)
            LibraryResult.ofItemList(items.subList(fromIndex, toIndex), params)
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
                is SeekToTimingDataIndexEvent -> handleSeekToTimingDataIndexEvent(event)
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
                /**
                 * When clicking on any item in Android Auto the clicked item will be the only
                 * item in [mediaItems]. So if we click on a playlist in Android Auto the playlist
                 * media id will be [mediaItems.first().mediaId] and [mediaItems.size] will be 1
                 */
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

                    launch {
                        addNewPlaybackToHistory(playlist.current)
                    }
                    runOnUiThread {
                        handleSetRepeatModeEvent(SetRepeatModeEvent(dataRepository.getData().repeatMode))
                    }

                    MediaSession.MediaItemsWithStartPosition(
                        playlist.playbacks.toMediaItems(),
                        playlist.currentPlaylistIndex,
                        startPositionMs
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
            runBlocking {
                dataRepository.update(repeatMode = event.repeatMode)
            }
            mediaSession.setCustomLayout(buildCustomNotificationLayout(event.repeatMode))
        }

        private fun handleSetTimingDataEvent(event: SetTimingDataEvent) {
            currentPlayer.updateTimingDataOfCurrentPlayback(event.timingData)
        }

        private fun handleSeekToTimingDataIndexEvent(event: SeekToTimingDataIndexEvent) {
            currentPlayer.seekToTimingDataIndex(event.index)
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

        when (playback) {
            is Remix -> {
                if (audioEffectController.setBassEnabled(playback.bassBoostEnabled))
                    audioEffectController.setBass(playback.bassBoost!!)
                if (audioEffectController.setVirtualizerEnabled(playback.virtualizerEnabled))
                    audioEffectController.setVirtualizerStrength(playback.virtualizerStrength!!)
                if (audioEffectController.setReverbEnabled(playback.reverbEnabled))
                    audioEffectController.setReverb(playback.reverb!!)

                audioEffectController.setPlaybackParameters(
                    playback.playbackParameters ?: PlaybackParameters()
                )

                if (audioEffectController.setEqualizerEnabled(playback.equalizerEnabled)) {

                    playback.equalizerBands?.forEach { equalizerBand ->
                        // TODO: Do we need all this information to differentiate different bands?
                        audioEffectController.getEqualizerBandIndex(
                            equalizerBand.lowerBandFrequency,
                            equalizerBand.upperBandFrequency,
                            equalizerBand.centerFrequency
                        )?.let { band ->
                            audioEffectController.setEqualizerBandLevel(band, equalizerBand.level)
                        }
                    }
                }
            }

            else -> {
                audioEffectController.setBassEnabled(false)
                audioEffectController.setVirtualizerEnabled(false)
                audioEffectController.setReverbEnabled(false)
                audioEffectController.setPlaybackParameters(PlaybackParameters())
                audioEffectController.setEqualizerEnabled(false)
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

    private fun getCurrentPlaylist(): LocalPlaylist? {
        val items = currentPlayer.mediaItems
        if (items.isEmpty())
            return null

        val mediaId =
            when (val mediaIds = items.map { MediaId.deserialize(it.mediaId) }) {
                predefinedPlaylistsRepository.songPlaylist.value.map { it.mediaId } ->
                    predefinedSongPlaylistMediaId

                predefinedPlaylistsRepository.remixPlaylist.value.map { it.mediaId } ->
                    predefinedRemixPlaylistMediaId

                else -> findPlaylistWithPlaybacks(mediaIds)
            } ?: return null

        return LocalPlaylist(
            mediaId,
            items.mapNotNull { it.mediaMetadata.playback }.toMutableList(),
            currentPlayer.currentMediaItemIndex
        )
    }

    private fun findPlaylistWithPlaybacks(mediaIds: List<MediaId>): MediaId? = runBlocking {
        playlistRepository.getPlaylists().find { it.items == mediaIds }?.mediaId
    }

    private suspend fun executeSearch(query: String, extras: Bundle): List<MediaItem> {
        return when (extras.getString(MediaStore.EXTRA_MEDIA_FOCUS)) {
            MediaStore.Audio.Playlists.ENTRY_CONTENT_TYPE -> {
                val playlist = extras.getString(MediaStore.EXTRA_MEDIA_ALBUM)
                searchStoredPlaybacks.byPlaylist(playlist).map { it.toMediaItem() }
            }

            MediaStore.Audio.Media.ENTRY_CONTENT_TYPE -> {
                val title = extras.getString(MediaStore.EXTRA_MEDIA_TITLE)
                val artist = extras.getString(MediaStore.EXTRA_MEDIA_ARTIST)
                searchStoredPlaybacks.byTitleArtist(title, artist).map { it.toMediaItem() }
            }

            else -> {
                // if query is blank search will return all playbacks (for commands like "play some music")
                searchStoredPlaybacks(query).map { it.toMediaItem() }
            }
        }
    }

    private inner class CustomPlayerEventListener : CustomPlayer.Listener {
        override fun onTimingDataUpdated(controller: TimingDataController?) {
            mediaSession.dispatchMediaEvent(TimingDataUpdatedEvent(controller))
        }
    }
}
