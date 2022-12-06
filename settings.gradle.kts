pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        maven { url = uri("https://jitpack.io") }
    }
}
rootProject.name = "Tachyon"
include(":app")
include(":media")
include(":user")
include(":util")
include(":core")
include(":testutils")
include(":artworkDownloader")
include(":database")
