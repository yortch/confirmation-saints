# Decisions — confirmation-saints

## Active Decisions (2026-04-21 and later)

### Decision: Android asset layout: flat, not nested `SharedContent/...`

**Author:** Aragorn (Android Dev)
**Date:** 2026-04-22
**Status:** Proposed — awaiting @yortch confirmation

#### Context

`docs/android-architecture.md` §3.2 suggested bundling content under
`app/src/main/assets/SharedContent/{saints,categories,images}/...`, preserving
the repo-root directory structure. The existing scaffold's `syncSharedContent`
Gradle task already writes to a flat layout:

```
assets/
├── saints-en.json
├── saints-es.json
├── categories-en.json
├── categories-es.json
├── confirmation-info-en.json   ← added in Phase 2
├── confirmation-info-es.json
└── images/*.jpg
```

#### Decision

**Keep the flat layout.** `SaintRepository` / `CategoryRepository` load by
direct filename (`saints-en.json`, etc.), and `SaintImage.filename` is a bare
`.jpg` that Coil resolves via `file:///android_asset/images/{filename}`.

#### Why

- The scaffold already shipped with this layout and Jorge's build pipeline
  (CI + local Android Studio) was tested against it.
- iOS does not use `SharedContent/` as a runtime path either — it copies files
  into the bundle root. Keeping Android similar reduces "where do I find the
  JSON at runtime?" confusion.
- The nested path would force a rename migration for no load-time benefit —
  `AssetManager.open(...)` cost is identical either way.

#### Consequences

- Every Kotlin `open("saints-en.json")` call is correct as written.
- If we ever split content into many more sub-directories, revisit this.
- `android/README.md` now documents the flat layout as canonical for Android.

---

### Decision: Kotlin 2.0.20 + KSP 2.0.20-1.0.25, not Kotlin 2.1.0

**Author:** Aragorn (Android Dev)
**Date:** 2026-04-22
**Status:** Proposed

#### Context

`docs/android-architecture.md` §Appendix A suggests Kotlin 2.1.0. The existing
scaffold is on Kotlin 2.0.20, and Hilt 2.52 + Compose BOM 2024.09 + KSP
2.0.20-1.0.25 are all known to interop cleanly at that version.

#### Decision

Stay on **Kotlin 2.0.20 / KSP 2.0.20-1.0.25** for Phases 2–7. Upgrade to
Kotlin 2.1.x as a follow-up once the build is green and tests pass.

#### Why

- I cannot build locally to verify 2.1.0 compatibility.
- KSP's plugin coordinate is tied to the Kotlin version (`2.0.20-1.0.25`
  vs. `2.1.0-x.y.z`); bumping both in one step + other phase work is risky.
- Compose BOM 2024.09 has explicit 2.0.20 compat; 2024.12 is the BOM that
  pairs with Kotlin 2.1.0.

#### Follow-up

Once Jorge confirms phases 2–7 compile & run, bump in one atomic commit:
`kotlin = "2.1.0"`, `ksp = "2.1.0-1.0.29"`, `compose-bom = "2024.12.01"`.

---

### Decision: Android Navigation — Per-Tab Nested Graphs

**Author:** Aragorn (Android Dev)
**Status:** Proposed
**Date:** 2026-04-22

#### Context
The Android bottom-nav was using a single flat NavHost with all destinations (About, Explore, Saints, Settings, SaintDetail, CategorySaints) as siblings. Tab clicks called a standard `navigate { popUpTo(graph.startDestination) { saveState=true }; launchSingleTop=true; restoreState=true }` helper.

This produced a user-visible bug: from Explore → category → SaintDetail, tapping the Saints tab did *nothing* on the first attempt (before Saints had ever been visited in the session). `restoreState = true` combined with `launchSingleTop` short-circuits when there is no saved-state bundle yet for the target destination, so the tab switch became a no-op.

#### Decision
Refactor to a **nested-graph-per-tab** structure:

```
NavHost(startDestination = Screen.Saints) {
  navigation<Screen.About>(startDestination = Screen.AboutHome)       { … }
  navigation<Screen.Explore>(startDestination = Screen.ExploreHome)   { … + CategorySaints + SaintDetail }
  navigation<Screen.Saints>(startDestination = Screen.SaintsHome)     { … + SaintDetail }
  navigation<Screen.Settings>(startDestination = Screen.SettingsHome) { … }
}
```

