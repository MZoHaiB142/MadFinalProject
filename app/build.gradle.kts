import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.madfinalproject"
    compileSdk = 34
    buildFeatures {
        viewBinding=true
        buildConfig=true
    }

    val localProperties = Properties().apply {
        val file = rootProject.file("local.properties")
        if (file.exists()) file.inputStream().use { load(it) }
    }

    defaultConfig {
        applicationId = "com.example.madfinalproject"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "OPENAI_API_KEY", "\"${localProperties.getProperty("OPENAI_API_KEY", "")}\"")
        buildConfigField("String", "OPENAI_MODEL", "\"${localProperties.getProperty("OPENAI_MODEL", "gpt-5.5")}\"")
        buildConfigField("String", "AI_BASE_URL", "\"${localProperties.getProperty("AI_BASE_URL", "https://api.openai.com/v1/")}\"")
        buildConfigField("String", "GEMINI_API_KEY", "\"${localProperties.getProperty("GEMINI_API_KEY", "")}\"")
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1,DEPENDENCIES}"
            excludes += "META-INF/INDEX.LIST"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/license.txt"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/NOTICE.txt"
            excludes += "META-INF/notice.txt"
            excludes += "META-INF/ASL2.0"
            excludes += "META-INF/*.kotlin_module"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
        isCoreLibraryDesugaringEnabled = true
    }
}


dependencies {
    // Firebase BOM (Versions manage karega)
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))

    // Firebase Services
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")

    // ✅ Firestore (Correct)
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-database")

    implementation("com.google.firebase:firebase-storage")

    // AndroidX Libraries
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.cardview)
    implementation(libs.material)

    // Google Play Services
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // Facebook Login
    implementation("com.facebook.android:facebook-login:17.0.0")

    // Image Loading
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation(libs.ext.junit)


    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // Gemini AI
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    // Volley
    implementation("com.android.volley:volley:1.2.1")
    // location ka liya ha
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // Ktor Dependencies
    implementation("io.ktor:ktor-client-android:2.3.11")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.11")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.11")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs_nio:2.0.2")
    implementation("com.github.bumptech.glide:glide:4.16.0")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    // Retrofit — FastAPI se baat karne ke liye
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
// OkHttp logging (debugging ke liye)
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
// Glide — already hai aapke paas
    implementation("com.github.bumptech.glide:glide:4.16.0")
}
