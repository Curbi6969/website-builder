plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21"
}

android {
    namespace = "com.rise.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.rise.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        // On-device voice (sherpa-onnx) ships native .so per ABI; limit to the common
        // phone architectures to keep the APK from carrying x86 desktop/emulator libs.
        ndk {
            abiFilters += setOf("arm64-v8a", "armeabi-v7a")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Sign release with the debug key so it installs locally for perf testing.
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        // java.time on minSdk 24 (available natively from API 26).
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.ui.text.google.fonts)

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.2")

    // Supabase (auth + database) + Ktor engine
    implementation(platform("io.github.jan-tennert.supabase:bom:3.1.4"))
    implementation("io.github.jan-tennert.supabase:auth-kt")
    implementation("io.github.jan-tennert.supabase:postgrest-kt")
    implementation("io.ktor:ktor-client-okhttp:3.1.3")

    // On-device Dutch speech: sherpa-onnx (Whisper STT + Piper TTS), prebuilt AAR in libs/.
    // Run scripts/fetch-voice-libs.sh to download it (gitignored, 57 MB).
    implementation(files("libs/sherpa-onnx-1.13.3.aar"))
    // tar.bz2 extraction for the downloaded voice models.
    implementation("org.apache.commons:commons-compress:1.27.1")

    debugImplementation(libs.androidx.ui.tooling)
}