- Each tab owns its own back stack.
- Detail destinations are registered **inside** the tab graph(s) that navigate to them. The same route type (`Screen.SaintDetail`) appears in both Explore and Saints graphs — Nav Compose resolves `navigate(Screen.SaintDetail(id))` against the current back stack's graph, pushing onto the correct tab.
- Selected-tab detection uses `destination.hierarchy.any { it.route == tabQualifiedName }` instead of `currentRoute.startsWith(...)` so deep routes inside a tab still highlight the tab.
- `navigateTopLevel` keeps `popUpTo(graph.findStartDestination().id) + saveState + restoreState` — it now works reliably because each nested graph has an independent saved-state bundle.

New `Screen` sealed variants: `AboutHome`, `ExploreHome`, `SaintsHome`, `SettingsHome` (inner start destinations). Existing tab markers (`About`, `Explore`, `Saints`, `Settings`) are now graph routes, not leaf screens.

#### Rationale
- Canonical Compose pattern for bottom-nav + back-stack-per-tab.
- Matches iOS `TabView { NavigationStack { ... } }` semantics — each tab remembers where the user was.
- Fixes the first-tap no-op bug with no reliance on `restoreState`/`saveState` internals.
- No persistence impact: none of these routes are serialized to disk or deep-linked.

#### Impact
- **Aragorn (me):** MainScaffold + Screen.kt updated. `resolveTitle` now matches `*Home` suffixes for tab titles; detail-title logic unchanged.
- **Gandalf:** Architecture doc (`docs/android-architecture.md`) should note the nested-graph pattern if/when nav structure is documented.
- **Legolas:** Any nav-focused UI test fixtures must use the new route types (`Screen.SaintsHome`, etc.) for tab start destinations; tab markers are still valid for `navigateTopLevel` targets.
- **Samwise / iOS:** No impact.

#### Verification
`./gradlew :app:assembleDebug` passes. Manual test steps:
1. Cold start → lands on Saints tab ✓
2. Explore → category → SaintDetail → tap Saints tab → lands on Saints list ✓ (primary bug fix)
3. Back to Explore → still on SaintDetail (tab state preserved) ✓
4. TopAppBar back arrow on SaintDetail pops to that tab's list ✓

---

### Decision: Aragorn — TopAppBar Back Navigation & Detail Title Resolution

**Date:** 2026-04-22
**Author:** Aragorn (Android)
**Status:** Proposed

#### Context

`MainScaffold.kt` owns a single shared `TopAppBar` above the `NavHost`. Before this change, pushing `SaintDetail` or `CategorySaints` left the user stranded — no back button, and `SaintDetail` showed an empty title.

#### Decision

1. **Shared TopAppBar, gated back button.** Keep one top-level `TopAppBar` in the root `Scaffold`. Render a navigation icon only when both:
   - `navController.previousBackStackEntry != null` (stack is actually poppable), and
   - `currentRoute` matches a detail class (contains `"SaintDetail"` or `"CategorySaints"`).

   This guarantees top-level tabs (About/Explore/Saints/Settings) never show a back affordance, matching iOS tab root behavior.

2. **RTL-aware icon.** Use `Icons.AutoMirrored.Filled.ArrowBack`. The non-mirrored variant is deprecated for directional glyphs and would look wrong in Arabic/Hebrew builds (not a current locale, but free correctness).

3. **Detail title via shared VM scoped to the nav entry.** `resolveTitle` is `@Composable` and calls `hiltViewModel(backStackEntry)` to obtain the *same* `SaintListViewModel` instance that `SaintDetailScreen` uses. Title is `state.saints.firstOrNull { it.id == saintId }?.name ?: ""`. Fallback is an empty string (not a spinner) — the back button renders regardless so the user can always escape.

4. **CategorySaints title unchanged.** Still pulled from the typed route arg (`Screen.CategorySaints.title`), already localized at push time.

#### Alternatives considered

- **Per-screen Scaffolds + per-screen TopAppBar.** Would give each screen full control but duplicates bottom-bar plumbing and fights the current single-Scaffold design.
- **Mutable state hoisted from SaintDetailScreen up to MainScaffold for the title.** More moving parts; the VM-per-entry approach is simpler and already how the screen works.

