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

## Phases 1–7 Complete (2026-04-21 to 2026-04-22)

- **Scaffold** (Phase 1): Gradle 8.9, Kotlin 2.0.20, Compose Material 3, Hilt, Coil 3, DataStore, kotlinx.serialization. Package `com.yortch.confirmationsaints`, minSdk 26, compileSdk/targetSdk 34. `syncSharedContent` Gradle task bridges to `SharedContent/` at build time.
- **Data + ViewModel + Localization** (Phases 2–7): Hilt DI, `SaintRepository`/`CategoryRepository` load JSON from assets, `LocalizationService` manages EN/ES via `StateFlow` (live language switch without Activity restart), `DataStore` for app state, typed nav with `@Serializable` routes, Compose Material 3 theme with dynamic color on API 31+.
- **Key decisions locked**: Flat `assets/*.json` layout, CompositionLocal for language propagation, icon-extended dependency added (Material default set too stripped for our needs).
- **Untested caveat**: No JDK on this dev machine — all Kotlin files verified only via compile/lint, not runtime until CI.
- **Adaptive launcher icons**: 60% scale in 108dp canvas = 21-22px margins at mdpi density, survives circle/squircle/teardrop masks.

---

## Archived Learnings (2026-04-21 to 2026-04-22)

Early phase learnings consolidated to reduce history size. Key takeaways:

