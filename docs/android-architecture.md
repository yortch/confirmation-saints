# Android Architecture Plan — Confirmation Saints

> **Author:** Gandalf (Lead) · **Date:** 2026-07-22  
> **Status:** Approved — Aragorn executes, Gandalf reviews  
> **Branch:** `squad/android-port`  
> **Goal:** Full feature parity with iOS v1.0.0

---

## 1. Module Structure

Single `:app` module for v1. Split into `:data`, `:domain`, `:ui` only if/when:

- Build times exceed 60 s on CI, or
- A second consumer (Wear OS, widget) is added.

Until then, package-level separation within `:app` is sufficient.

## 2. Package Layout

```
com.yortch.confirmationsaints/
├── data/
│   ├── model/          # Kotlin data classes: Saint, CategoryGroup, etc.
│   ├── json/           # JSON deserialization wrappers (SaintsFile, CategoriesFile)
│   └── repository/     # SaintRepository — loads JSON from assets, exposes Flow
├── localization/
│   ├── AppLanguage.kt          # enum: EN, ES
│   ├── AppStrings.kt           # in-memory translation map (mirrors iOS AppStrings)
│   └── LocalizationService.kt  # StateFlow<AppLanguage> + DataStore persistence
├── ui/
│   ├── theme/           # Material 3 theme, colors, typography
│   ├── navigation/      # NavHost + sealed route classes
│   ├── screens/
│   │   ├── saints/      # SaintListScreen, SaintDetailScreen
│   │   ├── categories/  # CategoryBrowseScreen, CategorySaintsScreen
│   │   ├── about/       # AboutConfirmationScreen
│   │   ├── settings/    # SettingsScreen
│   │   └── onboarding/  # WelcomeScreen (pager)
│   └── components/      # SaintRow, SaintImage, FilterChip, FlowRow
├── viewmodel/
│   ├── SaintListViewModel.kt
│   └── OnboardingViewModel.kt
├── util/
│   ├── DiacriticUtils.kt       # Normalizer.NFD + combining-mark strip
│   └── DateFormatUtils.kt      # MM-DD → localized display
└── ConfirmationSaintsApp.kt    # Application class (Hilt entry, DataStore init)
    MainActivity.kt              # setContent { AppNavHost() }
```

## 3. Data Layer

### 3.1 SharedContent → Android Assets (Copy at Build Time)

**Decision:** Copy `SharedContent/` into `android/app/src/main/assets/SharedContent/` via a Gradle task. The APK is self-contained; `SharedContent/` at the repo root remains the single source of truth.

Add to `android/app/build.gradle.kts`:

```kotlin
// Copy SharedContent into assets at build time (single source of truth)
val copySharedContent by tasks.registering(Sync::class) {
    from(rootProject.file("../SharedContent"))
    into(layout.buildDirectory.dir("intermediates/shared-content/SharedContent"))
}

android {
    sourceSets["main"].assets.srcDirs(
        "src/main/assets",
        layout.buildDirectory.dir("intermediates/shared-content")
    )
}

tasks.matching { it.name.startsWith("merge") && it.name.endsWith("Assets") }.configureEach {
    dependsOn(copySharedContent)
}
```

This means:

- **Never commit JSON or images into `android/`**. They are copied at build time.
- `.gitignore` already excludes `build/`; the intermediates dir is inside build.
- Gradle clean removes the copy. Rebuilding restores it.
- CI runs the same task — no manual step.

### 3.2 Image Strategy

Images are JPGs in `SharedContent/images/`. They are copied into assets alongside the JSON by the same Gradle task above, landing at `assets/SharedContent/images/{id}.jpg`.

Load via **Coil 3** (latest stable, Compose-first):

```toml
# gradle/libs.versions.toml
[versions]
coil = "3.1.0"

[libraries]
coil-compose = { module = "io.coil-kt.coil3:coil-compose", version.ref = "coil" }
```

Usage pattern — custom `AssetFetcher`:

```kotlin
@Composable
fun SaintImage(saint: Saint, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data("file:///android_asset/SharedContent/images/${saint.image?.filename}")
            .crossfade(true)
            .build(),
        contentDescription = saint.name,
        modifier = modifier.clip(CircleShape),
        contentScale = ContentScale.Crop
    )
}
```

