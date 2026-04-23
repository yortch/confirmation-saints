# Skill: iOS-to-Android Mapping

## When to Use

When porting any SwiftUI + MVVM iOS app to Jetpack Compose + MVVM Android, especially apps that:
- Have in-app language switching (not system locale)
- Load bundled JSON content per language
- Use cross-platform shared data files
- Need diacritic-insensitive search

## Pattern Map

| iOS Concept | Android Equivalent | Notes |
|---|---|---|
| `@Observable` class | `ViewModel` + `StateFlow<UiState>` | Collect with `collectAsStateWithLifecycle()` |
| `@AppStorage("key")` | `DataStore<Preferences>` + `Flow` | Keys: use same string names for cross-platform consistency |
| SwiftUI `EnvironmentKey` | Compose `CompositionLocal` | `staticCompositionLocalOf` for values that rarely change |
| `Bundle.main.url(forResource:)` | `context.assets.open("path")` | Assets are flat; use Gradle copy task for shared content |
| `NavigationStack` | `NavHost` + nested `navigation()` per tab | Each tab maintains its own back stack |
| `TabView` | `Scaffold` + `NavigationBar` | Material 3 `NavigationBarItem` |
| `searchable(text:)` modifier | `SearchBar` or `DockedSearchBar` composable | M3 search components |
| `String.containsIgnoringDiacritics()` | `Normalizer.NFD` + `\\p{InCombiningDiacriticalMarks}` regex strip | Must be used everywhere — direct `contains()` is a bug |
| `@MainActor` singleton service | Hilt `@Singleton` + `Dispatchers.Main` scope | Or `@ActivityRetainedScoped` |
| `Codable` struct | `@Serializable` data class | `kotlinx.serialization` with `ignoreUnknownKeys = true` |
| `.xcstrings` String Catalog | In-memory `Map<String, Map<String, String>>` | NOT `strings.xml` if in-app language switch is needed |
| Folder reference in Xcode | Gradle `Sync` task → asset source set | Never commit shared files into platform directories |
| `FlowLayout` (custom) | `FlowRow` (Compose Foundation 1.5+) | Built-in |
| `FilterChip` (custom) | Material 3 `FilterChip` | Built-in |
| `SplashView` (custom) | `SplashScreen` compat API (core-splashscreen) | System-level; customize via theme |

## Localization Pattern (In-App Switch Without Restart)

```
StateFlow<AppLanguage>  →  CompositionLocalProvider  →  Recomposition
       ↑                                                      ↓
  DataStore persistence                              AppStrings.localized(key, lang)
                                                     + ViewModel reloads JSON for new lang
```

Key rule: **Do not use `strings.xml`** for text that must switch instantly. Reserve `strings.xml` only for launcher app name and system dialogs.

## SharedContent Wiring

```
SharedContent/ (repo root, single source of truth)
       │
       ├── iOS: symlink + XcodeGen folder reference
       │
       └── Android: Gradle Sync task → build/intermediates/shared-content/
                    registered as assets.srcDir
```

Both platforms read from the same files. Neither commits copies.