- **Nested nav graphs + bottom tab state:** Fixed tab-switching bug by using per-tab navigation graphs instead of flat NavHost. Detail routes now live inside tab graphs; `backStackEntry.destination.hierarchy` correctly detects selected tab.
- **TopAppBar back button:** Gated by `navController.previousBackStackEntry != null` AND `currentRoute` matches detail (SaintDetail/CategorySaints). Use `Icons.AutoMirrored.Filled.ArrowBack` for RTL support.
- **Splash color:** `themes.xml` (Android 12+ system splash API) + `colors.xml` (#E53935 red, matches iOS). No dark variant — single declaration serves both themes.
- **HiltTestRunner wiring:** Pattern B; `HiltTestRunner` extends `AndroidJUnitRunner`, overrides `newApplication`. Wire `testInstrumentationRunner` in `app/build.gradle.kts` + add `hilt-android-testing` via `kspAndroidTest(...)` (not `androidTestImplementation`). Use shared version ref (2.52).
- **Unit tests implemented:** 32 tests green (`./gradlew testDebugUnitTest`): BirthDateParsing (6), SaintRepository (5), LocalizationService (6), CategoryMatching (5), Robolectric configured with `isIncludeAndroidResources = true`.

---

## Recent Work (2026-04-23 to 2026-04-25)

## Cross-Agent Update: Roster Expanded to 79 Saints (2026-04-23)
**From:** Samwise (Data) + Gandalf (Docs) completion  
**Status:** ✅ Merged into decisions.md
- Roster expanded from 70 → 79 saints (9 new saints added on branch `squad/add-saints-80-plus`)
- Android test count updated 70 → 79
- Build verified ✅; all 32 unit tests + 12 instrumentation tests green
- Documentation updated to "80+ saints" across README, docs/index.html, and appstore copy
- **Open question:** Jorge must decide if 79 matches the "80+ saints" target or if one more saint should be added

## Unit Test Implementation Complete (2026-04-22)

**All 32 unit tests implemented and passing:**
- BirthDateParsing: 6 tests ✅ (zero-padded years, null handling, octal safety)
- SaintRepository: 5 tests ✅ (EN/ES both load 70 saints, identical ID sets, null canonizationDate, image filename contract)
- LocalizationService: 6 tests ✅ (device locale default, StateFlow update, DataStore persistence, EN/ES string lookup, missing-key fallback)
- CategoryMatching: 5 tests ✅ (region match, cross-language ID consistency, display* arrays NOT matched, unknown value returns empty, young saints include Catherine of Siena)

**Verification:** `./gradlew :app:testDebugUnitTest` → **BUILD SUCCESSFUL**, 32 tests completed, 0 failures ✅

## Stale TODO Cleanup + CI Enablement (2026-04-22)

**Changed:**
1. `android/README.md`: Replaced 5 "TODO" labels with accurate directory descriptions; removed stale bullets
2. `.github/workflows/android-ci.yml`: Removed `if: false` guards on both jobs; enabled CI on PRs to main

**Verification:**
- `shared-content-parity` job invokes `python3 tests/shared-content-parity.py` ✅
- `android-build-and-test` job: `./gradlew assembleDebug + testDebugUnitTest` ✅
- JDK 17 (temurin) + Python 3.12 ✅

## 2026-04-23 — Cabrini source links + Settings reorder (squad/add-saints-80-plus)

**FIX 1 — Cabrini (and 26 others) tappable sources.**
Root cause was data: 27 saints had `sources` arrays that didn't match `sourceURLs` keys. Frodo fixed canonical JSON; Android picks up fix for free via `syncSharedContent` at build time.

**FIX 2 — Settings section order.**
`SettingsScreen.kt`: Swapped trailing `item {}` blocks so `Support & Legal` appears before `Content Sources`.

**Key learning:** Tappable-source contract is **data-driven**, not UI-driven. `sources[i]` must appear as a key in `sourceURLs` (case-sensitive exact match).

### Android Sources Schema — `SourceEntry` data class (2026-04-23)
- Added `@Serializable data class SourceEntry(name, url)`; replaced `sources: List<String>` + `sourceURLs: Map<String,String>?` on `Saint` with `sources: List<SourceEntry>`.
- Simplified `SourcesSection` in `SaintDetailScreen`; updated `SaintParsingTest` fixture.
- `./gradlew testDebugUnitTest assembleDebug` → green.

### Robolectric SDK 35 Support — Version Upgrade Required (2026-04-25)

**Issue:** After Android SDK upgrade to compileSdk/targetSdk 35, CategoryMatchingTest and SaintRepositoryTest failed with `IllegalArgumentException` at `DefaultSdkPicker.java:119`.

**Root cause:** Robolectric 4.13 does not support Android SDK 35.

**Fix:** Upgraded Robolectric from 4.13 to 4.16.1 in `android/gradle/libs.versions.toml`. 

**Verification:** All 35 unit tests pass locally (`./gradlew testDebugUnitTest` → BUILD SUCCESSFUL).

**Key learning:** When upgrading Android compileSdk/targetSdk, check Robolectric release notes. Robolectric lags ~1-2 versions behind new Android SDK releases. Error signature: `DefaultSdkPicker` + `IllegalArgumentException` = unsupported SDK version.

**Commit:** `003aa49` on develop branch.

### Android Release Version Bump — 1.0.2 (2026-04-25)

- Confirmed Android release versioning convention: `versionCode` increments by 1 for each patch release (`1.0.1` used code 2; `1.0.2` uses code 3).
- Updated `android/app/build.gradle.kts` and the Android app metadata table in `docs/android-architecture.md`.

### v1.0.2 Release Orchestration Completed (2026-04-25)
- **Session:** v1.0.2 Over 100 Saints batch orchestration
- **Outcome:** Android 1.0.2 (versionCode 3) bumped and validated; 103-saint content parity confirmed
- **Cross-team:** Samwise (22-saint content) ✅, Frodo (iOS 1.0.2) ✅, Legolas (batch sign-off) ✅
- **Release status:** GO for Google Play submission

## Learnings

### 2026-04-25 — Android onboarding dark-mode readability + platform submission docs
- `WelcomeScreen.kt` uses `MaterialTheme.colorScheme.background` directly, so top-level onboarding text must set Material text colors (`onBackground` for primary/body text, `onSurfaceVariant` for secondary copy) instead of relying on the default ambient content color.
- Keep Android/Google Play release copy in `docs/android/submission-info.md` and iOS/App Store release copy in `docs/appstore/submission-info.md`; patch fixes can diverge by platform, such as Android-only dark-mode welcome-screen readability.

### 2026-04-25 — Modern Day Saints Android filter
- Implemented Android `modern-day` era support as a derived filter from `birthDate` year >= 1900; no SharedContent schema or JSON edits were needed in the Android lane.
- Quick filter chips can use `SaintFilters.selectedEra` plus `CategoryMatcher.matchesEra(...)` to keep list filtering aligned with category browse matching.
- Verification: `./gradlew :app:testDebugUnitTest :app:compileDebugKotlin` passed; SharedContent currently has 13 EN/ES saints born in or after 1900.

### 2026-04-25 — LocalizationService DataStore test determinism
- `LocalizationService.language` is a `StateFlow`, so tests must not wait for a second emission when the persisted value equals the system-locale fallback; `StateFlow` suppresses duplicate values.
- For language-switch and persistence tests, choose a target language opposite the current/system fallback so the assertion proves persisted override behavior on both EN and ES CI hosts.
- Use the test coroutine scope for `PreferenceDataStoreFactory.create(scope = testScope)` so DataStore reads/writes are driven by `StandardTestDispatcher` instead of racing the default IO scope.

### 2026-04-29 — Tappable saint detail images (Android)
- Confirmed Android bundles saint images from `SharedContent/images/*.jpg` via `syncSharedContent` into `assets/images/`; iOS project includes `../SharedContent` in Resources and `SaintImageView` loads `SharedContent/images` from the app bundle.
- SharedContent image parity check: EN and ES both reference 103/103 saint image filenames, with 0 missing files in `SharedContent/images`.
- Android detail image remains circular at 128dp; tapping it opens a larger dialog preview using the same `file:///android_asset/images/{filename}` asset, with no new raster assets, duplicate files, or remote downloads.
- Verification: `cd android && ./gradlew :app:compileDebugKotlin --quiet` passed.

### 2026-04-29 — Pending Android release notes during closed testing
- Updated `docs/android/submission-info.md` for future Google Play notes while Android remains in Google's required closed testing phase; notes are explicitly marked pending/not production.
- No Android `versionName`/`versionCode` bump for docs-only release-note prep; keep Android notes separate from iOS/App Store 1.0.3 copy.
