package com.tachyonmusic.media.data

import android.os.Looper
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.TextureView
import androidx.media3.common.AudioAttributes
import androidx.media3.common.DeviceInfo
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Player.Command
import androidx.media3.common.Player.Commands
import androidx.media3.common.Player.EVENT_MEDIA_ITEM_TRANSITION
import androidx.media3.common.Player.EVENT_MEDIA_METADATA_CHANGED
import androidx.media3.common.Player.EVENT_POSITION_DISCONTINUITY
import androidx.media3.common.Player.EVENT_TIMELINE_CHANGED
import androidx.media3.common.Player.Events
import androidx.media3.common.Player.Listener
import androidx.media3.common.Player.RepeatMode
import androidx.media3.common.Timeline
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.Tracks
import androidx.media3.common.VideoSize
import androidx.media3.common.text.CueGroup
import androidx.media3.common.util.Size
import androidx.media3.common.util.UnstableApi
import com.tachyonmusic.media.domain.CustomPlayer
import java.lang.Integer.min

@UnstableApi
abstract class ReplaceableForwardingPlayer(player: Player) : CustomPlayer {
    protected var player: Player
        private set

    private val listeners: MutableList<Listener> = arrayListOf()
    private val playlist: MutableList<MediaItem> = arrayListOf()
    private var currentMediaItemIndex: Int = 0

    private val playerListener: Listener = PlayerListener()

    init {
        this.player = player
        player.addListener(playerListener)
    }

    /** Sets a new [Player] instance to which the state of the previous player is transferred. */
    override fun setPlayer(player: Player) {
        // Remove add all listeners before changing the player state.
        for (listener in listeners) {
            this.player.removeListener(listener)
            player.addListener(listener)
        }
        // Add/remove our listener we use to workaround the missing metadata support of CastPlayer.
        this.player.removeListener(playerListener)
        player.addListener(playerListener)

        // TODO: Handle repeat mode for cast player (should never be 0)
        player.repeatMode =
            if (this.player.repeatMode == 0) player.repeatMode else this.player.repeatMode
        player.shuffleModeEnabled = this.player.shuffleModeEnabled
        player.playlistMetadata = this.player.playlistMetadata
        player.trackSelectionParameters = this.player.trackSelectionParameters
        player.volume = this.player.volume
        player.playWhenReady = this.player.playWhenReady

        // Prepare the new player.
        player.setMediaItems(
            this.player.mediaItems,
            currentMediaItemIndex,
            this.player.contentPosition
        )
        player.prepare()

        // Stop the previous player. Don't release so it can be used again.
        this.player.clearMediaItems()
        this.player.stop()

        this.player = player
    }

    /** Calls [Player.getApplicationLooper] on the delegate and returns the result.  */
    override fun getApplicationLooper(): Looper {
        return player.applicationLooper
    }

    override fun addListener(listener: Listener) {
        player.addListener(listener)
        listeners.add(listener)
    }

    override fun removeListener(listener: Listener) {
        player.removeListener(listener)
        listeners.remove(listener)
    }

    override fun setMediaItems(mediaItems: MutableList<MediaItem>) {
        player.setMediaItems(mediaItems)
        playlist.clear()
        playlist.addAll(mediaItems)
    }

    override fun setMediaItems(mediaItems: MutableList<MediaItem>, resetPosition: Boolean) {
        player.setMediaItems(mediaItems, resetPosition)
        playlist.clear()
        playlist.addAll(mediaItems)
    }

    override fun setMediaItems(
        mediaItems: MutableList<MediaItem>,
        startWindowIndex: Int,
        startPositionMs: Long
    ) {
        player.setMediaItems(mediaItems, startWindowIndex, startPositionMs)
        playlist.clear()
        playlist.addAll(mediaItems)
    }

    override fun setMediaItem(mediaItem: MediaItem) {
        player.setMediaItem(mediaItem)
        playlist.clear()
        playlist.add(mediaItem)
    }

    override fun setMediaItem(mediaItem: MediaItem, startPositionMs: Long) {
        player.setMediaItem(mediaItem, startPositionMs)
        playlist.clear()
        playlist.add(mediaItem)
    }

    override fun setMediaItem(mediaItem: MediaItem, resetPosition: Boolean) {
        player.setMediaItem(mediaItem, resetPosition)
        playlist.clear()
        playlist.add(mediaItem)
    }

    override fun addMediaItem(mediaItem: MediaItem) {
        player.addMediaItem(mediaItem)
        playlist.add(mediaItem)
    }

