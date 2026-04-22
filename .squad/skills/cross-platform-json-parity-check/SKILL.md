# Skill: Cross-Platform JSON Parity Check

## When to use

A repo has a single source of truth (e.g. `SharedContent/`) consumed by
multiple platforms (iOS + Android), with **parallel per-language JSON files**
that must stay in lockstep on a subset of fields while allowing other fields
to diverge (localized display text, translations).

Symptoms the skill addresses:
- Two files — `foo-en.json`, `foo-es.json` — must agree on ids, on
  canonical identifier fields, and on any "shared key" (e.g. URLs).
- Display/freely-translated fields *should* differ.
- Drift silently causes runtime bugs that only surface on one platform.

## Pattern

1. **Write the guardrail in a platform-neutral language** (Python), not
   XCTest or JUnit. Unit-test frameworks are tied to a platform and imply
   running a full build to check data. Data invariants are
   language-agnostic and should fail fast in CI, in pre-commit, and
   locally with zero platform setup.

2. **Name the canonical key explicitly.** In this repo the canonical key
   is the saint `id`, and the secondary cross-file invariant is the
   set of source URLs. Both must be written down in the script header so
   future contributors understand what the check guarantees.

3. **Enumerate canonical vs display fields.** Canonical (must match):
   `patronOf, affinities, tags, region, lifeState, ageCategory, gender`.
   Display (may differ): `displayPatronOf, displayTags, displayAffinities`,
   plus free text (`name`, `country`, `biography`, `quote`,
   `whyConfirmationSaint`). Hard-code these lists in the script — do not
   infer.

4. **Emit a clear diff, not a boolean.** For each mismatch, print the
   id, the field, and the "only in EN" / "only in ES" set. Collect all
   errors before exiting so one run surfaces every issue.

5. **Include asset-existence checks.** If JSON references filenames
   (images), verify the files exist — drift in a filename is just as
   breaking as drift in a canonical field.

6. **Exit codes:** `0` pass, `1` drift (clear diff printed to stderr),
   `2` setup problem (paths not found). CI can distinguish.

7. **Ownership boundary:** When the script flags drift, *do not* let the
   tooling author silently fix the JSON — surface it via a decision
   inbox entry and hand it to the data owner. Parity tooling and data
   authorship are separate lanes.

## Example

See `tests/shared-content-parity.py` in this repo — ~180 LOC, no
dependencies beyond the Python stdlib, runnable as
`python3 tests/shared-content-parity.py`.

## Signals this skill worked

- Drift is caught at PR time, not at app launch.
- Contributors learn the canonical/display split from reading the script
  header, even before reading the architecture doc.
- Both platforms (iOS + Android) share one guardrail; neither re-implements
  it in its own test framework.
