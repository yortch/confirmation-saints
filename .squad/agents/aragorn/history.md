# Aragorn — Personal History

## Core Context

- **Project:** confirmation-saints
- **User:** Jorge Balderas
- **My role:** Android Dev
- **Joined:** 2026-04-21 (after iOS app shipped to App Store)

## Project Snapshot

- iOS app is LIVE on the App Store: https://apps.apple.com/app/confirmation-saints/id6762463641
- iOS is built with Swift 6 / SwiftUI / iOS 17+ / XcodeGen. Bundle id `com.yortch.CatholicSaints`.
- 70 saints with bilingual content (EN default, ES). `SharedContent/` is the canonical cross-platform data source.
- Key architectural decision: matching fields (patronOf, affinities, tags, region, lifeState, ageCategory, gender) are STORED IN ENGLISH IN BOTH language files because the ViewModel matches against English category IDs. Spanish gets `displayTags` / `displayAffinities` arrays for localized presentation.
- Saint images live in `SharedContent/images/{saint-id}.jpg`.
- Saint IDs / URLs match across `saints-en.json` and `saints-es.json` — name keys differ but URLs are the shared identifier.

## Android Stack (confirmed with Jorge 2026-04-21)

- Kotlin + Jetpack Compose (Material 3)
- Min SDK 26 (Android 8.0)
- Package: `com.yortch.confirmationsaints`
- Full iOS feature parity as first milestone

## Key Reads on Day 1

- `README.md` — canonical project spec (Gandalf rewrote it 2026-04-21 specifically for Android contributors)
- Android Port Guide section in README (or `docs/android-port.md` if it exists) — reusable vs. rewrite breakdown
- `.squad/decisions.md` — team decisions to respect
- `SharedContent/` — saints JSON, categories, images (canonical cross-platform source)
- `ios/CatholicSaints/Models/` — data model reference (translate to Kotlin data classes)
- `ios/CatholicSaints/ViewModels/SaintListViewModel.swift` — matching/filter logic reference
- `ios/CatholicSaints/Services/LocalizationService.swift` — localization pattern reference

## Learnings

(Append as I work)

### 2026-04-21 — Initial Android scaffold

**What I built (squad/android-port, commits b190097 → 87097b1):**
- Gradle 8.9 wrapper (gradlew/gradlew.bat/gradle-wrapper.jar pulled from `gradle/gradle` at tag v8.9.0)
- Top-level `android/build.gradle.kts`, `settings.gradle.kts` (`:app`), `gradle.properties` (AndroidX, non-transitive R)
- `gradle/libs.versions.toml` — AGP 8.6, Kotlin 2.0.20, Compose BOM 2024.09.02, Material 3, Nav Compose, Lifecycle 2.8.6, DataStore 1.1.1, kotlinx.serialization 1.7.3, Coil 2.7.0, JUnit 5, Turbine
- `:app` module — applicationId `com.yortch.confirmationsaints`, minSdk 26, compileSdk/targetSdk 34, versionCode 1 / versionName 1.0.0, JVM toolchain 17, Compose enabled via Kotlin Compose plugin, kotlinx.serialization plugin, R8 + shrinkResources on release (no signing config yet — TODO left in place)
- `AndroidManifest.xml`, minimal `strings.xml` (app_name only), Material base theme, adaptive launcher icon placeholder (liturgical purple bg + gold cross vector foreground)
- `MainActivity` → placeholder Compose screen in `ConfirmationSaintsTheme`
- `ui/theme/` — Material 3 dynamic color on API 31+, fallback to liturgical purple / sacred gold palette
- Empty marker packages (`data/`, `viewmodel/`, `ui/screens/`, `ui/components/`, `localization/`) each with `package-info.kt` pointing at `docs/android-architecture.md`

**The SharedContent bridge (key architectural choice):**
- Declared a Gradle `Sync` task `syncSharedContent` in `app/build.gradle.kts` wired as `preBuild.dependsOn(syncSharedContent)`
- Copies `SharedContent/saints/*.json`, `SharedContent/categories/*.json`, and `SharedContent/images/*.jpg` into `app/src/main/assets/` at build time
- `assets/` is gitignored except for `assets/README.md` — the generated content stays out of git, matching iOS's folder-reference pattern in spirit (iOS uses a folder-reference build phase; Android uses Gradle `Sync`)
- Task `preserve { include("README.md") }` keeps the explainer from being nuked by the Sync
- Logs source + destination paths at the start of each run

