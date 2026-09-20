---
id: d3f62297-3adf-4ca7-bb96-a84166866ce2
class: POLICY
loadGuidance: [ALWAYS]
title: "Use main merge to trigger Xcode Cloud release build"
author: "Jorge Balderas via Squad Coordinator"
createdAt: 2026-09-20T16:45:54.856Z
metadata: {}
---

### 2026-09-20: Use main merge to trigger Xcode Cloud release build
**By:** Jorge Balderas (via Copilot)
**What:** For this repository's app releases, after release changes are validated and approved, commit them, push the working branch, and merge into `main`. The merge to `main` triggers the Xcode Cloud build used for release; a locally generated iOS archive is not required for release preparation.
**Why:** The project uses Xcode Cloud as its iOS release build pipeline.
