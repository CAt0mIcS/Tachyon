package com.daton.mucify.user

import com.daton.media.device.Loop
import kotlinx.coroutines.flow.merge

/**
 * Manages synchronization of metadata and conflict resolution
 */
class SyncProvider {

    companion object {
        /**
         * Metadata was created locally and has never been synced
         */
        const val LOCAL_ONLY = 0

        /**
         * Metadata has already been synced at some point in time
         */
        const val LOCAL_REMOTE = 1
    }

    /**
     * Synchronizes remote and local settings. Detects and resolves conflicts
     * @return Either the new metadata or null if [localMetadata] is the newest one and doesn't need to be changed
     */
    fun sync(remoteMetadata: UserMetadata, localMetadata: UserMetadata): UserMetadata? {
        // Metadata has been uploaded from another device already --> Download
        if (remoteMetadata.syncType != LOCAL_ONLY && localMetadata.syncType == LOCAL_ONLY)
            return mergePlaybacks(remoteMetadata, localMetadata)

        // Offline is newer than online or offline has never been uploaded --> Upload offline
        if (localMetadata.syncType == LOCAL_ONLY || remoteMetadata.timestamp < localMetadata.timestamp) {
            localMetadata.syncType = LOCAL_REMOTE
            return mergePlaybacks(localMetadata, remoteMetadata)
        }

        // online is newer than offline --> Download
        if (remoteMetadata.timestamp > localMetadata.timestamp)
            return mergePlaybacks(remoteMetadata, localMetadata)

        return null
    }


    private fun mergePlaybacks(newMetadata: UserMetadata, oldMetadata: UserMetadata): UserMetadata =
        mergeLoops(mergePlaylists(newMetadata, oldMetadata), oldMetadata)

    private fun mergeLoops(newMetadata: UserMetadata, oldMetadata: UserMetadata): UserMetadata {
        // Cannot have any conflicts if one of the metadata doesn't have a loop
        // and we already know that the newMetadata is newer and has the most recent loop version
        if (newMetadata.loops.isEmpty() || oldMetadata.loops.isEmpty())
            return newMetadata

        val loopsToAdd = mutableListOf<Loop>()

        for (newLoop in newMetadata.loops) {
            for (oldLoop in oldMetadata.loops) {

                // Exact loop already saved in [oldMetadata], no action necessary
                if (newLoop == oldLoop)
                    continue

                // Some part of the loop is not the same, decide which to keep
                if (newLoop.mediaId == oldLoop.mediaId) {
                    TODO("Merge loops and decide which to keep")
                }

                // Loop only stored in old version
                if (!newMetadata.loops.contains(oldLoop)) // TODO: Check if ever false
                    loopsToAdd += oldLoop
            }
        }

        newMetadata.loops += loopsToAdd

        return newMetadata
    }

    private fun mergePlaylists(newMetadata: UserMetadata, oldMetadata: UserMetadata): UserMetadata {
        return newMetadata
    }
}