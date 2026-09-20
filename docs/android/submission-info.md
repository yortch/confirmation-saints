# Google Play Console Submission — Confirmation Saints (Android)

These notes are Android-specific. Keep App Store/iOS submission copy in `docs/appstore/submission-info.md` so platform-only fixes do not accidentally appear in the wrong store listing. Spanish release notes live in `docs/android/submission-info-es.md`.

## Android v1.1.0 Update Notes (pending — not yet uploaded)

This release prepares the v1.1.0 rollout on Google Play. Version metadata: `versionName` 1.1.0 / `versionCode` 5 (see "Version code note" below).

## What's New in v1.1.0

Confirmation Saints now includes 12 new bilingual saint/blessed profiles — including St. Stanley Rother, St. Philip Neri, St. John of Nepomuk, St. Norbert of Xanten, St. Paschal Baylon, St. Ignatius Maloyan, St. José Gregorio Hernández, St. Vincenza Maria Poloni, Bl. María del Carmen Rendiles, Bl. Bartolo Longo, St. María Troncatti, and Bl. Peter To Rot — bringing the library to 118 saints, all available offline in English and Spanish. Eleven new licensed portrait/logo images were added. St. María Troncatti's profile uses the official FMA (Salesian Sisters) canonization logo, used unaltered under FMA's usage authorization. One profile (Bl. Peter To Rot, recently canonized) intentionally ships without an image because no free/CC-licensed portrait could be verified — see `tests/shared-content-parity.py`.

This release also migrates the app to target Android 16 (API level 36) for continued Google Play compliance, with no user-facing behavior change.

### Google Play Release Notes (English)

```
• Added 12 new bilingual saints and blesseds — including St. Stanley Rother, St. Philip Neri, St. John of Nepomuk, and Bl. María del Carmen Rendiles — bringing the library to 118, all available offline in English and Spanish.
• Added new portrait images for several saints.
• Updated for the latest Android compatibility and security improvements.
```

### Version code note

The production release on Google Play is currently `versionName` 1.0.2 / `versionCode` 3 (see table below). Git history shows a prior attempt (`versionCode` 4 / `versionName` 1.0.3, commit `dbde3a7`) was bumped "to fix a Play Console upgrade-path error" and then reverted in commit `5bd946b` because "the released production version on Google Play is 1.0.2 (versionCode 3)". Since Google Play permanently retires any `versionCode` once uploaded to *any* track — even a discarded/rejected draft — `versionCode` 4 cannot be safely assumed to be free. This release uses `versionCode` 5 to avoid a possible collision. **Before uploading, confirm in Play Console → App bundle explorer whether 4 was ever consumed**; if it was never uploaded, `versionCode` 4 may be used instead and this file adjusted accordingly.

---

## Store Listing Highlights

### Short Description

Find your Catholic Confirmation saint. Browse 100+ saints, search by interest, and use English or Spanish offline.

### Full Description

Preparing for Catholic Confirmation? Confirmation Saints helps you find and choose the perfect patron saint for your Confirmation journey.

Browse over 100 Catholic saints with biographies, inspirational quotes, feast days, patron associations, and beautiful images. Search by name or interest, explore categories, and learn why choosing a Confirmation name is a meaningful Catholic tradition.

The Android app is built with native Kotlin and Jetpack Compose, supports English and Spanish, and works completely offline. No accounts, ads, analytics, or tracking.

---

## Android Release Metadata

| Field | Value |
|---|---|
| Version name (pending) | `1.1.0` |
| Version code (pending) | `5` (see "Version code note" above) |
| Version name (current production) | `1.0.2` |
| Version code (current production) | `3` |
| Package | `com.yortch.confirmationsaints` |
| Min SDK | API 26 |
| Target SDK | API 36 (Android 16) |
| Privacy | No data collected |
| Release status | Production — 1.0.2 live on Google Play; 1.1.0 prepared locally, not yet uploaded |
