package com.tachyonmusic.presentation.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tachyonmusic.app.R
import com.tachyonmusic.presentation.component.PlaybacksView
import com.tachyonmusic.presentation.main.component.BottomNavigationItem
import com.tachyonmusic.presentation.player.PlayerScreen

object PlaybackSearchScreen :
    BottomNavigationItem(R.string.btmNav_search, R.drawable.ic_search, "search") {
    @Composable
    operator fun invoke(
        navController: NavController,
        viewModel: PlaybackSearchViewModel = hiltViewModel()
    ) {

        var searchString by remember { mutableStateOf("") }
        val searchResults by viewModel.searchResults
        val albumArts = viewModel.albumArtworkLoading
        val focusRequester = remember { FocusRequester() }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            TextField(
                modifier = Modifier.focusRequester(focusRequester),
                value = searchString,
                singleLine = true,
                onValueChange = {
                    searchString = it
                    viewModel.onSearch(it)
                })

            PlaybacksView(items = searchResults, albumArts = albumArts) {
                viewModel.onItemClicked(it)
                navController.navigate(PlayerScreen.route)
            }
        }

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
    }
}