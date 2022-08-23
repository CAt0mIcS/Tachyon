package com.tachyonmusic.presentation.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.tachyonmusic.presentation.components.UserInformation
import com.tachyonmusic.presentation.destinations.RegisterScreenDestination

@Destination
@Composable
fun LoginScreen(
    navigator: DestinationsNavigator,
    viewModel: LoginViewModel = hiltViewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        UserInformation(
            buttonText = "Sign In",
            email = viewModel.email.value,
            password = viewModel.password.value,
            onButtonClicked = { viewModel.onSignInClicked() },
            onEmailChanged = { viewModel.email.value = it },
            onPasswordChanged = { viewModel.password.value = it }
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Dont' have an account?")
            Text(
                text = "Register",
                modifier = Modifier.clickable { navigator.navigate(RegisterScreenDestination()) })
        }
    }
}