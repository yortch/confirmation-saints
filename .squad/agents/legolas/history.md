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
