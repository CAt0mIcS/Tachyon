import org.gradle.api.JavaVersion

object Index {
    const val COMPILE_SDK = 35
    const val MIN_SDK = 21
    const val TARGET_SDK = COMPILE_SDK

    const val APP = 109
    const val APP_NAME = "Dev $APP"

    const val COMPOSE = "1.6.0"
    const val MEDIA3 = "1.5.1"
    const val ROOM = "2.6.1"
    const val PAGING = "3.1.1"
    const val MOCKK = "1.13.3"
    const val DAGGER_HILT = "2.51.1"

    val JAVA = JavaVersion.VERSION_17
    const val JVM_TARGET = "17"
    const val COMPOSE_COMPILER = "1.5.8"
    const val KOTLIN = "1.9.22"

    const val DEBUG_SYMBOL_LEVEL = "FULL"
}