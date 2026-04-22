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

### Welcome Screen & App Icon (2026-04-12)
- **Frodo** created `Views/Onboarding/WelcomeView.swift` — 4-page TabView onboarding with first-launch gating
- New files: `WelcomeView.swift`, modified `SettingsView.swift` (added "Show Welcome Screen" button)
- **Test coverage needed:** WelcomeView display logic, Settings button toggle, first-launch behavior
- **Samwise** generated app icon: 1024×1024 PNG with Chi-Rho design, purple gradient, gold accents, dove silhouette
- Icon integration complete; Xcode auto-generates smaller sizes from 1024×1024 source

### Anticipatory Android Test Scaffolding (2026-07-22)
- Aragorn was landing Phases 2–7 of the Android port on `squad/android-port` in parallel; my job was test scaffolding that does not depend on his yet-unwritten source.
- **Cross-platform parity guardrail** written as `tests/shared-content-parity.py` (Python, stdlib only). Enforces id parity, `sourceURLs` value-set parity, canonical field parity (`patronOf`, `affinities`, `tags`, `region`, `lifeState`, `ageCategory`, `gender`), per-saint image existence, and category group+value id parity. Exits 0/1/2 with diff on stderr. Ran against HEAD → **PASS** (70 saints in lockstep, no drift).
- **Rationale for Python (not XCTest / JUnit):** data invariants are language-agnostic; both platforms can share one guardrail instead of duplicating. Captured as `.squad/skills/cross-platform-json-parity-check/SKILL.md`.
- **Android unit test stubs** at `android/app/src/test/` — `SaintRepositoryTest`, `CategoryMatchingTest`, `LocalizationServiceTest`, `BirthDateParsingTest`. All `@Disabled` with TODO comments citing the specific contract from `.squad/decisions.md` (70-saint roster, English-canonical matching, in-app language switch, 0256 edge case). JUnit 5 (project already wires `junit-jupiter` + Turbine).
- **Android instrumentation stubs** at `android/app/src/androidTest/` — `WelcomeScreenNavigationTest`, `SaintListDisplayTest`, `LanguageSwitchTest`. `@Ignore`'d pending Aragorn's source.
- **CI hook** at `.github/workflows/android-ci.yml`, every job `if: false` so it's a scaffold only — flip on after Phases 2–7 stabilize.
- **Lane discipline:** wrote nothing under `android/app/src/main/` (Aragorn's lane). Found zero parity drift, so did NOT touch any JSON (Samwise's lane). Filed a decision inbox note at `.squad/decisions/inbox/legolas-parity-guardrail.md` asking Gandalf to confirm whether `country` should be canonical (currently excluded from the check — EN/ES country names may legitimately differ, e.g. "Italy" vs "Italia").
- **Key Android test conventions this session:**
  - JUnit 5 for `src/test/` (`org.junit.jupiter.api.Test`, `@Disabled`).
  - JUnit 4 for `src/androidTest/` (Compose UI test framework is still JUnit 4 — `org.junit.Test`, `@Ignore`). Do not accidentally mix these.
  - Package layout in test dirs mirrors `com.yortch.confirmationsaints` packages from `docs/android-architecture.md`.

