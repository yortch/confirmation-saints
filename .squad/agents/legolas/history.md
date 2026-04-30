# Legolas — History

## Core Context

### Early Project Setup (2026-04-12)
- **Foundation:** Gandalf established Swift 6 concurrency (Sendable models, @MainActor services); MVVM with Observable macro
- **Architecture:** XcodeGen generates .xcodeproj from project.yml (never edit .pbxproj); testable Saint/Category/LocalizedText structs
- **Welcome Screen:** Frodo created 4-page TabView onboarding (WelcomeView.swift) with first-launch gating via @AppStorage
- **App Icon:** Samwise generated 1024×1024 PNG (Chi-Rho design, purple gradient, gold accents, dove silhouette); Xcode auto-generates sizes
- **Test Strategy Established:** Diacritic-insensitive search (String+Diacritics.swift), cross-platform parity checks, Android test scaffolding patterns

### Android Test Architecture & QA (2026-04-12 through 2026-04-25)
- **JUnit conventions:** JUnit 5 for `src/test/` (@Disabled), JUnit 4 for `src/androidTest/` (@Ignore); Hilt pattern uses createEmptyComposeRule() + manual ActivityScenario.launch()
- **Compose UI testing:** createComposeRule() for self-contained composables; drive pagers via UI taps; waitUntil with 5s timeout for async DataStore
- **Guardrails:** Cross-platform parity check (Python script in `tests/shared-content-parity.py`) validates id parity, sourceURLs set, canonical field parity, image existence, category group parity
- **Scaffolding:** Unit test stubs (SaintRepositoryTest, CategoryMatchingTest, LocalizationServiceTest, etc.), instrumentation stubs (WelcomeScreenNavigationTest, SaintListDisplayTest, LanguageSwitchTest), CI hook with `if: false`
- **Major work:** HiltTestRunner landed (unlocked 12 tests); Robolectric SDK 35 support resolved; test count bumped to 103 saints; sources ↔ sourceURLs integrity test added
- **Icon work:** Splash icon refactored, launcher icon recut to 43% scale (diagonal-trap fix); adaptive icon densities updated
- **Data validation:** 19 saint backlog candidates approved, 103-saint expansion verified, Wikipedia biographies (22) reviewed with Spanish grammar corrections
- **Patterns documented:** `.squad/skills/android-compose-instrumentation/SKILL.md`, `.squad/skills/android-adaptive-icons/SKILL.md`

## Learnings

### Test Foundation & Core Architecture (2026-04-12)
- **Swift 6 concurrency:** All models Sendable, services @MainActor
- **MVVM:** Observable macro pattern, clean separation of data and logic
- **XcodeGen:** Never edit .pbxproj directly; regenerate from project.yml
- **Test strategy:** Diacritic-insensitive search, cross-platform parity checks, shared Python guardrails

---

## Recent Work (2026-04-25 onwards)

### Localization DataStore Test Determinism Review (2026-04-25)
- Approved the localization persistence test fix: choose the opposite of the current/default locale, drive DataStore with the test dispatcher-owned scope, and assert `StateFlow.value` after `advanceUntilIdle()` instead of waiting for a non-guaranteed duplicate emission.
- Validation passed: focused `LocalizationServiceTest` under default and Spanish JVM locales, plus full Android `:app:testDebugUnitTest` suite. Avoid running the same Gradle test task concurrently because test-result outputs can collide.

### Tappable Saint Detail Images QA (2026-04-29)
- Approved iOS + Android saint-detail image enlargement: both platforms keep the existing circular portrait and open the same bundled local image in a larger view (`SaintImageView.loadImage` on iOS; `file:///android_asset/images/$filename` on Android).
- App-size guardrail: no binary/image diffs, no tracked image-like asset additions, 103 SharedContent image files remain 8.5M, and EN/ES saint JSON has 103 unique image references with 0 missing files.
- Localization/a11y guardrail: verify visible affordance, semantic click label/role, close/done action, and EN→ES translations together; iOS string catalog/AppStrings and Android `AppStrings.kt` both covered the new image affordance strings.
- Focused gates passed: iOS simulator build (`xcodebuild ... generic/platform=iOS Simulator`), Android `:app:testDebugUnitTest`, `:app:compileDebugAndroidTestKotlin`, and `:app:compileDebugKotlin`.

