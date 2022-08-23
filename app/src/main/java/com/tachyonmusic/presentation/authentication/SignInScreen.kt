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

object SignInScreen : NavigationItem("sign_in") {

    @Composable
    operator fun invoke(
        navController: NavController,
        viewModel: LoginViewModel = hiltViewModel()
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
            Button(onClick = { viewModel.onSignInClicked() }) {
                Text(text = "Sign In")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Dont' have an account?")
                Text(
                    text = "Register",
                    modifier = Modifier.clickable { navController.navigate("register") })
            }
        }
    }

}