Coil natively resolves `file:///android_asset/` URIs — no custom fetcher needed.

### 3.3 Kotlin Data Models

Use `kotlinx.serialization`. Mirror the iOS `Saint` struct exactly.

```kotlin
@Serializable
data class Saint(
    val id: String,
    val name: String,
    val feastDay: String,
    val birthDate: String? = null,
    val deathDate: String? = null,
    val canonizationDate: String? = null,
    val country: String? = null,
    val region: String? = null,
    val gender: String? = null,
    val lifeState: String? = null,
    val ageCategory: String? = null,
    val patronOf: List<String> = emptyList(),
    val displayPatronOf: List<String>? = null,
    val tags: List<String> = emptyList(),
    val displayTags: List<String>? = null,
    val affinities: List<String> = emptyList(),
    val displayAffinities: List<String>? = null,
    val quote: String? = null,
    val biography: String,
    val whyConfirmationSaint: String? = null,
    val image: SaintImage? = null,
    val sources: List<String> = emptyList(),
    val sourceURLs: Map<String, String>? = null,
) {
    val isYoung: Boolean get() = ageCategory == "young"
}

@Serializable
data class SaintImage(
    val filename: String,
    val attribution: String,
)

@Serializable
data class SaintsFile(
    val version: String,
    val language: String,
    val lastUpdated: String,
    val saints: List<Saint>,
)
```

Category models follow the same pattern:

```kotlin
@Serializable
data class CategoriesFile(
    val version: String,
    val language: String,
    val lastUpdated: String,
    val categories: List<CategoryGroup>,
)

@Serializable
data class CategoryGroup(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val values: List<CategoryValue>,
)

@Serializable
data class CategoryValue(
    val id: String,
    val label: String,
)

@Serializable
data class ConfirmationInfoFile(
    val version: String,
    val language: String,
    val lastUpdated: String,
    val sections: List<ConfirmationSection>,
)

@Serializable
data class ConfirmationSection(
    val id: String,
    val title: String,
    val content: List<ConfirmationContent>,
    val sources: List<String>,
)

@Serializable
data class ConfirmationContent(
    val heading: String,
    val body: String,
)
```

### 3.4 Serialization

**Decision:** `kotlinx.serialization` 1.7.x (not Moshi).

Rationale: first-party Kotlin support, compile-time code gen (no reflection), multiplatform-ready, pairs with type-safe Navigation Compose routes.

```toml
# gradle/libs.versions.toml
[versions]
kotlin = "2.1.0"
kotlinx-serialization = "1.7.3"

[libraries]
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "kotlinx-serialization" }

[plugins]
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
```

### 3.5 JSON Loading (SaintRepository)

```kotlin
class SaintRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    fun loadSaints(language: AppLanguage): List<Saint> {
        val raw = context.assets.open("SharedContent/saints/saints-${language.code}.json")
            .bufferedReader().use { it.readText() }
        return json.decodeFromString<SaintsFile>(raw).saints
    }

    fun loadCategories(language: AppLanguage): List<CategoryGroup> {
        val raw = context.assets.open("SharedContent/categories/categories-${language.code}.json")
            .bufferedReader().use { it.readText() }
        return json.decodeFromString<CategoriesFile>(raw).categories
    }

    fun loadConfirmationInfo(language: AppLanguage): List<ConfirmationSection> {
        val raw = context.assets.open("SharedContent/content/confirmation-info-${language.code}.json")
            .bufferedReader().use { it.readText() }
        return json.decodeFromString<ConfirmationInfoFile>(raw).sections
    }
}
```

## 4. Localization Strategy

### The Problem

iOS uses `@AppStorage("appLanguage")` + a SwiftUI `EnvironmentKey` + `AppStrings.localized()` to switch language **without restarting the app**. Standard Android `strings.xml` localization ties to system locale and requires `Activity` recreation or `attachBaseContext` hacks. We must replicate the iOS live-switch behavior.

### The Solution

Two-layer localization matching iOS:

