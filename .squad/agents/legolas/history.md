# Legolas — History

## Project Context
- **Project:** confirmation-saints — Catholic Saints iOS App
- **User:** Jorge Balderas
- **Stack:** Swift / SwiftUI, iOS (iPhone + iPad)
- **Description:** App helping Catholic confirmation candidates (primarily teens, also adults) find and choose a patron saint. Features saint search by name, patron day, affinity, country, age, married status. Multilingual (EN/ES). Content sourced from Loyola Press, Focus, Lifeteen, Ascension Press, Hallow with attribution.
- **Key constraints:** Self-contained, easy content updates, cross-platform ready (Android later), include saint images with attribution.

## Learnings

### Test Foundation Ready (2026-04-12)
- **Gandalf** established Swift 6 concurrency foundation (Sendable models, @MainActor services)
- **Architecture**: MVVM with Observable macro — models are data-focused, services handle logic
- **XcodeGen setup** means .pbxproj regenerates from `project.yml` — never edit .pbxproj directly
- **Testable models**: Saint, Category, LocalizedText structs follow clean separation
- **Data layer ready**: 25 EN + 25 ES saints in `SharedContent/Data/saints-en/es.json`
- Test against: SaintDataService (JSON loading), LocalizedText (bilingual strings), filtering by affinity/category/country
