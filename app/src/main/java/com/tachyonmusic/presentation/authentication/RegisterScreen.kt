package com.tachyonmusic.presentation.authentication

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tachyonmusic.presentation.util.NavigationItem

object RegisterScreen : NavigationItem("register") {
    @Composable
    operator fun invoke(
        navController: NavController,
        viewModel: RegisterViewModel = hiltViewModel()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            TextField(value = viewModel.email.value, onValueChange = { viewModel.email.value = it })
            TextField(
                value = viewModel.password.value,
                onValueChange = { viewModel.password.value = it })
            Button(onClick = { viewModel.onRegisterClicked() }) {
                Text(text = "Register")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Already have an account?")
                Text(
                    text = "Login",
                    modifier = Modifier.clickable { navController.navigateUp() })
            }
        }
    }
}