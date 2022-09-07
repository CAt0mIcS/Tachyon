package com.tachyonmusic.util

interface IListenable<T> {
    val listeners: MutableSet<T>

    fun registerEventListener(listener: T)
    fun unregisterEventListener(listener: T)
    fun invokeEvent(e: (T) -> Unit)
}

class Listenable<T> : IListenable<T> {
    override val listeners = mutableSetOf<T>()

    override fun registerEventListener(listener: T) {
        listeners += listener
    }

    override fun unregisterEventListener(listener: T) {
        listeners -= listener
    }

    override fun invokeEvent(e: (T) -> Unit) {
        for (listener in listeners)
            e(listener)
    }
}
