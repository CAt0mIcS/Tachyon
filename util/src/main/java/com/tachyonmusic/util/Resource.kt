package com.tachyonmusic.util

import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

sealed class Resource<T>(val data: T? = null, val message: UiText? = null) {
    class Loading<T>(data: T? = null) : Resource<T>(data)
    class Success<T>(data: T? = null) : Resource<T>(data)
    class Error<T>(
        message: UiText? = null,
        data: T? = null,
        val exception: Throwable? = null
    ) : Resource<T>(data, message) {

        constructor(res: Error<*>, data: T? = null) : this(res.message, data, res.exception)
    }

    inline fun getOrElse(onErrorOrLoading: (T?, UiText?) -> T?): T? {

        contract {
            callsInPlace(onErrorOrLoading, InvocationKind.AT_MOST_ONCE)
        }

        return when (this) {
            is Success -> data
            else -> onErrorOrLoading(data, message)
        }
    }
}
