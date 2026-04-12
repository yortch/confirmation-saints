# Decision: Matching Fields Must Stay English in All Language Files

**Author:** Samwise (Data/Backend)  
**Date:** 2025-07-16  
**Status:** Implemented

## Context

Category browsing was broken — almost every category showed "0 saints" because the ViewModel matches category value IDs (English) against saint data fields. The Spanish saint file had translated matching fields (e.g., `patronOf: ["soldados"]` instead of `["soldiers"]`), so no matches were found.

## Decision

All **matching fields** — `patronOf`, `affinities`, `tags`, `region`, `lifeState`, `ageCategory`, `gender` — must use **English values** in both `saints-en.json` and `saints-es.json`. Only **display fields** (`name`, `biography`, `whyConfirmationSaint`, `quote`, `country`) should be translated.

This is because the ViewModel uses English category IDs for matching, and we agreed not to change the ViewModel or category IDs.

## Impact

- **Samwise (Data):** When adding new saints, always use English for matching fields in both language files.
- **Frodo (iOS):** No code changes needed — matching logic works as designed.
- **All:** Birth dates must use 4-digit year format (e.g., `"0256-01-01"` not `"256"`) for `Int(birthDate.prefix(4))` to parse correctly.

## New Saints Added

- **St. Monica** — fills Africa region, mothers patronage, cooking affinity, early-church era
- **St. Charbel Makhlouf** — fills Middle East region (was previously empty)
