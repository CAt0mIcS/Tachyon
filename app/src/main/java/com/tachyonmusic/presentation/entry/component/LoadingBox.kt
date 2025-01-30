package com.tachyonmusic.presentation.entry.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tachyonmusic.app.R
import com.tachyonmusic.presentation.theme.Theme

@Composable
fun LoadingBox() {
    Dialog(
        onDismissRequest = { },
        DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .clip(Theme.shapes.large)
                .background(
                    MaterialTheme.colorScheme.background,
                    Theme.shapes.large
                )
                .border(
                    BorderStroke(2.dp, MaterialTheme.colorScheme.surfaceContainer),
                    Theme.shapes.large
                )
        ) {
            Column {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(Theme.padding.medium)
                        .align(Alignment.CenterHorizontally)
                )

                Text(
                    stringResource(R.string.loading),
                    modifier = Modifier
                        .padding(
                            start = 80.dp,
                            end = 80.dp,
                            bottom = Theme.padding.medium
                        ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}