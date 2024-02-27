plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.tachyonmusic.artworkfetcher"
    compileSdk = Index.COMPILE_SDK

    defaultConfig {
        minSdk = Index.MIN_SDK
        targetSdk = Index.TARGET_SDK

        ndk.debugSymbolLevel = Index.DEBUG_SYMBOL_LEVEL
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = Index.JAVA
        targetCompatibility = Index.JAVA
    }

    kotlinOptions {
        jvmTarget = Index.JVM_TARGET
    }
}

dependencies {
    coroutines()
    gson()

    jsoup()

    projectUtil()
    projectLogger()

    unitTest()
}