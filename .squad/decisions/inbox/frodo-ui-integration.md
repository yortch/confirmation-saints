# Decision: UI Integration — Bilingual Data + Complete Views

**Author:** Frodo (iOS Dev)
**Date:** 2025-07-15
**Status:** Implemented

## What Changed
Integrated Samwise's bilingual saint data (25 saints EN+ES) into the app and built out a complete 5-tab UI.

## Key Decisions

### 1. Per-Language Loading (not LocalizedText)
Samwise's data uses separate JSON files per language. Dropped the `LocalizedText` struct in favor of plain `String` model fields. Language switching reloads the entire dataset from the correct file.

### 2. Shared ViewModel Pattern
A single `SaintListViewModel` is created in `ContentView` and passed to all tabs. This avoids duplicate data loading and keeps filtering state consistent across tabs.

### 3. Environment-Based Language
Language preference flows via `@AppStorage("appLanguage")` + custom `EnvironmentValues.appLanguage`. When language changes, `ContentView.onChange` triggers a full data reload.

### 4. Category Browsing via Computed Matching
Rather than pre-indexing saints into categories, `SaintListViewModel.saints(forCategoryGroup:valueId:)` dynamically matches saints against category criteria. This keeps the code simple and the data source authoritative.

### 5. Purple Accent Theme
Chose purple as the primary accent color — appropriate for confirmation (liturgical color) and appealing to teen users. Used gradients for saint avatars.

## Files Changed
- Models: `Saint.swift` (rewritten), `Category.swift` (simplified)
- Service: `SaintDataService.swift` (bilingual loading)
- ViewModel: `SaintListViewModel.swift` (new filtering)
- Views: All views rewritten/created (13 Swift files total)
- Localization: `Localizable.xcstrings` (40+ strings EN/ES)
- New files: `CategoryBrowseView.swift`, `SettingsView.swift`

## Impact on Other Agents
- **Legolas:** Tests need updating — old `LocalizedText`, `Affinity` enum, `ContentSource` types are gone. New model uses plain strings.
- **Samwise:** Data contract is stable. Any new saints just need matching fields in both language files.
- **Gandalf:** Architecture preserved (MVVM). Added Environment key for language.
