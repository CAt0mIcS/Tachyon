package com.tachyonmusic.util

import android.content.Context
import androidx.annotation.StringRes


sealed class UiText {
    data class DynamicString(val value: String) : UiText()
    class StringResource(@StringRes val resId: Int, vararg val arguments: String) : UiText()

    fun asString(context: Context) = when (this) {
        is DynamicString -> value
        is StringResource -> context.getString(resId, *arguments)
    }

    companion object {
        fun build(
            messageLink: Any?,
            vararg arguments: String
        ): UiText? {
            return when (messageLink) {
                is String -> DynamicString(messageLink)
                is Int -> StringResource(messageLink, *arguments)
                null -> null
                else -> throw IllegalArgumentException("Invalid message link type ${messageLink.javaClass.name}")
            }
        }
    }
}

