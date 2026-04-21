import org.gradle.api.tasks.Sync

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.yortch.confirmationsaints"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.yortch.confirmationsaints"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        vectorDrawables { useSupportLibrary = true }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            // TODO: add signingConfig when release signing is set up.
        }
        debug {
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
    }

    packaging {
        resources.excludes += setOf(
            "/META-INF/{AL2.0,LGPL2.1}",
            "/META-INF/LICENSE*",
        )
    }
}

dependencies {
    // AndroidX core / lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Persistence
    implementation(libs.androidx.datastore.preferences)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // Image loading
    implementation(libs.coil.compose)

    // Testing
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.turbine)
}

// -----------------------------------------------------------------------------
// syncSharedContent
//
// Bridge between the repo-root SharedContent/ directory (canonical cross-platform
// data source, shared with iOS) and the Android app's bundled assets. Runs as a
// preBuild dependency so every build — IDE or CLI — picks up the latest content.
//
// Inputs:   ../../SharedContent/{saints,categories}/*.json  +  images/*.jpg
// Output:   src/main/assets/{saints-*.json, categories-*.json, images/*.jpg}
//
// Uses Sync so the output directory always mirrors inputs (incremental + up-to-date
// aware). Do NOT commit the generated files — they are gitignored.
// -----------------------------------------------------------------------------
val sharedContentDir = rootProject.layout.projectDirectory.dir("../SharedContent")
val generatedAssetsDir = layout.projectDirectory.dir("src/main/assets")

val syncSharedContent by tasks.registering(Sync::class) {
    group = "confirmation-saints"
    description = "Copies SharedContent/ JSON + images into app assets/ for bundling."

    from(sharedContentDir.dir("saints")) {
        include("saints-en.json", "saints-es.json")
    }
    from(sharedContentDir.dir("categories")) {
        include("categories-en.json", "categories-es.json")
    }
    from(sharedContentDir.dir("images")) {
        include("*.jpg")
        into("images")
    }

    into(generatedAssetsDir)

    // Preserve the checked-in README.md explaining that assets/ is build-generated.
    preserve {
        include("README.md")
    }

    doFirst {
        logger.lifecycle(
            "syncSharedContent: ${sharedContentDir.asFile.absolutePath} -> " +
                generatedAssetsDir.asFile.absolutePath
        )
    }
}

tasks.named("preBuild").configure {
    dependsOn(syncSharedContent)
}
