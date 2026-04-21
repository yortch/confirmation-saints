# Decision: Hilt for Dependency Injection

**Author:** Gandalf (Lead)  
**Date:** 2026-07-22  
**Status:** Decided

## Decision

Use Hilt (2.54.x) for dependency injection in the Android app. ViewModels use `@HiltViewModel`. `SaintRepository`, `LocalizationService`, and `DataStore` are provided via Hilt modules.

## Rationale

- Standard Android DI choice with first-class Jetpack integration.
- `hilt-navigation-compose` provides `hiltViewModel()` for scoped ViewModel injection in Navigation Compose.
- Single `:app` module means Hilt's simplicity is appropriate (no Dagger component complexity).

## Impact

- **Aragorn:** `@HiltAndroidApp` on Application class, `@AndroidEntryPoint` on Activity, `@HiltViewModel` on ViewModels.
- **Legolas:** Test modules can swap `SaintRepository` for a fake via `@TestInstallIn`.
