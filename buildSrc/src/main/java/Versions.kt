import org.gradle.api.JavaVersion

object Version {
    const val COMPILE_SDK = 33
    const val MIN_SDK = 21
    const val TARGET_SDK = COMPILE_SDK

    const val APP = 73
    const val APP_NAME = "Dev $APP"

    const val COMPOSE = "1.4.0-alpha01"
    const val MEDIA3 = "1.0.0-beta02"

    val JAVA = JavaVersion.VERSION_11
}