package com.tachyonmusic.user.domain

import com.tachyonmusic.core.Resource
import com.tachyonmusic.user.Metadata
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    val metadata: Metadata

    val signedIn: Boolean

    suspend fun signIn(
        email: String,
        password: String
    ): Resource<Unit>

    suspend fun register(
        email: String,
        password: String
    ): Resource<Unit>

    fun signOut()

    fun upload()

    fun registerEventListener(listener: EventListener?)

    interface EventListener {
        fun onMetadataChanged() {}
    }
}