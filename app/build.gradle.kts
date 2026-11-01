plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.holaapk"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.holaapk"
        minSdk = 24
        targetSdk = 34
        versionCode = (System.getenv("VERSION_CODE") ?: "1").toInt()
        versionName = System.getenv("VERSION_CODE") ?: "1"
    }

    signingConfigs {
        create("release") {
            val ruta = System.getenv("KEYSTORE_PATH")
            if (ruta != null) {
                storeFile = file(ruta)
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = "hola"
                keyPassword = System.getenv("KEYSTORE_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
        }
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core:1.13.1")
}
