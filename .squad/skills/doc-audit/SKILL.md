# Skill: Documentation Audit vs. Implementation

Use when asked to "review the docs" or "make sure the docs are up to date" — especially before handing a codebase to a new contributor or porting to a new platform.

## Core principle
**Trust the code, not the docs.** Documentation drifts silently. Compare every documented claim against the actual source, data files, and build config. Treat the implementation as ground truth and the docs as a hypothesis.

## Procedure

1. **Inventory in parallel**
   - All doc files: `README.md`, `docs/**`, `**/README.md`, `.github/*.md`, `AGENTS.md`, `CONTRIBUTING.md`, `ARCHITECTURE.md`, `.squad/decisions.md`.
   - All ground-truth sources: build config (`project.yml`, `package.json`, `build.gradle`, etc.), core models, services, and representative data files.

2. **Extract claims from each doc.** For every concrete assertion — feature count, file path, field name, schema, tech version, build command — mark it as a claim to verify.

3. **Verify against the code.** Prioritize checking:
   - **Data schema claims** — load one real JSON/record and diff its shape against what the docs describe. This is where drift is most damaging (breaks new contributors immediately).
   - **Localization / i18n strategy** — these often migrate silently (in-file `{en,es}` objects → per-language files is a common evolution).
   - **Counts and versions** — "50 saints", "Swift 5", "iOS 15+" — cheap to verify, commonly stale.
   - **"How to add X" tutorials** — run through them mentally against the current model; stale ones actively mislead.
   - **File paths and directory layout** — compare against `ls`/`find`.
   - **Build commands** — confirm they still produce the intended output.

4. **Classify each claim** as: ✅ accurate / 🔄 stale / ❌ obsolete / ➕ missing.

5. **Make surgical edits.** Rewrite stale sections; do not rewrite accurate ones. For each change note *what was wrong* in the commit message — this is the audit's real deliverable.

6. **Add what's missing** only if required by context (e.g. shipped status, cross-platform port guide, data-model section that never existed).

## Red flags to scan for proactively

- Type names in docs that no longer exist in code (`grep -r TheTypeName src/`).
- Field names in example JSON that don't appear in the model struct.
- Counts hardcoded in docs ("50 saints", "3 languages") — always re-count from data files.
- Commands prefixed with the wrong working directory after a repo restructure.
- "Future plans" that were already shipped.
- Old product/display names after a rename.

## Output

- A commit (or PR) with surgical doc edits.
- A plain-English summary listing *what was stale and why*, per file — this is what the requester actually wants to read.
- A flag list of any code-level staleness noticed in passing (do not fix as part of the audit; route to the right owner).

## When to add new docs vs. edit existing

- **Edit** when the doc exists and covers the topic but is wrong.
- **Add a new section** when the current state introduces a concept the docs never addressed (e.g. "the app is now shipped", "here's the cross-platform port guide").
- **Flag as obsolete** (don't silently delete) when an entire document no longer describes reality — removal is a decision for the Lead, not the auditor.
