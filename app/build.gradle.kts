plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")

    id("kotlin-kapt")
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
    }

    buildTypes {
        release {
            isDebuggable = false
            isShrinkResources = true
            isMinifyEnabled = true

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
        kotlinCompilerExtensionVersion = "1.3.0"
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
    implementation(Dependency.Compose.COIL)


    projectCore()
    projectMedia()
    projectPlaybackLayerDatabase()
    projectUtil()
    projectLogger()

    unitTest()
    androidTest()

    androidTestImplementation(Dependency.Room.RUNTIME)
    kaptAndroidTest(Dependency.Room.COMPILER)
}