### Android Instrumentation Tests — Phases 1–7 Unblocked (2026-07-23)
- **Trigger:** Aragorn landed Phases 1–7 (PR #1 open). Three `@Ignore`'d stubs in `android/app/src/androidTest/java/com/yortch/confirmationsaints/ui/` could now be implemented against real source.
- **Outcome:** Compile-clean (`./gradlew :app:compileDebugAndroidTestKotlin` → BUILD SUCCESSFUL). 2 tests live, 10 still `@Ignore`'d with specific single-root-cause reasons.
- **What went live (no `@Ignore`):**
  - `WelcomeScreenNavigationTest#should_show_welcome_screen_when_hasSeenWelcome_is_false`
  - `WelcomeScreenNavigationTest#should_navigate_to_saint_list_when_get_started_is_tapped`
  - Both use `createComposeRule()` hosting `WelcomeScreen` directly with an explicit `CompositionLocalProvider(LocalAppLanguage provides AppLanguage.EN)`. No Hilt needed because `WelcomeScreen(onComplete)` has no `hiltViewModel()` dependency.
- **What stays `@Ignore`'d (10 tests across all 3 files):** All blocked on **one** missing piece: there is no `HiltTestRunner` in `testInstrumentationRunner`, so `MainActivity` (`@AndroidEntryPoint`) can't launch and `hiltViewModel()` defaults in `SaintListScreen` + `SettingsScreen` can't be resolved.
- **Key Compose UI test patterns that worked:**
  - **`createComposeRule()` > `createAndroidComposeRule<MainActivity>()` when the composable is self-contained.** Don't pay the Hilt tax for pure composables in a Hilt app. Wrote this up as `.squad/skills/android-compose-instrumentation/SKILL.md`.
  - **Drive Compose Pager through the UI, not state.** `pagerState` is `remember`'d inside `WelcomeScreen`; the test taps "Next" × 3 to reach the final CTA. Matches user behavior and doesn't require exposing internals.
  - **`composeRule.waitForIdle()` after every Next click.** Pager animation otherwise races assertions.
  - **Explicit `CompositionLocalProvider(LocalAppLanguage provides AppLanguage.EN)`** — the local has a default, so omitting it passes silently but doesn't exercise the real language-propagation path.
- **Production-code gaps found (filed to Aragorn via `.squad/decisions/inbox/legolas-android-instrumentation-tests.md`):**
  - No `HiltTestRunner` class + `testInstrumentationRunner` line in `app/build.gradle.kts` defaultConfig. Required before any `@HiltAndroidTest` can run. Exact wiring (runner class, dependencies: `hilt-android-testing`, `kspAndroidTest` for `hilt-android-compiler`, `androidx.test:runner`) documented in the decision file — ready for Aragorn to paste in.
  - No production semantic tags needed — `onNodeWithText` against `AppStrings` literals is sufficient. No production code was edited.
- **Files touched (test sources only):**
  - `android/app/src/androidTest/java/com/yortch/confirmationsaints/ui/WelcomeScreenNavigationTest.kt` — 2 live, 2 `@Ignore`'d with reasons.
  - `android/app/src/androidTest/java/com/yortch/confirmationsaints/ui/SaintListDisplayTest.kt` — 0 live, 4 `@Ignore`'d with reasons + detailed TODOs citing stable EN/ES saint name pairs.
  - `android/app/src/androidTest/java/com/yortch/confirmationsaints/ui/LanguageSwitchTest.kt` — 0 live, 4 `@Ignore`'d with reasons + TODOs naming the EN↔ES string pairs ("Settings"↔"Ajustes", "Language"↔"Idioma", "Saints"↔"Santos").
  - Class-level `@Ignore` annotations removed from all three so individual methods become independently un-ignorable as the Hilt harness lands.
- **No new dependencies added** to `app/build.gradle.kts`. The existing `androidx.compose.ui.test.junit4` + `ui-test-manifest` setup was sufficient. Hilt-specific deps only become necessary once Aragorn lands the test runner (spec'd in decision file).
- **Lane discipline:** 100% test-side changes. No edits under `android/app/src/main/`. Where guidance conflicted (user asked to remove `@Ignore` vs. rule forbidding feature wiring), I followed the lane rule and filed a sharp, actionable decision-inbox note with copy-pasteable wiring for Aragorn.

### HiltTestRunner Landed — 10 Tests Unblocked (2026-04-21)
- **Trigger:** Aragorn landed HiltTestRunner (see `.squad/decisions.md#hilttestrunner-wiring-for-android-instrumentation-tests-2026-04-21`)
- **Outcome:** All 10 `@Ignore`'d tests now ready to un-ignore and implement against real MainActivity + hiltViewModel()
- **Next Steps:** Remove @Ignore, annotate with @HiltAndroidTest, add HiltAndroidRule + ComposeRule per skill pattern

### Hilt Tests Unblocked — 12/12 Live (2026-07-24)
- **Trigger:** Aragorn wired `HiltTestRunner` + `testInstrumentationRunner` + Hilt test deps (`.squad/decisions/inbox/aragorn-hilttestrunner-wiring.md`). All 10 previously `@Ignore`'d tests became implementable.
- **Outcome:** `./gradlew :app:compileDebugAndroidTestKotlin` → **BUILD SUCCESSFUL**. `@Ignore` count across the three UI test files: **0**. Total live: 12/12.
- **Files:** `WelcomeScreenNavigationTest.kt` (4), `SaintListDisplayTest.kt` (4), `LanguageSwitchTest.kt` (4). All `@HiltAndroidTest`.
- **DataStore seeding pattern that worked (confirmed, not just speculation):**
  - `createAndroidComposeRule<MainActivity>()` launches the Activity in its rule's `before()` — that runs BEFORE `@Before`, so `hiltRule.inject()` + `prefs.setHasSeenWelcome(true)` happen TOO LATE for "initial destination" tests (Welcome would flash first).
  - Switched to `createEmptyComposeRule()` + manual `ActivityScenario.launch(MainActivity::class.java)` in each test body. This lets `@Before` inject + seed, then each test re-seeds pre-launch as needed. `composeRule` attaches to whichever Compose hierarchy becomes current — no per-activity wiring required.
  - Baseline reset in `@Before` (`setHasSeenWelcome(false); setLanguage(EN)`) prevents test-order pollution — DataStore persists across tests in the same HiltTestApplication process.
  - Promoted this pattern to `.squad/skills/android-compose-instrumentation/SKILL.md` as "Pattern B-lazy". Bumped skill confidence to medium.
- **Compose assertion gotchas that bit me:**
  - `onNodeWithText("Settings")` after language switch is ambiguous — both bottom-nav label AND top-bar title match. Used `onAllNodesWithText(...).fetchSemanticsNodes().size >= 2` to assert both places recomposed.
  - Top-bar "Saints" collides with the nav tab "Saints". For the "arrived on Saint List" assertion, the search-field placeholder `"Name, interest, country..."` is a unique landmark — used it as the "I'm on Saint List" oracle throughout.
  - `LazyColumn` only composes visible rows; naive `onNodeWithText("St. Thérèse of Lisieux")` fails if it's below the fold. Fix: type a search query first to collapse the list to one match — also exercises the filter pipeline. Avoids needing a testTag on the LazyColumn (would have required prod-code edit).
  - `composeRule.waitUntil { … fetchSemanticsNodes().isNotEmpty() }` is the right idiom for DataStore-driven recomposition (async write → flow emit → recompose). `waitForIdle()` alone is insufficient — coroutine launches on `appScope` aren't in the Compose frame clock.
- **ActivityScenario was necessary** — specifically for `should_skip_welcome_on_relaunch_when_hasSeenWelcome_is_true` (must seed pre-launch, can't with auto-launch rule) and for `should_persist_language_selection_across_activity_recreation` (needed `scenario.recreate()`). `createEmptyComposeRule` + manual launch was the pattern that covered all cases uniformly, so used it across all three files for consistency.
- **Navigation assertions that were tricky:**
  - Settings → tap "Español" radio → wait for `Ajustes`. The `LocalizationService.setLanguage` launches on `appScope` (not `viewModelScope`), so the write is async and the flow emits on another dispatcher. `waitUntil` with a 5s timeout was reliable; direct assertion without waiting was flaky.
  - Tab switching via bottom-nav `Text` labels works (`onNodeWithText("Santos").performClick()`) because `NavigationBarItem` exposes its Text child semantics. No content-description needed.
- **No production-code edits.** Every test hook used existing semantics. Two new `androidTest` files deps needed: none — `androidx.test:runner` transitively provides `androidx.test.core.app.ActivityScenario`, already declared.

### Android Splash Icon Refactor & Launcher Icon Recut (2026-04-22)
- **Splash icon fix (Aragorn):** Fixed splash screen icon cropping. Root cause: `themes.xml` was reusing adaptive foreground (with 21px margins) for splash, causing double-padding. Created dedicated `ic_splash.png` (288dp, full-bleed) across 5 densities + updated `themes.xml` reference.
- **Launcher icon recut (Aragorn):** Prior adaptive icon implementation used 60% scale (claim was WRONG; linear 66/108 ≈ 61% ignored diagonal trap). Correct scale = 43.2% (66dp ÷ (108dp÷√2)). Square content rotated in circular mask must fit diagonally, not just width/height. At 60%, diagonal = 91.6dp (overshoot 25.6dp). At 43%, diagonal = 65.7dp (0.3dp clearance inside safe zone). Updated `FOREGROUND_INNER_RATIO` in `_generate_android_icon.py` (0.60 → 0.43) + regenerated all 5 launcher densities. Full geometric analysis at `.squad/decisions/decisions.md#android-launcher-icon-scale-correction-60-43`.
- **QA impact:** Both splash and launcher now correct. If running UI tests on icon placement/scale, verify against physical device/emulator post-rebuild.
- **Patterns documented:** `.squad/skills/android-adaptive-icons/SKILL.md` — reusable for future icon work. Includes "Diagonal Trap" mistake section so future readers don't repeat the 60% error.
