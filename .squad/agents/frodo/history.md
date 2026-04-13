# Frodo — History

## Project Context
- **Project:** confirmation-saints — Catholic Saints iOS App
- **User:** Jorge Balderas
- **Stack:** Swift / SwiftUI, iOS (iPhone + iPad)
- **Description:** App helping Catholic confirmation candidates (primarily teens, also adults) find and choose a patron saint. Features saint search by name, patron day, affinity, country, age, married status. Multilingual (EN/ES). Content sourced from Loyola Press, Focus, Lifeteen, Ascension Press, Hallow with attribution.
- **Key constraints:** Self-contained, easy content updates, cross-platform ready (Android later), include saint images with attribution.

## Learnings

### Architecture Foundation (2026-04-12)
- **Gandalf** established MVVM + SwiftUI with Observable macro (iOS 17+)
- **XcodeGen** generates `CatholicSaints.xcodeproj` from `project.yml`
- **Swift 6 concurrency**: All models are Sendable, services are @MainActor
- **UI localization**: .xcstrings String Catalog for EN/ES UI strings
- Navigate using NavigationStack (not deprecated NavigationView)

### Data Layer Ready (2026-04-12)
- **Samwise** created 25 EN + 25 ES saints with full schema compliance
- Saint data in `SharedContent/saints/saints-en.json` and `saints-es.json`
- Confirmation content in `SharedContent/content/confirmation-info-en/es.json`
- Categories in `SharedContent/categories/categories-en/es.json`
- Affinities field enables teen-focused saint matching (sports, music, art, science, etc.)
- Images with attribution in `SharedContent/images/`

### UI Integration Complete (2026-04-12)
- Dropped `LocalizedText` struct — Samwise's per-language files mean plain String model fields
- Language switching via `@AppStorage("appLanguage")` + custom `EnvironmentValues.appLanguage`
- Single shared `SaintListViewModel` in ContentView, passed to all 5 tabs
- 5 tabs: Saints (browse), Explore (categories), Search (filters), About Confirmation, Settings
- `SaintDataService` loads from `SharedContent/saints/`, `/categories/`, `/content/` subdirectories
- Purple accent theme (liturgical + teen-friendly), SF Symbols, gradient avatars
- `FlowLayout` custom Layout for tag chips, `FilterChip` reusable component
- Category matching is dynamic (computed from saint fields, not pre-indexed)
- Build verified: `xcrun swiftc -typecheck -swift-version 6` passes clean on all 13 files
- XcodeGen regen successful with new view directories

### App Rename & Welcome Screen (2026-07-13)
- App renamed from "Catholic Saints" → "Confirmation Saints" (display name only; folder stays `CatholicSaints/`)
- Updated `project.yml` (INFOPLIST_KEY_CFBundleDisplayName, PRODUCT_NAME) and README.md
- Created `Views/Onboarding/WelcomeView.swift` — 4-page TabView onboarding with PageTabViewStyle
- Uses `@AppStorage("hasSeenWelcome")` flag: shown on first launch, skipped after
- `CatholicSaintsApp.swift` conditionally renders WelcomeView vs ContentView
- Settings gets "Show Welcome Screen" button that resets `hasSeenWelcome` to false
- Purple/gold liturgical gradient theme, SF Symbols (cross, magnifying glass, book, sparkles)
- All new strings added to Localizable.xcstrings with EN/ES translations
- Build verified clean with 14 Swift files (added WelcomeView.swift)
- User preference: prefers "Confirmation Saints" as app name

### Category Navigation Bug Fix (2025-07-15)
- `CategorySaintsListView` had a duplicate `.navigationDestination(for: Saint.self)` that conflicted with the parent `NavigationStack` in `CategoryBrowseView`
- Fix: removed the inner `.navigationDestination` — the parent's declaration handles all `NavigationLink(value: saint)` resolution
- Lesson: in SwiftUI, never declare duplicate `.navigationDestination` for the same type at multiple levels of a `NavigationStack` hierarchy — only the outermost one (on/near the `NavigationStack`) should exist

### UI Improvements: Locale, Links, Images, System Integration (2026-04-12)
- Default language now derived from `Locale.current.language.languageCode` — `systemDefaultLanguage` global constant used by both `CatholicSaintsApp` and `SettingsView`
- `@AppStorage` only applies its default when no value previously saved, so existing users keep their manual choice
- `sourceURLs: [String: String]?` added to `Saint` model — optional, backward compatible with older JSON
- Sources section now renders `Link` views for sources with URLs, plain `Text` for others
- Created `SaintImageView` shared component (tries asset catalog → bundle path → fallback initial circle)
- Both `SaintDetailView` and `SaintRowView` now use `SaintImageView` — removed duplicated `colorForSaint` helpers
- App icon redesigned: white dove on red background with Pentecost flames (per user directive)
- **Cross-agent Update:** Samwise populated all 27 saints with `sourceURLs` and standardized source names to English in both language files
- Build verified: `xcrun swiftc -typecheck` passes clean, XcodeGen regen successful

