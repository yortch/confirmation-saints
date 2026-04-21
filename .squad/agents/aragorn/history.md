# Aragorn — Personal History

## Core Context

- **Project:** confirmation-saints
- **User:** Jorge Balderas
- **My role:** Android Dev
- **Joined:** 2026-04-21 (after iOS app shipped to App Store)

## Project Snapshot

- iOS app is LIVE on the App Store: https://apps.apple.com/app/confirmation-saints/id6762463641
- iOS is built with Swift 6 / SwiftUI / iOS 17+ / XcodeGen. Bundle id `com.yortch.CatholicSaints`.
- 70 saints with bilingual content (EN default, ES). `SharedContent/` is the canonical cross-platform data source.
- Key architectural decision: matching fields (patronOf, affinities, tags, region, lifeState, ageCategory, gender) are STORED IN ENGLISH IN BOTH language files because the ViewModel matches against English category IDs. Spanish gets `displayTags` / `displayAffinities` arrays for localized presentation.
- Saint images live in `SharedContent/images/{saint-id}.jpg`.
- Saint IDs / URLs match across `saints-en.json` and `saints-es.json` — name keys differ but URLs are the shared identifier.

## Android Stack (confirmed with Jorge 2026-04-21)

- Kotlin + Jetpack Compose (Material 3)
- Min SDK 26 (Android 8.0)
- Package: `com.yortch.confirmationsaints`
- Full iOS feature parity as first milestone

## Key Reads on Day 1

- `README.md` — canonical project spec (Gandalf rewrote it 2026-04-21 specifically for Android contributors)
- Android Port Guide section in README (or `docs/android-port.md` if it exists) — reusable vs. rewrite breakdown
- `.squad/decisions.md` — team decisions to respect
- `SharedContent/` — saints JSON, categories, images (canonical cross-platform source)
- `ios/CatholicSaints/Models/` — data model reference (translate to Kotlin data classes)
- `ios/CatholicSaints/ViewModels/SaintListViewModel.swift` — matching/filter logic reference
- `ios/CatholicSaints/Services/LocalizationService.swift` — localization pattern reference

## Learnings

(Append as I work)

### 2026-04-21 — Initial Android scaffold

**What I built (squad/android-port, commits b190097 → 87097b1):**
- Gradle 8.9 wrapper (gradlew/gradlew.bat/gradle-wrapper.jar pulled from `gradle/gradle` at tag v8.9.0)
- Top-level `android/build.gradle.kts`, `settings.gradle.kts` (`:app`), `gradle.properties` (AndroidX, non-transitive R)
- `gradle/libs.versions.toml` — AGP 8.6, Kotlin 2.0.20, Compose BOM 2024.09.02, Material 3, Nav Compose, Lifecycle 2.8.6, DataStore 1.1.1, kotlinx.serialization 1.7.3, Coil 2.7.0, JUnit 5, Turbine
- `:app` module — applicationId `com.yortch.confirmationsaints`, minSdk 26, compileSdk/targetSdk 34, versionCode 1 / versionName 1.0.0, JVM toolchain 17, Compose enabled via Kotlin Compose plugin, kotlinx.serialization plugin, R8 + shrinkResources on release (no signing config yet — TODO left in place)
- `AndroidManifest.xml`, minimal `strings.xml` (app_name only), Material base theme, adaptive launcher icon placeholder (liturgical purple bg + gold cross vector foreground)
- `MainActivity` → placeholder Compose screen in `ConfirmationSaintsTheme`
- `ui/theme/` — Material 3 dynamic color on API 31+, fallback to liturgical purple / sacred gold palette
- Empty marker packages (`data/`, `viewmodel/`, `ui/screens/`, `ui/components/`, `localization/`) each with `package-info.kt` pointing at `docs/android-architecture.md`

**The SharedContent bridge (key architectural choice):**
- Declared a Gradle `Sync` task `syncSharedContent` in `app/build.gradle.kts` wired as `preBuild.dependsOn(syncSharedContent)`
- Copies `SharedContent/saints/*.json`, `SharedContent/categories/*.json`, and `SharedContent/images/*.jpg` into `app/src/main/assets/` at build time
- `assets/` is gitignored except for `assets/README.md` — the generated content stays out of git, matching iOS's folder-reference pattern in spirit (iOS uses a folder-reference build phase; Android uses Gradle `Sync`)
- Task `preserve { include("README.md") }` keeps the explainer from being nuked by the Sync
- Logs source + destination paths at the start of each run

**Build verification — could NOT run locally:**
- No JDK installed on this Mac (`/usr/bin/java` stub reports "Unable to locate a Java Runtime")
- `gradle` CLI not on PATH
- Therefore `./gradlew :app:assembleDebug` was not executed
- Local Android SDK is at `~/Library/Android/sdk` with platform `android-36.1` + build-tools `37.0.0` — NOT the `android-34` that `app/build.gradle.kts` targets. Either (a) bump compileSdk/targetSdk to 36 and bump AGP to a version supporting 36, or (b) install `platforms;android-34` via sdkmanager. Leaving the decision to whoever builds first; noted in `android/README.md`.
- The Gradle wrapper jar is real (43 KB, valid zip pulled from the upstream repo tag) and the wrapper scripts were fetched from the same tag — the scaffold is buildable on a machine with JDK 17 + SDK 34.

**Gotchas / things I deliberately did NOT do:**
- Did NOT write `Saint`/`Category` data classes, JSON loading, LocalizationService, ViewModels, navigation, or screens. Gandalf's plan at `docs/android-architecture.md` will dictate serialization pattern and data flow. Tempting to front-run this; stayed in my lane.
- Did NOT scatter UI strings into `strings.xml` — only `app_name` lives there. The in-app localization strategy (mirror iOS's canonical-English-id + `display*` arrays pattern) will be decided by Gandalf.
- Did NOT adapt `_generate_icon.py` for Android adaptive icons — used a simple gold-cross vector placeholder. A follow-up should port the Chi-Rho design to a two-layer adaptive icon (foreground PNG or vector at 108dp with 72dp safe zone + background color / drawable).

**Key file paths (for future me):**
- Gradle sync task: `android/app/build.gradle.kts` (`val syncSharedContent by tasks.registering(Sync::class)`)
- Theme: `android/app/src/main/java/com/yortch/confirmationsaints/ui/theme/Theme.kt`
- Assets destination: `android/app/src/main/assets/` (build-generated; see `assets/README.md`)
- Launcher icon: `android/app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` (adaptive)
- Version catalog: `android/gradle/libs.versions.toml` — single source of truth for all dependency versions

**Next round (after reading `docs/android-architecture.md`):**
- Data classes with `@Serializable`
- JSON loader reading from `assets/` via `AssetManager`
- `SaintListViewModel` mirroring iOS filtering/search (diacritic-insensitive via `java.text.Normalizer`)
- Nav graph + screens
- DataStore for `appLanguage` / `hasSeenWelcome`
- Localization service

