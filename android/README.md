# Confirmation Saints — Android

> Android port of [Confirmation Saints](../README.md). The iOS app is live on the [App Store](https://apps.apple.com/app/confirmation-saints/id6762463641); this port brings the same experience to Android.

## Status

🚧 **Scaffolding complete.** Gradle project, app module, theme, and the
`SharedContent → assets` sync task are in place. Data classes, JSON loading,
ViewModels, screens, navigation, and localization will follow the architecture
plan in [`../docs/android-architecture.md`](../docs/android-architecture.md).

## Stack

- **Kotlin** 2.0 + **Jetpack Compose** (Compose BOM 2024.09) + **Material 3**
- **Min SDK 26** (Android 8.0) / target + compile SDK 34
- **AGP 8.6**, Gradle 8.9 wrapper
- **Navigation Compose**, **Lifecycle / ViewModel Compose**
- **DataStore Preferences** (for `appLanguage`, `hasSeenWelcome`)
- **kotlinx.serialization** for JSON
- **Coil** for image loading
- **JUnit 5** + **Turbine** for tests
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
        │   ├── data/         # TODO: repositories, DTOs
        │   ├── localization/ # TODO: language service
        │   ├── ui/theme/     # Theme.kt, Color.kt, Type.kt
        │   ├── ui/screens/   # TODO
        │   ├── ui/components/# TODO
        │   └── viewmodel/    # TODO
        └── res/              # strings, themes, launcher icon
```

## What's deliberately NOT here yet

Per the split of concerns with the lead architect, the following are **deferred**
to the architecture plan in [`../docs/android-architecture.md`](../docs/android-architecture.md):

- Kotlin data classes for `Saint` / `Category`
- JSON loading / repository layer
- `SaintListViewModel` (matching / filtering / search — mirrors iOS)
- Localization service and language-switch UX
- Navigation graph, screens, onboarding
- DataStore wiring for `appLanguage` / `hasSeenWelcome`

## Big picture

See the **🤖 Android Port Guide** in the [root README](../README.md#-android-port-guide)
for the canonical schema, what is reusable as-is from iOS, what needs native
reimplementation, and the cross-platform decisions this port must respect.
