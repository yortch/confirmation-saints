# Android Robolectric Unit Tests with Assets

**Type:** Testing Pattern  
**Confidence:** Verified (confirmation-saints Android app)  
**Created:** 2026-04-22  
**Author:** Aragorn (Android Dev)

## Problem

Android local unit tests (JVM, not instrumentation) need to access bundled app assets (e.g., JSON files in `src/main/assets/`). By default, Robolectric cannot read these assets and returns empty lists or file-not-found errors.

## Solution

Enable asset access in Robolectric tests via Android Gradle Plugin configuration:

```kotlin
// app/build.gradle.kts
android {
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}
```

## Pattern: Asset-Backed Repository Test

```kotlin
@RunWith(RobolectricTestRunner::class)
class SaintRepositoryTest {
    private lateinit var context: Context
    private lateinit var repository: SaintRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        val json = Json { ignoreUnknownKeys = true }
        repository = SaintRepository(context, json)
    }

    @Test
    fun should_load_english_saints_list() {
        val saints = repository.loadSaints(AppLanguage.EN)
        assertTrue("EN saints list should not be empty", saints.isNotEmpty())
    }
}
```

**Key APIs:**
- `ApplicationProvider.getApplicationContext()` from androidx.test:core
- `@RunWith(RobolectricTestRunner::class)` enables Android framework simulation
- `context.assets.open("saints-en.json")` works at test runtime if `isIncludeAndroidResources = true`

## Pattern: DataStore StateFlow Testing

Use `kotlinx-coroutines-test` + Turbine for DataStore/StateFlow tests:

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class LocalizationServiceTest {
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var testDataStore: DataStore<Preferences>
    private lateinit var testScope: CoroutineScope

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        val tmpFile = File.createTempFile("test_prefs", ".preferences_pb")
        tmpFile.deleteOnExit()
        testDataStore = PreferenceDataStoreFactory.create { tmpFile }
        
        testScope = CoroutineScope(testDispatcher + Job())
    }

    @After
    fun tearDown() {
        testScope.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun should_update_stateflow_when_language_is_switched() = runTest(testDispatcher) {
        service.language.test {
            val initial = awaitItem()
            service.setLanguage(AppLanguage.ES)
            testDispatcher.scheduler.advanceUntilIdle()
            val updated = awaitItem()
            assertEquals(AppLanguage.ES, updated)
        }
    }
}
```

**Key patterns:**
1. Use `StandardTestDispatcher` for deterministic coroutine execution
2. Create in-memory DataStore via `PreferenceDataStoreFactory.create { tempFile }`
3. Use `testDispatcher.scheduler.advanceUntilIdle()` to complete async work
4. Handle StateFlow `initialValue` emission (might be default before DataStore read)
5. Turbine's `.test { awaitItem() }` simplifies StateFlow testing

## Dependencies

```toml
# gradle/libs.versions.toml
[versions]
robolectric = "4.16.1"  # Supports SDK 35, SDK 36 (upgrade from 4.13 when targeting SDK 35+)
kotlinx-coroutines-test = "1.8.1"
turbine = "1.1.0"

[libraries]
robolectric = { module = "org.robolectric:robolectric", version.ref = "robolectric" }
kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "kotlinx-coroutines-test" }
turbine = { module = "app.cash.turbine:turbine", version.ref = "turbine" }
androidx-test-ext-junit = "androidx.test.ext:junit:1.2.1"
```

```kotlin
// app/build.gradle.kts
dependencies {
    testImplementation(libs.robolectric)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.test.ext.junit)
}
```

## Common Mistakes

### ❌ Mistake 1: Not enabling includeAndroidResources
Tests fail with empty lists or FileNotFoundException when trying to read assets.

**Fix:** Add `testOptions.unitTests.isIncludeAndroidResources = true` to build.gradle.kts.

### ❌ Mistake 2: Using robolectric.properties with wrong manifest path
Robolectric 4.13+ with AGP 8.x auto-detects manifest location. Manual `robolectric.properties` often causes `IllegalArgumentException: manifest not found`.

**Fix:** Remove `robolectric.properties` and rely on AGP's automatic configuration.

### ❌ Mistake 3: Robolectric version doesn't support target SDK
Tests fail with `IllegalArgumentException at DefaultSdkPicker.java:119` when `targetSdk` is higher than Robolectric supports.

**Fix:** Check [Robolectric releases](https://github.com/robolectric/robolectric/releases) for SDK support:
- Robolectric 4.13: supports up to SDK 34
- Robolectric 4.16.1: supports SDK 35 and SDK 36
- When upgrading Android `compileSdk` or `targetSdk`, upgrade Robolectric accordingly

**Alternative:** Add `@Config(sdk = 34)` to test classes to use a lower SDK version (temporary workaround).

### ❌ Mistake 4: Not advancing test dispatcher after async work
StateFlow/DataStore updates happen asynchronously. Tests that don't advance the dispatcher see stale values.

**Fix:** Call `testDispatcher.scheduler.advanceUntilIdle()` after triggering async updates.

### ❌ Mistake 5: Ignoring StateFlow initialValue emission
StateFlow emits `initialValue` immediately, before DataStore read completes. Tests expecting DataStore-persisted value as first emission fail.

**Fix:** Either (1) consume initial emission then await DataStore value, or (2) conditionally check if first emission is default and await second.

## Verification Commands

```bash
# Run all local unit tests
./gradlew :app:testDebugUnitTest

# Run specific test class
./gradlew :app:testDebugUnitTest --tests "*.SaintRepositoryTest"

# Run with info logging to debug Robolectric issues
./gradlew :app:testDebugUnitTest --info | grep -A 20 "Robolectric"
```

## Related Skills

- `android-compose-instrumentation` — for Compose UI tests (androidTest, not JVM unit tests)
- `cross-platform-asset-sync-via-gradle` — how assets are copied from SharedContent/ to app/src/main/assets/

## Real-World Example

From `confirmation-saints/android/app/src/test/java/`:
- `SaintRepositoryTest.kt` — loads 70 saints from assets/saints-en.json via Robolectric
- `CategoryMatchingTest.kt` — verifies cross-language matching uses canonical English values
- `LocalizationServiceTest.kt` — tests DataStore persistence and StateFlow language switching with Turbine

All tests pass with 0 failures after enabling `isIncludeAndroidResources = true`.
