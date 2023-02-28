package com.tachyonmusic.presentation.util

import androidx.compose.runtime.snapshots.SnapshotStateList

// TODO
inline fun <T> SnapshotStateList<T>.update(action: (List<T>) -> List<T>) {
    val old = this.toMutableList()
    clear()
    addAll(action(old))
}