# Android Unit Tests — `src/test/`

JVM unit tests for the Android app. Run with:

```sh
cd android && ./gradlew :app:testDebugUnitTest
```

## Suite

| File | Under test | Phase | Status |
|------|-----------|-------|--------|
| `data/SaintRepositoryTest.kt` | JSON loading, 79-saint roster, EN/ES id parity | 2 | stub |
| `data/CategoryMatchingTest.kt` | Category matching uses English-canonical values regardless of active language | 3 | stub |
| `localization/LocalizationServiceTest.kt` | StateFlow updates, DataStore persistence, AppStrings lookup + fallback | 4 | stub |
| `util/BirthDateParsingTest.kt` | 4-digit year extraction incl. 0256 edge case | 2 | stub |

## Division of labor

**Aragorn** implements Phases 2–7 of the Android port under `src/main/`.
**Legolas** fills in the bodies of these stubs once Aragorn's sources land
and the final method signatures stabilize. Each stub is annotated
`@Disabled(...)` so the suite stays green while the scaffold is in flux —
flip that off as each target API is wired up.

The stubs intentionally reference contracts from `.squad/decisions.md`
(cross-language matching, 79-saint roster, canonical English ids,
in-app language switch) rather than speculative APIs. When filling in
bodies, update the stub to match Aragorn's actual signatures — but do
not weaken the assertions.

## Framework

JUnit 5 (`junit-jupiter`) + Turbine for Flow testing. See
`gradle/libs.versions.toml` and `app/build.gradle.kts`.
