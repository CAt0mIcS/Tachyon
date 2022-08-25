package com.tachyonmusic.core

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

sealed class UiText {
    data class DynamicString(val value: String) : UiText()
    data class StringResource(@StringRes val resId: Int) : UiText()

    // TODO: Not working
    @Composable
    fun asString() = when (this) {
        is DynamicString -> value
        is StringResource -> stringResource(resId)
    }

    fun asString(context: Context) = when (this) {
        is DynamicString -> value
        is StringResource -> context.getString(resId)
    }
}
