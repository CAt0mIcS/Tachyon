package com.tachyonmusic.presentation.main

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tachyonmusic.app.R
import com.tachyonmusic.presentation.authentication.SignInScreen
import com.tachyonmusic.presentation.main.component.BottomNavigationItem

object LibraryScreen :
    BottomNavigationItem(R.string.btmNav_library, R.drawable.ic_library, "library") {

    @Composable
    operator fun invoke(
        navController: NavController,
        viewModel: LibraryViewModel = hiltViewModel()
    ) {
        Button(
            onClick = { navController.navigate(SignInScreen.route) },
        ) {
            Text("Sign In")
        }
    }
}
