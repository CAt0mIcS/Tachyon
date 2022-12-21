package com.tachyonmusic.presentation.authentication

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.app.R
import com.tachyonmusic.domain.use_case.authentication.SignInUser
import com.tachyonmusic.logger.Log
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject


@HiltViewModel
class SignInViewModel @Inject constructor(
    private val signInUser: SignInUser,
    private val log: Logger = Log()
) : ViewModel() {

    private val _email = mutableStateOf("spam.2222@web.de")
    val email: State<String> = _email

    private val _password = mutableStateOf("password")
    val password: State<String> = _password

    private val _error = mutableStateOf<UiText?>(null)
    val error: State<UiText?> = _error


    fun onSignInClicked() {
        signInUser(email.value, password.value).onEach { resource ->
            if (resource is Resource.Error) {
                _error.value = resource.message ?: UiText.StringResource(R.string.unknown_error)
//                log.exception(resource.exception, resource.message) TODO
            } else {
                _error.value = null
            }
        }.launchIn(viewModelScope)
    }

    fun onEmailChanged(email: String) {
        _email.value = email
    }

    fun onPasswordChanged(password: String) {
        _password.value = password
    }
}