# Decisions — confirmation-saints

## Active Decisions

### Decision:### Decision: Project Architecture — Catholic Saints iOS App
**Author:** Gandalf (Lead) | **Date:** 2026-04-12 | **Status:** Active

#### Context
Greenfield iOS app for helping confirmation candidates choose a patron saint. Must support English/Spanish, be easy to update, and prepare for future Android version.

#### Decisions

**1. MVVM + SwiftUI with Observable macro**
- iOS 17+ deployment target
- Swift 6 strict concurrency (Sendable models, @MainActor services)
- NavigationStack (not deprecated NavigationView)

**2. XcodeGen for project management**
- `project.yml` at repo root defines the Xcode project
- Run `xcodegen generate` to rebuild `.xcodeproj`
- Never edit `.pbxproj` by hand

**3. Dual localization strategy**
- **UI strings** → `.xcstrings` String Catalog (Apple-native, Xcode-editable)
- **Saint content** → JSON with `LocalizedText` struct (`{"en": "...", "es": "..."}`)
- Rationale: JSON content is platform-agnostic and reusable for Android

**4. SharedContent directory for cross-platform data**
- `SharedContent/Data/saints.json` and `categories.json` live outside the iOS source
- Bundled as a folder reference in the iOS app
- Android app can consume the same files

**5. Source attribution required**
- Every saint entry includes a `sources` array with name + URL
- Every image includes `imageAttribution`
- Content sources: Loyola Press, CNA, Franciscan Media, Focus, Lifeteen, Ascension Press, Hallow

#### Impact
- Frodo (iOS) builds views and features within this architecture
- Samwise (Data) populates saints.json following the established schema
- Legolas (QA) writes tests against these models and services

---



---

### Decision:### Decision: UI Integration — Bilingual Data + Complete Views
**Author:** Frodo (iOS Dev) | **Date:** 2026-04-12 | **Status:** Implemented

#### What Changed
Integrated Samwise's bilingual saint data (25 saints EN+ES) into the app and built out a complete 5-tab UI.

#### Key Decisions
1. **Per-Language Loading (not LocalizedText):** Dropped the `LocalizedText` struct in favor of plain `String` model fields. Language switching reloads the entire dataset from the correct file.
2. **Shared ViewModel Pattern:** Single `SaintListViewModel` created in `ContentView` and passed to all tabs. Avoids duplicate data loading; keeps filtering state consistent.
3. **Environment-Based Language:** Language preference flows via `@AppStorage("appLanguage")` + custom `EnvironmentValues.appLanguage`. Language changes trigger full data reload.
4. **Category Browsing via Computed Matching:** Dynamic `SaintListViewModel.saints(forCategoryGroup:valueId:)` matches saints against category criteria. Keeps code simple and data authoritative.
5. **Purple Accent Theme:** Purple chosen for confirmation (liturgical color) and teen appeal. Gradients for avatars.

#### Files Changed
- Models: `Saint.swift` (rewritten), `Category.swift` (simplified)
- Service: `SaintDataService.swift` (bilingual loading)
- ViewModel: `SaintListViewModel.swift` (new filtering, 13 Swift files total)
- Views: Rewrote all views — `ContentView`, `SaintsView`, `CategoryBrowseView`, `SearchView`, `ConfirmationInfoView`, `SettingsView`, `SaintDetailView`, `SaintRowView`, `FilterChip`, `FlowLayout`, `Environment+appLanguage`, `LocalizedString`, `SaintAvatarView`
- Localization: `Localizable.xcstrings` (40+ strings EN/ES)

#### Build Status
- ✅ Compile clean: `xcrun swiftc -typecheck -swift-version 6` passes on all 13 files

#### Impact on Other Agents
- **Legolas:** Tests need updating — old `LocalizedText`, `Affinity` enum, `ContentSource` types are gone. New model uses plain strings.
- **Samwise:** Data contract is stable. Any new saints just need matching fields in both language files.
- **Gandalf:** Architecture preserved (MVVM). Added Environment key for language.

---



---

### Decision:### Decision: Saint Data Schema Design
**Author:** Samwise (Data/Backend) | **Date:** 2025-07-15 | **Status:** Implemented

#### Context
The app needs a platform-agnostic, bilingual data format for saint information that supports filtering, localization, and future Android reuse.

