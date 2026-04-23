# Confirmation Saints — Android

> Android port of [Confirmation Saints](../README.md). The iOS app is live on the [App Store](https://apps.apple.com/app/confirmation-saints/id6762463641); this port brings the same experience to Android.

## Status

🚀 **Phases 2–7 implemented** (per [`../docs/android-architecture.md`](../docs/android-architecture.md)).
Data layer, localization + DataStore, all core screens (list / detail / explore /
category-saints / about), onboarding pager, and settings are in place, plus
navigation graph, splash screen, and launcher icon generation. **Not yet
compile-verified** — no JDK available to Aragorn in the authoring environment;
Jorge must run `./gradlew :app:assembleDebug` to validate.

## Stack

- **Kotlin** 2.0 + **Jetpack Compose** (Compose BOM 2024.09) + **Material 3**
  (incl. `material-icons-extended`)
- **Min SDK 26** (Android 8.0) / target + compile SDK 34
- **AGP 8.6**, Gradle 8.9 wrapper, **KSP** 2.0.20-1.0.25
- **Hilt** 2.52 (DI) + **Navigation Compose** (typed `@Serializable` routes) +
  **Lifecycle / ViewModel Compose**
- **DataStore Preferences** (`appLanguage`, `hasSeenWelcome`)
- **kotlinx.serialization** 1.7.3 for JSON
- **Coil 3** (`io.coil-kt.coil3`) — resolves `file:///android_asset/` natively
- **AndroidX SplashScreen** 1.0.1 (API 23+ compatible)
- **JUnit 4** + **Turbine** + **Robolectric** for tests
- Application id: `com.yortch.confirmationsaints`

## Prerequisites

- **JDK 17** (toolchain enforces this)
- **Android SDK** with platform 34 installed (Android Studio Iguana/Jellyfish or
  `sdkmanager "platforms;android-34" "build-tools;34.0.0"`)
- A `local.properties` file pointing at your SDK — or the `ANDROID_HOME` /
  `ANDROID_SDK_ROOT` env var set:

  ```properties
  # android/local.properties
  sdk.dir=/Users/you/Library/Android/sdk
  ```

## Build

```bash
cd android
./gradlew :app:assembleDebug
```

To install on a connected device / emulator:

```bash
cd android
./gradlew :app:installDebug
```

## SharedContent → assets bridge

The canonical data source is [`../SharedContent/`](../SharedContent/). It is
**never forked**; instead, the `syncSharedContent` Gradle task (declared in
`app/build.gradle.kts` and wired as a `preBuild` dependency) copies:

| From                                          | To                                      |
| --------------------------------------------- | --------------------------------------- |
| `SharedContent/saints/saints-{en,es}.json`    | `app/src/main/assets/saints-*.json`     |
| `SharedContent/categories/categories-{en,es}.json` | `app/src/main/assets/categories-*.json` |
| `SharedContent/content/confirmation-info-{en,es}.json` | `app/src/main/assets/confirmation-info-*.json` |
| `SharedContent/images/*.jpg`                  | `app/src/main/assets/images/`           |

These generated files are gitignored; only `assets/README.md` is committed.
Every build picks up the latest content automatically — no manual step.

## Project layout

```
android/
├── build.gradle.kts          # top-level plugins
├── settings.gradle.kts       # includes :app
├── gradle.properties
├── gradle/libs.versions.toml # version catalog (single source for versions)
├── gradlew, gradlew.bat, gradle/wrapper/   # Gradle 8.9 wrapper
└── app/
    ├── build.gradle.kts      # :app module (incl. syncSharedContent task)
    ├── proguard-rules.pro
    └── src/main/
        ├── AndroidManifest.xml
        ├── assets/           # build-generated from SharedContent/
        ├── java/com/yortch/confirmationsaints/
        │   ├── MainActivity.kt
        │   ├── data/         # SaintRepository, CategoryRepository, model classes, JSON serialization
        │   ├── localization/ # LocalizationService, AppLanguage, AppStrings
        │   ├── ui/theme/     # Theme.kt, Color.kt, Type.kt
        │   ├── ui/screens/   # saints, categories, about, settings, onboarding screens
        │   ├── ui/components/# SaintRow, SaintImage, AppFilterChip
        │   └── viewmodel/    # RootViewModel, SaintListViewModel, SettingsViewModel
        └── res/              # strings, themes, launcher icon
```

## What's deliberately NOT here yet

- **Release signing config.** `buildTypes.release` has `TODO: add signingConfig`.
- **Adaptive-icon polish.** `_generate_android_icon.py` scales the iOS 1024×1024
  icon into all five density buckets. Swap in a purpose-built Chi-Rho
  foreground once ready.

## Big picture

See the **🤖 Android Port Guide** in the [root README](../README.md#-android-port-guide)
for the canonical schema, what is reusable as-is from iOS, what needs native
reimplementation, and the cross-platform decisions this port must respect.
