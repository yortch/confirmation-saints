# Decision: kotlinx.serialization Chosen Over Moshi

**Author:** Gandalf (Lead)  
**Date:** 2026-07-22  
**Status:** Decided

## Decision

Use `kotlinx.serialization` (1.7.x) for all JSON parsing in the Android app. Do not use Moshi, Gson, or Jackson.

## Rationale

- First-party Kotlin library with compile-time code generation (no reflection).
- Pairs naturally with type-safe Navigation Compose routes (both use `@Serializable`).
- Multiplatform-ready if KMP is considered later.
- `ignoreUnknownKeys = true` handles future schema additions gracefully.

## Impact

- **Aragorn:** All data classes annotated with `@Serializable`. Plugin: `kotlin-serialization`.
- **Legolas:** Unit tests can use the same `Json` instance for test fixtures.
