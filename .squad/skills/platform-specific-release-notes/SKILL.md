---
name: "platform-specific-release-notes"
description: "Keep mobile store submission notes accurate when iOS and Android releases diverge."
domain: "release-documentation"
confidence: "medium"
source: "observed"
---

## Context

Use when updating App Store Connect or Google Play Console copy for Confirmation Saints, especially when the same shared-content release has platform-specific fixes.

## Patterns

- Keep iOS copy under `docs/appstore/` and Android copy under `docs/android/`.
- Label store docs by platform in the title and version heading.
- Include shared release context, such as saint-count changes, only when it applies to that platform build.
- Add platform-only fixes only to the platform's store notes.

## Examples

- `docs/appstore/submission-info.md` tracks iOS/App Store notes.
- `docs/android/submission-info.md` tracks Android/Google Play notes and can include Android-only fixes like dark-mode onboarding readability.

## Anti-Patterns

- Do not copy every Android bug fix into App Store notes.
- Do not use generic "app release" wording when the submission document is store-specific.
