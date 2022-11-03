plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")

    id("com.google.gms.google-services")
    id("com.google.firebase.firebase-perf")
    id("com.google.firebase.crashlytics")

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

        testInstrumentationRunner = "com.tachyonmusic.HiltTestRunner"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            storeFile = file("D:\\dev\\Android\\Android Keys\\profile_release_key.keystore")
            storePassword = "profile_release_key"
            keyAlias = "profile_release_key"
            keyPassword = "profile_release_key"
        }
    }

    buildTypes {
        release {
            isDebuggable = false
            isShrinkResources = true
            isMinifyEnabled = true

            // TODO: Some serialization class is missing when [minifyEnabled] = true, use (-keep class ... { *; }) in proguard file
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
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
        }
    }
}

dependencies {
    firebase()
    googleCast()
    gson()
    coroutines()
    lifecycle()
    compose()

    dagger()
    implementation(Dependency.DaggerHilt.NAVIGATION_COMPOSE)

    implementation(Dependency.Media3.MEDIA_SESSION)

    projectCore()
    projectMedia()
    projectUser()
    projectUtil()

    localTest()
    androidTest()
}