import org.gradle.api.tasks.Sync
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

// Load release signing config from android/keystore.properties (gitignored).
// If the file is absent (e.g. on CI without secrets), release builds fall back
// to unsigned and assembleDebug still works.
val keystorePropsFile = rootProject.file("keystore.properties")
val keystoreProps = Properties().apply {
    if (keystorePropsFile.exists()) {
        keystorePropsFile.inputStream().use { load(it) }
    }
}
val hasReleaseSigning = keystoreProps.getProperty("storeFile") != null &&
    rootProject.file(keystoreProps.getProperty("storeFile", "")).exists()

android {
    namespace = "com.yortch.confirmationsaints"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.yortch.confirmationsaints"
        minSdk = 26
        targetSdk = 35
        versionCode = 4
        versionName = "1.0.3"

        vectorDrawables { useSupportLibrary = true }

        testInstrumentationRunner = "com.yortch.confirmationsaints.HiltTestRunner"
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = rootProject.file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        debug {
            isMinifyEnabled = false
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
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
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Splash screen (Android 12+ compatible)
    implementation(libs.androidx.core.splashscreen)

    // Persistence
    implementation(libs.androidx.datastore.preferences)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // Image loading (Coil 3 — resolves file:///android_asset/ natively)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // Dependency injection
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    ksp(libs.hilt.compiler)

    // Unit testing (JUnit4 for broad tool compatibility with Robolectric)
    testImplementation(libs.junit4)
    testImplementation(libs.turbine)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    // Instrumented / Compose UI testing
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
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
    from(sharedContentDir.dir("content")) {
        include("confirmation-info-en.json", "confirmation-info-es.json")
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