### Expanded Wikipedia Biography QA (2026-04-29)
- Reviewed Samwise's expansion of the 22 Wikipedia-sourced saint biographies. EN/ES record counts, ids, schemas, canonical matching fields, images, and source entries remained unchanged; only `lastUpdated` and biography text changed.
- Biography depth improved from roughly 250–500 characters to roughly 750–900 characters per changed entry, closer to the existing roster median (~1.1–1.2k) while keeping a teen-friendly/reverent tone.
- QA rejected the batch for two Spanish copy issues in changed biographies: Jacinta Marto's "una de los tres pastorcitos" phrasing and Agnes's "valen la pena defenderse" construction.
- Validation passed: shared-content parity, schema/id/canonical/source invariant check, and focused Android data-loading tests (`SaintRepositoryTest`, `SourcesIntegrityTest`, `SaintParsingTest`).

### Expanded Wikipedia Biography Re-review (2026-04-29)
- Approved Frodo's Spanish copy fixes for Jacinta Marto (`formó parte de los tres pastorcitos`) and Agnes (`vale la pena defender la dignidad, los límites y la fe`).
- Revalidation passed: JSON parse/count/unique IDs, changed-record schema/biography-only diff check, EN/ES canonical/source URL parity, `python3 tests/shared-content-parity.py`, and focused Android data-loading tests.

### SwiftUI Duplicate Chip IDs QA (2026-04-29)
- Approved Frodo's iOS `SaintDetailView` fix for duplicate SwiftUI chip IDs: patron/tag `ForEach` loops now key by local indices, so legitimate repeated strings such as `prayer` render as separate chips without mutating saint data.
- Focused validation passed: `git diff --check`, iOS simulator build and launch on iPhone 17, and verified `UIAccessibilityLoaderWebShared` warning remains ignorable.

### iOS 1.0.3 Release Prep QA (2026-04-29)
- Release-prep-only changes approvable when `ios/project.yml` reports `MARKETING_VERSION` 1.0.3 / build 3, Android stays at `versionName` 1.0.2 / `versionCode` 3 during closed testing, and store notes separate iOS App Store release copy from pending Android Google Play notes.
- Validation pattern: run XcodeGen, confirm `.pbxproj` diff unchanged, focused iOS simulator build on iPhone 17.

### App Store Screenshot Dimension QA (2026-04-29)
- iPhone App Store screenshots in `docs/appstore/` should target 1284×2778 px per `docs/appstore/submission-info.md`.
- For v1.0.3 saints-list/detail screenshot validation, `03-saints-list.png` and `05-saint-detail.png` are 1284×2778, non-interlaced RGBA PNGs, visually non-blank, and exact pixel matches to source screenshots.

### Spanish App Store Screenshot Review (2026-07-23)
- **Reviewed:** `docs/appstore/es/` (7 screenshots) + iOS code changes for Spanish feast day localization and image preview feature.
- **Screenshots:** All 7 present (01-welcome through 07-ipad-about). iPhone 01-06 are 1284×2778. iPad 07 is 2064×2752 (matches EN). All visibly show Spanish UI text.
- **Code changes:** `Saint.formattedFeastDay(language:)` added for ES date formatting; `SaintRowView` and `SaintDetailView` now pass language. Also includes image preview feature and ForEach ID fixes.
- **Test gap:** No iOS unit test target exists — `formattedFeastDay(language:)` has no automated coverage.
- **Verdict:** APPROVED with caveat — missing unit tests for the new localized date formatter.

### Marketing Page Android Tab Review (2026-07-24)
- **Reviewed:** `docs/index.html` — Frodo's update to add Android closed testing CTA and instructions.
- **Hero CTA:** App Store button unchanged (line 556). New "Download test version for Android" button added below (line 562) with green gradient styling, links to `#platform-android`.
- **Anchor/JS:** Hash-based navigation correctly activates Android tab via `platformFromHash()` → `switchPlatform('android')`. Handles `#platform-android` and `#android`. DOMContentLoaded and hashchange listeners both work.
- **Android tab copy:** Badge says "Closed Testing", heading says "Android Version Now in Google Play Closed Testing" — no longer "coming soon."
- **Tester instructions:** Ordered list: (1) join Google Group `testers-community@googlegroups.com`, (2) download from Play Store. Both links present and correct.
- **CSS/JS:** Minimal additions (`.android-test-btn`, `.testing-steps`, hash helpers). No breakage to existing iOS tab or responsive layout.
- **Verdict:** APPROVED — all 8 validation criteria pass.
