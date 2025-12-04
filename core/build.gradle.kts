plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.sigma.music.core"
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

    sourceSets {
        getByName("main") {
            java.srcDirs("common", "datastore", "designsystem", "di", "parser", "util")
        }
    }
}

dependencies {
    implementation(project(":domain"))

    // Hilt for DI
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    // Room for DatabaseModule
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // DataStore for UserPreferencesRepository
    implementation(libs.androidx.datastore.preferences)

    // Media3 for PlayerModule
    implementation(libs.androidx.media3.exoplayer)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Android Core
    implementation(libs.androidx.core.ktx)
}
