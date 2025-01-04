package com.tachyonmusic.media.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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
import com.tachyonmusic.core.RepeatMode
import com.tachyonmusic.core.data.RemoteArtwork
import com.tachyonmusic.core.data.constants.MetadataKeys
import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.core.domain.MediaId
import com.tachyonmusic.core.domain.TimingData
import com.tachyonmusic.core.domain.TimingDataController
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Playlist
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
import com.tachyonmusic.playback_layers.domain.events.PlayerMessageEvent
import com.tachyonmusic.util.EventSeverity
import com.tachyonmusic.util.UiText
import com.tachyonmusic.util.domain.EventChannel
import com.tachyonmusic.util.future
import com.tachyonmusic.util.ms
import com.tachyonmusic.util.runOnUiThread
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.lang.Integer.max
import javax.inject.Inject
import kotlin.random.Random

/**
 * DOCUMENTATION
 * https://developer.android.com/guide/topics/media/media3
 */

@UnstableApi
@AndroidEntryPoint
open class MediaPlaybackService : MediaLibraryService(), Player.Listener {

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

    @Inject
    lateinit var eventChannel: EventChannel

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

    private var currentPlayback: Playback? = null
    private var currentPlaylist: Playlist? = null


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

        mediaSession =
            with(MediaLibrarySession.Builder(this, currentPlayer, MediaLibrarySessionCallback())) {
                getSingleTopActivity()?.let { setSessionActivity(it) }
                build()
            }

        setListener(MediaSessionServiceListener())
    }

    /**
     * Returns the single top session activity. It is used by the notification when the app task is
     * active and an activity is in the fore or background.
     *
     * Tapping the notification then typically should trigger a single top activity. This way, the
     * user navigates to the previous activity when pressing back.
     *
     * If null is returned, [MediaSession.setSessionActivity] is not set by the demo service.
     */
    open fun getSingleTopActivity(): PendingIntent? = null

    /**
     * Returns a back stacked session activity that is used by the notification when the service is
     * running standalone as a foreground service. This is typically the case after the app has been
     * dismissed from the recent tasks, or after automatic playback resumption.
     *
     * Typically, a playback activity should be started with a stack of activities underneath. This
     * way, when pressing back, the user doesn't land on the home screen of the device, but on an
     * activity defined in the back stack.
     *
     * See [androidx.core.app.TaskStackBuilder] to construct a back stack.
     *
     * If null is returned, [MediaSession.setSessionActivity] is not set by the demo service.
     */
    open fun getBackStackedActivity(): PendingIntent? = null

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession =
        mediaSession

