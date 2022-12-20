import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.project

object Dependency {
    object Firebase {
        const val BOM = "com.google.firebase:firebase-bom:30.3.2"
        const val CORE = "com.google.firebase:firebase-core"
        const val AUTH = "com.google.firebase:firebase-auth-ktx"
        const val FIRESTORE = "com.google.firebase:firebase-firestore-ktx"

        const val ANALYTICS = "com.google.firebase:firebase-analytics-ktx"
        const val CRASHLYTICS = "com.google.firebase:firebase-crashlytics-ktx"
        const val PERFORMANCE = "com.google.firebase:firebase-perf-ktx"
    }

    object Compose {
        const val APP_COMPAT = "androidx.appcompat:appcompat:1.5.1"
        const val ANDROID_MATERIAL = "com.google.android.material:material:1.7.0"
        const val COMPOSE_SLIDERS = "com.github.krottv:compose-sliders:0.1.14"
        const val UI = "androidx.compose.ui:ui:${Version.COMPOSE}"
        const val COMPOSE_MATERIAL = "androidx.compose.material:material:${Version.COMPOSE}"
        const val UI_TOOLING_PREVIEW = "androidx.compose.ui:ui-tooling-preview:${Version.COMPOSE}"
        const val NAVIGATION = "androidx.navigation:navigation-compose:2.5.3"
        const val LIVEDATA = "androidx.compose.runtime:runtime-livedata:${Version.COMPOSE}"
        const val ACTIVITY = "androidx.activity:activity-compose:1.6.1"
        const val UI_TOOLING = "androidx.compose.ui:ui-tooling:${Version.COMPOSE}"
        const val COIL = "io.coil-kt:coil-compose:2.2.2"
        const val PAGING = "androidx.paging:paging-compose:1.0.0-alpha17"

        object Accompanist {
            const val NAVIGATION_ANIMATION =
                "com.google.accompanist:accompanist-navigation-animation:0.28.0"
        }
    }

    object Paging {
        const val PAGING = "androidx.paging:paging-runtime:${Version.PAGING}"
        const val COMPOSE = Compose.PAGING
    }

    object Lifecycle {
        const val RUNTIME = "androidx.lifecycle:lifecycle-runtime-ktx:2.5.1"
        const val LIVEDATA = "androidx.lifecycle:lifecycle-livedata-ktx:2.6.0-alpha03"
    }

    object Coroutine {
        const val ANDROID = "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4"
        const val GUAVA = "org.jetbrains.kotlinx:kotlinx-coroutines-guava:1.6.4"
    }

    object DaggerHilt {
        const val NAVIGATION_COMPOSE = "androidx.hilt:hilt-navigation-compose:1.0.0"
        const val HILT_ANDROID = "com.google.dagger:hilt-android:2.43.2"
        const val COMPILER = "com.google.dagger:hilt-compiler:2.43.2"
    }

    object Cast {
        const val CAST_FRAMEWORK = "com.google.android.gms:play-services-cast-framework:21.2.0"
    }

    object GSON {
        const val GSON = "com.google.code.gson:gson:2.9.0"
    }

    object Jsoup {
        const val JSOUP = "org.jsoup:jsoup:1.15.3"
    }

    object Glide {
        const val GLIDE = "com.github.bumptech.glide:glide:4.14.2"
        const val GLIDE_COMPOSE = "com.github.bumptech.glide:compose:1.0.0-alpha.1"
    }

    object Test {
        const val ANDROIDX_CORE = "androidx.test:core:1.4.0"
        const val JUNIT = "junit:junit:4.13.2"
        const val ARCH_CORE_TESTING = "androidx.arch.core:core-testing:2.1.0"
        const val COROUTINES_TEST = "org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4"

        const val COMPOSE_TEST = "androidx.compose.ui:ui-test-junit4:${Version.COMPOSE}"
        const val TEST_MANIFEST = "androidx.compose.ui:ui-test-manifest:${Version.COMPOSE}"

