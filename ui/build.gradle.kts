plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.gemini.music.ui"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
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
    implementation(project(":domain"))
    // NOTE: UI 不應直接依賴 :player 模組 (Clean Architecture)
    // MusicController 介面定義在 :domain，實作透過 Hilt 在 :app 層級注入
    implementation(project(":core:designsystem"))
    implementation(project(":core:common"))


    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // UI: Jetpack Compose (Material 3)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.material.icons.extended)

    // Image Loading & Palette
    implementation(libs.coil.compose)
    implementation(libs.androidx.palette)

    // Lifecycle for Compose
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.0")
    implementation(libs.androidx.activity.compose)
    implementation("androidx.appcompat:appcompat:1.6.1")
    
    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Android Core
    implementation(libs.androidx.core.ktx)
    
    // SplashScreen
    implementation(libs.androidx.core.splashscreen)

    // ===== Testing Dependencies =====
    // JUnit
    testImplementation("junit:junit:4.13.2")

    // MockK for mocking
    testImplementation("io.mockk:mockk:1.13.10")

    // Coroutines Test
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

    // Turbine for Flow testing
    testImplementation("app.cash.turbine:turbine:1.1.0")

    // AndroidX Arch Testing (for InstantTaskExecutorRule)
    testImplementation("androidx.arch.core:core-testing:2.2.0")
}
