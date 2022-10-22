package com.tachyonmusic.core

import com.tachyonmusic.util.IListenable
import com.tachyonmusic.util.Listenable
import java.util.function.Predicate
import java.util.function.UnaryOperator

class ListenableMutableList<T>(collection: Collection<T>) :
    ArrayList<T>(),
    IListenable<ListenableMutableList.EventListener<T>> by Listenable() {

    init {
        addAll(collection)
    }

    override fun add(element: T): Boolean {
        return super.add(element).also {
            invokeEvent {
                it.onChanged(this)
                it.onItemAdded(size - 1, this)
            }
        }
    }

    override fun add(index: Int, element: T) {
        super.add(index, element).also {
            invokeEvent {
                it.onChanged(this)
                it.onItemAdded(index, this)
            }
        }
    }

    override fun addAll(elements: Collection<T>): Boolean {
        val startIdx = size
        return super.addAll(elements).also {
            invokeEvent {
                it.onChanged(this)
                it.onItemAdded(startIdx, this)
            }
        }
    }

    override fun addAll(index: Int, elements: Collection<T>): Boolean {
        return super.addAll(index, elements).also {
            invokeEvent {
                it.onChanged(this)
                it.onItemAdded(index, this)
            }
        }
    }

    override fun clear() {
        super.clear()
        invokeEvent {
            it.onChanged(this)
            it.onItemRemoved(this)
        }
    }

    override fun remove(element: T): Boolean {
        return super.remove(element).also {
            invokeEvent {
                it.onChanged(this)
                it.onItemRemoved(this)
            }
        }
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        return super.removeAll(elements.toSet()).also {
            invokeEvent {
                it.onChanged(this)
                it.onItemRemoved(this)
            }
        }
    }

    override fun removeIf(filter: Predicate<in T>): Boolean {
        TODO()
    }

    override fun removeAt(index: Int): T {
        return super.removeAt(index).also {
            invokeEvent {
                it.onChanged(this)
                it.onItemRemoved(this)
            }
        }
    }

    override fun set(index: Int, element: T): T {
        return super.set(index, element).also {
            invokeEvent {
                it.onChanged(this)
                // TODO: Clean this class up, should call onAdded/onRemoved here also
            }
        }
    }

    override fun replaceAll(operator: UnaryOperator<T>) {
        TODO()
    }

    override fun removeRange(fromIndex: Int, toIndex: Int) {
        TODO()
    }


    interface EventListener<T> {
        fun onChanged(list: List<T>) {}
        fun onItemAdded(index: Int, list: List<T>) {}
        fun onItemRemoved(list: List<T>) {}
    }
}