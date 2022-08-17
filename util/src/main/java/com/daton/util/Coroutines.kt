package com.daton.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.CoroutineContext


/**
 * Launches a new coroutine using the [context] specified
 * @return the job that was launched using [CoroutineScope.launch]
 */
fun launch(context: CoroutineContext, block: suspend CoroutineScope.() -> Unit): Job =
    CoroutineScope(context).launch {
        block()
    }
