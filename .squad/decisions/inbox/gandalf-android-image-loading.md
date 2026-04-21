# Decision: Coil 3 for Image Loading from Assets

**Author:** Gandalf (Lead)  
**Date:** 2026-07-22  
**Status:** Decided

## Decision

Use Coil 3 (`io.coil-kt.coil3:coil-compose:3.1.0`) for loading saint images from `assets/SharedContent/images/`. Use `file:///android_asset/` URI scheme — Coil resolves this natively without a custom fetcher.

## Rationale

- Coil 3 is Compose-first, Kotlin-first, and lightweight.
- `file:///android_asset/` is a standard Android URI scheme that Coil handles out of the box.
- Same images serve both platforms from `SharedContent/images/`.
- Crossfade transitions and caching come for free.

## Impact

- **Aragorn:** Use `AsyncImage` with `file:///android_asset/SharedContent/images/{filename}` model.
- **Samwise:** No change — image filenames match saint `id`.