        const val DAGGER_TEST = "com.google.dagger:hilt-android-testing:2.37"
        const val DAGGER_TEST_COMPILER = "com.google.dagger:hilt-android-compiler:2.37"
        const val JUNIT_EXT = "androidx.test.ext:junit:1.1.3"
        const val TEST_RUNNER = "androidx.test:runner:1.4.0"
    }

    object Media3 {
        const val EXOPLAYER = "androidx.media3:media3-exoplayer:${Version.MEDIA3}"
        const val MEDIA_SESSION = "androidx.media3:media3-session:${Version.MEDIA3}"
        const val CAST = "androidx.media3:media3-cast:${Version.MEDIA3}"
    }

    object Room {
        const val RUNTIME = "androidx.room:room-runtime:${Version.ROOM}"
        const val COMPILER = "androidx.room:room-compiler:${Version.ROOM}"
        const val COROUTINES = "androidx.room:room-ktx:${Version.ROOM}"
        const val PAGING = "androidx.room:room-paging:${Version.ROOM}"
    }
}

fun DependencyHandler.firebase() {
    implementation(platform(Dependency.Firebase.BOM))
    implementation(Dependency.Firebase.CORE)
    implementation(Dependency.Firebase.AUTH)
    implementation(Dependency.Firebase.FIRESTORE)
}

fun DependencyHandler.firebaseAnalytics() {
    implementation(Dependency.Firebase.ANALYTICS)
    implementation(Dependency.Firebase.CRASHLYTICS)
    implementation(Dependency.Firebase.PERFORMANCE)
}

fun DependencyHandler.compose() {
    implementation(Dependency.Compose.APP_COMPAT)
    implementation(Dependency.Compose.ANDROID_MATERIAL)

    implementation(Dependency.Compose.COMPOSE_SLIDERS)
    implementation(Dependency.Compose.PAGING)

    implementation(Dependency.Compose.UI)
    implementation(Dependency.Compose.COMPOSE_MATERIAL)
    implementation(Dependency.Compose.UI_TOOLING_PREVIEW)

    implementation(Dependency.Compose.NAVIGATION)

    implementation(Dependency.Compose.LIVEDATA)
//    kapt(Dependency.Compose.JUNIT4)

    implementation(Dependency.Compose.ACTIVITY)

    implementation(Dependency.Compose.Accompanist.NAVIGATION_ANIMATION)

    debugImplementation(Dependency.Compose.UI_TOOLING)
//    debugImplementation(Dependency.Compose.TEST_MANIFEST)
}

fun DependencyHandler.lifecycle() {
    implementation(Dependency.Lifecycle.RUNTIME)
    implementation(Dependency.Lifecycle.LIVEDATA)
}

fun DependencyHandler.coroutines() {
    implementation(Dependency.Coroutine.ANDROID)
    implementation(Dependency.Coroutine.GUAVA)
}

fun DependencyHandler.dagger() {
    implementation(Dependency.DaggerHilt.HILT_ANDROID)
    kapt(Dependency.DaggerHilt.COMPILER)
}

fun DependencyHandler.paging() {
    implementation(Dependency.Paging.PAGING)
    implementation(Dependency.Paging.COMPOSE)
}

fun DependencyHandler.room() {
    implementation(Dependency.Room.RUNTIME)
    annotationProcessor(Dependency.Room.COMPILER)

    implementation(Dependency.Room.PAGING)

    // To use Kotlin annotation processing tool (kapt)
    kapt(Dependency.Room.COMPILER)
    // To use Kotlin Symbol Processing (KSP)
//    ksp(Dependency.Room.COMPILER)

    // optional - Kotlin Extensions and Coroutines support for Room
    implementation(Dependency.Room.COROUTINES)

    // optional - RxJava2 support for Room
//    implementation("androidx.room:room-rxjava2:$room_version")

    // optional - RxJava3 support for Room
//    implementation("androidx.room:room-rxjava3:$room_version")

    // optional - Guava support for Room, including Optional and ListenableFuture
//    implementation("androidx.room:room-guava:$room_version")

    // optional - Test helpers
//    testImplementation("androidx.room:room-testing:$room_version")
}

