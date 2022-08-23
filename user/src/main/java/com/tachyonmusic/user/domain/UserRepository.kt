package com.tachyonmusic.user.domain

import com.tachyonmusic.user.Metadata

interface UserRepository {
    val metadata: Metadata

    val signedIn: Boolean

    fun signIn(
        email: String,
        password: String,
        onComplete: (Boolean /*isSuccessful*/, String? /*errorMsg*/) -> Unit
    )

    fun register(
        email: String,
        password: String,
        onComplete: (Boolean /*isSuccessful*/, String? /*errorMsg*/) -> Unit
    )

    fun signOut()

    fun upload()

    fun registerEventListener(listener: EventListener?)

    interface EventListener {
        fun onMetadataChanged() {}
    }
}