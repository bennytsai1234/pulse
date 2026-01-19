plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.pulse.music.player"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    testOptions {
        unitTests.all {
            it.useJUnitPlatform()
        }
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":core:common"))

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    // Media3
    api(libs.androidx.media3.session)
    implementation(libs.androidx.media3.common)
    implementation(libs.androidx.media3.exoplayer)

    // Guava for ListenableFuture
    implementation(libs.kotlinx.coroutines.guava)

    // Android Core
    implementation(libs.androidx.core.ktx)

    // ===== Testing Dependencies =====
    // JUnit 5
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)

    // MockK for mocking
    testImplementation(libs.mockk)

    // Coroutines Test
    testImplementation(libs.kotlinx.coroutines.test)

    // Turbine for Flow testing
    testImplementation(libs.turbine)
}
