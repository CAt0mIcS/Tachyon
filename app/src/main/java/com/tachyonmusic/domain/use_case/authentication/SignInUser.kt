package com.tachyonmusic.domain.use_case.authentication

import com.tachyonmusic.util.Resource
import kotlinx.coroutines.flow.flow

class SignInUser {
    operator fun invoke(emailIn: String, password: String) = flow<Resource<Unit>> {
//        emit(Resource.Loading())
//
//        val email = emailIn.trim()
//        if (!AccountVerificator.verifyEmail(email))
//            emit(Resource.Error(UiText.StringResource(R.string.invalid_email_error)))
//        else if (!AccountVerificator.verifyPassword(password))
//            emit(Resource.Error(UiText.StringResource(R.string.invalid_password_error)))
//        else
//            emit(repository.signIn(email, password))
        emit(Resource.Success())
    }
}