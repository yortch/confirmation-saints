# Decision: Saints Batch 2 — Apostles & Holy Family Convention

**Author:** Samwise (Data/Backend)
**Date:** 2026-07-17
**Status:** Proposed

## Context

Added 18 saints (32 → 50 total), including all 12 apostles + Paul, the Holy Family (Joseph, Joachim, Anne), and two educators (John Bosco, Marcellin Champagnat).

## Decisions

### 1. Pre-Congregation Saints Use `null` canonizationDate
Apostles, Holy Family members, and other pre-congregation saints have `canonizationDate: null` since formal canonization processes didn't exist in their era. This matches the pattern established for archangels and Marian apparitions.

### 2. Approximate Dates for Ancient Saints
Birth/death dates for apostles use approximate years in 4-digit format (e.g., `"0005-01-01"`, `"0064-01-01"`). This follows the existing `prefix(4)` era-matching convention while acknowledging the dates are approximate.

### 3. Apostle ID Convention
Apostle IDs use the pattern `{name}-apostle` (e.g., `peter-apostle`, `paul-apostle`) to distinguish from potential future saints sharing the same name (e.g., Peter vs. Peter Faber). Exceptions: `james-greater`, `james-less`, `jude-thaddeus`, `simon-zealot` — which already have natural distinguishers.

## Impact
- **Frodo (iOS):** 18 new saints in browse/search. No code changes needed — data-driven UI handles them automatically.
- **Legolas (QA):** May want to verify category matching for new saints, especially Middle East region coverage (now 16 saints in that region).
- **Bundle size:** +1.2MB images (18 new JPGs at ~400px width).
