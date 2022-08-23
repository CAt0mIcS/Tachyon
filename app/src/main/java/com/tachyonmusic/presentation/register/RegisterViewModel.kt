package com.tachyonmusic.presentation.register

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.tachyonmusic.user.User
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class RegisterViewModel @Inject constructor() : ViewModel() {
    val email = mutableStateOf("")
    val password = mutableStateOf("")

    fun onRegisterClicked() {
        User.register(email.value, password.value) {}
    }
}