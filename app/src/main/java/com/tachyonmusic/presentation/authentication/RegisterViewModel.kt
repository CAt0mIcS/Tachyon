package com.tachyonmusic.presentation.authentication

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tachyonmusic.app.R
import com.tachyonmusic.core.Resource
import com.tachyonmusic.core.UiText
import com.tachyonmusic.domain.use_case.RegisterUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject


@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUser: RegisterUser
) : ViewModel() {

    private val _email = mutableStateOf("")
    val email: State<String> = _email

    private val _password = mutableStateOf("")
    val password: State<String> = _password

    private val _error = mutableStateOf<UiText?>(null)
    val error: State<UiText?> = _error


    fun onRegisterClicked() {
        registerUser(email.value, password.value).onEach { resource ->
            _error.value = if (resource is Resource.Error)
                resource.message ?: UiText.StringResource(R.string.unknown_error)
            else
                null
        }.launchIn(viewModelScope)
    }

    fun onEmailChanged(email: String) {
        _email.value = email
    }

    fun onPasswordChanged(password: String) {
        _password.value = password
    }
}