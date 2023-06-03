import org.gradle.api.JavaVersion

object Version {
    const val COMPILE_SDK = 33
    const val MIN_SDK = 23
    const val TARGET_SDK = COMPILE_SDK

    const val APP = 78
    const val APP_NAME = "Dev $APP"

    const val COMPOSE = "1.4.0-alpha04"
    const val MEDIA3 = "1.0.0"
    const val ROOM = "2.4.3"
    const val PAGING = "3.1.1"
    const val MOCKK = "1.13.3"

    val JAVA = JavaVersion.VERSION_11
}