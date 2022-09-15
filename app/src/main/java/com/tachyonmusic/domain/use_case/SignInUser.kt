package com.tachyonmusic.domain.use_case

import com.tachyonmusic.app.R
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.UiText
import com.tachyonmusic.domain.util.AccountVerificator
import com.tachyonmusic.user.domain.UserRepository
import kotlinx.coroutines.flow.flow

class SignInUser(
    private val repository: UserRepository
) {
    operator fun invoke(emailIn: String, password: String) = flow {
        emit(Resource.Loading())

        val email = emailIn.trim()
        if (!AccountVerificator.verifyEmail(email))
            emit(Resource.Error(UiText.StringResource(R.string.invalid_email_error)))
        else if (!AccountVerificator.verifyPassword(password))
            emit(Resource.Error(UiText.StringResource(R.string.invalid_password_error)))
        else
            emit(repository.signIn(email, password))
    }
}