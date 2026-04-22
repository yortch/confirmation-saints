# Android Compose Instrumentation Testing (Hilt + DataStore aware)

**Owner:** Legolas (QA)
**Confidence:** medium (validated 2026-07-24 — 12 live tests across 3 files
compile clean against Hilt 2.52 + androidx.test:runner 1.6.2 + the patterns
below).
**Scope:** `android/app/src/androidTest/` for this project, but the pattern
generalizes to any Hilt + Compose + DataStore app.

## Decision tree: which test rule to use?

```
Composable under test calls hiltViewModel() or
Activity is @AndroidEntryPoint?
   │
   ├── NO  → createComposeRule()
   │         Host the Composable directly. Provide CompositionLocals
   │         explicitly. Use test-owned callbacks instead of real VMs.
   │         → Fast, no Hilt setup required.
   │
   └── YES → createAndroidComposeRule<MainActivity>() +
             @HiltAndroidTest + @get:Rule(order=0) HiltAndroidRule(this)
             REQUIRES: HiltTestRunner wired as testInstrumentationRunner.
```

Key insight: **a single app can need both.** Pure-UI composables (this repo's
`WelcomeScreen` is a good example — it takes only `onComplete: () -> Unit`)
can be tested cheaply with `createComposeRule()` even in a Hilt codebase.
Don't pay the Hilt tax for composables that don't need it.

## Pattern A — Hilt-free composable test (fastest)

```kotlin
class WelcomeScreenNavigationTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun invokes_onComplete_when_cta_tapped() {
        var completed = false
        composeRule.setContent {
            CompositionLocalProvider(LocalAppLanguage provides AppLanguage.EN) {
                WelcomeScreen(onComplete = { completed = true })
            }
        }
        repeat(3) {
            composeRule.onNodeWithText("Next").performClick()
            composeRule.waitForIdle()
        }
        composeRule.onNodeWithText("Let's Go!").performClick()
        assertTrue(completed)
    }
}
```

Notes:
- Always provide the `LocalAppLanguage` CompositionLocal — the Compose tree
  reads it at root and will crash without a default that matches prod.
- Use `composeRule.waitForIdle()` after any pager/animation trigger before
  the next assertion.
- Assert on stable English literals from `AppStrings`. Avoid resource-id
  lookups — this project deliberately does NOT use `strings.xml` for
  switchable UI text (see `AppStrings.kt` docblock).

## Pattern B — Hilt + MainActivity test (full integration)

Required wiring (one-time, in the app module):

1. Create `HiltTestRunner` in `androidTest/`:
   ```kotlin
   class HiltTestRunner : AndroidJUnitRunner() {
       override fun newApplication(cl: ClassLoader?, name: String?, ctx: Context?) =
           super.newApplication(cl, HiltTestApplication::class.java.name, ctx)
   }
   ```
2. `app/build.gradle.kts`:
   ```kotlin
   defaultConfig {
       testInstrumentationRunner = "com.yortch.confirmationsaints.HiltTestRunner"
   }
   dependencies {
       androidTestImplementation("com.google.dagger:hilt-android-testing:<hilt>")
       kspAndroidTest("com.google.dagger:hilt-android-compiler:<hilt>")
       androidTestImplementation("androidx.test:runner:<runner>")
   }
   ```
3. Test class shape:
   ```kotlin
   @HiltAndroidTest
   class MyFlowTest {
       @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)
       @get:Rule(order = 1) val composeRule = createAndroidComposeRule<MainActivity>()

       @Before fun setUp() = hiltRule.inject()
   }
   ```

## Pattern B-lazy — seeding DataStore BEFORE the Activity exists

**Problem:** `createAndroidComposeRule<MainActivity>()` launches MainActivity
in the rule's `before()` phase — which runs BEFORE `@Before`. If a test needs
`hasSeenWelcome=true` (or `appLanguage=ES`) pre-launch so MainActivity's first
composition lands on the correct screen, `@Before` is too late. The Welcome
pager will flash or the wrong initial destination will render before the
DataStore write propagates, causing flaky assertions.

**Solution:** drop the auto-launching rule. Use `createEmptyComposeRule()`
and drive the Activity manually via `ActivityScenario`:

```kotlin
@HiltAndroidTest
class MyTest {
    @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)
    @get:Rule(order = 1) val composeRule = createEmptyComposeRule()

    @Inject lateinit var prefs: PreferencesRepository

    @Before fun setUp() {
        hiltRule.inject()
        runBlocking {
            // Baseline reset — tests share a HiltTestApplication process and
            // DataStore writes persist across them. Reset prevents order-
            // dependent flakes.
            prefs.setHasSeenWelcome(false)
            prefs.setLanguage(AppLanguage.EN)
        }
    }

    @Test fun seed_true_then_launch() {
        runBlocking { prefs.setHasSeenWelcome(true) }  // before launch
        ActivityScenario.launch(MainActivity::class.java).use {
            composeRule.waitUntil(5_000) { /* stable landmark */ }
            // assertions — Welcome was never composed
        }
    }

    @Test fun recreate_activity() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        try {
            // ... drive UI, mutate state ...
            scenario.recreate()
            composeRule.waitUntil(10_000) { /* post-recreate landmark */ }
        } finally { scenario.close() }
    }
}
```

Why `createEmptyComposeRule()` works: it provides the Compose testing
infrastructure (idling resource, semantics tree access, `waitUntil`) without
owning an Activity. When `ActivityScenario.launch` starts MainActivity, the
compose rule attaches to whichever Compose hierarchy becomes current. Assertions
via `composeRule.onNodeWithText(...)` route into that tree automatically.

