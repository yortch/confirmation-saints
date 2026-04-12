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