| Content | Mechanism | Android location |
|---|---|---|
| UI strings (buttons, labels) | `AppStrings` Kotlin object (in-memory map) | `localization/AppStrings.kt` |
| Saint / category / info content | Per-language JSON files (switched by reloading) | `assets/SharedContent/**/*-{lang}.json` |

**Do NOT use `strings.xml`** for user-visible text that must respond to the in-app switch. Reserve `strings.xml` only for system-level strings (app name in launcher, permissions rationale) that must follow the device locale.

### Implementation

```kotlin
enum class AppLanguage(val code: String, val displayName: String) {
    EN("en", "English"),
    ES("es", "Español");

    companion object {
        fun fromCode(code: String): AppLanguage =
            entries.find { it.code == code } ?: EN

        fun fromSystemLocale(): AppLanguage {
            val tag = Locale.getDefault().language
            return if (tag == "es") ES else EN
        }
    }
}
```

```kotlin
class LocalizationService(private val dataStore: DataStore<Preferences>) {

    private val languageKey = stringPreferencesKey("appLanguage")

    val language: StateFlow<AppLanguage> = dataStore.data
        .map { prefs ->
            val code = prefs[languageKey]
            if (code != null) AppLanguage.fromCode(code) else AppLanguage.fromSystemLocale()
        }
        .stateIn(CoroutineScope(Dispatchers.Main), SharingStarted.Eagerly, AppLanguage.fromSystemLocale())

    suspend fun setLanguage(lang: AppLanguage) {
        dataStore.edit { it[languageKey] = lang.code }
    }
}
```

Provide through the Compose tree via `CompositionLocal`:

```kotlin
val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.EN }

// In MainActivity / root composable:
val language by localizationService.language.collectAsStateWithLifecycle()

CompositionLocalProvider(LocalAppLanguage provides language) {
    AppNavHost(...)
}
```

`AppStrings` mirrors the iOS `AppStrings` struct:

```kotlin
object AppStrings {
    private val translations: Map<String, Map<String, String>> = mapOf(
        "es" to mapOf(
            "About" to "Acerca de",
            "Saints" to "Santos",
            "Explore" to "Explorar",
            "Settings" to "Ajustes",
            // ... full map ported from iOS LocalizationService.swift
        )
    )

    fun localized(key: String, language: AppLanguage): String {
        if (language == AppLanguage.EN) return key
        return translations[language.code]?.get(key) ?: key
    }
}
```

### When language changes:

1. `LocalizationService.language` emits new value.
2. `CompositionLocalProvider` recomposes the tree with the new `LocalAppLanguage`.
3. `SaintListViewModel` observes `language` and reloads JSON from assets.
4. All `AppStrings.localized()` calls pick up the new language. **No Activity restart.**

## 5. State Management

**Pattern:** `ViewModel` + `StateFlow`, collected via `collectAsStateWithLifecycle()` in Compose.

This mirrors iOS's `@Observable` + `@MainActor` pattern:

| iOS | Android |
|---|---|
| `@Observable class SaintListViewModel` | `class SaintListViewModel : ViewModel()` |
| `@Published` / `var` properties | `MutableStateFlow<T>` / `StateFlow<T>` |
| `viewModel.filteredSaints` | `viewModel.uiState.collectAsStateWithLifecycle()` |

ViewModel exposes a single `UiState` sealed class or data class:

```kotlin
data class SaintListUiState(
    val saints: List<Saint> = emptyList(),
    val categories: List<CategoryGroup> = emptyList(),
    val confirmationSections: List<ConfirmationSection> = emptyList(),
    val searchText: String = "",
    val selectedRegion: String? = null,
    val selectedLifeState: String? = null,
    val selectedAgeCategory: String? = null,
    val selectedGender: String? = null,
    val selectedAffinity: String? = null,
) {
    val filteredSaints: List<Saint> get() = /* port of iOS filteredSaints */
    val hasActiveFilters: Boolean get() = /* port of iOS hasActiveFilters */
}
```

## 6. Navigation

**Decision:** Jetpack Navigation Compose with `kotlinx.serialization`-based type-safe routes.

