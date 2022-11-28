package com.tachyonmusic.presentation.profile

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.tachyonmusic.app.R
import com.tachyonmusic.presentation.main.component.BottomNavigationItem

object ProfileScreen :
    BottomNavigationItem(R.string.btmNav_profile, R.drawable.ic_profile, "profile") {

    @Composable
    operator fun invoke(
        viewModel: ProfileViewModel = hiltViewModel()
    ) {

    }

}