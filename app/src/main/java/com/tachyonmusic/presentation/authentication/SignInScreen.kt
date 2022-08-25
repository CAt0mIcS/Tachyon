package com.tachyonmusic.presentation.authentication

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tachyonmusic.core.NavigationItem
import com.tachyonmusic.app.R

object SignInScreen : NavigationItem("sign_in") {

    @Composable
    operator fun invoke(
        navController: NavController,
        viewModel: LoginViewModel = hiltViewModel()
    ) {
        val email = viewModel.email.value
        val password = viewModel.password.value
        val error = viewModel.error.value

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            TextField(value = email, onValueChange = { viewModel.onEmailChanged(it) })
            TextField(
                value = password,
                onValueChange = { viewModel.onPasswordChanged(it) })
            Button(onClick = { viewModel.onSignInClicked() }) {
                Text(text = stringResource(R.string.sign_in))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = stringResource(R.string.dont_have_account_question))
                Text(
                    text = stringResource(R.string.register),
                    modifier = Modifier.clickable { navController.navigate(RegisterScreen.route) })
            }

            if (error != null) {
                val context = LocalContext.current
                Text(
                    text = error.asString(context),
                    color = MaterialTheme.colors.error
                )
            }
        }
    }

}