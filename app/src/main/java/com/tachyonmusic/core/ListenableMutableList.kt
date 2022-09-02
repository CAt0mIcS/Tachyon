package com.tachyonmusic.core

import java.util.function.Predicate
import java.util.function.UnaryOperator

class ListenableMutableList<T>(collection: Collection<T>) : ArrayList<T>() {
    private val eventListeners: ArrayList<EventListener<T>> = arrayListOf()

    init {
        addAll(collection)
    }

    fun addListener(listener: EventListener<T>) {
        eventListeners += listener
    }

    fun removeListener(listener: EventListener<T>) {
        eventListeners -= listener
    }

    override fun add(element: T): Boolean {
        return super.add(element).also {
            for (listener in eventListeners)
                listener.onItemAdded(size - 1, this)
        }
    }

    override fun add(index: Int, element: T) {
        super.add(index, element).also {
            for (listener in eventListeners)
                listener.onItemAdded(index, this)
        }
    }

    override fun addAll(elements: Collection<T>): Boolean {
        val startIdx = size
        return super.addAll(elements).also {
            for (listener in eventListeners)
                listener.onItemAdded(startIdx, this)
        }
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        return super.addAll(index, elements).also {
            for (listener in eventListeners)
                listener.onItemAdded(index, this)
        }
    }

    override fun clear() {
        super.clear()
        for (listener in eventListeners)
            listener.onItemRemoved(this)
    }

    override fun remove(element: T): Boolean {
        return super.remove(element).also {
            for (listener in eventListeners)
                listener.onItemRemoved(this)
        }
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        return super.removeAll(elements.toSet()).also {
            for (listener in eventListeners)
                listener.onItemRemoved(this)
        }
    }

    override fun removeIf(filter: Predicate<in T>): Boolean {
        TODO()
    }

    override fun removeAt(index: Int): T {
        val elem = getOrNull(index)
        return super.removeAt(index).also {
            for (listener in eventListeners)
                listener.onItemRemoved(this)
        }
    }

    override fun set(index: Int, element: T): T {
        TODO()
    }

    override fun replaceAll(operator: UnaryOperator<T>) {
        TODO()
    }

    override fun removeRange(fromIndex: Int, toIndex: Int) {
        TODO()
    }


    interface EventListener<T> {
        fun onItemAdded(index: Int, list: List<T>) {}
        fun onItemRemoved(list: List<T>) {}
    }
}