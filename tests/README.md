# Cross-Platform Tests

Platform-neutral tests that guard shared artifacts (content, schemas) used by
both the iOS and Android apps. These live outside `ios/` and `android/` on
purpose — they should not be tied to XCTest or JUnit.

## `shared-content-parity.py`

Enforces the invariants from the "SharedContent/ is the Canonical
Cross-Platform Data Source" decision (2026-04-21):

- `saints-en.json` and `saints-es.json` contain the same set of saint ids.
- For each saint, the set of source URLs (values of `sourceURLs`) matches
  across EN/ES — URLs are the canonical shared key.
- For each saint, these English-canonical fields are byte-identical across
  EN/ES: `patronOf`, `affinities`, `tags`, `region`, `lifeState`,
  `ageCategory`, `gender`.
- Every saint has a corresponding image at `SharedContent/images/<id>.jpg`.
- `categories-en.json` and `categories-es.json` expose the same group ids
  and the same value ids within each group.

### Run

```sh
python3 tests/shared-content-parity.py
```

Exits `0` on success, `1` on parity drift (with a clear diff), `2` if
`SharedContent/` cannot be located.

Point at a non-default location with:

```sh
python3 tests/shared-content-parity.py --shared-content /some/other/SharedContent
```

### When to run

- Locally before pushing any change that touches `SharedContent/`.
- In CI on every PR (see `.github/workflows/android-ci.yml`, gated until
  Phases 2–7 settle).

### Ownership

- **Samwise** owns the data. If this script flags drift, file an entry in
  `.squad/decisions/inbox/` and hand the repair to Samwise — do **not**
  rewrite the JSON as a side effect of whatever task you were doing.
- **Legolas** owns the guardrail itself (this script + docs).
