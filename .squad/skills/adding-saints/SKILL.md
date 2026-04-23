# Skill: Adding Saints to the Roster

## When to use

Jorge asks to add N saints to the Confirmation Saints app, or to expand the roster with a themed batch (e.g. "add more Doctors", "add more modern saints"). Applies to any edit that changes the saint count.

## Pattern

Every addition is **9 linked changes**, not 2. Missing any one breaks a build, a test, or a CI parity check.

### 1. Diff against existing roster first

```bash
python3 -c "import json; print([s['id'] for s in json.load(open('SharedContent/saints/saints-en.json'))['saints']])"
```

The user's list may include saints you already have. Never assume the delta is the full requested list.

### 2. Add EN + ES entries (canonical English values in both)

Per decision "SharedContent/ is the Canonical Cross-Platform Data Source" (`.squad/decisions.md`):

- **Canonical fields (English in BOTH files):** `patronOf`, `affinities`, `tags`, `region`, `lifeState`, `ageCategory`, `gender`
- **Display fields (Spanish in ES file):** `name`, `country`, `quote`, `biography`, `whyConfirmationSaint`, `displayPatronOf`, `displayTags`, `displayAffinities`
- **Shared IDs:** same `id` across both files; same `sourceURLs` values
- `canonizationDate: null` for pre-congregation saints (apostles, early-church) AND for Blessed entries
- `birthDate` / `deathDate` use 4-digit zero-padded years (`"0088-01-01"`, not `"88-01-01"`) — Android parsing treats shorter strings as octal

### 3. Controlled vocabulary: reuse existing tag/affinity values

Before inventing a tag, check what's already in use:

```bash
python3 -c "import json; d=json.load(open('SharedContent/saints/saints-en.json')); print(sorted({t for s in d['saints'] for t in s.get('tags',[])}))"
```

Specific conventions:
- **Doctor of the Church:** tag `"Doctor of the Church"` (capitalized, with spaces). Spanish `displayTag`: `"Doctor de la Iglesia"` or `"Doctora de la Iglesia"` (gendered).
- **Blessed (not canonized):** name prefix `"Bl."` (EN) / `"Bta."` (ES); tag `"Blessed"` (capital B); `canonizationDate: null`.
- **Evangelist:** tag `"evangelist"` (lowercase).
- **Apostolic era:** no explicit "apostolic_era" tag in use — instead use `"apostle"` + `"biblical"` or `"early-church"`.

### 4. Download an image (≤ 400 px wide from Wikimedia Commons)

Use or extend `_download_saint_images.py`. Steps:

1. Add `<saint-id> → <Commons filename>` entry to `SAINT_IMAGES` dict.
2. Candidate-list strategy: try several likely filenames in priority order.
3. Fall back to Commons search API:
   ```
   https://commons.wikimedia.org/w/api.php?action=query&list=search&srnamespace=6&srsearch=<query>&format=json
   ```
4. Always inspect `extmetadata.LicenseShortName` — modern saints (post-1955 death) rarely have PD portraits.
5. File name MUST be `<saint-id>.jpg` even if source is PNG (existing convention).

### 5. Set attribution honestly

```json
"image": {
  "filename": "<id>.jpg",
  "attribution": "Public domain, via Wikimedia Commons"
}
```

Override when the actual license differs:
- CC BY-SA 4.0 → `"CC BY-SA 4.0, via Wikimedia Commons"` / `"CC BY-SA 4.0, vía Wikimedia Commons"`
- CC0 → `"CC0, via Wikimedia Commons"` / `"CC0, vía Wikimedia Commons"`

### 6. Update the Android hardcoded count test

`android/app/src/test/java/com/yortch/confirmationsaints/data/SaintRepositoryTest.kt` has:

```kotlin
fun should_return_exactly_<N>_saints_for_each_language()
```

Update: method name, doc comment, assertions (`enSaints.size == N`, `esSaints.size == N`), and `android/app/src/test/README.md`. Grep for the old count across `android/app/src/test/` to catch all occurrences.

### 7. Run parity guard

```bash
python3 tests/shared-content-parity.py
```

Must exit 0 before committing. Catches: mismatched ids, canonical-field drift, sourceURL drift, missing image files.

### 8. Smoke-test both platforms

```bash
cd ios && xcodebuild -project CatholicSaints.xcodeproj -scheme CatholicSaints \
  -destination 'platform=iOS Simulator,name=iPhone 17' build
cd android && ./gradlew :app:assembleDebug :app:testDebugUnitTest
```

Both must succeed.

### 9. Commit hygiene

- One or two focused commits on a `squad/add-saints-*` branch
- Required trailer: `Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>`
- **Do not push, do not open a PR** — Jorge decides when.

## Pitfalls Observed

- **Octal-unsafe 3-digit years:** `"088-01-01"` parses as year 72 on Android. Always 4 digits.
- **Invented tag values:** splits categories silently — a saint tagged `"writers"` (plural) won't match a filter on `"writer"`. Reuse existing vocabulary.
- **Assumed PD:** 20th-century saints rarely have PD portraits. Statue/altar photos licensed CC0 / CC BY-SA 4.0 are the honest fallback.
- **Forgot the Android test:** build fails with `AssertionError at SaintRepositoryTest.kt:53`. Always update the hardcoded count.
- **Duplicate in request list:** the user's list may overlap the existing roster. Diff before promising a final count.

## Inputs that usually change together

- `SharedContent/saints/saints-en.json`
- `SharedContent/saints/saints-es.json`
- `SharedContent/images/<id>.jpg` (one per new saint)
- `_download_saint_images.py` (mapping entry for reproducibility)
- `android/app/src/test/java/com/yortch/confirmationsaints/data/SaintRepositoryTest.kt`
- `android/app/src/test/README.md`
