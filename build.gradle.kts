import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

buildscript {
    repositories {
        google()
        mavenCentral()

        maven { url = uri("https://jitpack.io") }
    }

    dependencies {
        classpath("com.google.gms:google-services:4.4.2")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.50")
    }
}

plugins {
    id("com.android.application") version "8.3.0" apply false
    id("com.android.library") version "8.3.0" apply false
    id("org.jetbrains.kotlin.android") version Index.KOTLIN apply false

    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
}

// Show basic test information when running CI
tasks.withType(Test::class) {
    testLogging {
        exceptionFormat = TestExceptionFormat.FULL
        events = setOf(
            TestLogEvent.STARTED,
            TestLogEvent.SKIPPED,
            TestLogEvent.PASSED,
            TestLogEvent.FAILED
        )
        showStandardStreams = true
    }
}