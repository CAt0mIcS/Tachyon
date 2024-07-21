package com.tachyonmusic.presentation.library.search

import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.tachyonmusic.app.R
import com.tachyonmusic.core.data.constants.PlaceholderArtwork
import com.tachyonmusic.core.data.constants.PlaybackType
import com.tachyonmusic.presentation.NavigationItem
import com.tachyonmusic.presentation.core_components.HorizontalPlaybackView
import com.tachyonmusic.presentation.entry.SwipingStates
import com.tachyonmusic.presentation.theme.Theme
import com.tachyonmusic.presentation.util.isEnabled
import kotlinx.coroutines.launch

object PlaybackSearchScreen : NavigationItem("playback_search/{playbackType}") {
    val arguments = listOf(
        navArgument("playbackType") { type = NavType.StringType }
    )


    @Composable
    operator fun invoke(
        arguments: Bundle,
        draggable: AnchoredDraggableState<SwipingStates>,
        viewModel: PlaybackSearchViewModel = hiltViewModel()
    ) {
        val playbackType = PlaybackType.build(arguments.getString("playbackType")!!)

        val searchLocation by viewModel.searchLocation.collectAsState()
        val searchQuery by viewModel.searchQuery.collectAsState()
        val searchResults by viewModel.searchResults.collectAsState()
        val focusRequester = remember { FocusRequester() }
        val keyboardController = LocalSoftwareKeyboardController.current
        val scope = rememberCoroutineScope()

        val interactionSource: MutableInteractionSource =
            remember { MutableInteractionSource() }

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
        Column {
            BasicTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = Theme.padding.medium,
                        end = Theme.padding.medium,
                        top = Theme.padding.medium
                    )
                    .shadow(Theme.shadow.medium, shape = Theme.shapes.medium)
                    .clip(Theme.shapes.medium)
                    .defaultMinSize(
                        minWidth = TextFieldDefaults.MinWidth,
                        minHeight = TextFieldDefaults.MinHeight
                    )
                    .focusRequester(focusRequester)
                    .onFocusChanged {
                        if (it.isFocused)
                            keyboardController?.show()

                    },
                value = searchQuery,
                onValueChange = {
                    viewModel.search(it, playbackType)
                },
                singleLine = true
            ) { innerTextField ->
                TextFieldDefaults.DecorationBox(
                    value = searchQuery,
                    innerTextField = innerTextField,
                    enabled = true,
                    singleLine = true,
                    visualTransformation = VisualTransformation.None,
                    interactionSource = interactionSource,
                    isError = false,
                    colors = TextFieldDefaults.colors().copy(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,

                        focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    placeholder = {
                        Text(
                            text = stringResource(androidx.appcompat.R.string.search_menu_title),
                            fontSize = 22.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search Playbacks",
                            modifier = Modifier.scale(1.2f)
                        )
                    },
                    trailingIcon = {
                        Icon(
                            painterResource(searchLocation.icon),
                            contentDescription = "Change search location",
                            modifier = Modifier
                                .scale(.9f)
                                .clip(Theme.shapes.extraLarge)
                                .clickable {
                                    viewModel.updateSearchLocation(searchLocation.next)
                                }
                        )
                    },
                    contentPadding = PaddingValues(0.dp),
                )
            }

            Spacer(Modifier.height(Theme.padding.large))
            Box(
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.surfaceContainerLowest,
                        shape = Theme.shapes.large
                    )
                    .clip(Theme.shapes.large)
                    .fillMaxHeight()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = Theme.padding.medium,
                            top = Theme.padding.small,
                            end = Theme.padding.medium
                        )
                ) {

                    items(searchResults) {
                        HorizontalPlaybackView(
                            playback = it.playback,
                            modifier = Modifier
                                .isEnabled(it.playback.isPlayable)
                                .padding(bottom = Theme.padding.extraSmall),
                            artwork = it.playback.artwork ?: PlaceholderArtwork
                        ) {
                            viewModel.onItemClicked(it.playback)
                            scope.launch {
                                draggable.animateTo(SwipingStates.EXPANDED)
                            }
                            keyboardController?.hide()
                        }
                    }
                }
            }
        }
    }
}