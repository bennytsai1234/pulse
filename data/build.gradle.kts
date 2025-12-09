plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.gemini.music.data"
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


}

dependencies {
    implementation(project(":domain"))
    implementation(project(":core:common"))
    implementation(libs.androidx.datastore.preferences)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Android Core
    implementation(libs.androidx.core.ktx)

    // Amplituda (Audio Waveform)
    // Amplituda (Audio Waveform)
    implementation(libs.amplituda)

    // Network
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging)

    // ===== Testing Dependencies =====
    // JUnit
    testImplementation("junit:junit:4.13.2")

    // Robolectric for Android unit tests
    testImplementation("org.robolectric:robolectric:4.11.1")

    // Room Testing
    testImplementation("androidx.room:room-testing:2.6.1")

    // AndroidX Test Core
    testImplementation("androidx.test:core-ktx:1.5.0")
    testImplementation("androidx.arch.core:core-testing:2.2.0")

    // MockK for mocking
    testImplementation("io.mockk:mockk:1.13.10")

    // Coroutines Test
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

    // Turbine for Flow testing
    testImplementation("app.cash.turbine:turbine:1.1.0")
}