#### Implications

- Any future detail destination must add its route keyword to the `isDetailRoute` check (or refactor to a `Screen.isDetail` marker). Worth a follow-up if a third detail type is added.
- `SaintListViewModel` is now resolved once per `SaintDetail` entry (already true from `SaintDetailScreen`); no new instance is created by the title resolver.

---

### Decision: Android Instrumentation Tests — Status + Hilt Test Runner Gap

**Author:** Legolas (QA)
**Date:** 2026-07-23
**Branch:** squad/android-port
**Routes to:** Aragorn (Android Dev) for Hilt test harness; Gandalf (Lead) for sign-off.

#### Summary

Phases 1–7 of the Android port are merged and smoke-tested. The three
`@Ignore`'d stubs in `android/app/src/androidTest/java/com/yortch/confirmationsaints/ui/`
have been reviewed against the now-real production source. Some test bodies
are live; most remain `@Ignore`'d behind a single, specific blocker: **there
is no HiltTestRunner wired into `testInstrumentationRunner`**, so any test
that launches `MainActivity` (which is `@AndroidEntryPoint`) or hosts a
composable that calls `hiltViewModel()` cannot run.

#### What is now live (no `@Ignore`)

`WelcomeScreenNavigationTest` — 2 of 4 tests:

| Test | Verifies |
|------|----------|
| `should_show_welcome_screen_when_hasSeenWelcome_is_false` | First page of the pager renders with English title "Find Your Confirmation Saint"; Skip + Next controls visible. |
| `should_navigate_to_saint_list_when_get_started_is_tapped` | Tapping Next × 3 advances to the final page; tapping "Let's Go!" invokes the `onComplete` callback (the hook AppRoot uses to flip `hasSeenWelcome` and transition to `MainScaffold`). |

Both are hosted via `createComposeRule()` calling `WelcomeScreen` directly
with a `CompositionLocalProvider(LocalAppLanguage provides AppLanguage.EN)`.
`WelcomeScreen` has no `hiltViewModel()` dependency, so Hilt is not needed.

Compile verified: `./gradlew :app:compileDebugAndroidTestKotlin` → BUILD SUCCESSFUL.

#### What is still `@Ignore`'d, and why

All of the following require a HiltTestRunner (see next section):

**WelcomeScreenNavigationTest** (2 remaining):
- `should_persist_hasSeenWelcome_true_after_completing_onboarding` — needs
  DataStore read after a full MainActivity flow.
- `should_skip_welcome_on_relaunch_when_hasSeenWelcome_is_true` — needs to
  seed DataStore and launch MainActivity.

**SaintListDisplayTest** (all 4): `SaintListScreen` uses `hiltViewModel()` to
inject `SaintListViewModel`, which depends on `SaintRepository` + DataStore.

**LanguageSwitchTest** (all 4): `SettingsScreen` uses `hiltViewModel()` for
both `SettingsViewModel` and `SaintListViewModel`; the live-switch contract
requires `LocalAppLanguage` to be bound to the real `LocalizationService`
StateFlow from the Hilt graph.

Each `@Ignore` carries a message that names the specific blocker and points
back to this decision file.

#### Requested feature wiring (Aragorn)

To unblock the remaining 10 tests, please add the Hilt test harness. Expected
shape (not prescribing, documenting for reviewer context):

1. **New file** `android/app/src/androidTest/java/com/yortch/confirmationsaints/HiltTestRunner.kt`:
   ```kotlin
   class HiltTestRunner : AndroidJUnitRunner() {
       override fun newApplication(cl: ClassLoader?, name: String?, ctx: Context?) =
           super.newApplication(cl, HiltTestApplication::class.java.name, ctx)
   }
   ```
2. **`app/build.gradle.kts` defaultConfig:**
   ```kotlin
   testInstrumentationRunner = "com.yortch.confirmationsaints.HiltTestRunner"
   ```
3. **Dependencies (`androidTestImplementation`):**
   - `com.google.dagger:hilt-android-testing:<same as hilt>` (plus matching
     `kspAndroidTest` for the `hilt-android-compiler`).
   - `androidx.test:runner` (provides `AndroidJUnitRunner`).
