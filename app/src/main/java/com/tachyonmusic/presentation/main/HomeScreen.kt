package com.tachyonmusic.presentation.main

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.material.TextFieldDefaults.indicatorLine
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.tachyonmusic.app.R
import com.tachyonmusic.core.domain.playback.Playback
import com.tachyonmusic.core.domain.playback.Song
import com.tachyonmusic.presentation.main.component.BottomNavigationItem
import com.tachyonmusic.presentation.theme.Theme


object HomeScreen :
    BottomNavigationItem(R.string.btmNav_home, R.drawable.ic_home, "home") {

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    operator fun invoke(
        navController: NavController,
        viewModel: HomeViewModel = hiltViewModel()
    ) {
        var searchText by remember { mutableStateOf("") }
        val history by viewModel.history.collectAsState()

//        LaunchedEffect(true) {
//            // Load album art when the view is active, TODO: Unload when view becomes inactive and don't load on main coroutine
//            viewModel.loadArtworkState(history.filterIsInstance<Song>())
//        }


        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Theme.padding.small)
        ) {


            val interactionSource: MutableInteractionSource =
                remember { MutableInteractionSource() }

            BasicTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(Theme.shadow.large, shape = Theme.shapes.medium)
                    .background(Theme.colors.surface, shape = Theme.shapes.medium)
                    .defaultMinSize(
                        minWidth = TextFieldDefaults.MinWidth,
                        minHeight = TextFieldDefaults.MinHeight
                    ),
                value = searchText,
                onValueChange = { searchText = it },
                textStyle = TextStyle.Default.copy(fontSize = 25.sp),
                singleLine = true,
            ) { innerTextField ->
                TextFieldDefaults.TextFieldDecorationBox(
                    value = searchText,
                    innerTextField = innerTextField,
                    placeholder = { //
                        Text(
                            text = stringResource(androidx.appcompat.R.string.search_menu_title),
                            fontSize = 25.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painterResource(R.drawable.ic_search),
                            contentDescription = "Search Playbacks",
                            modifier = Modifier.scale(1.6f)
                        )
                    },
                    interactionSource = interactionSource,
                    visualTransformation = VisualTransformation.None,
                    singleLine = true,
                    enabled = true,
                    isError = false,
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Theme.colors.surface,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Theme.colors.onSurface
                    ),
                    contentPadding = PaddingValues(0.dp)
                )
            }


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(top = Theme.padding.large),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "Recently Played",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center
                ) {
                    TextButton(
                        onClick = {},
                    ) {
                        Text(
                            "View All",
                            color = Color.Blue,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Theme.padding.extraSmall)
            ) {
                items(history) { playback ->
                    PlaybackView(
                        playback,
                        (playback as Song).artwork?.asImageBitmap()
                    )
                }
            }

            Text(
                "Recommended for You",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = Theme.padding.large)
            )

            // TODO: Recommendations
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Theme.padding.small)
            ) {
                items(history) { playback ->
                    PlaybackView(
                        playback,
                        (playback as Song).artwork?.asImageBitmap()
                    )
                }
            }
        }
    }
}


@Composable
fun PlaybackView(playback: Playback, artwork: ImageBitmap? = null) {
    Column(
        modifier = Modifier
            .padding(start = Theme.padding.extraSmall)
            .shadow(Theme.shadow.small, shape = Theme.shapes.medium)
            .background(Theme.colors.surface, shape = Theme.shapes.medium)
            .border(BorderStroke(1.dp, Theme.colors.secondary), shape = Theme.shapes.medium)
    ) {
        if (artwork != null)
            Image(
                bitmap = artwork,
                contentDescription = "Album Artwork",
                modifier = Modifier
                    .padding(Theme.padding.extraSmall)
                    .size(100.dp, 100.dp)
                    .clip(Theme.shapes.medium)
            )
        else
            Image(
                painterResource(R.drawable.artwork_image_placeholder),
                "Album Artwork Placeholder",
                modifier = Modifier
                    .padding(Theme.padding.extraSmall)
                    .size(100.dp, 100.dp)
                    .clip(Theme.shapes.medium)
            )

        Text(
            modifier = Modifier.padding(start = Theme.padding.small),
            text = playback.title ?: "No Title",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )

        Text(
            modifier = Modifier.padding(
                start = Theme.padding.small * 2,
                bottom = Theme.padding.small
            ),
            text = playback.artist ?: "No Artist",
            fontSize = 12.sp
        )
    }
}