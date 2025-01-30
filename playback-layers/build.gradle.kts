plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")

    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}

android {
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
            isMinifyEnabled = false

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
    namespace = "com.tachyonmusic.playback_layers"
}

dependencies {
    dagger()
    coroutines()
    json()

    implementation("androidx.documentfile:documentfile:1.0.1")

    projectCore()
    projectUtil()
    projectLogger()
    projectPlaybackLayerDatabase()
    projectMetadataApi()
}