    override fun addMediaItem(index: Int, mediaItem: MediaItem) {
        player.addMediaItem(index, mediaItem)
        playlist.add(index, mediaItem)
    }

    override fun addMediaItems(mediaItems: MutableList<MediaItem>) {
        player.addMediaItems(mediaItems)
        playlist.addAll(mediaItems)
    }

    override fun addMediaItems(index: Int, mediaItems: MutableList<MediaItem>) {
        player.addMediaItems(index, mediaItems)
        playlist.addAll(index, mediaItems)
    }

    override fun moveMediaItem(currentIndex: Int, newIndex: Int) {
        player.moveMediaItem(currentIndex, newIndex)
        playlist.add(min(newIndex, playlist.size), playlist.removeAt(currentIndex))
    }

    override fun moveMediaItems(fromIndex: Int, toIndex: Int, newIndex: Int) {
        val removedItems: ArrayDeque<MediaItem> = ArrayDeque()
        val removedItemsLength = toIndex - fromIndex
        for (i in removedItemsLength - 1 downTo 0) {
            removedItems.addFirst(playlist.removeAt(fromIndex + i))
        }
        playlist.addAll(min(newIndex, playlist.size), removedItems)
    }

    override fun replaceMediaItem(index: Int, mediaItem: MediaItem) {
        player.replaceMediaItem(index, mediaItem)
    }

    override fun setAudioAttributes(audioAttributes: AudioAttributes, handleAudioFocus: Boolean) {
        player.setAudioAttributes(audioAttributes, handleAudioFocus)
    }

    override fun replaceMediaItems(
        fromIndex: Int,
        toIndex: Int,
        mediaItems: MutableList<MediaItem>
    ) {
        player.replaceMediaItems(fromIndex, toIndex, mediaItems)
    }

    override fun removeMediaItem(index: Int) {
        player.removeMediaItem(index)
        playlist.removeAt(index)
    }

    override fun removeMediaItems(fromIndex: Int, toIndex: Int) {
        player.removeMediaItems(fromIndex, toIndex)
        val removedItemsLength = toIndex - fromIndex
        for (i in removedItemsLength - 1 downTo 0) {
            playlist.removeAt(fromIndex + i)
        }
    }

    override fun clearMediaItems() {
        player.clearMediaItems()
        playlist.clear()
    }

    /** Calls [Player.isCommandAvailable] on the delegate and returns the result.  */
    override fun isCommandAvailable(command: @Command Int): Boolean {
        return player.isCommandAvailable(command)
    }

    /** Calls [Player.canAdvertiseSession] on the delegate and returns the result.  */
    override fun canAdvertiseSession(): Boolean {
        return player.canAdvertiseSession()
    }

    /** Calls [Player.getAvailableCommands] on the delegate and returns the result.  */
    override fun getAvailableCommands(): Commands {
        return player.availableCommands
    }

    /** Calls [Player.prepare] on the delegate.  */
    override fun prepare() {
        player.prepare()
    }

    /** Calls [Player.getPlaybackState] on the delegate and returns the result.  */
    override fun getPlaybackState(): Int {
        return player.playbackState
    }

    /** Calls [Player.getPlaybackSuppressionReason] on the delegate and returns the result.  */
    override fun getPlaybackSuppressionReason(): Int {
        return player.playbackSuppressionReason
    }

    /** Calls [Player.isPlaying] on the delegate and returns the result.  */
    override fun isPlaying(): Boolean {
        return player.isPlaying
    }

    /** Calls [Player.getPlayerError] on the delegate and returns the result.  */
    override fun getPlayerError(): PlaybackException? {
        return player.playerError
    }

    /** Calls [Player.play] on the delegate.  */
    override fun play() {
        player.play()
    }

    /** Calls [Player.pause] on the delegate.  */
    override fun pause() {
        player.pause()
    }

    /** Calls [Player.setPlayWhenReady] on the delegate.  */
    override fun setPlayWhenReady(playWhenReady: Boolean) {
        player.playWhenReady = playWhenReady
    }

    /** Calls [Player.getPlayWhenReady] on the delegate and returns the result.  */
    override fun getPlayWhenReady(): Boolean {
        return player.playWhenReady
    }

    /** Calls [Player.setRepeatMode] on the delegate.  */
    override fun setRepeatMode(repeatMode: @RepeatMode Int) {
        player.repeatMode = repeatMode
    }

