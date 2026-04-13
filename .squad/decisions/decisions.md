# Decisions — confirmation-saints

## Active Decisions

### Decision: Project Architecture — Catholic Saints iOS App
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

### Decision: UI Integration — Bilingual Data + Complete Views
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

### Decision: Saint Data Schema Design
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

### Decision: Matching Fields Must Stay English in All Language Files
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

### Decision: User Directives — System Locale, Clickable Sources, Saint Images, Icon Design
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

### Decision: Source URLs and Standardized Source Names
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

### Decision: Dictionary-Based In-App Localization
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

### Decision: Reactive Language Switching Pattern
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