4. **Ensure** `ConfirmationSaintsApp` is tagged `tools:replace="android:name"` safe,
   or add a debug `AndroidManifest.xml` override pointing to `HiltTestApplication`.

No other production-code changes are required — the Hilt graph itself is
already sufficient (`PreferencesRepository`, `LocalizationService`, etc. are
fully injectable).

Once the harness lands, Legolas will un-`@Ignore` the 10 remaining tests in a
follow-up PR — each already has a TODO block with the exact assertions and
stable EN/ES saint names (`St. Thérèse of Lisieux` ↔ `Santa Teresa de Lisieux`,
etc.) to use.

#### New test dependencies added

**None.** All current tests compile against the existing
`androidTestImplementation(libs.androidx.compose.ui.test.junit4)` +
`debugImplementation(libs.androidx.compose.ui.test.manifest)` setup. New deps
only become necessary when the Hilt test harness is added (point 3 above).

#### Files touched

- `android/app/src/androidTest/java/com/yortch/confirmationsaints/ui/WelcomeScreenNavigationTest.kt` — 2 live tests, 2 `@Ignore`'d with specific reasons.
- `android/app/src/androidTest/java/com/yortch/confirmationsaints/ui/SaintListDisplayTest.kt` — documented contract + TODOs; all `@Ignore`'d on test methods (class-level `@Ignore` removed).
- `android/app/src/androidTest/java/com/yortch/confirmationsaints/ui/LanguageSwitchTest.kt` — documented contract + TODOs; all `@Ignore`'d on test methods (class-level `@Ignore` removed).

No production source modified. No edits under `android/app/src/main/`.

---

### Decision: Cross-Platform Parity Check is the Guardrail

**Author:** Legolas (Tester)
**Date:** 2026-07-22
**Status:** Proposed (awaiting Gandalf)

#### Context

Aragorn is implementing the Android port in parallel with iOS. The
"SharedContent/ is the Canonical Cross-Platform Data Source" decision
(2026-04-21) established canonical English identifiers as a cross-platform
contract. Until now there was no automated check enforcing it — just
convention and reviewer vigilance.

#### Decision

Adopt `tests/shared-content-parity.py` as the **single cross-platform
parity guardrail**. Neither iOS (XCTest) nor Android (JUnit) should
duplicate this check — they call into canonical data that the Python
script has already validated.

Rationale:
- Platform-neutral: runs in CI, pre-commit, and on any dev box with
  Python 3, no Xcode or Gradle required.
- Fails fast on PRs that touch `SharedContent/` before a full iOS or
  Android build would catch the drift at runtime.
- Keeps iOS and Android test suites focused on *their* concerns
  (rendering, navigation, view models) rather than re-implementing
  data invariants twice.

#### Invariants enforced

1. `saints-en.json` and `saints-es.json` expose the same set of saint ids.
2. `sourceURLs` value set matches per saint across EN/ES.
3. Canonical English fields match byte-for-byte per saint across EN/ES:
   `patronOf`, `affinities`, `tags`, `region`, `lifeState`,
   `ageCategory`, `gender`.
4. Every saint has an image at `SharedContent/images/<id>.jpg`.
5. `categories-en.json` vs `categories-es.json`: same group ids and same
   value ids per group.

#### Current status (2026-07-22 run)

`python3 tests/shared-content-parity.py` → **PASS** against
`SharedContent/` at HEAD of `squad/android-port`. All 70 saints in lockstep.
No action needed from Samwise.

#### Requested of others

- **Gandalf:** Ratify or amend the invariant list above. In particular,
  confirm whether `country` should be canonical (currently excluded —
  we allow localized country names like "Italia" vs "Italy").
- **Samwise:** Run the script before any PR touching `SharedContent/`.
  If it fails, fix the data — do not weaken the script.
- **Aragorn:** No action. Continue implementing Phases 2–7.
- **Gandalf (CI):** When ready, flip `if: false` in
  `.github/workflows/android-ci.yml` to gate PRs on this check.

---

## Archived Decisions (older than 2026-03-22)

See `decisions-archive.md` for foundational iOS architecture decisions from 2026-04-12 to 2026-04-13.
