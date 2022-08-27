package com.tachyonmusic.util

import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.*
import java.util.concurrent.Callable
import kotlin.coroutines.CoroutineContext


/**
 * Launches a new coroutine using the [context] specified
 * @return the job that was launched using [CoroutineScope.launch]
 */
fun launch(context: CoroutineContext, block: suspend CoroutineScope.() -> Unit): Job =
    CoroutineScope(context).launch {
        block()
    }


/**
 * Launches a new coroutine using the [context] specified
 * @return the deferred that was launched using [CoroutineScope.launch]
 */
fun <T> async(context: CoroutineContext, block: suspend CoroutineScope.() -> T) =
    CoroutineScope(context).async {
        block()
    }


fun <T> future(
    context: CoroutineDispatcher,
    action: suspend CoroutineScope.() -> T
): ListenableFuture<T> =
    Futures.submit(Callable {

        runBlocking {
            action()
        }

    }, context.asExecutor())
