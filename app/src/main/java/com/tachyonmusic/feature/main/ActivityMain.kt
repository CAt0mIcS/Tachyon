package com.tachyonmusic.feature.main

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.tachyonmusic.app.R
import com.tachyonmusic.feature.main.util.Permission
import com.tachyonmusic.feature.main.util.PermissionManager
import com.tachyonmusic.ui.theme.TachyonTheme

class ActivityMain : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TachyonTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Greeting("Android")
                }
            }
        }

        PermissionManager.from(this).apply {
            request(Permission.ReadStorage)
            rationale(getString(R.string.storage_permission_rationale))
            checkPermission { result: Boolean ->
                if (result) {
                    println("Storage permission granted")
                } else
                    println("Storage permission NOT granted")
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    TachyonTheme {
        Greeting("Android")
    }
}