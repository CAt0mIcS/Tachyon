package com.tachyonmusic.data.repository

import com.tachyonmusic.util.Duration
import kotlinx.coroutines.flow.StateFlow

interface StateRepository {
    val isLoading: StateFlow<Boolean>

    /**
     * Sets [isLoading] to true until the task with [name] and all other tasks have stopped loading
     * @param name arbitrary unique task name
     * @return false if task with [name] already exists else true
     */
    fun queueLoadingTask(name: String): Boolean

    /**
     * Finishes the task with [name]. If all tasks are finished [isLoading] will be set to false here
     * @param name arbitrary unique task name that was used to start the task in [queueLoadingTask]
     * @return false if task with [name] doesn't exist else true
     */
    fun finishLoadingTask(name: String): Boolean

    /**
     * Like [finishLoadingTask]
     * @param timeout timeout before finishing the task to allow other tasks to start
     */
    suspend fun finishLoadingTask(name: String, timeout: Duration): Boolean

    fun isLoadingTaskRunning(name: String): Boolean
}