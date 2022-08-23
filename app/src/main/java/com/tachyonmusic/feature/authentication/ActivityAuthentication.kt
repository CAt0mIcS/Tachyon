package com.tachyonmusic.feature.authentication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.tachyonmusic.ui.theme.TachyonTheme

class ActivityAuthentication : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TachyonTheme {

            }
        }
    }
}