    /** Calls [Player.getRepeatMode] on the delegate and returns the result.  */
    override fun getRepeatMode(): Int {
        return player.repeatMode
    }

    /** Calls [Player.setShuffleModeEnabled] on the delegate.  */
    override fun setShuffleModeEnabled(shuffleModeEnabled: Boolean) {
        player.shuffleModeEnabled = shuffleModeEnabled
    }

    /** Calls [Player.getShuffleModeEnabled] on the delegate and returns the result.  */
    override fun getShuffleModeEnabled(): Boolean {
        return player.shuffleModeEnabled
    }

    /** Calls [Player.isLoading] on the delegate and returns the result.  */
    override fun isLoading(): Boolean {
        return player.isLoading
    }

    /** Calls [Player.seekToDefaultPosition] on the delegate.  */
    override fun seekToDefaultPosition() {
        player.seekToDefaultPosition()
    }

    /** Calls [Player.seekToDefaultPosition] on the delegate.  */
    override fun seekToDefaultPosition(mediaItemIndex: Int) {
        player.seekToDefaultPosition(mediaItemIndex)
    }

    /** Calls [Player.seekTo] on the delegate.  */
    override fun seekTo(positionMs: Long) {
        player.seekTo(positionMs)
    }

    /** Calls [Player.seekTo] on the delegate.  */
    override fun seekTo(mediaItemIndex: Int, positionMs: Long) {
        player.seekTo(mediaItemIndex, positionMs)
    }

    /** Calls [Player.getSeekBackIncrement] on the delegate and returns the result.  */
    override fun getSeekBackIncrement(): Long {
        return player.seekBackIncrement
    }

    /** Calls [Player.seekBack] on the delegate.  */
    override fun seekBack() {
        player.seekBack()
    }

    /** Calls [Player.getSeekForwardIncrement] on the delegate and returns the result.  */
    override fun getSeekForwardIncrement(): Long {
        return player.seekForwardIncrement
    }

    /** Calls [Player.seekForward] on the delegate.  */
    override fun seekForward() {
        player.seekForward()
    }

    /** Calls [Player.hasPreviousMediaItem] on the delegate and returns the result.  */
    override fun hasPreviousMediaItem(): Boolean {
        return player.hasPreviousMediaItem()
    }

    /**
     * Calls [Player.seekToPreviousWindow] on the delegate.
     *
     */
    @Deprecated("Use {@link #seekToPreviousMediaItem()} instead.")
    override fun seekToPreviousWindow() {
        player.seekToPreviousWindow()
    }

    /** Calls [Player.seekToPreviousMediaItem] on the delegate.  */
    override fun seekToPreviousMediaItem() {
        player.seekToPreviousMediaItem()
    }

    /** Calls [Player.seekToPrevious] on the delegate.  */
    override fun seekToPrevious() {
        player.seekToPrevious()
    }

    /** Calls [Player.getMaxSeekToPreviousPosition] on the delegate and returns the result.  */
    override fun getMaxSeekToPreviousPosition(): Long {
        return player.maxSeekToPreviousPosition
    }

    /**
     * Calls [Player.hasNext] on the delegate and returns the result.
     *
     */
    @Deprecated("Use {@link #hasNextMediaItem()} instead.")
    override fun hasNext(): Boolean {
        return player.hasNext()
    }

    /**
     * Calls [Player.hasNextWindow] on the delegate and returns the result.
     *
     */
    @Deprecated("Use {@link #hasNextMediaItem()} instead.")
    override fun hasNextWindow(): Boolean {
        return player.hasNextWindow()
    }

    /** Calls [Player.hasNextMediaItem] on the delegate and returns the result.  */
    override fun hasNextMediaItem(): Boolean {
        return player.hasNextMediaItem()
    }

    /**
     * Calls [Player.next] on the delegate.
     *
     */
    @Deprecated("Use {@link #seekToNextMediaItem()} instead.")
    override fun next() {
        player.next()
    }

    /**
     * Calls [Player.seekToNextWindow] on the delegate.
     *
     */
    @Deprecated("Use {@link #seekToNextMediaItem()} instead.")
    override fun seekToNextWindow() {
        player.seekToNextWindow()
    }

    /** Calls [Player.seekToNextMediaItem] on the delegate.  */
    override fun seekToNextMediaItem() {
        player.seekToNextMediaItem()
    }

    /** Calls [Player.seekToNext] on the delegate.  */
    override fun seekToNext() {
        player.seekToNext()
    }

