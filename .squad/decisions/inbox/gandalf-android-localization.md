# Decision: In-App Localization via StateFlow + DataStore (Not System Locale)

**Author:** Gandalf (Lead)  
**Date:** 2026-07-22  
**Status:** Decided

## Decision

Android uses a `LocalizationService` holding a `StateFlow<AppLanguage>`, backed by `DataStore<Preferences>` for persistence. A Compose `CompositionLocal` provides the current language through the tree. UI strings are served by an in-memory `AppStrings` Kotlin map (ported from iOS). **Standard Android `strings.xml` is NOT used for user-facing text** that must respond to the in-app language switch.

`strings.xml` is reserved only for system-level strings (app name in launcher, permission rationale).

## Rationale

- iOS switches language without restarting. Users expect the same on Android.
- `strings.xml` localization is tied to system locale and requires `Activity` recreation or `attachBaseContext` hacks — fragile and inconsistent.
- The `AppStrings` map is already maintained on iOS; porting it is lower risk than managing parallel `strings.xml` files.
- Saint content switches by reloading the appropriate `saints-{lang}.json` from assets.

## Impact

- **Aragorn:** Implement `LocalizationService`, `AppStrings`, `CompositionLocalProvider`. All UI text calls `AppStrings.localized(key, language)`.
- **Samwise:** No impact (JSON files are the same).
- **Legolas:** Test that changing language updates both UI strings and saint content without Activity restart.