//    override fun onTaskRemoved(rootIntent: Intent?) {
//        super.onTaskRemoved(rootIntent)
//
//        // Remove the notification if the playback is paused in the app and it's then swiped from the backstack
//        // to avoid stale notification
//        if (!exoPlayer.playWhenReady)
//            mediaSession.release()
//    }

    override fun onDestroy() {
        getBackStackedActivity()?.let { mediaSession.setSessionActivity(it) }
        ioScope.coroutineContext.cancelChildren()
        audioEffectController.release()
        mediaSession.release()
        exoPlayer.release()
        castPlayer?.release()
        clearListener()

        super.onDestroy()
    }

    private inner class MediaLibrarySessionCallback : MediaLibrarySession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult = supportedCommands

        override fun onPlaybackResumption(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo
        ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> = future(Dispatchers.IO) {
            // TODO: Test
            log.debug("onPlaybackResumption with playlist: $currentPlaylist, playback: $currentPlayback")
            val recentlyPlayed = dataRepository.getData().currentPositionInRecentlyPlayedPlayback

            if (currentPlaylist == null)
                MediaSession.MediaItemsWithStartPosition(
                    emptyList(), C.INDEX_UNSET, C.TIME_UNSET
                )
            else
                MediaSession.MediaItemsWithStartPosition(
                    currentPlaylist!!.playbacks.map { it.toMediaItem() },
                    currentPlaylist!!.currentPlaylistIndex,
                    recentlyPlayed.inWholeMilliseconds
                )
        }

        override fun onPostConnect(session: MediaSession, controller: MediaSession.ControllerInfo) {
            /**
             * TODO
             *  When setting some playback parameters (speed, reverb, timing data), then closing
             *  the app (swiping from backstack), and then pausing the playback through the media
             *  notification: When opening the app again and starting the recently played, the
             *  playback parameters will still be saved. When doing it the other way around, however,
             *  the playback parameters will reset to default
             */
            val playback = currentPlayback
                ?: currentPlayer.currentMediaItem?.let { Playback.fromMediaItem(it) }

            log.info("Dispatching StateUpdateEvent with $playback, playWhenReady=${currentPlayer.playWhenReady}, and repeatMode=${currentPlayer.coreRepeatMode}")
            mediaSession.dispatchMediaEvent(
                SessionSyncEvent(playback, currentPlaylist, currentPlayer.playWhenReady)
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
                    .setUri(MediaId.deserialize(it.mediaId).uri ?: return@mapNotNull null)
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
                is PlaybackUpdateEvent -> handlePlaybackUpdateEvent(event)
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
                        playbackRepository.playlists.find { it.mediaId == mediaId }
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
                    val repeatMode = dataRepository.getData().repeatMode
                    runOnUiThread {
                        handleSetRepeatModeEvent(SetRepeatModeEvent(repeatMode))
                    }

                    MediaSession.MediaItemsWithStartPosition(
                        playlist.playbacks.map { it.toMediaItem() },
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

        private fun handlePlaybackUpdateEvent(event: PlaybackUpdateEvent) {
            currentPlayback = event.currentPlayback
            currentPlaylist = event.currentPlaylist
            setPlaybackAudioEffects(event.currentPlayback ?: return)
            currentPlayer.updateTimingDataOfCurrentPlayback(event.currentPlayback.timingData)
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
        val playback = mediaItem?.let { Playback.fromMediaItem(it) } ?: return
        setPlaybackAudioEffects(playback)

        if (reason != Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED) {
            ioScope.launch {
                addNewPlaybackToHistory(playback)
            }
            updatePlayback { playback }
        }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        if (!isPlaying) {
            val playback =
                currentPlayer.currentMediaItem?.let { Playback.fromMediaItem(it) } ?: return
            val currentPos = currentPlayer.currentPosition.ms
            ioScope.launch {
                saveRecentlyPlayed(
                    RecentlyPlayed(
                        playback.mediaId,
                        currentPos,
                        playback.duration,
                        ArtworkType.getType(playback),
                        if (playback.artwork is RemoteArtwork)
                            (playback.artwork as RemoteArtwork).uri
                                .toString() else null
                    )
                )
            }
        }
    }


    override fun onPlayerError(error: PlaybackException) {
        val errorStr =
            "Player error: ${error.errorCodeName} (${error.errorCode}): ${error.localizedMessage}"
        log.error(errorStr)
        if (error.errorCode == PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS
            || error.errorCode == PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND
        ) {
            eventChannel.push(
                PlayerMessageEvent(
                    UiText.StringResource(
                        R.string.error_media_not_found,
                        currentPlayback?.title ?: "Unknown"
                    ),
                    EventSeverity.Error
                )
            )
        } else {
            eventChannel.push(
                PlayerMessageEvent(
                    UiText.DynamicString(errorStr),
                    EventSeverity.Error
                )
            )
        }
    }

    @OptIn(UnstableApi::class)
    private inner class MediaSessionServiceListener : Listener {

        /**
         * This method is only required to be implemented on Android 12 or above when an attempt is made
         * by a media controller to resume playback when the {@link MediaSessionService} is in the
         * background.
         */
        override fun onForegroundServiceStartNotAllowedException() {
            if (
                Build.VERSION.SDK_INT >= 33 &&
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                // Notification permission is required but not granted
                return
            }
            val notificationManagerCompat =
                NotificationManagerCompat.from(this@MediaPlaybackService)
            ensureNotificationChannel(notificationManagerCompat)
            val builder =
                NotificationCompat.Builder(this@MediaPlaybackService, CHANNEL_ID)
                    .setSmallIcon(R.drawable.play)
                    .setContentTitle(getString(R.string.notification_content_title))
                    .setStyle(
                        NotificationCompat.BigTextStyle()
                            .bigText(getString(R.string.notification_content_text))
                    )
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .also { builder -> getBackStackedActivity()?.let { builder.setContentIntent(it) } }
            notificationManagerCompat.notify(NOTIFICATION_ID, builder.build())
        }

        private fun ensureNotificationChannel(notificationManagerCompat: NotificationManagerCompat) {
            if (
                Build.VERSION.SDK_INT < 26 ||
                notificationManagerCompat.getNotificationChannel(CHANNEL_ID) != null
            ) {
                return
            }

            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.notification_content_title),
                    NotificationManager.IMPORTANCE_DEFAULT,
                )
            notificationManagerCompat.createNotificationChannel(channel)
        }
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

    private fun setPlaybackAudioEffects(playback: Playback) {
        if (audioEffectController.setBassEnabled(playback.bassBoostEnabled))
            audioEffectController.setBass(playback.bassBoost)
        if (audioEffectController.setVirtualizerEnabled(playback.virtualizerEnabled))
            audioEffectController.setVirtualizerStrength(playback.virtualizerStrength)
        if (audioEffectController.setReverbEnabled(playback.reverbEnabled))
            audioEffectController.setReverb(playback.reverb!!)

        playback.playbackParameters.let { params ->
            currentPlayer.playbackParameters = PlaybackParameters(params.speed, params.pitch)
            currentPlayer.volume = params.volume // TODO: Volume boosting (higher than 1)
        }

        if (audioEffectController.setEqualizerEnabled(playback.equalizerEnabled)) {

            if (playback.equalizerPreset != null && audioEffectController.currentPreset != playback.equalizerPreset) {
                audioEffectController.setEqualizerPreset(playback.equalizerPreset!!)
                updatePlayback {
                    playback.copy(equalizerBands = audioEffectController.bands.value)
                }
                return
            }

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

    private fun updatePlayback(action: () -> Playback) {
        val newPlayback = action()
        currentPlayback = newPlayback
        mediaSession.dispatchMediaEvent(PlaybackUpdateEvent(newPlayback, currentPlaylist))
    }

    private suspend fun executeSearch(query: String, extras: Bundle): List<MediaItem> {
        return when (extras.getString(MediaStore.EXTRA_MEDIA_FOCUS)) {
            MediaStore.Audio.Playlists.ENTRY_CONTENT_TYPE -> {
                val playlist = extras.getString(MediaStore.EXTRA_MEDIA_ALBUM)
                searchStoredPlaybacks.byPlaylist(playlist).map { it.playlist!!.toMediaItem() }
            }

            MediaStore.Audio.Media.ENTRY_CONTENT_TYPE -> {
                val title = extras.getString(MediaStore.EXTRA_MEDIA_TITLE)
                val artist = extras.getString(MediaStore.EXTRA_MEDIA_ARTIST)
                searchStoredPlaybacks.byTitleArtist(title, artist)
                    .map { it.playback!!.toMediaItem() }
            }

            else -> {
                // if query is blank search will return all playbacks (for commands like "play some music")
                // using a random playback type to select music search location
                searchStoredPlaybacks(
                    query,
                    PlaybackType.build("*${Random.nextInt(0, 3)}*")
                ).map { it.playback?.toMediaItem() ?: it.playlist!!.toMediaItem() }
            }
        }
    }

    private inner class CustomPlayerEventListener : CustomPlayer.Listener {
        override fun onTimingDataUpdated(controller: TimingDataController?) {
            if (controller == currentPlayback?.timingData)
                return

            mediaSession.dispatchMediaEvent(
                PlaybackUpdateEvent(
                    currentPlayback?.copy(
                        timingData = controller ?: TimingDataController(
                            listOf(TimingData(0.ms, currentPlayback!!.duration))
                        )
                    ) ?: return,
                    currentPlaylist
                )
            )
        }
    }

    companion object {
        private const val NOTIFICATION_ID = 1859
        private const val CHANNEL_ID = "tachyon_foreground_unable_to_start_notification"
    }
}
