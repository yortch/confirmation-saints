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

## Core Context

### Phases 1 & 2–7 Complete (2026-04-21 to 2026-04-22)
- **Scaffold** (Phase 1): Gradle 8.9, Kotlin 2.0.20, Compose Material 3, Hilt, Coil 3, DataStore, kotlinx.serialization. Package `com.yortch.confirmationsaints`, minSdk 26, compileSdk/targetSdk 34. `syncSharedContent` Gradle task bridges to `SharedContent/` at build time.
- **Data + ViewModel + Localization** (Phases 2–7): Hilt DI, `SaintRepository`/`CategoryRepository` load JSON from assets, `LocalizationService` manages EN/ES via `StateFlow` (live language switch without Activity restart), `DataStore` for app state, typed nav with `@Serializable` routes, Compose Material 3 theme with dynamic color on API 31+.
- **Key decisions locked**: Flat `assets/*.json` layout, CompositionLocal for language propagation, icon-extended dependency added (Material default set too stripped for our needs).
- **Untested caveat**: No JDK on this dev machine — all Kotlin files verified only via compile/lint, not runtime until CI.
- **Adaptive launcher icons**: 60% scale in 108dp canvas = 21-22px margins at mdpi density, survives circle/squircle/teardrop masks.

## Learnings (ongoing)

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

## Learnings — Android Adaptive Icons + Splash Screen (2026-04-??)

**Task:** Fix cropped app icon on both Android home screen launcher AND system splash screen.

**Root cause analysis:**
- **Adaptive launcher icon was CORRECT**: The foreground PNG (`ic_launcher_foreground.png`) properly implements the 66dp-of-108dp safe zone rule using 60% scale (21-22px margins at mdpi density). This ensures the visible content survives circle/squircle/teardrop/rounded-square masks applied by different launchers.
- **Splash screen was BROKEN**: The `themes.xml` splash style was reusing `@mipmap/ic_launcher_foreground` for `windowSplashScreenAnimatedIcon`. Since this drawable already has adaptive padding, when displayed on the splash screen it appeared heavily cropped — the logo was only 60% of the intended size.

**The fix:**
1. Created dedicated **splash icon** (`ic_splash.png`) at 288dp, full-bleed with NO padding, so the logo appears complete on the splash screen.
2. Updated `themes.xml` `Theme.ConfirmationSaints.Splash` to use `@mipmap/ic_splash` instead of `@mipmap/ic_launcher_foreground`.
3. Updated `_generate_android_icon.py` to generate `ic_splash.png` at all densities alongside the adaptive launcher icons.

**Key Android adaptive icon rules (for future work):**
- **108dp canvas, 66dp safe zone**: Adaptive icons use a 108×108dp canvas. Only the inner 66×66dp circle (centered) is guaranteed visible across all launcher mask shapes. That's ~61% of the canvas.
- **60% scale recommendation**: Scaling content to 60% of the canvas (~65dp at mdpi) provides safe margins of 21-22px on all sides, ensuring no cropping.
- **Separate splash icons**: The system splash screen (androidx.core.splashscreen) should use a dedicated full-bleed drawable, NOT the adaptive foreground, to avoid double-padding.
- **Generator script contract**: `_generate_android_icon.py` now outputs:
  - `ic_launcher.png` / `ic_launcher_round.png` (legacy API <26, 48dp)
  - `ic_launcher_foreground.png` (adaptive, 108dp canvas with 66dp safe zone content)
  - `ic_splash.png` (splash screen, 288dp full-bleed)

**Verification:**
- Build: `./gradlew :app:assembleDebug` → BUILD SUCCESSFUL
- Adaptive foreground: 108×108px at mdpi with 21-22px margins ✅
- Splash icon: 288×288px at mdpi, full-bleed ✅
- All densities (mdpi/hdpi/xhdpi/xxhdpi/xxxhdpi) generated ✅

**Files changed:**
- `android/app/src/main/res/values/themes.xml` — updated splash icon reference
- `android/app/src/main/res/mipmap-*/ic_splash.png` — new splash icons (5 densities)
- `_generate_android_icon.py` — added splash icon generation to script

