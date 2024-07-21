plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")

    id("kotlin-kapt")
    id("kotlin-android")
    id("com.google.dagger.hilt.android")
    id("dagger.hilt.android.plugin")
}


kotlin {
    sourceSets {
        debug {
            kotlin.srcDir("build/generated/ksp/debug/kotlin")
        }
        release {
            kotlin.srcDir("build/generated/ksp/release/kotlin")
        }
    }
}

android {
    compileSdk = Index.COMPILE_SDK

    defaultConfig {
        applicationId = "com.tachyonmusic"
        minSdk = Index.MIN_SDK
        targetSdk = Index.TARGET_SDK
        versionCode = Index.APP
        versionName = Index.APP_NAME

        ndk.debugSymbolLevel = Index.DEBUG_SYMBOL_LEVEL
        testInstrumentationRunner = "com.tachyonmusic.testutils.HiltTestRunner"

        vectorDrawables {
            useSupportLibrary = true
        }

        manifestPlaceholders["redirectSchemeName"] = "spotify-sdk"
        manifestPlaceholders["redirectHostName"] = "auth"
    }

    buildTypes {
        release {
//            isDebuggable = false
//            isShrinkResources = true
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

        freeCompilerArgs = listOf(
            "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-Xopt-in=androidx.compose.foundation.ExperimentalFoundationApi",
            "-Xopt-in=androidx.constraintlayout.compose.ExperimentalMotionApi",
            "-Xopt-in=androidx.compose.animation.ExperimentalAnimationApi",
            "-Xopt-in=androidx.compose.material3.ExperimentalMaterial3Api",
            "-Xopt-in=kotlin.contracts.ExperimentalContracts",
            "-Xopt-in=androidx.media3.common.util.UnstableApi"
        )
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }

    namespace = "com.tachyonmusic.app"

    composeOptions {
        kotlinCompilerExtensionVersion = Index.COMPOSE_COMPILER
    }

    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/LICENSE-notice.md"
        }
    }
}

dependencies {
    googleCast()
    googlePlay()
    json()
    coroutines()
    lifecycle()
    compose()

    paging()
    dagger()
    implementation(Dependency.DaggerHilt.NAVIGATION_COMPOSE)
    ads()

    implementation(Dependency.Media3.MEDIA_SESSION)
    implementation(Dependency.Media3.CAST)
    implementation(Dependency.Compose.COIL)

    implementation(Dependency.JSON.GSON)

    projectCore()
    projectMedia()
    projectPlaybackLayers()
    projectPlaybackLayerDatabase()
    projectUtil()
    projectLogger()

    unitTest()
    androidTest()

    androidTestImplementation(Dependency.Room.RUNTIME)
    kaptAndroidTest(Dependency.Room.COMPILER)
}