```kotlin
// Route definitions
@Serializable object AboutRoute
@Serializable object ExploreRoute
@Serializable object SaintsRoute
@Serializable object SettingsRoute
@Serializable data class SaintDetailRoute(val saintId: String)
@Serializable data class CategorySaintsRoute(val groupId: String, val valueId: String, val title: String)
@Serializable object WelcomeRoute
```

Top-level structure mirrors iOS's `TabView` → `NavigationStack`:

```
MainActivity
└── AppNavHost
    ├── WelcomeRoute (conditional on !hasSeenWelcome)
    └── MainScaffold (Scaffold + NavigationBar)
        ├── Tab: About → AboutConfirmationScreen
        ├── Tab: Explore → CategoryBrowseScreen → CategorySaintsScreen → SaintDetailScreen
        ├── Tab: Saints → SaintListScreen → SaintDetailScreen
        └── Tab: Settings → SettingsScreen
```

Use `NavHost` with nested navigation graphs per tab. Each tab maintains its own back stack (matching iOS `NavigationStack` per tab).

```toml
# gradle/libs.versions.toml
[versions]
navigation = "2.9.0"

[libraries]
navigation-compose = { module = "androidx.navigation:navigation-compose", version.ref = "navigation" }
```

## 7. Persistence (DataStore)

```toml
[versions]
datastore = "1.1.4"

[libraries]
datastore-preferences = { module = "androidx.datastore:datastore-preferences", version.ref = "datastore" }
```

Persisted keys (must match iOS semantics):

| Key | Type | Default | iOS equivalent |
|---|---|---|---|
| `appLanguage` | `String` ("en" / "es") | System locale auto-detect | `@AppStorage("appLanguage")` |
| `hasSeenWelcome` | `Boolean` | `false` | `@AppStorage("hasSeenWelcome")` |

```kotlin
val Context.dataStore by preferencesDataStore(name = "settings")

// Read hasSeenWelcome
val hasSeenWelcome: Flow<Boolean> = context.dataStore.data
    .map { it[booleanPreferencesKey("hasSeenWelcome")] ?: false }
```

## 8. Diacritic-Insensitive Search

Port iOS's `String+Diacritics.swift` using `java.text.Normalizer`:

```kotlin
object DiacriticUtils {
    private val combiningMarks = "\\p{InCombiningDiacriticalMarks}+".toRegex()

    fun String.stripDiacritics(): String =
        Normalizer.normalize(this, Normalizer.Form.NFD)
            .replace(combiningMarks, "")

    fun String.containsIgnoringDiacritics(other: String): Boolean =
        this.stripDiacritics().contains(other.stripDiacritics(), ignoreCase = true)

    fun String.equalsIgnoringDiacritics(other: String): Boolean =
        this.stripDiacritics().equals(other.stripDiacritics(), ignoreCase = true)
}
```

**Non-negotiable:** All search, filter, and category matching MUST use these functions. Direct `contains()` or `==` on saint text is a bug.

## 9. Category Matching Logic

Port `SaintListViewModel.saints(forCategoryGroup:valueId:)` exactly:

```kotlin
fun saintsForCategory(groupId: String, valueId: String, saints: List<Saint>): List<Saint> {
    val normalizedId = valueId.replace("-", " ")
    return saints.filter { saint ->
        when (groupId) {
            "patronage" -> saint.patronOf.any { it.containsIgnoringDiacritics(normalizedId) }
            "interests" -> saint.affinities.any { it.containsIgnoringDiacritics(normalizedId) }
                    || saint.tags.any { it.containsIgnoringDiacritics(normalizedId) }
            "age-category" -> saint.ageCategory == valueId
            "region" -> saint.region?.equalsIgnoringDiacritics(normalizedId) == true
            "life-state" -> saint.lifeState == valueId
            "era" -> matchesEra(saint, valueId)
            "gender" -> saint.gender == valueId
            else -> false
        }
    }.sortedWith(SaintNameComparator)
}

private fun matchesEra(saint: Saint, era: String): Boolean {
    val year = saint.birthDate?.take(4)?.toIntOrNull() ?: return false
    return when (era) {
        "early-church" -> year < 500
        "medieval" -> year in 500..<1500
        "early-modern" -> year in 1500..<1800
        "modern" -> year in 1800..<1950
        "contemporary" -> year >= 1950
        else -> false
    }
}
```