fun DependencyHandler.googleCast() {
    implementation(Dependency.Cast.CAST_FRAMEWORK)
}

fun DependencyHandler.gson() {
    implementation(Dependency.GSON.GSON)
}

fun DependencyHandler.jsoup() {
    implementation(Dependency.Jsoup.JSOUP)
}

fun DependencyHandler.glide() {
    implementation(Dependency.Glide.GLIDE)
    implementation(Dependency.Glide.GLIDE_COMPOSE)
}

fun DependencyHandler.projectMedia() {
    implementation(project(":media"))
}

fun DependencyHandler.projectCore() {
    implementation(project(":core"))
}

fun DependencyHandler.projectUser() {
    implementation(project(":user"))
}

fun DependencyHandler.projectArtworkFetcher() {
    implementation(project(":artworkFetcher"))
}

fun DependencyHandler.projectLogger() {
    implementation(project(":logger"))
}

fun DependencyHandler.projectUtil() {
    implementation(project(":util"))
}

fun DependencyHandler.projectDatabase() {
    implementation(project(":database"))
}


fun DependencyHandler.unitTest(
    configName: String = "testImplementation",
    addTestUtilsProject: Boolean = true
) {
    add(configName, Dependency.Test.ANDROIDX_CORE)
    add(configName, Dependency.Test.JUNIT)
    add(configName, Dependency.Test.ARCH_CORE_TESTING)
    add(configName, Dependency.Test.COROUTINES_TEST)
    if (addTestUtilsProject)
        add(configName, project(":testutils"))
}

fun DependencyHandler.androidTest(
    configName: String = "androidTestImplementation",
    addTestUtilsProject: Boolean = true
) {

    kaptAndroidTest(Dependency.Test.DAGGER_TEST_COMPILER)

    add(configName, Dependency.Test.JUNIT)
    add(configName, Dependency.Test.COMPOSE_TEST)
    add(configName, Dependency.Test.TEST_MANIFEST)
    add(configName, Dependency.Test.DAGGER_TEST)
    add(configName, Dependency.Test.COROUTINES_TEST)
    add(configName, Dependency.Test.ARCH_CORE_TESTING)
    add(configName, Dependency.Test.JUNIT_EXT)
    add(configName, Dependency.Test.ANDROIDX_CORE)
    add(configName, Dependency.Test.TEST_RUNNER)
    if (addTestUtilsProject)
        add(configName, project(":testutils"))
}

fun DependencyHandler.media3() {
    implementation(Dependency.Media3.EXOPLAYER)
    implementation(Dependency.Media3.MEDIA_SESSION)
    implementation(Dependency.Media3.CAST)
}

fun DependencyHandler.implementation(depName: String) {
    add("implementation", depName)
}

fun DependencyHandler.implementation(dep: org.gradle.api.artifacts.Dependency) {
    add("implementation", dep)
}

private fun DependencyHandler.debugImplementation(depName: String) {
    add("debugImplementation", depName)
}

private fun DependencyHandler.kapt(depName: String) {
    add("kapt", depName)
}

private fun DependencyHandler.ksp(depName: String) {
    add("ksp", depName)
}

private fun DependencyHandler.annotationProcessor(depName: String) {
    add("annotationProcessor", depName)
}

private fun DependencyHandler.testImplementation(depName: String) {
    add("testImplementation", depName)
}

private fun DependencyHandler.testImplementation(dep: org.gradle.api.artifacts.Dependency) {
    add("testImplementation", dep)
}

private fun DependencyHandler.androidTestImplementation(depName: String) {
    add("androidTestImplementation", depName)
}

private fun DependencyHandler.androidTestImplementation(dep: org.gradle.api.artifacts.Dependency) {
    add("androidTestImplementation", dep)
}

private fun DependencyHandler.kaptAndroidTest(depName: String) {
    add("kaptAndroidTest", depName)
}