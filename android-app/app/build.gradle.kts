plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.example.musicbridge"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.example.musicbridge"
        minSdk = 26
        targetSdk = 35
        versionCode = 2
        versionName = "0.1.1"
    }
}