**Critical:** Match on canonical English ids (`patronOf`, `tags`, `affinities`, `region`, `lifeState`, `ageCategory`, `gender`). Render from `display*` arrays for the UI.

## 10. Theming

**Decision:** Material 3 with Material Design conventions, not an iOS port. Use an iOS-inspired color palette.

```kotlin
// Purple/gold liturgical palette (matches iOS accent colors)
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6A1B9A),         // deep purple (liturgical)
    onPrimary = Color.White,
    secondary = Color(0xFFD4A017),        // gold accent
    tertiary = Color(0xFFC62828),          // red (used for emphasis, like iOS)
    surface = Color(0xFFFFFBFE),
    background = Color(0xFFFFFBFE),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFCE93D8),
    secondary = Color(0xFFFFD54F),
    tertiary = Color(0xFFEF9A9A),
)
```

- Use `MaterialTheme.typography` for all text — no hard-coded sizes.
- Support dynamic color on Android 12+ as an opt-in (future; not v1).
- Dark mode support from day one — Material 3 handles it via `isSystemInDarkTheme()`.

## 11. Accessibility

- **Content descriptions** on all saint images (`contentDescription = saint.name`).
- **Dynamic type** via Material 3 type scale — never use fixed `sp` values for body text.
- **RTL safety** — use `Modifier.padding(start = ...)` not `padding(left = ...)`.
- **Minimum touch targets** — 48dp per Material guidelines (Compose enforces by default).
- **Semantic grouping** — use `Modifier.semantics(mergeDescendants = true)` for saint row cards.
- **TalkBack testing** required before release (delegated to Legolas).

## 12. Dependency Injection

**Decision:** Hilt for v1.

```toml
[versions]
hilt = "2.54.1"
hilt-navigation-compose = "1.2.0"

[libraries]
hilt-android = { module = "com.google.dagger:hilt-android", version.ref = "hilt" }
hilt-compiler = { module = "com.google.dagger:hilt-android-compiler", version.ref = "hilt" }
hilt-navigation-compose = { module = "androidx.hilt:hilt-navigation-compose", version.ref = "hilt-navigation-compose" }

[plugins]
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
ksp = { id = "com.google.devtools.ksp", version = "2.1.0-1.0.29" }
```

Provide `SaintRepository`, `LocalizationService`, and `DataStore` via Hilt modules. ViewModels use `@HiltViewModel` + `@Inject constructor`.

## 13. Testing Strategy

Delegated to **Legolas (QA)**. Framework choices:

| Layer | Tool | Purpose |
|---|---|---|
| Unit | JUnit 5 + kotlinx-coroutines-test | Model parsing, filter logic, diacritic utils, era matching |
| Flow | Turbine 1.2+ | StateFlow emissions from ViewModel and LocalizationService |
| UI | Compose UI Test (`createComposeRule`) | Screen rendering, navigation, language switch |
| Integration | Robolectric | SaintRepository loading from test assets |

Priority test cases:
1. Diacritic-insensitive search matches "Therese" → "Thérèse"
2. Category matching for all 7 group types
3. Language switch reloads content without Activity restart
4. Welcome screen gating (shown on first launch, skipped after)
5. Era derivation from birth dates

## 14. CI / Build Notes

- **Gradle version catalog** (`gradle/libs.versions.toml`) for all dependency versions.
- **Kotlin DSL** (`.gradle.kts`) for all build files.
- **R8** enabled for release builds (default minification).
- **Compile SDK:** 35 (latest stable).
- **Min SDK:** 26 (Android 8.0 — ~97% device coverage).
- **Target SDK:** 35.
- **Java toolchain:** 17.
- **No CI hookup yet** — will be added when scaffolding is stable. Plan: GitHub Actions with `./gradlew assembleDebug` + `./gradlew test`.

## 15. App Metadata

