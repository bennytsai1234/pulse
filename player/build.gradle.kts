plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.sigma.music.player"
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

    sourceSets {
        getByName("main") {
            java.srcDirs("manager", "notification", "service", "di")
        }
    }
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":core"))

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    // Media3
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.common)
    implementation(libs.androidx.media3.exoplayer)

    // Guava for ListenableFuture
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-guava:1.7.3")

    // Android Core
    implementation(libs.androidx.core.ktx)
}