    /** Calls [Player.setPlaybackParameters] on the delegate.  */
    override fun setPlaybackParameters(playbackParameters: PlaybackParameters) {
        player.playbackParameters = playbackParameters
    }

    /** Calls [Player.setPlaybackSpeed] on the delegate.  */
    override fun setPlaybackSpeed(speed: Float) {
        player.setPlaybackSpeed(speed)
    }

    /** Calls [Player.getPlaybackParameters] on the delegate and returns the result.  */
    override fun getPlaybackParameters(): PlaybackParameters {
        return player.playbackParameters
    }

    override fun stop() {
        player.stop()
    }

    override fun release() {
        player.release()
        playlist.clear()
    }


    /** Calls [Player.getCurrentTracks] on the delegate and returns the result.  */
    override fun getCurrentTracks(): Tracks {
        return player.currentTracks
    }

    /** Calls [Player.getTrackSelectionParameters] on the delegate and returns the result.  */
    override fun getTrackSelectionParameters(): TrackSelectionParameters {
        return player.trackSelectionParameters
    }

    /** Calls [Player.setTrackSelectionParameters] on the delegate.  */
    override fun setTrackSelectionParameters(parameters: TrackSelectionParameters) {
        player.trackSelectionParameters = parameters
    }

    /** Calls [Player.getMediaMetadata] on the delegate and returns the result.  */
    override fun getMediaMetadata(): MediaMetadata {
        return player.mediaMetadata
    }

    /** Calls [Player.getPlaylistMetadata] on the delegate and returns the result.  */
    override fun getPlaylistMetadata(): MediaMetadata {
        return player.playlistMetadata
    }

    /** Calls [Player.setPlaylistMetadata] on the delegate.  */
    override fun setPlaylistMetadata(mediaMetadata: MediaMetadata) {
        player.playlistMetadata = mediaMetadata
    }

    /** Calls [Player.getCurrentManifest] on the delegate and returns the result.  */
    override fun getCurrentManifest(): Any? {
        return player.currentManifest
    }

    /** Calls [Player.getCurrentTimeline] on the delegate and returns the result.  */
    override fun getCurrentTimeline(): Timeline {
        return player.currentTimeline
    }

    /** Calls [Player.getCurrentPeriodIndex] on the delegate and returns the result.  */
    override fun getCurrentPeriodIndex(): Int {
        return player.currentPeriodIndex
    }

    /**
     * Calls [Player.getCurrentWindowIndex] on the delegate and returns the result.
     *
     */
    @Deprecated("Use {@link #getCurrentMediaItemIndex()} instead.")
    override fun getCurrentWindowIndex(): Int {
        return player.currentWindowIndex
    }

    /** Calls [Player.getCurrentMediaItemIndex] on the delegate and returns the result.  */
    override fun getCurrentMediaItemIndex(): Int {
        return player.currentMediaItemIndex
    }

    /**
     * Calls [Player.getNextWindowIndex] on the delegate and returns the result.
     *
     */
    @Deprecated("Use {@link #getNextMediaItemIndex()} instead.")
    override fun getNextWindowIndex(): Int {
        return player.nextWindowIndex
    }

    /** Calls [Player.getNextMediaItemIndex] on the delegate and returns the result.  */
    override fun getNextMediaItemIndex(): Int {
        return player.nextMediaItemIndex
    }

    /**
     * Calls [Player.getPreviousWindowIndex] on the delegate and returns the result.
     *
     */
    @Deprecated("Use {@link #getPreviousMediaItemIndex()} instead.")
    override fun getPreviousWindowIndex(): Int {
        return player.previousWindowIndex
    }

    /** Calls [Player.getPreviousMediaItemIndex] on the delegate and returns the result.  */
    override fun getPreviousMediaItemIndex(): Int {
        return player.previousMediaItemIndex
    }

    /** Calls [Player.getCurrentMediaItem] on the delegate and returns the result.  */
    override fun getCurrentMediaItem(): MediaItem? {
        return player.currentMediaItem
    }

    /** Calls [Player.getMediaItemCount] on the delegate and returns the result.  */
    override fun getMediaItemCount(): Int {
        return player.mediaItemCount
    }

    /** Calls [Player.getMediaItemAt] on the delegate and returns the result.  */
    override fun getMediaItemAt(index: Int): MediaItem {
        return player.getMediaItemAt(index)
    }

    /** Calls [Player.getDuration] on the delegate and returns the result.  */
    override fun getDuration(): Long {
        return player.duration
    }

