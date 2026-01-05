plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.madfinalproject"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.madfinalproject"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    // For Groovy (build.gradle)
    implementation("com.google.firebase:firebase-analytics")

// For Kotlin DSL (build.gradle.kts)
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation(libs.appcompat)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.bom)
    implementation(platform(libs.firebase.bom))
    implementation(libs.constraintlayout)
    implementation(libs.cardview)
    implementation(libs.material)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("com.facebook.android:facebook-login:17.0.0")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")
    implementation ("com.google.firebase:firebase-bom:32.7.0")
    implementation ("com.google.firebase:firebase-storage:20.3.0")
    implementation ("com.google.firebase:firebase-auth:22.3.0")
    implementation ("com.google.firebase:firebase-database:20.3.0")
    implementation ("com.google.android.material:material:1.11.0")
}