`ActivityScenario` is in `androidx.test:core`, pulled in transitively by
`androidx.test:runner` — no extra dependency needed.

## DataStore seeding for instrumentation tests

Two options, pick based on test intent:

**Option 1 — seed via the real repository (integration style, preferred):**
```kotlin
@Inject lateinit var prefs: PreferencesRepository
@Before fun seed() = runBlocking {
    hiltRule.inject()
    prefs.setHasSeenWelcome(false)
}
```
This exercises the real write path and proves the DataStore file is writable
in the test environment. Requires Hilt test harness.

**Option 2 — replace the module with `@TestInstallIn` (isolation style):**
Use when you need a clean DataStore per test. Swap the AppModule's
`provideDataStore` with one rooted at
`InstrumentationRegistry.getInstrumentation().targetContext.filesDir` +
a test-scoped filename, and delete the file in `@After`.

Either way, **do not** try to spin up a separate DataStore in the test JVM
and expect the app process to see it — instrumentation tests run in two
processes.

## Assertion style (this project)

- Prefer `onNodeWithText(...)` — the app uses the English key as the display
  string for `AppLanguage.EN`, so literals in tests double as documentation.
- For Spanish, look up the pair in `AppStrings.spanish` at the top of
  `android/app/src/main/java/com/yortch/confirmationsaints/localization/AppStrings.kt`
  (e.g. "Settings" ↔ "Ajustes").
- For saint rows, use stable name pairs from `SharedContent/saints/saints-{en,es}.json`:
  - `therese-of-lisieux` → "St. Thérèse of Lisieux" / "Santa Teresa de Lisieux"
  - `joan-of-arc`        → "St. Joan of Arc"        / "Santa Juana de Arco"
  - `patrick`            → "St. Patrick"            / "San Patricio"
- Substring match (`onNodeWithText(..., substring = true)`) avoids false
  negatives when the UI wraps or trims a row label.
- **Never** assert on ordering — saint list order is not contractually
  stable across platforms (see `SaintNameComparator.kt`).

## Waiting for DataStore → Flow → Compose recomposition

DataStore writes from `LocalizationService.setLanguage` / `markWelcomeSeen`
launch on `appScope` (or `viewModelScope`), then emit through the StateFlow,
then the `collectAsStateWithLifecycle` call site recomposes. That round trip
is NOT captured by `composeRule.waitForIdle()` — idling only covers the
Compose frame clock, not external coroutine dispatchers.

Correct idiom for post-mutation assertions:

```kotlin
composeRule.onNodeWithText("Español").performClick()
composeRule.waitForIdle()
composeRule.waitUntil(5_000) {
    composeRule.onAllNodesWithText("Ajustes").fetchSemanticsNodes().isNotEmpty()
}
composeRule.onNodeWithText("Ajustes").assertIsDisplayed()
```

The `waitUntil` block polls the semantics tree until the recomposition lands.
5 seconds is generous — typical latency is < 200 ms on an emulator. Use 10 s
for assertions that involve reloading assets (saints-es.json re-parse in
`SaintListViewModel.load`).

## Landmark selection — avoiding ambiguous text

Many strings in this app appear in multiple places at once:
- `"Saints"` → bottom-nav label AND top-bar title on Saints tab.
- `"Settings"` → bottom-nav label AND top-bar title on Settings tab.
- After ES switch: `"Santos"` and `"Ajustes"` get the same duplication.

Guidance:
- For "I am on the Saint List" assertions, prefer the search-field
  placeholder `"Name, interest, country..."` — it's unique to SaintListScreen.
- For "I am on Settings" assertions, prefer the `"Language"` section header
  (or `"Idioma"` in ES).
- When you legitimately need to assert BOTH places recomposed (e.g.,
  "language change propagated across top-bar and nav"), use
  `onAllNodesWithText(...).fetchSemanticsNodes().size >= 2`.

## LazyColumn rows that are below the fold

`onNodeWithText("St. Thérèse of Lisieux")` fails if the row isn't in the
composed viewport — LazyColumn only composes visible items. Two workarounds
that avoid touching production code:

1. **Filter first, assert second.** Type a search query that narrows the list
   to the target saint: `performTextInput("Thérèse")` → the LazyColumn now
   contains exactly one row, guaranteed visible.
2. `performScrollToNode(hasText(..., substring = true))` on a scrollable
   ancestor — requires identifying the LazyColumn among multiple scrollables
   (filter chip row also scrolls). Option 1 is simpler and also exercises the
   filter pipeline.

Never add a `testTag` to a production composable just to support a
scrollable-picker query — the search-filter approach is always available.

## Pitfalls observed in this codebase

- `MainActivity` is `@AndroidEntryPoint`; without `HiltTestRunner` it will
  fail at launch with a cryptic `ClassCastException` on the Application.
- `SaintListScreen` and `SettingsScreen` both use `hiltViewModel()` as a
  default parameter. You **cannot** substitute a hand-built ViewModel
  cleanly without forking the composable — go through Hilt.
- `LocalAppLanguage` has a default (`AppLanguage.EN`), so forgetting to
  provide it in a standalone test silently passes but doesn't exercise the
  real language-propagation path. Always provide it explicitly.
- Pager state lives inside `WelcomeScreen`; you can't drive it from the
  test except via user-visible controls (Next, Skip). That's by design —
  tests should imitate the user.

## Related

- `.squad/skills/cross-platform-json-parity-check/SKILL.md` — language-agnostic
  data invariants; complements the Compose UI layer covered here.
- `.squad/decisions/inbox/legolas-android-instrumentation-tests.md` — current
  gaps (Hilt test runner) blocking full integration coverage.
