# Android Compose Instrumentation Testing (Hilt + DataStore aware)

**Owner:** Legolas (QA)
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
