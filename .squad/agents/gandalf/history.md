# Gandalf — History

## Project Context
- **Project:** confirmation-saints — Catholic Saints iOS App
- **User:** Jorge Balderas
- **Stack:** Swift / SwiftUI, iOS (iPhone + iPad)
- **Description:** App helping Catholic confirmation candidates (primarily teens, also adults) find and choose a patron saint. Features saint search by name, patron day, affinity, country, age, married status. Multilingual (EN/ES). Content sourced from Loyola Press, Focus, Lifeteen, Ascension Press, Hallow with attribution.
- **Key constraints:** Self-contained, easy content updates, cross-platform ready (Android later), include saint images with attribution.

## Learnings

### Architecture Decisions (2026-04-12)
- **MVVM + SwiftUI** with Observable macro (iOS 17+)
- **XcodeGen** (`project.yml`) generates `CatholicSaints.xcodeproj` — never edit .pbxproj manually
- **Dual localization strategy**: `.xcstrings` String Catalog for UI strings, JSON `LocalizedText` objects for saint content (cross-platform reusable)
- **SharedContent/Data/** holds platform-agnostic JSON (saints.json, categories.json) — designed for Android reuse
- **Swift 6 concurrency**: All models are `Sendable`, services are `@MainActor`
- **iOS 17 deployment target**: Uses NavigationStack, searchable, Observable — but avoids iOS 18+ Tab API

### Key File Paths
- `project.yml` — XcodeGen spec (run `xcodegen generate` to rebuild .xcodeproj)
- `CatholicSaints/App/CatholicSaintsApp.swift` — App entry point
- `CatholicSaints/Models/Saint.swift` — Core data model with `LocalizedText`
- `CatholicSaints/Services/SaintDataService.swift` — JSON data loader
- `CatholicSaints/Resources/Localizable.xcstrings` — UI string translations (EN/ES)
- `SharedContent/Data/saints.json` — All saint data (bilingual)
- `SharedContent/Data/categories.json` — Category definitions

### User Preferences
- Jorge prefers clean architecture with clear separation
- Content must attribute sources (Loyola Press, CNA, Franciscan Media, etc.)
- Teen-friendly affinities: sports, music, art, science, etc.

### App Rename & Welcome Screen (2026-04-12)
- **Frodo** renamed app: "Catholic Saints" → "Confirmation Saints" (display name only; internal CatholicSaints/ folder unchanged)
- Updated `ios/project.yml` (INFOPLIST_KEY_CFBundleDisplayName, PRODUCT_NAME) and README.md
- Created `Views/Onboarding/WelcomeView.swift` — 4-page TabView with first-launch gating (`@AppStorage("hasSeenWelcome")`)
- New pattern: OnboardingPageView reusable component for consistent page layout
- All strings added to Localizable.xcstrings with EN/ES translations
- Settings gets "Show Welcome Screen" button for replay
- **Samwise** generated app icon: Chi-Rho design with purple-to-indigo gradient, gold halo, dove silhouette, gold accents
- Icon programmatically generated via `_generate_icon.py` (Pillow), 1024×1024 PNG
- Asset output: `ios/CatholicSaints/Resources/Assets.xcassets/AppIcon.appiconset/app-icon-1024.png`
- Xcode auto-generates all required icon sizes from 1024×1024 source
- No architecture changes — follows existing MVVM + @AppStorage pattern


### Documentation Audit Pre-Android (2026-04-21)
- iOS app shipped v1.0.0 to App Store (id6762463641). Docs refreshed ahead of Android port.
- **Most surprising staleness in README:** the documented data model was obsolete — described a `LocalizedText {en,es}` per-field model with fields like `birthYear`, `countryOfOrigin`, `isYoungSaint`, `imageName`/`imageAttribution`, and a nonexistent `categories` field on saints. The actual implementation migrated (some time before the audit) to **per-language JSON files** (`saints-en.json`, `saints-es.json`) with plain-string fields plus parallel `displayPatronOf` / `displayTags` / `displayAffinities` arrays. Matching is done against **canonical English ids** in `patronOf` / `tags` / `affinities` / `region` / `lifeState` / `ageCategory` / `gender`, which are identical across both language files. Anyone relying on the old README to build Android would have produced a broken data layer.
- Saint count in docs drifted too: history.md referenced 50 / 54; actual is **70**.
- README "How to Add a New Language" was also stale (told contributors to add a field to the `LocalizedText` Swift struct, which no longer exists). Replaced with per-file instructions.
- `android/README.md` still carried the old "Catholic Saints — Android" name. Updated to current "Confirmation Saints" branding and pointed to the new Android Port Guide section in the root README.
- Marketing site (`docs/index.html`) and `docs/appstore/` were already current (recent commits updated App Store link and submission assets) — left untouched.
- `.github/copilot-instructions.md` does not exist; no dev-facing docs found beyond README.
- **Minor code-level staleness spotted (out of scope for doc task, flagging for Frodo):** `SettingsView.swift` hardcodes `Text("0.1.0")` for version while `project.yml` has `MARKETING_VERSION: 1.0.0`. Should read from `Bundle.main.infoDictionary`.

### Key Android-Port Considerations (captured in README + android/README)
- **SharedContent/ is the canonical single source of truth.** Android wires it via a Gradle asset source-set include, does not fork.
- Filter on canonical English ids; render from `display*` arrays. Do not filter against Spanish labels.
- Diacritic-insensitive search is non-negotiable (iOS uses `String+Diacritics`; Android = `Normalizer.NFD` + strip combining marks, or `Collator` SECONDARY).
- Persisted keys to preserve cross-platform: `appLanguage`, `hasSeenWelcome`.
- Language auto-detect from system locale on first launch (ES → es, else en), overridable in Settings.
- Feast day format is `MM-DD` (no year).
- Display name "Confirmation Saints" — internal module name is free to follow Android conventions.
- Suggested bundle id: `com.jorgebalderas.confirmationsaints` (iOS uses `com.jorgebalderas.ConfirmationSaints`).

### Android Architecture Plan (2026-07-22)
- Produced `docs/android-architecture.md` — prescriptive, 20-section plan for full Android port.
- **Package:** `com.yortch.confirmationsaints` (user confirmed; differs from iOS `com.jorgebalderas.*`).
- **Key iOS → Android mappings:**
  - `@Observable` ViewModel → `ViewModel` + `StateFlow` + `collectAsStateWithLifecycle()`
  - `@AppStorage` → `DataStore<Preferences>`
  - SwiftUI `EnvironmentKey` for language → Compose `CompositionLocal`
  - `AppStrings.localized()` — ported as-is (in-memory Kotlin map, NOT `strings.xml`)
  - `String+Diacritics.swift` → `Normalizer.NFD` + regex strip
  - `NavigationStack` per tab → `NavHost` with nested graphs per tab
  - `TabView` → Material 3 `NavigationBar`
  - `FlowLayout` → Compose `FlowRow`
  - `FilterChip` → Material 3 `FilterChip`
  - Bundle JSON loading → `AssetManager.open()` + `kotlinx.serialization`
  - `SplashView` (custom) → Android `SplashScreen` compat API
- **Critical localization decision:** In-app language switch uses `StateFlow<AppLanguage>` + `CompositionLocal`, NOT `strings.xml` system locale. This mirrors iOS's instant-switch behavior.
- **SharedContent wiring:** Gradle `Sync` task copies `SharedContent/` into build intermediates as an asset source set. Never committed to `android/`. Single source of truth preserved.
- **Image loading:** Coil 3 with `file:///android_asset/` URI scheme — no custom fetcher needed.
- **Serialization:** `kotlinx.serialization` chosen over Moshi (compile-time, multiplatform-ready).
- **DI:** Hilt for v1 (standard Android choice, pairs with Navigation Compose).
- **8-phase work decomposition** defined with explicit dependency ordering.

### Documentation & Marketing Copy — Saint Count References (2026-04-22)
- Updated all documentation and marketing-facing copy to reflect new saint count: **80+** (previously 70).
- **Key files updated:**
  - `README.md`: Feature list + Future Plans section + new "What's New" note
  - `docs/index.html`: Meta description, hero badge, gallery subtitle, stats section (5 instances)
  - `docs/appstore/submission-info.md`: Promotional text + App description
  - `docs/appstore/screen-recording-script.md`: Video caption
  - `docs/appstore/review-response.md`: App value proposition + suggested user flow
- **What's New entry:** Added note about 10 new saints (Teresa of Ávila, Ignatius of Loyola, Anthony of Padua, Luke the Evangelist, + 6 others).
- **Deliberately NOT updated:** Historical test confirmations in `.squad/decisions.md` (Android JVM test section notes 70 saints at time of implementation — that's a historical record, not a current fact). Agent history.md files and session logs also untouched per governance.
- **Learning:** Marketing copy is distributed across README, docs/index.html, and docs/appstore/ metadata. Always check all three when saint count changes again.

### Roster Now 81 — "80+ Copy Truthful" (2026-04-23)
- **Cross-Agent Sync (Scribe):** Samwise added St. George + St. Mariana de Jesús de Paredes on `squad/add-saints-80-plus`
- **Status:** Roster expanded 79→81 saints
- **Resolution:** Marketing docs now match actual count. "80+ saints" copy is truthful at 81 saints.
- **Decision:** Merging this branch resolves prior discrepancy (79 saints but "80+" in marketing) — no further copy updates needed post-merge.

