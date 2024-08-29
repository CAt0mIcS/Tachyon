plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")

    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")

    id("kotlinx-serialization")
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
            isMinifyEnabled = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = Index.COMPOSE_COMPILER
    }

    compileOptions {
        sourceCompatibility = Index.JAVA
        targetCompatibility = Index.JAVA
    }

    kotlinOptions {
        jvmTarget = Index.JVM_TARGET

        freeCompilerArgs = listOf(
            "-opt-in=androidx.media3.common.util.UnstableApi",
            "-opt-in=kotlin.contracts.ExperimentalContracts",
        )
    }
    namespace = "com.tachyonmusic.core"
}

dependencies {
    json()
    dagger()

    landscapistGlide()

    projectArtworkFetcher()
    projectUtil()
    projectLogger()

    implementation(Dependency.Media3.MEDIA_SESSION)
    implementation(Dependency.Compose.FOUNDATION)
    implementation(Dependency.Compose.COMPOSE_MATERIAL3)

    unitTest()
    androidTest()
}