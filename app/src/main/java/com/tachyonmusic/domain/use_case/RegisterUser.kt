package com.tachyonmusic.domain.use_case

import com.tachyonmusic.domain.util.AccountVerificator
import com.tachyonmusic.domain.util.AuthenticationException
import com.tachyonmusic.user.domain.UserRepository

class RegisterUser(
    private val repository: UserRepository
) {
    @Throws(AuthenticationException::class)
    operator fun invoke(emailIn: String, password: String) {
        val email = emailIn.trim()
        if (!AccountVerificator.verifyEmail(email))
            throw AuthenticationException("")
        if (!AccountVerificator.verifyPassword(password))
            throw AuthenticationException("")

        repository.register(email, password) { isSuccessful, errorMsg ->
            if (!isSuccessful)
                throw AuthenticationException(errorMsg ?: "")
        }
    }
}