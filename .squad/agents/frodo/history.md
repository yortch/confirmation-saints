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
