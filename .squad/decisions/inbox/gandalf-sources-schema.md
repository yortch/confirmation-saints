# Decision: Sources Schema Refactor

**Date:** 2026-04-23  
**Author:** Gandalf (Lead/Architect)  
**Status:** Ready for implementation  
**Unblocks:** Samwise (data), Frodo (iOS), Aragorn (Android), Legolas (test)

---

## 1. New Schema Shape

Replace `sources: [String]` + `sourceURLs: {String: String}?` with a single ordered array:

```json
"sources": [
  { "name": "Franciscan Media", "url": "https://www.franciscanmedia.org/..." },
  { "name": "Catholic News Agency", "url": "https://www.catholicnewsagency.com/..." }
]
```

**Field names:** `name` and `url` (not `title`/`href` — matches existing usage and is idiomatic).  
**Order:** Display order. Render top-to-bottom as listed.  
**Nullability:** `sources` is required (may be empty `[]`). Each entry must have non-empty `name` and `url`.

---

## 2. Migration Strategy

One-shot rewrite of `saints-en.json` and `saints-es.json`. Match by saint `id` (identical across both files).

### Edge-case handling:

| Scenario | Resolution |
|----------|------------|
| `sources[]` entry **not in** `sourceURLs{}` | **Fail migration** — must be fixed manually first. Do NOT silently drop. |
| `sourceURLs{}` key **not in** `sources[]` | **Fail migration** — orphan URL indicates data error. Fix manually. |
| URL is empty string | **Fail migration** — every source must have non-empty URL. |

**Rationale:** We're shipping 81 saints, ~150 sources. Better to fail fast now than silently lose data. Samwise fixes any mismatches before running migration.

---

## 3. iOS Model Change

In `ios/CatholicSaints/Models/Saint.swift`:

```swift
struct SourceEntry: Codable, Hashable, Sendable {
    let name: String
    let url: String
}

struct Saint: Codable, Identifiable, Hashable, Sendable {
    // ... existing fields ...
    let sources: [SourceEntry]  // ← replaces sources: [String] + sourceURLs: [String: String]?
}
```

Remove the old `sources: [String]` and `sourceURLs: [String: String]?` fields entirely.

---

## 4. Android Model Change

In `android/.../data/model/Saint.kt`:

```kotlin
@Serializable
data class SourceEntry(
    val name: String,
    val url: String,
)

@Serializable
data class Saint(
    // ... existing fields ...
    val sources: List<SourceEntry> = emptyList(),  // ← replaces sources + sourceURLs
)
```

Remove the old `sources: List<String>` and `sourceURLs: Map<String, String>?` fields.

---

## 5. View Render Logic

### iOS (`SaintDetailView.swift` — `sourcesSection`):
```swift
ForEach(saint.sources, id: \.name) { source in
    Link(destination: URL(string: source.url)!) {
        HStack {
            Text(source.name).font(.subheadline)
            Spacer()
            Image(systemName: "arrow.up.right.square").font(.caption)
        }
        .foregroundStyle(.blue)
    }
}
```
No lookup needed — iterate and render directly.

### Android (`SaintDetailScreen.kt` — `SourcesSection`):
```kotlin
saint.sources.forEach { source ->
    Row(Modifier.clickable { uriHandler.openUri(source.url) }.padding(12.dp)) {
        Text(source.name, Modifier.weight(1f), color = MaterialTheme.colorScheme.primary)
        Icon(Icons.Default.OpenInNew, ...)
    }
}
```
No `sourceURLs?.get()` lookup — direct property access.

---

## 6. Integrity Test

**Placement:** `android/app/src/test/java/com/yortch/confirmationsaints/data/SourcesIntegrityTest.kt`  
(Android JVM tests run in CI without emulator; this is where `SaintParsingTest.kt` already lives.)

### Assertions (per saint, both language files):

1. **sources is non-empty** — every saint must cite at least one source.
2. **Each entry has non-empty `name`** — no blank labels.
3. **Each entry has non-empty `url`** — no broken links allowed.
4. **URL is well-formed** — `startsWith("https://")` (all our sources use HTTPS).
5. **Parity check (cross-file):**
   - Same saint IDs exist in both `saints-en.json` and `saints-es.json`.
   - Same number of sources per saint ID.
   - Same URLs per saint ID (order may differ, but set must match — content is language-agnostic).

### Why Android?
Android already has `SaintParsingTest.kt` + Gradle `testDebugUnitTest` that loads `SharedContent/` via copy task. Legolas adds the new test alongside. iOS lacks a JSON-test pattern; adding one is more work with less CI benefit.

---

## 7. Work Decomposition

| Agent | Task | Touches | Depends On |
|-------|------|---------|------------|
| **Samwise** | Migrate `saints-en.json` + `saints-es.json` to new schema | `SharedContent/saints/*.json` | None — start immediately |
| **Frodo** | Update `Saint.swift`, `SaintsFile`, and `sourcesSection` | `ios/.../Models/Saint.swift`, `ios/.../SaintDetailView.swift` | Samwise (needs new schema to decode) |
| **Aragorn** | Update `Saint.kt`, `SaintsFile.kt`, and `SourcesSection` | `android/.../model/Saint.kt`, `android/.../SaintDetailScreen.kt` | Samwise (needs new schema) |
| **Legolas** | Add `SourcesIntegrityTest.kt` with assertions above | `android/.../data/SourcesIntegrityTest.kt` | Samwise (test runs against new data) |

### Parallelism:
- **Samwise starts first** — data migration unblocks everyone else.
- **Frodo + Aragorn + Legolas can start stub work immediately** (model shapes are defined above), but must wait for Samwise's commit before final testing.
- **No file-touch conflicts** — each agent owns separate files.

### Hard sequencing:
1. Samwise commits data changes.
2. Frodo/Aragorn/Legolas pull, complete implementations, run tests.
3. Single merge to `squad/add-saints-80-plus` once all four pass CI.

---

## Summary

This schema change eliminates the mismatch bug class by design. One ordered array of `{name, url}` objects replaces the fragile parallel-array pattern. Migration fails loudly on any inconsistency. The integrity test prevents future regressions.

Total scope: 2 JSON files, 2 model files, 2 view files, 1 test file. Four agents, minimal sequencing friction.
