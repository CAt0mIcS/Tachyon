package com.tachyonmusic.presentation.player.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tachyonmusic.app.R
import com.tachyonmusic.presentation.theme.Theme


@Composable
fun IconForward(timeSeconds: Long, modifier: Modifier = Modifier) {
    assert(timeSeconds in 1..99)

    Box(modifier = modifier) {
        Icon(painterResource(R.drawable.ic_forward), contentDescription = null)
        Text(
            timeSeconds.toString(),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 6.sp,
            modifier = Modifier.align(Alignment.Center).offset(y = 1.dp)
        )
    }
}


@Composable
fun IconRewind(timeSeconds: Long, modifier: Modifier = Modifier) {
    assert(timeSeconds in 1..99)

    Box(modifier = modifier) {
        Icon(painterResource(R.drawable.ic_rewind), contentDescription = null)
        Text(
            timeSeconds.toString(),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 6.sp,
            modifier = Modifier.align(Alignment.Center).offset(y = 1.dp)
        )
    }
}