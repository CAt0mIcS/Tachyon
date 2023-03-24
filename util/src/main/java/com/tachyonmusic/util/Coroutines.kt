@file:OptIn(ExperimentalContracts::class)

package com.tachyonmusic.util

import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.*
import java.util.concurrent.Callable
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


fun <T> future(
    context: CoroutineDispatcher,
    action: suspend CoroutineScope.() -> T
): ListenableFuture<T> =
    Futures.submit(Callable {

        runBlocking {
            action()
        }

    }, context.asExecutor())


/**
 * Dispatches [block] to UI thread while suspending current coroutine until [block] finishes
 */
@OptIn(ExperimentalContracts::class)
suspend fun <T> runOnUiThread(block: suspend CoroutineScope.() -> T): T {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return withContext(Dispatchers.Main) {
        block()
    }
}

/**
 * Dispatches [block] to UI thread without suspending current coroutine
 */
fun CoroutineScope.runOnUiThreadAsync(block: suspend CoroutineScope.() -> Unit) =
    launch(Dispatchers.Main) {
        block()
    }