#### Decision
- Separate JSON files per language (`saints-en.json`, `saints-es.json`) rather than a single multilingual file
- Each saint has a stable `id` (kebab-case) shared across languages for cross-referencing
- Categories use shared `id` values; only display labels are localized
- `feastDay` stored as `MM-DD` (no year) for calendar-based lookups
- `canonizationDate` is nullable (for beatified saints and pre-Congregation era saints)
- `affinities[]` field enables interest-based matching (the app's key feature)
- Images referenced by filename in a shared `images/` directory with per-saint attribution

#### Rationale
- Per-language files keep each file a reasonable size and simplify loading for a single locale
- Shared IDs enable the app to switch languages without losing context
- Nullable canonization dates accommodate the range of sainthood statuses (Blessed vs. Saint vs. ancient)
- Affinities are separate from patronage to enable more personal, interest-based saint matching

#### Impact
- iOS app loads only the needed language file at runtime
- Future Android app uses the same JSON files directly
- Adding a new saint requires adding to both EN and ES files with matching IDs

---



---

### Decision:### Decision: Matching Fields Must Stay English in All Language Files
**Author:** Samwise (Data/Backend) | **Date:** 2025-07-16 | **Status:** Implemented

#### Context
Category browsing was broken — almost every category showed "0 saints" because the ViewModel matches category value IDs (English) against saint data fields. The Spanish saint file had translated matching fields (e.g., `patronOf: ["soldados"]` instead of `["soldiers"]`), so no matches were found.

#### Decision
All **matching fields** — `patronOf`, `affinities`, `tags`, `region`, `lifeState`, `ageCategory`, `gender` — must use **English values** in both `saints-en.json` and `saints-es.json`. Only **display fields** (`name`, `biography`, `whyConfirmationSaint`, `quote`, `country`) should be translated.

This is because the ViewModel uses English category IDs for matching, and we agreed not to change the ViewModel or category IDs.

#### Impact
- **Samwise (Data):** When adding new saints, always use English for matching fields in both language files.
- **Frodo (iOS):** No code changes needed — matching logic works as designed.
- **All:** Birth dates must use 4-digit year format (e.g., `"0256-01-01"` not `"256"`) for `Int(birthDate.prefix(4))` to parse correctly.

---



---

### Decision:### Decision: User Directives — System Locale, Clickable Sources, Saint Images, Icon Design
**Author:** Jorge Balderas (via Copilot) | **Date:** 2026-04-12 | **Status:** Implemented

#### Directives
1. App language should default to iOS system language (not hardcoded "en"), with manual override in Settings preserved
2. App icon changed to: white dove on red background (Pentecost theme)
3. Sources in saint detail view should be clickable links
4. Include saint pictures where possible and available

#### Implementation
- **System Locale Default:** `systemDefaultLanguage` global constant checks `Locale.current.language.languageCode` ("es" → Spanish, else English). Used in both app init and settings; `@AppStorage` preserves manual overrides.
- **Clickable Sources:** Added `sourceURLs: [String: String]?` to `Saint` model. `SaintDetailView.sourcesSection` renders `Link` for sources with URLs, plain text otherwise.
- **Saint Images:** Created `SaintImageView` reusable component. Fallback chain: asset catalog → `SharedContent/images/` bundle path → colored initial circle.
- **Icon Design:** White dove on red background with Pentecost flame accents, procedurally generated via `_generate_icon.py`

#### Status
- ✅ Frodo: iOS implementation complete, builds clean
- ✅ Samwise: All 27 saints × 2 languages populated with `sourceURLs`, source names standardized to English in ES file
- ✅ Icon: Generated and integrated into Xcode project

---



---

### Decision:### Decision: Source URLs and Standardized Source Names
**Author:** Samwise (Data/Backend) | **Date:** 2026-04-12 | **Status:** Implemented

#### Context
Sources in saint detail view should be clickable links (per user directive). Requires URLs and consistent naming across language files.

#### Decision
- Added `sourceURLs` dictionary to all 27 saints in both `saints-en.json` and `saints-es.json`
- Each source in the `sources` array has a corresponding URL in `sourceURLs` (keyed by source name)
- Spanish file source names standardized to English (consistent with matching-fields convention)
- URLs point to saint-specific pages on Loyola Press, Catholic Encyclopedia (newadvent.org), Focus, Lifeteen, Ascension Press, Hallow
- `sourceURLs` identical across both language files (URLs are language-independent)

#### Impact
- iOS UI can render sources as interactive `Link` views
- No per-language URL mapping needed
- Standardization ensures data consistency and simplifies cross-platform reuse

---



---

### Decision:### Decision: Dictionary-Based In-App Localization
**Author:** Frodo (iOS Dev) | **Date:** 2026-04-12 | **Status:** Implemented

#### Context
`String(localized:)` uses the iOS system locale, not the in-app `appLanguage` setting. When a user switches language in Settings, JSON content updates but all UI strings (tab labels, section headers, button text) stayed in the system language.

#### Decision
Created `AppStrings.localized(_:language:)` in `LocalizationService.swift` — a dictionary-based approach that maps EN keys to ES translations. All views now use this instead of `String(localized:)`.

**Why dictionary over .lproj bundles:** The .lproj approach requires proper Xcode project localization setup (known localizations in build settings). The dictionary approach is self-contained, guaranteed to work, and easy to maintain alongside the existing `.xcstrings` file.

#### Impact
- **All agents:** When adding new UI strings, add them to BOTH `Localizable.xcstrings` (for reference/tooling) AND `LocalizationService.swift` translations dictionary.
- **Samwise:** No data layer impact — saint content was already language-switched via JSON files.
- **Gandalf:** Pattern is `AppStrings.localized("Key", language: language)` with `@Environment(\.appLanguage) private var language` in each view.
- **Frodo:** 62 UI string calls migrated in this session.

---



---

### Decision:### Decision: Reactive Language Switching Pattern
**Author:** Frodo (iOS Dev) | **Date:** 2025-07-18 | **Status:** Implemented

#### Decision

All views displaying localized *content data* (saints, categories, confirmation info) must reactively observe the viewModel — never hold captured value-type snapshots. Detail views receive an ID + viewModel reference, not a pre-resolved model object.

#### Context

Language switching via `@AppStorage("appLanguage")` triggers `SaintListViewModel.loadData(language:)` which replaces the saints/categories arrays. Views that held `let saint: Saint` (a value-type copy from the old array) didn't update. This made it appear that language switching was broken on already-open screens.

#### Pattern

- **Do:** `SaintDetailView(saintId: saint.id, viewModel: viewModel)` — reactive lookup
- **Don't:** `SaintDetailView(saint: saint)` — stale capture

#### Impact

- **Legolas (QA):** Navigation tests should verify saint detail content updates when language changes without re-navigation
- **Samwise (Data):** Saint `id` field must remain stable across language files (already the case)
- **Gandalf (Arch):** Same pattern should apply to any future detail views for content that varies by language

---



---

### Decision:### Decision: UI Polish — Splash Screen, Date Formatting, Tab Reorder
**Author:** Frodo (iOS Dev) | **Date:** 2026-04-13 | **Status:** Implemented

#### Changes

1. **Splash Screen:** New `SplashView.swift` displays branded red overlay with app icon + "Confirmation Saints" text for 1.5s on launch, then fades out smoothly. Integrated into `CatholicSaintsApp.swift` as an overlay. Created `SplashLogo.imageset` since `Image("AppIcon")` cannot reference the AppIcon asset set directly.

2. **Date Formatting:** New `SaintDateFormatter` utility in `Extensions/DateFormatting.swift` converts ISO dates to human-readable `dd-Mon-yyyy` format (e.g., "02-Jan-1873"). Detects ancient approximate dates (month=01, day=01, year<800) and shows year only. Applied to all date displays in `SaintDetailView` (birthDate, deathDate, canonizationDate).

3. **Tab Reorder:** Changed default tab order from Saints→Explore→Search→About→Settings to **About→Explore→Saints→Search→Settings**, making About the landing page (tag 0).

#### Files Modified
- `SplashView.swift` (new)
- `Extensions/DateFormatting.swift` (new)
- `Assets.xcassets/SplashLogo.imageset/` (new)
- `CatholicSaintsApp.swift` (splash overlay integration)
- `ContentView.swift` (default tab index)
- `SaintDetailView.swift` (date formatter applied)

#### Build Status
✅ Swift typecheck passed clean  
✅ XcodeGen regenerated successfully  
✅ iPhone 17 simulator tested

#### Impact
- **Legolas (QA):** `SaintDateFormatter` should have unit tests; splash screen UI should be visually verified
- **Samwise (Data):** No data changes required
- **Gandalf (Arch):** No architectural changes; follows existing patterns

---



---

### Decision:### Decision: Spanish Display Tags and Affinities
**Author:** Samwise (Data/Backend) | **Date:** 2026-04-13 | **Status:** Implemented

#### Context
Spanish saints file displayed English tags and affinities in the UI because translations were not available. While English matching fields must remain for category browsing, display fields should be localized for proper Spanish UI rendering.

#### Decision
Added `displayTags[]` and `displayAffinities[]` arrays to all 54 saints in `saints-es.json`. These provide properly translated Spanish versions for UI display while keeping English `tags` and `affinities` intact for category matching and search.

**Translation Coverage:**
- 100+ unique tags with gender-appropriate forms (e.g., "mística", "escritora", "Doctora de la Iglesia", "fundadora", "misionera")
- 65 unique affinities fully translated
- All saints include both arrays

#### Implementation
- **Data:** Updated `saints-es.json` with new arrays
- **Model:** `Saint.swift` now has optional `displayTags` and `displayAffinities` fields
- **View:** `SaintDetailView` renders display versions when available, falls back to English
- **Search:** `SaintListViewModel.search()` matches both English and Spanish arrays

#### Pattern for Future Work
Any localized display fields should use the `display*` prefix convention. This preserves matching-field stability while enabling language-specific UI rendering.

#### Impact
- **Frodo (iOS):** UI now displays Spanish tags/affinities for Spanish language mode
- **Legolas (QA):** Search filtering tests should verify both EN and ES queries work
- **Gandalf (Arch):** New convention established for dual-language display patterns
- **Samwise (Data):** Apply same pattern to any future display-only fields requiring translation

---

## Android Port Decisions



---

### Decision:### Decision: kotlinx.serialization Chosen Over Moshi

**Author:** Gandalf (Lead) | **Date:** 2026-04-21 | **Status:** Decided

#### Decision

Use `kotlinx.serialization` (1.7.x) for all JSON parsing in the Android app. Do not use Moshi, Gson, or Jackson.

#### Rationale

- First-party Kotlin library with compile-time code generation (no reflection).
- Pairs naturally with type-safe Navigation Compose routes (both use `@Serializable`).
- Multiplatform-ready if KMP is considered later.
- `ignoreUnknownKeys = true` handles future schema additions gracefully.

#### Impact

- **Aragorn:** All data classes annotated with `@Serializable`. Plugin: `kotlin-serialization`.
- **Legolas:** Unit tests can use the same `Json` instance for test fixtures.

---



---

### Decision:### Decision: SharedContent Gradle Sync Task — Canonical Assets Bridge

**Author:** Gandalf (Lead) & Aragorn (Android Dev) | **Date:** 2026-04-21 | **Status:** Implemented

#### Decision

`SharedContent/` (JSON data + images) is copied into Android `assets/` at build time via a Gradle `Sync` task (type: `org.gradle.api.tasks.Sync`) wired as a `preBuild` dependency. The task whitelists only `saints-*.json`, `categories-*.json`, and `images/*.jpg` for APK inclusion. Generated assets are gitignored (except `assets/README.md`).

#### Implementation

```kotlin
val syncSharedContent by tasks.registering(Sync::class) {
    from(sharedContentDir.dir("saints")) { include("saints-en.json", "saints-es.json") }
    from(sharedContentDir.dir("categories")) { include("categories-en.json", "categories-es.json") }
    from(sharedContentDir.dir("images")) { include("*.jpg"); into("images") }
    into(layout.projectDirectory.dir("src/main/assets"))
    preserve { include("README.md") }
}
tasks.named("preBuild").configure { dependsOn(syncSharedContent) }
```

#### Rationale

- APK must be self-contained (no runtime path to repo root).
- `SharedContent/` at repo root remains the single source of truth.
- `Sync` (not `Copy`) guarantees destination mirrors source — stale files auto-deleted.
- Incremental and up-to-date aware. Hooking into `preBuild` means every build picks up latest content automatically.

#### Contract for Other Agents

- **Samwise (Data):** Continue editing `SharedContent/` as normal. Android picks up changes automatically on next build.
- **Gandalf/Frodo:** The task's inclusion list is authoritative for "what ships in APK". If adding new `SharedContent/` category (e.g., `confirmation-info-*.json`), update the task's `from(...)` blocks in the same PR.
- **Legolas (QA):** Android parity tests automatically get latest assets via `preBuild` dependency.

#### Impact

- No duplication between iOS and Android content paths.
- Android APK grows by `SharedContent/images/` size (~2.8 MB) + JSON (~100 KB).

---



---

### Decision:### Decision: Coil 3 for Image Loading from Assets

**Author:** Gandalf (Lead) | **Date:** 2026-04-21 | **Status:** Decided

#### Decision

Use Coil 3 (`io.coil-kt.coil3:coil-compose:3.1.0`) for loading saint images from `assets/SharedContent/images/`. Use `file:///android_asset/` URI scheme — Coil resolves this natively without custom fetcher.

#### Rationale

- Coil 3 is Compose-first, Kotlin-first, and lightweight.
- `file:///android_asset/` is a standard Android URI scheme that Coil handles out of the box.
- Same image filenames used on both iOS and Android via `SharedContent/images/`.
- Crossfade transitions and in-memory caching come for free.

#### Implementation Pattern

```kotlin
AsyncImage(
    model = "file:///android_asset/SharedContent/images/${saint.id}.jpg",
    contentDescription = saint.name,
    modifier = Modifier.size(64.dp),
    contentScale = ContentScale.Crop,
    placeholder = painterResource(id = R.drawable.ic_placeholder)
)
```

#### Impact

- **Aragorn:** Compose `SaintDetailView` and `SaintRowView` use `AsyncImage`.
- **Samwise:** Image filenames must match saint `id` (already the case).
- **Legolas:** Image loading tests verify fallback placeholder appears on missing assets.

---



---

### Decision:### Decision: In-App Localization via StateFlow + DataStore (Not System Locale)

**Author:** Gandalf (Lead) | **Date:** 2026-04-21 | **Status:** Decided

#### Decision

Android uses a `LocalizationService` holding a `StateFlow<AppLanguage>`, backed by `DataStore<Preferences>` for persistence. A Compose `CompositionLocal` provides the current language through the tree. UI strings are served by an in-memory `AppStrings` Kotlin map (ported from iOS). **Standard Android `strings.xml` is NOT used for user-facing text** that must respond to in-app language switch.

`strings.xml` is reserved only for system-level strings (app name in launcher, permission rationale).

#### Rationale

- iOS switches language without restarting. Users expect the same on Android.
- `strings.xml` localization is tied to system locale and requires `Activity` recreation or `attachBaseContext` hacks — fragile and inconsistent.
- The `AppStrings` map is already maintained on iOS; porting it is lower risk than managing parallel `strings.xml` files.
- Saint content switches by reloading the appropriate `saints-{lang}.json` from assets (no `strings.xml` dependency).

#### Implementation Pattern

```kotlin
@Composable
private fun SaintListScreen(viewModel: SaintListViewModel) {
    val language by localLanguage.current
    val appStrings = AppStrings(language)
    
    Column {
        Text(appStrings.localized("screen_saints_title"))
        LazyColumn {
            items(viewModel.saints) { saint ->
                SaintRow(saint, appStrings)
            }
        }
    }
}
```

#### Contract

- All UI text calls `AppStrings.localized(key, language)` with the current language from `CompositionLocal`.
- When adding new UI strings, add to both `AppStrings` map (active) and `strings.xml` (reference only).
- Saint detail content and category names are never hardcoded — they come from JSON (no `strings.xml` translation).

#### Impact

- **Aragorn:** Implement `LocalizationService`, `AppStrings`, `CompositionLocalProvider`. Wire language state into all screens.
- **Samwise:** No impact (JSON files already localized by language).
- **Legolas:** Test that changing language updates both UI strings and saint content without Activity restart.

---



---

### Decision:### Decision: Hilt for Dependency Injection

**Author:** Gandalf (Lead) | **Date:** 2026-04-21 | **Status:** Decided

#### Decision

Use Hilt (2.54.x) for dependency injection in the Android app. ViewModels use `@HiltViewModel`. `SaintRepository`, `LocalizationService`, and `DataStore` are provided via Hilt modules.

#### Rationale

- Standard Android DI choice with first-class Jetpack integration.
- `hilt-navigation-compose` provides `hiltViewModel()` for scoped ViewModel injection in Navigation Compose.
- Single `:app` module means Hilt's simplicity is appropriate (no Dagger component complexity).

#### Implementation Pattern

```kotlin
@HiltAndroidApp
class CatholicSaintsApp : Application()

@AndroidEntryPoint
class MainActivity : ComponentActivity()

@HiltViewModel
class SaintListViewModel @Inject constructor(
    private val repo: SaintRepository,
    private val localization: LocalizationService
) : ViewModel()

// In Compose:
val viewModel: SaintListViewModel = hiltViewModel()
```

#### Contract

- **Legolas:** Test modules can swap `SaintRepository` for a fake via `@TestInstallIn` + custom qualifier.
- All injectable dependencies must have explicit `@Inject` constructors or Hilt `@Provides` bindings.

#### Impact

- **Aragorn:** `@HiltAndroidApp` on Application, `@AndroidEntryPoint` on Activity, `@HiltViewModel` on all ViewModels.
- Test infrastructure can mock repository and localization service.



---

