package com.tachyonmusic.data.repository

import com.tachyonmusic.domain.repository.StateRepository
import com.tachyonmusic.logger.domain.Logger
import com.tachyonmusic.util.Duration
import com.tachyonmusic.util.delay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext

const val STATE_LOADING_TASK_STARTUP = "Startup"

class StateRepositoryImpl(
    val scope: CoroutineScope,
    private val log: Logger
) : StateRepository {
    /**
     * Task scheduling system to always display loading popup if anything is loading
     * [LoadingTask.STARTUP] needs to be finished using [finishLoadingTask] since we always want to
     * show the popup when starting the app
     */
    val tasks = MutableStateFlow(listOf(STATE_LOADING_TASK_STARTUP))
    override val isLoading = tasks.map {
        it.isNotEmpty()
    }.stateIn(scope + Dispatchers.IO, SharingStarted.Lazily, true)
    private val taskLock = Any()

    override fun queueLoadingTask(name: String): Boolean {
        log.debug("Queueing task $name with isLoading = ${isLoading.value}...")
        val ret = synchronized(taskLock) {
            if (tasks.value.contains(name)) {
                log.debug("Task $name is already queued")
                return@synchronized false
            }
            tasks.update { it + name }
            return@synchronized true
        }

        log.debug("Ending StateRepository::queueLoadingTask of $name with isLoading = ${isLoading.value}")
        return ret
    }

    override fun finishLoadingTask(name: String): Boolean {
        log.debug("Finishing task $name with isLoading = ${isLoading.value}...")
        val ret = synchronized(taskLock) {
            if (!tasks.value.contains(name)) {
                log.debug("Task $name does not exist")
                return@synchronized false
            }

            tasks.update { it - name }
            return@synchronized true
        }

        log.debug("Ending StateRepository::finishLoadingTask of $name with isLoading = ${isLoading.value}")
        return ret
    }

    override suspend fun finishLoadingTask(name: String, timeout: Duration) =
        withContext(Dispatchers.IO) {
            log.debug("Finishing task $name with isLoading = ${isLoading.value} and timeout = $timeout...")
            launch {
                delay(timeout)
                finishLoadingTask(name)
            }
            log.debug("Timeout is set, returning from function and letting next thread do the rest...")
            true // TODO
        }

    override fun isLoadingTaskRunning(name: String) = synchronized(taskLock) {
        tasks.value.contains(name)
    }

    override fun listenToTask(name: String) = tasks.map {
        it.contains(name)
    }
}