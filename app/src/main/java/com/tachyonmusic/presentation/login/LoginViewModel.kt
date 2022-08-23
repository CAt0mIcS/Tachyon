package com.tachyonmusic.presentation.login

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.tachyonmusic.user.User
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class LoginViewModel @Inject constructor() : ViewModel() {

    val email = mutableStateOf("spam.2222@web.de")
    val password = mutableStateOf("password")

    fun onSignInClicked() {
        // TODO: User cases && error handling
        User.signIn(email.value, password.value) {}
    }
}