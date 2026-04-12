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

### Cross-Platform Restructure (2026-07-15)
- Moved iOS project under `ios/` directory (CatholicSaints/, CatholicSaints.xcodeproj/, project.yml)
- SharedContent/ stays at repo root as the cross-platform data layer
- Removed obsolete `SharedContent/Data/` (superseded by `SharedContent/saints/`, `categories/`, `content/`)
- Created `android/` placeholder for future Kotlin/Compose app
- Updated `ios/project.yml` to reference `../SharedContent/` 
- SaintDataService.swift bundle paths unchanged — SharedContent folder reference in bundle still works
- Build verified with swiftc typecheck after restructure
- Key file paths updated: `ios/project.yml`, `ios/CatholicSaints/...`
