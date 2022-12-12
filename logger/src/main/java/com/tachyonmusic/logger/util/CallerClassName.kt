package com.tachyonmusic.logger.util

object CallerClassName {
    operator fun invoke(): String? {
        val stElements = Thread.currentThread().stackTrace.filter {
            !it.className.contains("com.tachyonmusic.logger") &&
                    !it.toString().contains("dalvik.system.VMStack.getThreadStackTrace") &&
                    !it.toString().contains("java.lang.Thread.getStackTrace")
        }
        val name = stElements.getOrNull(0)?.className
        val idx = (name?.lastIndexOf('.') ?: -2) + 1
        return if (idx != -1)
            name?.substring(idx)
        else null
    }
}