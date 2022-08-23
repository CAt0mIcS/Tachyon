package com.tachyonmusic.domain.util

object AccountVerificator {
    const val MINIMUM_PASSWORD_LENGTH = 5

    fun verifyEmail(email: String) =
        email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()

    fun verifyPassword(password: String) =
        password.isNotBlank() && password.length >= MINIMUM_PASSWORD_LENGTH
}