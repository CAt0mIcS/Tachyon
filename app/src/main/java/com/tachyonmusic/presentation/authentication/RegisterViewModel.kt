package com.tachyonmusic.presentation.authentication

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.tachyonmusic.domain.use_case.UseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class RegisterViewModel @Inject constructor(private val useCases: UseCases) : ViewModel() {
    val email = mutableStateOf("")
    val password = mutableStateOf("")

    fun onRegisterClicked() {
        useCases.registerUser(email.value, password.value)
    }
}