### UI Polish & Localization Overhaul (2025-07-16)
- **Task 1:** Default tab changed to Explore (index 1) via `@State selectedTab = 1` and `TabView(selection:)` with `.tag(0)` through `.tag(4)`
- **Task 2:** Fixed Explore → Saint navigation — `CategorySaintsListView` now uses closure-based `NavigationLink { SaintDetailView }` instead of value-based `NavigationLink(value: saint)` which conflicted with closure-based links higher in the NavigationStack
- **Task 3:** Replaced SF Symbol `cross.fill` (medical cross) with custom `LatinCrossView` using Canvas for a true Christian cross on the welcome page
- **Task 4:** Created `AppStrings.localized(_:language:)` dictionary-based localization service — all `String(localized:)` calls replaced so UI strings respect the in-app language setting, not just the system locale
- Lesson: `String(localized:)` always uses system locale; for in-app language switching, use a manual translation dictionary or bundle-path approach
- Lesson: Mixing closure-based and value-based NavigationLinks in the same NavigationStack causes resolution issues on iOS 17+; pick one style per stack
- Build verified clean with Swift 6, XcodeGen regen successful


## Learnings

- **Task 5 (Source URL Fix):** Replaced 46 broken sourceURLs across saints-en.json and saints-es.json. Loyola Press removed /catholic-resources/saints/ pages; Hallow removed /blog/ paths; Ascension Press, Focus, and Lifeteen all returned 404s.
- Lesson: Loyola Press saint pages are gone entirely — no new URL pattern works. Franciscan Media (franciscanmedia.org/saint-of-the-day/) is the best drop-in replacement for biographical saint content.
- Lesson: Hallow migrated blog posts to /saints/{name}/ but only for popular saints. Many niche entries were not migrated.
- Lesson: Catholic News Agency (catholicnewsagency.com/saint/) and EWTN (ewtn.com/catholicism/saints/) are reliable fallbacks with stable URL patterns.
- Lesson: When fixing URLs in both EN and ES files, match by URL value (not saint name) since the saint names differ by language.
- Lesson: The EN and ES files can have slightly different broken URLs for the same saint (e.g., Guadalupe had a typo hfrancisallow.com only in EN, while ES had the correct hallow.com domain but still a 404 path).

### Cross-Agent Sync: Image & URL Updates (2026-04-12T21:12:34Z)
**From:** Samwise (saint-images) + Frodo (source-urls) completion  
**Status:** ✅ Merged into decisions.md
- All 32 saints now have verified public domain images from Wikimedia Commons
- All 46 broken source URLs successfully replaced with verified alternatives
- Saint data integrity maintained across EN/ES files
- Decision records: "Saint Image Sources from Wikimedia Commons" and "Source URL Replacement Strategy"
- UI rendering already supports image display and clickable source links — no code changes needed

### Language Reactivity Bug Fix (2025-07-18)
- **Bug:** Switching language (EN↔ES) in Settings didn't update already-open views (saint details, category saint lists). User had to navigate away and back.
- **Root cause:** `SaintDetailView` received `let saint: Saint` (a captured value-type snapshot) at navigation time. When `SaintListViewModel.loadData()` reloaded saints for the new language, the detail view still held the old-language `Saint` struct. Same issue for `CategorySaintsListView` which received `let saints: [Saint]`.
- **Fix:** Changed `SaintDetailView` to accept `saintId: String` + `viewModel: SaintListViewModel` and compute the saint reactively via `viewModel.saints.first { $0.id == saintId }`. Changed `CategorySaintsListView` to accept `groupId`/`valueId` + `viewModel` and compute saints reactively. Updated all navigation sites (`SaintListView`, `SearchView`, `CategoryBrowseView`) to pass saint IDs and viewModel references.
- **Pattern:** Never pass captured content model values (`let saint: Saint`) to detail views when that content can change (e.g., language switch). Always pass an ID + an @Observable data source so the view re-renders reactively.
- **Navigation change:** `SaintListView` and `SearchView` now use `.navigationDestination(for: String.self)` (saint ID) instead of `.navigationDestination(for: Saint.self)`.
- Build verified clean on iPhone 17 simulator.

### Splash Screen, Date Formatting & Tab Reorder (2025-07-19)
- **Task 1 (Splash Screen):** Created `SplashView.swift` — red background with centered app icon (SplashLogo image set) and "Confirmation Saints" text. Shows as overlay in `CatholicSaintsApp.swift` for 1.5s then fades out. Created `SplashLogo.imageset` in Assets.xcassets (reuses app-icon-1024.png) since `Image("AppIcon")` doesn't work for AppIcon asset sets.
- **Task 2 (Date Formatting):** Created `SaintDateFormatter` enum in `Extensions/DateFormatting.swift`. Formats ISO dates as dd-Mon-yyyy (e.g., "02-Jan-1873"). Detects approximate ancient dates (month=01, day=01, year<800) and shows year only. Applied in `SaintDetailView` for birthDate, deathDate, canonizationDate display.
- **Task 3 (Tab Reorder):** Changed tab order to About→Explore→Saints→Search→Settings. About is now tag(0) and default landing tab (`selectedTab = 0`).
- Lesson: Can't use `Image("AppIcon")` in SwiftUI — the AppIcon.appiconset is special. Create a separate imageset with the same file for in-app icon display.
- Build verified clean on iPhone 17 simulator.
