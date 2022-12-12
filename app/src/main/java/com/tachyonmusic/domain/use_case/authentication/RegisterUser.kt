package com.tachyonmusic.domain.use_case.authentication

import com.tachyonmusic.app.R
import com.tachyonmusic.domain.util.AccountVerificator
import com.tachyonmusic.user.domain.UserRepository
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.UiText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RegisterUser(
    private val repository: UserRepository
) {
    operator fun invoke(emailIn: String, password: String): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        val email = emailIn.trim()
        if (!AccountVerificator.verifyEmail(email))
            emit(Resource.Error(UiText.StringResource(R.string.invalid_email_error)))
        else if (!AccountVerificator.verifyPassword(password))
            emit(Resource.Error(UiText.StringResource(R.string.invalid_password_error)))
        else
            emit(repository.register(email, password))
    }
}