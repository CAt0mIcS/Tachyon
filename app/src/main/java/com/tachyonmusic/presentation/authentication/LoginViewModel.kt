package com.tachyonmusic.presentation.authentication

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.tachyonmusic.domain.use_case.UseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class LoginViewModel @Inject constructor(private val useCases: UseCases) : ViewModel() {

    val email = mutableStateOf("spam.2222@web.de")
    val password = mutableStateOf("password")

    fun onSignInClicked() {
        useCases.signInUser(email.value, password.value)
    }
}