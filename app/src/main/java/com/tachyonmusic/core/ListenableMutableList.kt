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
                listener.onItemAdded(size - 1, listOf(element))
        }
    }

    override fun add(index: Int, element: T) {
        super.add(index, element).also {
            for (listener in eventListeners)
                listener.onItemAdded(index, listOf(element))
        }
    }

    override fun addAll(elements: Collection<T>): Boolean {
        val startIdx = size
        return super.addAll(elements).also {
            for (listener in eventListeners)
                listener.onItemAdded(startIdx, elements)
        }
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        return super.addAll(index, elements).also {
            for (listener in eventListeners)
                listener.onItemAdded(index, elements)
        }
    }

    override fun clear() {
        val items = mutableListOf<T>().apply { addAll(this) }
        super.clear()
        for (listener in eventListeners)
            listener.onItemRemoved(items)
    }

    override fun remove(element: T): Boolean {
        return super.remove(element).also {
            for (listener in eventListeners)
                listener.onItemRemoved(listOf(element))
        }
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        return super.removeAll(elements.toSet()).also {
            for (listener in eventListeners)
                listener.onItemRemoved(elements)
        }
    }

    override fun removeIf(filter: Predicate<in T>): Boolean {
        TODO()
    }

    override fun removeAt(index: Int): T {
        val elem = getOrNull(index)
        return super.removeAt(index).also {
            for (listener in eventListeners)
                listener.onItemRemoved(listOf(elem))
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
        fun onItemAdded(index: Int, items: Collection<T>) {}
        fun onItemRemoved(items: Collection<T?>) {}
    }
}