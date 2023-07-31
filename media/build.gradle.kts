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

    compileOptions {
        sourceCompatibility = Version.JAVA
        targetCompatibility = Version.JAVA
    }

    kotlinOptions {
        jvmTarget = Version.JVM_TARGET
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

    implementation(files(Dependency.Spotify.AAR))
    implementation("com.spotify.android:auth:2.0.2")
    implementation("com.github.kaaes:spotify-web-api-android:0.4.1")

    projectCore()
    projectPlaybackLayerDatabase()
    projectPlaybackLayers()
    projectUtil()
    projectLogger()
    projectArtworkFetcher()

    unitTest()
    androidTest()
}