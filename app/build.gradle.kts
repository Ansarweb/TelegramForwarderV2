plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.ansarweb.telegramforwarderv2"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.ansarweb.telegramforwarderv2"
        minSdk = 26
        targetSdk = 35

        versionCode = 3
        versionName = "2.1.0"
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
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")

    implementation("com.github.tdlibx:td:1.8.56")
}
