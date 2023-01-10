package com.tachyonmusic.presentation.authentication

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.app.R
import com.tachyonmusic.domain.use_case.authentication.RegisterUser
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.util.Resource
import com.tachyonmusic.util.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject


@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUser: RegisterUser,
    private val log: Logger
) : ViewModel() {

    private val _email = mutableStateOf("")
    val email: State<String> = _email

    private val _password = mutableStateOf("")
    val password: State<String> = _password

    private val _error = mutableStateOf<UiText?>(null)
    val error: State<UiText?> = _error


    fun onRegisterClicked() {
        registerUser(email.value, password.value).onEach { resource ->
            if (resource is Resource.Error) {
                _error.value = resource.message ?: UiText.StringResource(R.string.unknown_error)
//                log.exception(resource.exception, resource.message)
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