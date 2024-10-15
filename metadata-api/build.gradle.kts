plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.tachyonmusic.metadata_api"
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
    json()

    jsoup()

    eAlvaBrainz()

    implementation("com.ealva:ealvalog:0.5.6-SNAPSHOT")
    implementation("com.ealva:ealvalog-core:0.5.6-SNAPSHOT")


    projectUtil()
    projectLogger()

    unitTest()
}