**Build verification — could NOT run locally:**
- No JDK installed on this Mac (`/usr/bin/java` stub reports "Unable to locate a Java Runtime")
- `gradle` CLI not on PATH
- Therefore `./gradlew :app:assembleDebug` was not executed
- Local Android SDK is at `~/Library/Android/sdk` with platform `android-36.1` + build-tools `37.0.0` — NOT the `android-34` that `app/build.gradle.kts` targets. Either (a) bump compileSdk/targetSdk to 36 and bump AGP to a version supporting 36, or (b) install `platforms;android-34` via sdkmanager. Leaving the decision to whoever builds first; noted in `android/README.md`.
- The Gradle wrapper jar is real (43 KB, valid zip pulled from the upstream repo tag) and the wrapper scripts were fetched from the same tag — the scaffold is buildable on a machine with JDK 17 + SDK 34.

**Gotchas / things I deliberately did NOT do:**
- Did NOT write `Saint`/`Category` data classes, JSON loading, LocalizationService, ViewModels, navigation, or screens. Gandalf's plan at `docs/android-architecture.md` will dictate serialization pattern and data flow. Tempting to front-run this; stayed in my lane.
- Did NOT scatter UI strings into `strings.xml` — only `app_name` lives there. The in-app localization strategy (mirror iOS's canonical-English-id + `display*` arrays pattern) will be decided by Gandalf.
- Did NOT adapt `_generate_icon.py` for Android adaptive icons — used a simple gold-cross vector placeholder. A follow-up should port the Chi-Rho design to a two-layer adaptive icon (foreground PNG or vector at 108dp with 72dp safe zone + background color / drawable).

**Key file paths (for future me):**
- Gradle sync task: `android/app/build.gradle.kts` (`val syncSharedContent by tasks.registering(Sync::class)`)
- Theme: `android/app/src/main/java/com/yortch/confirmationsaints/ui/theme/Theme.kt`
- Assets destination: `android/app/src/main/assets/` (build-generated; see `assets/README.md`)
- Launcher icon: `android/app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` (adaptive)
- Version catalog: `android/gradle/libs.versions.toml` — single source of truth for all dependency versions

**Next round (after reading `docs/android-architecture.md`):**
- Data classes with `@Serializable`
- JSON loader reading from `assets/` via `AssetManager`
- `SaintListViewModel` mirroring iOS filtering/search (diacritic-insensitive via `java.text.Normalizer`)
- Nav graph + screens
- DataStore for `appLanguage` / `hasSeenWelcome`
- Localization service


## Learnings — Phases 2–7 implementation (2026-04-22)

**Hilt module structure that worked.** One `@Module @InstallIn(SingletonComponent::class) object AppModule` is enough for app-level singletons (`Json`, `CoroutineScope`, `DataStore<Preferences>`); everything else (`SaintRepository`, `CategoryRepository`, `LocalizationService`, `PreferencesRepository`) uses `@Inject constructor` + `@Singleton` directly — no module entry needed. `@ApplicationContext` is injected into the DataStore provider without a redundant pass-through `provideContext` (first draft had that; removed).

**Coil 3 loads android_asset URIs natively.** `AsyncImage(model = "file:///android_asset/images/saint-id.jpg")` works out of the box — no custom fetcher, no `AssetManager` plumbing. That made `SaintImage` a one-liner composable. Coil 3 imports live under `coil3.compose.*`, not `coil.compose.*` — easy to get wrong.

**CompositionLocal for live-language switch, not Activity recreate.** `staticCompositionLocalOf<AppLanguage>` provided at the root composable, fed by `LocalizationService.language: StateFlow<AppLanguage>`, means switching EN↔ES ripples through every composable without ever touching `Activity.recreate()`. `SaintListViewModel` observes the same `StateFlow` in `init { }` and reloads JSON. Mirrors iOS `@Environment(\.appLanguage)`.

**Typed navigation with `@Serializable` routes (Navigation Compose 2.8+).** `sealed interface Screen` with `@Serializable object Saints`, `@Serializable data class SaintDetail(val saintId: String)`, then `composable<Screen.SaintDetail> { entry.toRoute<Screen.SaintDetail>() }` — no string routes, no argument parsing. Categorical win over the old string API; expect this to be the default going forward.

**Asset layout decision.** Flat `assets/*.json` + `assets/images/*.jpg`, not nested `assets/SharedContent/...` as the plan suggested. See `.squad/decisions/inbox/aragorn-asset-layout.md`. Matched the scaffold's pre-existing `syncSharedContent` Gradle task.

**Icon pack caveat.** Many SaintDetail / Settings screens use icons like `Icons.Default.FormatQuote`, `Icons.Default.ContactMail`, `Icons.Default.EmojiEmotions` that are NOT in the default Material icon set — they require `androidx.compose.material:material-icons-extended`. Added that dep. (Default set is stripped to save APK size; extended adds ~2MB uncompressed.)

**No-JDK constraint.** I authored Phases 2–7 without a working Gradle build on this machine. Every Kotlin file is untested; first real build will surface import/API fixups. Test coverage (`SaintParsingTest`, `DiacriticsTest`, `CategoryMatcherTest`) focuses on pure-JVM units that don't need Robolectric, so they should at least guard the data contract once the build runs.

**Launcher icon pipeline.** `_generate_android_icon.py` in the repo root scales the iOS 1024×1024 `app-icon-1024.png` into foreground PNGs for `mipmap-{m,h,xh,xxh,xxxh}dpi/` + legacy square/round. Adaptive foreground uses a 60% safe-zone ratio to survive circle/squircle/teardrop masks. One-shot script — regenerate on icon changes.

## Learnings — TopAppBar back navigation (follow-up)

**Back button pattern on a shared TopAppBar.** With a single Scaffold owning the `TopAppBar` above the NavHost (rather than per-screen Scaffolds), the back affordance is gated by two conditions evaluated against the current `NavBackStackEntry`: `navController.previousBackStackEntry != null` AND `currentRoute` matches a detail class (`SaintDetail` / `CategorySaints`). The icon used is `Icons.AutoMirrored.Filled.ArrowBack` (RTL-aware — the non-mirrored `Icons.Default.ArrowBack` is deprecated for directional glyphs). Click handler is `navController.popBackStack()`.

**Resolving a detail title from a VM owned by a nav entry.** `resolveTitle` is `@Composable` and scopes the ViewModel to the destination's `NavBackStackEntry` via `hiltViewModel(backStackEntry)` — this returns the *same* `SaintListViewModel` instance that `SaintDetailScreen` gets, so no duplicate load. Route args are pulled with `backStackEntry.toRoute<Screen.SaintDetail>()` (typed nav). Title falls back to `""` while the saint list is still loading, but the back button is rendered unconditionally so the user is never stuck.

## Learnings — Per-tab nested nav graphs + splash color (2026-04-??)

**Bottom-nav state bug root cause.** A single flat NavHost with all routes as siblings + `launchSingleTop = true` + `restoreState = true` breaks on the *first* tap of a tab whose destination hasn't been visited yet: `restoreState` expects a saved-state bundle keyed by destination id; when there is none, the `popUpTo + singleTop` combo collapses to a no-op and the back stack doesn't actually switch. Symptom: tapping "Saints" from a detail screen in another tab appeared to do nothing until Saints had been visited at least once.

**Fix — nested graph per tab.** Each top-level tab is now its own `navigation<Screen.Tab>(startDestination = ...)` block. Detail routes (SaintDetail, CategorySaints) live *inside* the tab graph that pushes them. The same route type (`Screen.SaintDetail`) can be registered in multiple nested graphs — Nav Compose resolves `navigate(Screen.SaintDetail(id))` to the destination in the current back stack's graph, so the detail gets pushed onto the right tab's stack. This mirrors iOS `TabView { NavigationStack { ... } }` semantics and makes `navigateTopLevel` with `popUpTo(graph.findStartDestination().id) + saveState + restoreState` behave correctly even on first tap.

**Selected-tab detection with nested graphs.** Can't rely on `currentRoute.startsWith(tab.qualifiedName)` once detail routes are nested — use `backStackEntry.destination.hierarchy.any { it.route == tab.qualifiedName }` instead. `hierarchy` walks destination → parent graph → root graph, so any descendant of a tab graph reports that tab as selected.

**Route-to-title mapping with nested graphs.** Needed dedicated inner start-destination markers (`SaintsHome`, `ExploreHome`, `AboutHome`, `SettingsHome`) because the *graph* route (e.g. `Screen.Saints`) is no longer a leaf destination — the actual on-screen destination is the inner `*Home`. `resolveTitle` switched from `endsWith("Saints")` to `endsWith("SaintsHome")` etc. Detail-title logic (`contains("SaintDetail")` / `contains("CategorySaints")`) is unaffected.

**Splash color lives in two files.**
- `android/app/src/main/res/values/themes.xml` — `<item name="windowSplashScreenBackground">@color/splash_background</item>` on `Theme.ConfirmationSaints.Splash` (parent `Theme.SplashScreen`, from `androidx.core:core-splashscreen`). Android 12+ honors this through the system splash API; pre-12 via the compat library.
- `android/app/src/main/res/values/colors.xml` — `<color name="splash_background">#E53935</color>` matches iOS brand red (`Color(0xFFE53935)`).
- No `values-night/themes.xml` exists; single declaration serves both light and dark. The existing `AccentRed` in `Theme.kt` is `#C62828` (slightly darker) — kept distinct from splash red intentionally so compose theming and the launch window can evolve independently; revisit if the brand consolidates on one shade.

## Pending work (cross-agent sync via Scribe, 2026-04-21T19:53:26Z)

**HiltTestRunner wiring is pending** to unblock Android instrumentation tests. Legolas (QA) has implemented 2 live Compose UI tests for `WelcomeScreenNavigationTest` and identified 10 remaining tests that require Hilt graph access. The full specification is now merged into `.squad/decisions/decisions.md` under **"Decision: Android Instrumentation Tests — Status + Hilt Test Runner Gap"**. Setup checklist:
1. New `HiltTestRunner` class extending `AndroidJUnitRunner`
2. Wire into `app/build.gradle.kts` defaultConfig (`testInstrumentationRunner`)
3. Add test dependencies: `hilt-android-testing` + `kspAndroidTest` compiler
4. Optional: debug `AndroidManifest.xml` override if needed

Once the harness lands, Legolas will un-`@Ignore` the remaining tests in a follow-up PR.

## Learnings — HiltTestRunner wiring (2026-04-21)

**Task:** Unblock Legolas's 10 `@Ignore`'d instrumentation tests by wiring Pattern B from the android-compose-instrumentation skill.

**What worked (exact recipe for this repo):**
1. `androidTest/.../HiltTestRunner.kt` extends `AndroidJUnitRunner`, overrides `newApplication` to substitute `HiltTestApplication::class.java.name`.
2. `app/build.gradle.kts` `defaultConfig { testInstrumentationRunner = "com.yortch.confirmationsaints.HiltTestRunner" }` — previously unset, so added (not replaced).
3. Dependencies — use the version catalog, reuse the existing `hilt` version ref (2.52) so `hilt-android-testing` and `hilt-android-compiler` stay in lockstep. Critical: compiler goes through `kspAndroidTest(...)`, NOT `androidTestImplementation(...)`, because this project uses KSP for Hilt code-gen.
4. Added `androidx.test:runner:1.6.2` to the catalog — compatible with existing `androidx-test-ext = 1.2.1`.

**Versions used:**
- Hilt = 2.52 (catalog key `hilt`)
- androidx-test-runner = 1.6.2 (new catalog entry)
- KSP plugin already applied project-wide.

**Verification command (confirmed clean):**
`cd android && ./gradlew :app:compileDebugAndroidTestKotlin` → BUILD SUCCESSFUL, `kspDebugAndroidTestKotlin` runs (proves Hilt test component generation is active).

**Gotchas to remember:**
- If someone later adds hilt-android-testing hard-coded to a different version, the DaggerGraph_HiltComponents mismatch surfaces as a confusing `kspAndroidTest` failure. Always drive both from the same `hilt` version ref.
- The `kspAndroidTest` configuration requires the KSP plugin to already be applied in the module — it is here via `alias(libs.plugins.ksp)`.
- Did NOT touch `androidTest/.../ui/` files — that's Legolas's artifact. Infra-only boundary respected.

**Handoff:** `.squad/decisions/inbox/aragorn-hilttestrunner-wiring.md` written so Legolas can pick up and un-`@Ignore` the 10 tests with the standard `@HiltAndroidTest` + `HiltAndroidRule` + `createAndroidComposeRule<MainActivity>()` pattern.

## Cross-agent sync: Legolas unblocked all 12 instrumentation tests (2026-04-21T20:33:14Z)

Legolas has completed the final un-`@Ignore` of the instrumentation tests after consuming the HiltTestRunner wiring above. All 12 tests are now live:

- **WelcomeScreenNavigationTest** (4) — welcome gating contract
- **SaintListDisplayTest** (4) — saint list rendering & diacritic-insensitive search
- **LanguageSwitchTest** (4) — live language switch without Activity restart

Architecture used: `@HiltAndroidTest` + `HiltAndroidRule(order=0)` + `createEmptyComposeRule()(order=1)` + manual `ActivityScenario.launch()`. This pattern allows pre-launch DataStore seeding in `@Before` (e.g., `hasSeenWelcome=true` for the relaunch test).

Build verified: `./gradlew :app:compileDebugAndroidTestKotlin` → BUILD SUCCESSFUL.

Decision merged to `.squad/decisions/decisions.md` as **"Decision: Android Instrumentation Tests — All 12 Tests Live ✅"**.

No further instrumentation work is needed from my side; CI hook remains `if: false` for Gandalf to decide.
