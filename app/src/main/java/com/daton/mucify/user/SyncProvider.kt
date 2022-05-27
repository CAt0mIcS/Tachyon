package com.daton.mucify.user

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
            return remoteMetadata

        // Offline is newer than online or offline has never been uploaded --> Upload offline
        if (localMetadata.syncType == LOCAL_ONLY || remoteMetadata.timestamp < localMetadata.timestamp) {
            localMetadata.syncType = LOCAL_REMOTE
            User.uploadMetadata(localMetadata)
            return localMetadata
        }

        // online is newer than offline --> Download
        if (remoteMetadata.timestamp > localMetadata.timestamp)
            return remoteMetadata

        return null
    }
}