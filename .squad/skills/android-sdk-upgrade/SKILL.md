# Skill: Android SDK Upgrade Checklist

## When to Use
When upgrading Android `compileSdk` or `targetSdk` to a new API level.

## Context
Android SDK upgrades require coordinated updates across multiple dependencies, especially testing frameworks that need SDK-specific runtime support.

## Checklist

### 1. Update Build Configuration
- [ ] Update `compileSdk` in `app/build.gradle.kts`
- [ ] Update `targetSdk` in `defaultConfig`
- [ ] Check if `minSdk` should be updated (usually stable)

### 2. Check Testing Framework Compatibility

#### Robolectric
Robolectric requires explicit SDK support for each API level.

**Error signature:**
```
initializationError FAILED
  java.lang.IllegalArgumentException at RobolectricTestRunner.java:216
    Caused by: java.lang.IllegalArgumentException at DefaultSdkPicker.java:119
```

**Resolution:**
1. Visit [Robolectric releases](https://github.com/robolectric/robolectric/releases)
2. Find the first version that lists your target SDK in release notes
3. Update `robolectric` version in `gradle/libs.versions.toml`

**Example:** SDK 35 requires Robolectric 4.14+

#### Other Test Dependencies
- [ ] Check AndroidX Test library versions
- [ ] Check Compose test library versions (usually managed by BOM)
- [ ] Verify JUnit version compatibility

### 3. Update Gradle Plugin Versions
- [ ] Check Android Gradle Plugin (AGP) compatibility with new SDK
- [ ] Verify Kotlin version supports new SDK features
- [ ] Check KSP plugin compatibility

### 4. Review Deprecated APIs
- [ ] Scan build output for deprecation warnings
- [ ] Check Android release notes for breaking changes
- [ ] Update code using deprecated APIs (can be deferred if warnings only)

### 5. Verification Steps
```bash
# Clean build
./gradlew clean

# Run unit tests
./gradlew testDebugUnitTest

# Run instrumented tests (if available)
./gradlew connectedDebugAndroidTest

# Build release APK
./gradlew assembleRelease
```

### 6. CI/CD Updates
- [ ] Verify CI uses updated SDK in build environment
- [ ] Check if Docker images need SDK updates
- [ ] Update GitHub Actions workflow Android setup versions if specified

## Common Issues

### Issue: Robolectric tests fail with DefaultSdkPicker error
**Cause:** Robolectric version doesn't support the new SDK  
**Fix:** Upgrade Robolectric to compatible version

### Issue: Compose tests fail with version conflicts
**Cause:** Compose BOM might not support new SDK yet  
**Fix:** Check Compose BOM release notes, may need to wait for compatible BOM

### Issue: Build succeeds but runtime crashes on new APIs
**Cause:** Using new SDK APIs without runtime checks  
**Fix:** Add `@RequiresApi` annotations and runtime SDK_INT checks

## Version Support Timeline Pattern

Android frameworks typically follow this support timeline:
1. **Android SDK released** (e.g., SDK 35)
2. **AGP support** (usually within 1-2 months)
3. **Robolectric support** (typically 2-4 months after SDK)
4. **Compose support** (varies, usually aligned with AGP)

Plan upgrades accordingly: don't upgrade to bleeding-edge SDK until test frameworks catch up.

## Files Typically Modified

```
android/
├── gradle/libs.versions.toml        # Version catalog updates
├── build.gradle.kts                 # AGP version
└── app/
    ├── build.gradle.kts             # compileSdk, targetSdk, test config
    └── src/
        └── test/                    # May need @Config annotations
```

## Historical Examples from This Project

### SDK 35 Upgrade (2026-04-25)
- **Change:** compileSdk 34 → 35, targetSdk 34 → 35
- **Issue:** Robolectric 4.13 incompatible with SDK 35
- **Fix:** Upgraded Robolectric 4.13 → 4.16.1
- **Commit:** `003aa49`
- **Tests affected:** CategoryMatchingTest, SaintRepositoryTest (all Robolectric tests)
- **Result:** Android unit tests pass

## References
- [Android API Levels](https://apilevels.com/)
- [Robolectric Releases](https://github.com/robolectric/robolectric/releases)
- [AGP Release Notes](https://developer.android.com/build/releases/gradle-plugin)
