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

        freeCompilerArgs = listOf(
            "-Xopt-in=kotlin.contracts.ExperimentalContracts",
            "-Xopt-in=androidx.media3.common.util.UnstableApi",
        )
    }

    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/LICENSE-notice.md"
        }
    }

    namespace = "com.tachyonmusic.media"
}

dependencies {
    coroutines()
    media3()
    googleCast()
    dagger()
    implementation("androidx.documentfile:documentfile:1.0.1")

    implementation("org.nanohttpd:nanohttpd:2.3.1")

    projectCore()
    projectPlaybackLayerDatabase()
    projectPlaybackLayers()
    projectUtil()
    projectLogger()
    projectArtworkFetcher()

    unitTest()
    androidTest()
}