    /** Calls [Player.getCurrentPosition] on the delegate and returns the result.  */
    override fun getCurrentPosition(): Long {
        return player.currentPosition
    }

    /** Calls [Player.getBufferedPosition] on the delegate and returns the result.  */
    override fun getBufferedPosition(): Long {
        return player.bufferedPosition
    }

    /** Calls [Player.getBufferedPercentage] on the delegate and returns the result.  */
    override fun getBufferedPercentage(): Int {
        return player.bufferedPercentage
    }

    /** Calls [Player.getTotalBufferedDuration] on the delegate and returns the result.  */
    override fun getTotalBufferedDuration(): Long {
        return player.totalBufferedDuration
    }

    /**
     * Calls [Player.isCurrentWindowDynamic] on the delegate and returns the result.
     *
     */
    @Deprecated("Use {@link #isCurrentMediaItemDynamic()} instead.")
    override fun isCurrentWindowDynamic(): Boolean {
        return player.isCurrentWindowDynamic
    }

    /** Calls [Player.isCurrentMediaItemDynamic] on the delegate and returns the result.  */
    override fun isCurrentMediaItemDynamic(): Boolean {
        return player.isCurrentMediaItemDynamic
    }

    /**
     * Calls [Player.isCurrentWindowLive] on the delegate and returns the result.
     *
     */
    @Deprecated("Use {@link #isCurrentMediaItemLive()} instead.")
    override fun isCurrentWindowLive(): Boolean {
        return player.isCurrentWindowLive
    }

    /** Calls [Player.isCurrentMediaItemLive] on the delegate and returns the result.  */
    override fun isCurrentMediaItemLive(): Boolean {
        return player.isCurrentMediaItemLive
    }

    /** Calls [Player.getCurrentLiveOffset] on the delegate and returns the result.  */
    override fun getCurrentLiveOffset(): Long {
        return player.currentLiveOffset
    }

    /**
     * Calls [Player.isCurrentWindowSeekable] on the delegate and returns the result.
     *
     */
    @Deprecated("Use {@link #isCurrentMediaItemSeekable()} instead.")
    override fun isCurrentWindowSeekable(): Boolean {
        return player.isCurrentWindowSeekable
    }

    /** Calls [Player.isCurrentMediaItemSeekable] on the delegate and returns the result.  */
    override fun isCurrentMediaItemSeekable(): Boolean {
        return player.isCurrentMediaItemSeekable
    }

    /** Calls [Player.isPlayingAd] on the delegate and returns the result.  */
    override fun isPlayingAd(): Boolean {
        return player.isPlayingAd
    }

    /** Calls [Player.getCurrentAdGroupIndex] on the delegate and returns the result.  */
    override fun getCurrentAdGroupIndex(): Int {
        return player.currentAdGroupIndex
    }

    /** Calls [Player.getCurrentAdIndexInAdGroup] on the delegate and returns the result.  */
    override fun getCurrentAdIndexInAdGroup(): Int {
        return player.currentAdIndexInAdGroup
    }

    /** Calls [Player.getContentDuration] on the delegate and returns the result.  */
    override fun getContentDuration(): Long {
        return player.contentDuration
    }

    /** Calls [Player.getContentPosition] on the delegate and returns the result.  */
    override fun getContentPosition(): Long {
        return player.contentPosition
    }

    /** Calls [Player.getContentBufferedPosition] on the delegate and returns the result.  */
    override fun getContentBufferedPosition(): Long {
        return player.contentBufferedPosition
    }

    /** Calls [Player.getAudioAttributes] on the delegate and returns the result.  */
    override fun getAudioAttributes(): AudioAttributes {
        return player.audioAttributes
    }

    /** Calls [Player.setVolume] on the delegate.  */
    override fun setVolume(volume: Float) {
        player.volume = volume
    }

    /** Calls [Player.getVolume] on the delegate and returns the result.  */
    override fun getVolume(): Float {
        return player.volume
    }

    /** Calls [Player.getVideoSize] on the delegate and returns the result.  */
    override fun getVideoSize(): VideoSize {
        return player.videoSize
    }

    /** Calls [Player.getSurfaceSize] on the delegate and returns the result.  */
    override fun getSurfaceSize(): Size {
        return player.surfaceSize
    }

    /** Calls [Player.clearVideoSurface] on the delegate.  */
    override fun clearVideoSurface() {
        player.clearVideoSurface()
    }

    /** Calls [Player.clearVideoSurface] on the delegate.  */
    override fun clearVideoSurface(surface: Surface?) {
        player.clearVideoSurface(surface)
    }