**No changes needed:**
- Adaptive launcher icon XML (`ic_launcher.xml` / `ic_launcher_round.xml`) — already correct
- WelcomeScreen composable — uses Material Icons, not launcher icon
- iOS icon generator — separate pipeline, unaffected

## Learnings — Android Adaptive Icons: The Diagonal Trap (2026-04-22)

**Task:** Fix cropped launcher icon on Android home screen. Jorge reported the splash screen was fixed (full-bleed ic_splash.png worked), but the launcher icon was still cropped despite earlier "60% scale is correct" conclusion.

**Root cause:** The 60% scale was WRONG. My earlier analysis made a **critical geometric error**: I calculated safe-zone margins linearly (66dp ÷ 108dp ≈ 61%) but failed to account for the fact that a **SQUARE icon's DIAGONAL** must fit within the **CIRCULAR** mask.

**The math that matters:**
- Safe zone: 66dp diameter circle
- For a square to fit in a circle: max side = diameter ÷ √2
- 66dp ÷ √2 ≈ **46.7dp** ≈ **43.2% of 108dp canvas**
- At 60% scale: content was 64.8dp × 64.8dp, diagonal 91.6dp
  - Overshoot: 91.6dp - 66dp = **25.6dp beyond safe zone** (39% too large!) ❌
- At 43% scale: content is 46.4dp × 46.4dp, diagonal 65.7dp
  - Clearance: 66dp - 65.7dp = **0.3dp inside safe zone** ✅

**What I measured (before fix):**
- mdpi foreground: 108×108px canvas, 65×65px content (60.2%), margins 21px (19.4%)
- Content diagonal: 91.9px = 91.9dp at mdpi scale
- All 5 densities showed margins at 19.4-20.1% — "borderline" per my script
- But the real issue: diagonal exceeded safe circle by 39%!

**The fix:**
1. Updated `_generate_android_icon.py`: `FOREGROUND_INNER_RATIO = 0.43` (was 0.60)
2. Regenerated all adaptive foreground PNGs (5 densities)
3. Verified all densities: content diagonal now 65.1-66.0dp, fitting safely in 66dp circle

**After fix measurements:**
- mdpi: 46×46px content, 31px margins (28.7%), diagonal 65.1dp ✅
- hdpi: 70×70px content, 46px margins (28.4%), diagonal 66.0dp ✅
- xhdpi: 93×93px content, 61px margins (28.2%), diagonal 65.8dp ✅
- xxhdpi: 139×139px content, 92px margins (28.4%), diagonal 65.5dp ✅
- xxxhdpi: 186×186px content, 123px margins (28.5%), diagonal 65.8dp ✅
- All clearances: 0.0-0.9dp inside safe zone

**Build verification:** `./gradlew :app:assembleDebug` → BUILD SUCCESSFUL

**Key lesson learned:** When working with adaptive icons and circular masks, **always verify the diagonal**, not just the linear dimensions. The 60% "rule of thumb" is fundamentally flawed for square content in round masks. The correct scale is ~43% to account for Pythagorean geometry.

**Files changed:**
- `_generate_android_icon.py` — corrected scale from 0.60 to 0.43 with geometric explanation
- `android/app/src/main/res/mipmap-*/ic_launcher_foreground.png` — all 5 densities regenerated

**Skill updated:** `.squad/skills/android-adaptive-icons/SKILL.md` now includes:
- "Mistake 0: Using linear percentage for circular safe zone" — the diagonal trap
- Corrected all code examples to use 43% scale
- Updated verification script to calculate and check diagonal vs. safe circle
- Confidence level bumped to "verified (corrected)"

**Measurement technique for future verification:**
```python
content_diagonal_dp = content_width * math.sqrt(2) / scale
safe_circle_dp = 66
if content_diagonal_dp <= safe_circle_dp:
    print(f"✅ FITS with {safe_circle_dp - content_diagonal_dp:.1f}dp clearance")
```


