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
    compileSdk = Version.COMPILE_SDK

    defaultConfig {
        applicationId = "com.tachyonmusic"
        minSdk = Version.MIN_SDK
        targetSdk = Version.TARGET_SDK
        versionCode = Version.APP
        versionName = Version.APP_NAME

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
        sourceCompatibility = Version.JAVA
        targetCompatibility = Version.JAVA
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }

    namespace = "com.tachyonmusic.app"

    composeOptions {
        kotlinCompilerExtensionVersion = Version.COMPOSE_COMPILER
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
    gson()
    coroutines()
    lifecycle()
    compose()

    paging()
    dagger()
    implementation(Dependency.DaggerHilt.NAVIGATION_COMPOSE)

    implementation(Dependency.Media3.MEDIA_SESSION)
    implementation(Dependency.Media3.CAST)
    implementation(Dependency.Compose.COIL)

    implementation(files(Dependency.Spotify.AAR))
    implementation("com.spotify.android:auth:2.0.2")
    implementation("com.github.kaaes:spotify-web-api-android:0.4.1")
    implementation(Dependency.GSON.GSON)

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