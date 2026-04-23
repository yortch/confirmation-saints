# 81 Saints + Wikipedia-First Attribution Policy

**Author:** Samwise (Data/Backend)  
**Date:** 2026-04-23  
**Branch:** `squad/add-saints-80-plus`  
**Status:** Implemented ✅

## Summary

Added St. George and St. Mariana de Jesús de Paredes to reach 81 saints total. Audited attribution for all recent additions (9 saints from commit 8f5727a + Frances Cabrini) — all complete. Established **Wikipedia as the primary trusted source** for new saint research in the adding-saints skill documentation.

## Changes Made

### 1. Two New Saints (79 → 81)

**St. George** (feast April 23):
- Early Christian martyr (~280-303 AD), patron of England, soldiers, scouts
- Famous dragon legend represents triumph of good over evil
- Region: Europe (widely venerated, England primary)
- Tags: martyr, soldier, early-church, warrior
- Image: Paolo Uccello painting (PD via Wikimedia Commons)
- Sources: Wikipedia (EN) + Catholic Encyclopedia

**St. Mariana de Jesús de Paredes** (feast May 26):
- "Lily of Quito" — first canonized saint of Ecuador (1950)
- Lived 1618-1645, mystic/penitent/laywoman (Third Order Franciscan)
- Region: Americas (Latin American)
- Tags: mystic, penitent, Third Order, Latin American, virgin
- Image: Colonial portrait (PD via Wikimedia Commons)
- Sources: Wikipedia (EN) + Franciscan Media

Both saints follow established schema:
- English canonical fields (patronOf, tags, affinities, region, lifeState, ageCategory, gender) in **both** EN/ES files
- Spanish display arrays (displayPatronOf, displayTags, displayAffinities) in ES file only
- 4-digit zero-padded birthDate format (`0280-01-01` for St. George)
- `canonizationDate: null` for St. George (pre-congregation martyr)
- `sourceURLs` dictionary with ≥2 sources (Wikipedia primary)
- `imageAttribution` string reflecting actual license

### 2. Attribution Audit — All Complete ✅

Reviewed all 10 potentially missing attributions:

**9 saints from commit 8f5727a (2026-04-23):**
- St. John Neumann — ✅ Franciscan Media + CNA, PD image
- St. Moses the Black — ✅ Franciscan Media + CNA, CC BY-SA 4.0 image
- St. Vladimir of Kiev — ✅ CNA + Franciscan Media, CC BY-SA 4.0 image
- St. Ignatius of Loyola — ✅ Franciscan Media + CNA, PD image
- Bl. Imelda Lambertini — ✅ CNA + Catholic Encyclopedia, PD image
- St. Isidore the Farmer — ✅ Franciscan Media + CNA, PD image
- St. Teresa of Ávila — ✅ Franciscan Media + CNA, PD image
- St. Anthony of Padua — ✅ Franciscan Media + CNA, PD image
- St. Josemaría Escrivá — ✅ CNA + Franciscan Media, CC0 image

**Frances Cabrini (earlier batch):**
- ✅ Franciscan Media + CNA, PD image

**Conclusion:** No gaps. All recently added saints have complete `sourceURLs` and `imageAttribution`.

### 3. Skill Update — Wikipedia-First Policy

Modified `.squad/skills/adding-saints/SKILL.md`:

**NEW: "Trusted Sources (in order)" section:**

1. **Wikipedia (EN + ES articles)** — biographical facts, feast date, patronage, canonization date. Use the **English Wikipedia URL** in `sourceURLs` for both language files (per schema requirement that sourceURLs values must match across languages).

2. **Wikimedia Commons** — images. Check `extmetadata.LicenseShortName` via API. Prefer PD; CC BY-SA acceptable with proper attribution.

3. **Catholic.org / Franciscan Media / vaticannews.va / CNA** — tiebreakers and additional context.

**Emphasized:** Both `sourceURLs` (dictionary) AND `imageAttribution` (string) are **REQUIRED fields** for every saint. Never leave blank.

## Rationale

### Why Wikipedia First?

1. **Consistency:** Wikipedia exists for virtually every canonized saint + most Blessed. Provides standardized biographical structure (birth/death dates, feast day, patronage, canonization date).

2. **Reliability:** Wikipedia articles cite primary sources (Vatican, Catholic Encyclopedia, scholarly works). Peer-reviewed via edit history. EN + ES articles often cross-validate facts.

3. **Accessibility:** Free, multilingual, comprehensive. No paywall or registration.

4. **Image licensing transparency:** Wikimedia Commons has robust license metadata via API (`extmetadata.LicenseShortName`). Eliminates guesswork about PD vs CC licenses.

5. **Already in use:** Both new saints (George, Mariana) used Wikipedia as primary source. Matches existing practice for many recent additions.

### Why Not Wikipedia Only?

- **Spiritual context missing:** Wikipedia biographies are factual but rarely explain *why* a saint is compelling for confirmation candidates. Need Catholic sources (Franciscan Media, CNA) for "whyConfirmationSaint" field and spiritual significance.

- **Image gaps:** Not all saints have PD images on Commons (especially 20th-century). Need fallback to CC BY-SA/CC0 with honest attribution.

## Impact on Future Adds

- **Streamlined research:** Start with Wikipedia EN + ES articles → extract dates, patronage, tags → download Wikimedia image → supplement biography with Franciscan Media/CNA for spiritual angle.

- **Attribution compliance:** Wikipedia URL in `sourceURLs`, Wikimedia license in `imageAttribution`. No more "TODO: add source" placeholders.

- **Quality control:** Cross-check EN/ES Wikipedia articles for date consistency. If they disagree, escalate to Catholic Encyclopedia or Vatican sources.

## Verification

- `python3 tests/shared-content-parity.py` → ✅ PASSED
- `cd android && ./gradlew :app:assembleDebug :app:testDebugUnitTest` → ✅ BUILD SUCCESSFUL, all tests green
- `cd ios && xcodebuild -project CatholicSaints.xcodeproj -scheme CatholicSaints -destination 'platform=iOS Simulator,name=iPhone 17' build` → ✅ BUILD SUCCEEDED

## Files Changed

**Data:**
- `SharedContent/saints/saints-en.json` — added 2 saints
- `SharedContent/saints/saints-es.json` — added 2 saints
- `SharedContent/images/george.jpg` — PD (64KB)
- `SharedContent/images/mariana-de-jesus-de-paredes.jpg` — PD (74KB)
- `_download_saint_images.py` — added 2 image mappings

**Documentation:**
- `.squad/skills/adding-saints/SKILL.md` — Wikipedia-first policy
- `.squad/agents/samwise/history.md` — logged this work

**No test changes needed:** `android/app/src/test/java/.../SaintRepositoryTest.kt` was already updated to expect 81 saints in a prior commit on this branch.

## Team Impact

- **Frodo (iOS):** 2 new saints appear automatically in search/browse. No UI changes.
- **Aragorn (Android):** Build passes. Test already expected 81.
- **Legolas (QA):** Category coverage unchanged. New saints match existing schema.
- **Gandalf (Lead):** No architectural changes. Wikipedia sourcing aligns with open-source ethos.

## Next Steps

1. **Jorge decides:** Merge this branch, or add more saints first?
2. **If merging:** Update app store screenshots/videos to reflect 81 saints (currently say "80+").
3. **Future batches:** Follow Wikipedia-first workflow from updated skill doc.
