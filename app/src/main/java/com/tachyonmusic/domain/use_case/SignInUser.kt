package com.tachyonmusic.domain.use_case

import com.tachyonmusic.domain.util.AccountVerificator
import com.tachyonmusic.domain.util.AuthenticationException
import com.tachyonmusic.user.domain.UserRepository
import kotlin.jvm.Throws

class SignInUser(
    private val repository: UserRepository
) {
    @Throws(AuthenticationException::class)
    operator fun invoke(email: String, password: String) {
        if (!AccountVerificator.verifyEmail(email))
            throw AuthenticationException("")
        if (!AccountVerificator.verifyPassword(password))
            throw AuthenticationException("")

        repository.signIn(email, password) { isSuccessful, errorMsg ->
            if (!isSuccessful)
                throw AuthenticationException(errorMsg ?: "")
        }
    }
}