    /** Calls [Player.setVideoSurface] on the delegate.  */
    override fun setVideoSurface(surface: Surface?) {
        player.setVideoSurface(surface)
    }

    /** Calls [Player.setVideoSurfaceHolder] on the delegate.  */
    override fun setVideoSurfaceHolder(surfaceHolder: SurfaceHolder?) {
        player.setVideoSurfaceHolder(surfaceHolder)
    }

    /** Calls [Player.clearVideoSurfaceHolder] on the delegate.  */
    override fun clearVideoSurfaceHolder(surfaceHolder: SurfaceHolder?) {
        player.clearVideoSurfaceHolder(surfaceHolder)
    }

    /** Calls [Player.setVideoSurfaceView] on the delegate.  */
    override fun setVideoSurfaceView(surfaceView: SurfaceView?) {
        player.setVideoSurfaceView(surfaceView)
    }

    /** Calls [Player.clearVideoSurfaceView] on the delegate.  */
    override fun clearVideoSurfaceView(surfaceView: SurfaceView?) {
        player.clearVideoSurfaceView(surfaceView)
    }

    /** Calls [Player.setVideoTextureView] on the delegate.  */
    override fun setVideoTextureView(textureView: TextureView?) {
        player.setVideoTextureView(textureView)
    }

    /** Calls [Player.clearVideoTextureView] on the delegate.  */
    override fun clearVideoTextureView(textureView: TextureView?) {
        player.clearVideoTextureView(textureView)
    }

    /** Calls [Player.getCurrentCues] on the delegate and returns the result.  */
    override fun getCurrentCues(): CueGroup {
        return player.currentCues
    }

    /** Calls [Player.getDeviceInfo] on the delegate and returns the result.  */
    override fun getDeviceInfo(): DeviceInfo {
        return player.deviceInfo
    }

    /** Calls [Player.getDeviceVolume] on the delegate and returns the result.  */
    override fun getDeviceVolume(): Int {
        return player.deviceVolume
    }

    /** Calls [Player.isDeviceMuted] on the delegate and returns the result.  */
    override fun isDeviceMuted(): Boolean {
        return player.isDeviceMuted
    }

    /** Calls [Player.setDeviceVolume] on the delegate.  */
    override fun setDeviceVolume(volume: Int) {
        player.deviceVolume = volume
    }

    override fun setDeviceVolume(volume: Int, flags: Int) {
        player.setDeviceVolume(volume, flags)
    }

    /** Calls [Player.increaseDeviceVolume] on the delegate.  */
    override fun increaseDeviceVolume() {
        player.increaseDeviceVolume()
    }

    override fun increaseDeviceVolume(flags: Int) {
        player.increaseDeviceVolume(flags)
    }

    /** Calls [Player.decreaseDeviceVolume] on the delegate.  */
    override fun decreaseDeviceVolume() {
        player.decreaseDeviceVolume()
    }

    override fun decreaseDeviceVolume(flags: Int) {
        player.decreaseDeviceVolume(flags)
    }

    /** Calls [Player.setDeviceMuted] on the delegate.  */
    override fun setDeviceMuted(muted: Boolean) {
        player.isDeviceMuted = muted
    }

    override fun setDeviceMuted(muted: Boolean, flags: Int) {
        player.setDeviceMuted(muted, flags)
    }


    private inner class PlayerListener : Listener {
        override fun onEvents(player: Player, events: Events) {
            if (events.contains(EVENT_MEDIA_ITEM_TRANSITION)
                && !events.contains(EVENT_MEDIA_METADATA_CHANGED)
            ) {
                // CastPlayer does not support onMetaDataChange. We can trigger this here when the
                // media item changes.
                if (playlist.isNotEmpty()) {
                    for (listener in listeners) {
                        listener.onMediaMetadataChanged(
                            playlist[player.currentMediaItemIndex].mediaMetadata
                        )
                    }
                }
            }
            if (events.contains(EVENT_POSITION_DISCONTINUITY)
                || events.contains(EVENT_MEDIA_ITEM_TRANSITION)
                || events.contains(EVENT_TIMELINE_CHANGED)
            ) {
                if (!player.currentTimeline.isEmpty) {
                    currentMediaItemIndex = player.currentMediaItemIndex
                }
            }
        }
    }
}


val Player.mediaItems: List<MediaItem>
    get() = List(mediaItemCount) {
        getMediaItemAt(it)
    }