plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")

    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}

android {
    compileSdk = Version.COMPILE_SDK

    defaultConfig {
        minSdk = Version.MIN_SDK
        targetSdk = Version.TARGET_SDK

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
        kotlinCompilerExtensionVersion = Version.COMPOSE_COMPILER
    }

    compileOptions {
        sourceCompatibility = Version.JAVA
        targetCompatibility = Version.JAVA
    }

    kotlinOptions {
        jvmTarget = Version.JVM_TARGET
    }
    namespace = "com.tachyonmusic.core"
}

dependencies {
    gson()
    dagger()

    landscapist_glide()

    projectArtworkFetcher()
    projectUtil()
    projectLogger()

    implementation(Dependency.Media3.MEDIA_SESSION)
    implementation(Dependency.Compose.FOUNDATION)
    implementation(Dependency.Compose.COMPOSE_MATERIAL)

    unitTest()
    androidTest()
}