| Field | Value |
|---|---|
| Application ID | `com.yortch.confirmationsaints` |
| Display name | Confirmation Saints |
| Min SDK | 26 |
| Target SDK | 35 |
| Compile SDK | 35 |
| Kotlin | 2.1.0 |
| Compose BOM | 2025.01.01 (or latest stable) |
| Version name | 1.0.2 |
| Version code | 3 |

---

## 16. Screen Inventory (iOS → Android Mapping)

| iOS Screen | iOS File | Android Screen | Notes |
|---|---|---|---|
| `CatholicSaintsApp` | `App/CatholicSaintsApp.swift` | `MainActivity` + `AppNavHost` | Root + navigation |
| `ContentView` (TabView) | `Views/ContentView.swift` | `MainScaffold` (NavigationBar) | 4 tabs: About, Explore, Saints, Settings |
| `WelcomeView` (4-page pager) | `Views/Onboarding/WelcomeView.swift` | `WelcomeScreen` (HorizontalPager) | First-launch gating |
| `SplashView` | `Views/Onboarding/SplashView.swift` | Android splash screen API | Use `SplashScreen` compat library |
| `SaintListView` | `Views/Saints/SaintListView.swift` | `SaintListScreen` | Search + filter chips |
| `SaintDetailView` | `Views/Saints/SaintDetailView.swift` | `SaintDetailScreen` | Full saint profile |
| `SaintRowView` | `Views/Saints/SaintRowView.swift` | `SaintRow` composable | List item |
| `SaintImageView` | `Views/Saints/SaintImageView.swift` | `SaintImage` composable | Coil AsyncImage from assets |
| `CategoryBrowseView` | `Views/Categories/CategoryBrowseView.swift` | `CategoryBrowseScreen` | Grid of category groups |
| `CategorySaintsListView` | (in CategoryBrowseView.swift) | `CategorySaintsScreen` | Filtered saint list |
| `AboutConfirmationView` | `Views/Info/AboutConfirmationView.swift` | `AboutConfirmationScreen` | Markdown-ish sections |
| `SettingsView` | `Views/Settings/SettingsView.swift` | `SettingsScreen` | Language picker, welcome replay, version, sources, legal links |
| `FilterChip` | `Views/Components/FilterChip.swift` | Material 3 `FilterChip` | Built-in M3 component |
| `FlowLayout` | (in SaintDetailView.swift) | `FlowRow` (Compose Foundation) | Built-in since Compose 1.5 |

## 17. Feast Day Formatting

Port iOS's `formattedFeastDay` computed property. Format is `MM-DD` in JSON.

```kotlin
fun formatFeastDay(feastDay: String, language: AppLanguage): String {
    val parts = feastDay.split("-")
    if (parts.size != 2) return feastDay
    val month = parts[0].toIntOrNull() ?: return feastDay
    val day = parts[1].toIntOrNull() ?: return feastDay
    val calendar = Calendar.getInstance().apply {
        set(Calendar.MONTH, month - 1)
        set(Calendar.DAY_OF_MONTH, day)
    }
    val locale = if (language == AppLanguage.ES) Locale("es") else Locale.ENGLISH
    val formatter = SimpleDateFormat("MMMM d", locale)
    return formatter.format(calendar.time)
}
```

## 18. Sortable Name

Port iOS's `sortableName` for alphabetical ordering that strips title prefixes:

```kotlin
object SaintNameComparator : Comparator<Saint> {
    private val prefixes = listOf("St. ", "Bl. ", "Our Lady of ", "Santa ", "San ", "Santo ", "Beato ", "Beata ")

    private fun sortableName(name: String): String {
        for (prefix in prefixes) {
            if (name.startsWith(prefix)) return name.removePrefix(prefix)
        }
        return name
    }

    override fun compare(a: Saint, b: Saint): Int =
        sortableName(a.name).compareTo(sortableName(b.name), ignoreCase = true)
}
```

---

## 19. Work Decomposition

Phases ordered by dependency. No time estimates — just sequencing.

