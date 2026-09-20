# Google Play Console Submission — Confirmation Saints (Android)

These notes are Android-specific. Keep App Store/iOS submission copy in `docs/appstore/submission-info.md` so platform-only fixes do not accidentally appear in the wrong store listing. Spanish release notes live in `docs/android/submission-info-es.md`.

## Android v1.1.1 Update Notes (pending — not yet uploaded)

This release prepares the v1.1.1 rollout on Google Play. Version metadata: `versionName` 1.1.1 / `versionCode` 6 (see "Version code note" below).

## What's New in v1.1.1

- Added a **Rate & Review** action in Settings (between "App Info" and "Onboarding"), using the Google Play In-App Review API with a Play Store fallback link, so users can rate the app without leaving it.
- Corrected St. María Troncatti's profile: her portrait attribution now correctly credits the official FMA (Salesian Sisters) canonization logo and its authorized use, fixing a labeling issue from the prior release.
- General stability, translation-accuracy, and content-quality improvements, including strengthened automated checks that keep English and Spanish saint data in sync.

### Google Play Release Notes (English)

```
• Added a Rate & Review option in Settings so you can quickly leave a review.
• Fixed an attribution/labeling issue on St. María Troncatti's profile image.
• General stability and content-quality improvements.
```

### Version code note

The production release on Google Play is currently `versionName` 1.0.2 / `versionCode` 3 (see table below). Git history shows a prior attempt (`versionCode` 4 / `versionName` 1.0.3, commit `dbde3a7`) was bumped "to fix a Play Console upgrade-path error" and then reverted in commit `5bd946b` because "the released production version on Google Play is 1.0.2 (versionCode 3)". Since Google Play permanently retires any `versionCode` once uploaded to *any* track — even a discarded/rejected draft — `versionCode` 4 cannot be safely assumed to be free. `versionCode` 5 / `versionName` 1.1.0 was prepared locally in a prior cycle (12 new bilingual saint profiles, Android 16/API 36 target) but was **never uploaded to Google Play** — that release was superseded before submission. This release uses `versionCode` 6 / `versionName` 1.1.1, which folds in the 1.1.0 content plus the Rate & Review action and the Troncatti attribution fix, to avoid any possible collision with a discarded draft. **Before uploading, confirm in Play Console → App bundle explorer whether 4 or 5 were ever consumed**; if they were never uploaded, a lower `versionCode` may be reusable and this file adjusted accordingly.

### History: v1.1.0 (prepared locally, never uploaded)

`versionCode` 5 / `versionName` 1.1.0 was fully prepared in a prior cycle but was **not uploaded to Google Play** before this 1.1.1 cycle superseded it. Preserved here for record-keeping:

> Confirmation Saints now includes 12 new bilingual saint/blessed profiles — including St. Stanley Rother, St. Philip Neri, St. John of Nepomuk, St. Norbert of Xanten, St. Paschal Baylon, St. Ignatius Maloyan, St. José Gregorio Hernández, St. Vincenza Maria Poloni, Bl. María del Carmen Rendiles, Bl. Bartolo Longo, St. María Troncatti, and Bl. Peter To Rot — bringing the library to 118 saints, all available offline in English and Spanish. Eleven new licensed portrait/logo images were added. St. María Troncatti's profile uses the official FMA (Salesian Sisters) canonization logo, used unaltered under FMA's usage authorization. One profile (Bl. Peter To Rot, recently canonized) intentionally ships without an image because no free/CC-licensed portrait could be verified — see `tests/shared-content-parity.py`. This release also migrated the app to target Android 16 (API level 36) for continued Google Play compliance, with no user-facing behavior change.
>
> At the time, St. María Troncatti's Spanish `image.attribution` was accidentally left as an exact, untranslated copy of the English text — this is the labeling issue fixed in 1.1.1 above.

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
| Version name (pending) | `1.1.1` |
| Version code (pending) | `6` (see "Version code note" above) |
| Version name (current production) | `1.0.2` |
| Version code (current production) | `3` |
| Package | `com.yortch.confirmationsaints` |
| Min SDK | API 26 |
| Target SDK | API 36 (Android 16) |
| Privacy | No data collected |
| Release status | Production — 1.0.2 live on Google Play; 1.1.0 was prepared locally but never uploaded; 1.1.1 prepared locally, not yet uploaded |
