package com.tachyonmusic.presentation.player.component.cast

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.tachyonmusic.app.R

@Composable
fun MediaRouter(modifier: Modifier, iconWidth: Dp) {
    val context = LocalContext.current

    val mediaRouteProviderViewModel = MediaRouteViewModel(context)

    Box(modifier = modifier.size(iconWidth)) {
        AndroidView(factory = {
            mediaRouteProviderViewModel.buttonView
        }, modifier = Modifier.fillMaxSize())

        Box(modifier = Modifier
            .fillMaxSize()
            .clickable { mediaRouteProviderViewModel.onClick() })
    }
}