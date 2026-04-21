# Decision: SharedContent Copied into Android Assets via Gradle Task

**Author:** Gandalf (Lead)  
**Date:** 2026-07-22  
**Status:** Decided

## Decision

`SharedContent/` (JSON data + images) is copied into `android/app/build/intermediates/shared-content/` at build time via a Gradle `Sync` task. The output directory is registered as an additional `assets.srcDir`. Files land at `assets/SharedContent/saints/`, `assets/SharedContent/categories/`, etc.

## Rationale

- APK must be self-contained (no runtime path to repo root).
- `SharedContent/` at repo root remains the single source of truth.
- Gradle `clean` removes the copy; rebuild restores it.
- No SharedContent files are ever committed under `android/`.

## Impact

- **Aragorn:** Implement the Gradle task in `app/build.gradle.kts`.
- **Samwise:** Continue editing `SharedContent/` as before; Android picks it up automatically.
- **Legolas:** CI builds will include the copy task; no manual step.
- **Gandalf:** Reject any PR that commits JSON/images under `android/`.