| Phase | Scope | Depends on | Owner | Key deliverables |
|---|---|---|---|---|
| **1 — Scaffold** | Gradle setup, package layout, SharedContent Gradle copy task, Compose app skeleton, Material 3 theme, Hilt wiring | — | Aragorn | `build.gradle.kts`, `libs.versions.toml`, `MainActivity`, theme, empty `AppNavHost` |
| **2 — Data layer** | Kotlin models, `SaintRepository`, JSON loading from assets, category matching logic, diacritic utils, sortable name comparator, era matching | Phase 1 | Aragorn | All `data/` and `util/` packages, unit tests for parsing + matching |
| **3 — Localization** | `AppLanguage` enum, `AppStrings` translation map, `LocalizationService` with `StateFlow` + `DataStore`, `CompositionLocal` wiring | Phase 1 | Aragorn | `localization/` package, language switch without restart verified |
| **4 — Core screens** | Saint list (search + filter chips), Saint detail, Category browse, Category saints list, About Confirmation | Phases 2, 3 | Aragorn | All `ui/screens/` except onboarding and settings |
| **5 — Onboarding** | Welcome pager (4 pages), `hasSeenWelcome` DataStore flag, first-launch gating, splash screen | Phase 3 | Aragorn | `WelcomeScreen`, conditional navigation |
| **6 — Settings** | Language picker, reset onboarding, version display, content sources, privacy/support links | Phases 3, 4 | Aragorn | `SettingsScreen` |
| **7 — Polish** | App icon (adaptive icon from existing PNG), splash branding, app name in manifest, ProGuard/R8 rules for serialization | Phases 4–6 | Aragorn + Samwise (icon) | Manifest metadata, adaptive icon layers |
| **8 — Store prep** | Release signing config, Play Console metadata, screenshots, store listing copy | Phase 7 | **Not executed yet** — just noted | Signing config in `build.gradle.kts`, `fastlane/` or manual |

### Phase dependency graph

```
Phase 1 (Scaffold)
  ├── Phase 2 (Data)
  │     └── Phase 4 (Core screens) ─── Phase 6 (Settings)
  └── Phase 3 (Localization)                │
        ├── Phase 4 (Core screens)          │
        └── Phase 5 (Onboarding)            │
                                            ▼
                                     Phase 7 (Polish)
                                            │
                                            ▼
                                     Phase 8 (Store prep)
```

---

## 20. Key Library Versions Summary

| Library | Version | Purpose |
|---|---|---|
| Kotlin | 2.1.0 | Language |
| Compose BOM | 2025.01.01 | UI toolkit |
| kotlinx.serialization | 1.7.3 | JSON parsing |
| Navigation Compose | 2.9.0 | Screen navigation |
| DataStore Preferences | 1.1.4 | Persistence |
| Coil 3 | 3.1.0 | Image loading |
| Hilt | 2.54.1 | Dependency injection |
| Lifecycle | 2.9.0 | ViewModel + lifecycle |
| Core Splash Screen | 1.0.1 | Splash API |
| JUnit 5 | 5.11.x | Unit tests |
| Turbine | 1.2.0 | Flow testing |
| Material 3 | (via BOM) | Theming + components |

---

## Appendix A: Files That Must NOT Be Committed to `android/`

- Any file from `SharedContent/` (JSON, images) — always copied at build time
- Generated build artifacts
- IDE-specific files (`.idea/`, `*.iml`) — add to `.gitignore`

## Appendix B: Cross-Platform Contracts

These are invariants that Android must preserve to stay in sync with iOS:

1. **Canonical ids are English.** Filter/match on `patronOf`, `tags`, `affinities`, `region`, `lifeState`, `ageCategory`, `gender`.
2. **Display from `display*` arrays** (or fall back to canonical if `display*` is null).
3. **Image filename = saint `id` + `.jpg`.**
4. **Feast day format is `MM-DD`** (no year).
5. **Preference keys:** `appLanguage` (string), `hasSeenWelcome` (boolean).
6. **Language auto-detect:** system locale `es` → Spanish, else English.
7. **Diacritic-insensitive search everywhere.**
8. **Era date ranges:** early-church <500, medieval 500–1499, early-modern 1500–1799, modern 1800–1949, contemporary ≥1950.
9. **Sort strips title prefixes:** St., Bl., Our Lady of, Santa, San, Santo, Beato, Beata.
