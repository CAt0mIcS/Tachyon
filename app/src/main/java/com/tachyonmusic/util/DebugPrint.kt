package com.tachyonmusic.util

import android.widget.Toast
import com.tachyonmusic.TachyonApplication

// TODO: Remove
fun debugPrint(text: String) {
    Toast.makeText(TachyonApplication.instance!!.applicationContext, text, Toast.LENGTH_SHORT).show()
}