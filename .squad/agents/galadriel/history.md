# Galadriel — History

## Core Context

- **Project:** confirmation-saints — a Catholic Confirmation companion app helping candidates (primarily teens, also adults) find a patron saint.
- **User:** Jorge Balderas
- **Primary stack:** iOS (Swift/SwiftUI) and Android (Kotlin/Compose) — shipped. iOS v1.0.1 is live in the App Store; Android is in Google Play review.
- **My stack:** Remotion (React + TypeScript) for promotional video — isolated in a `video/` subdirectory at repo root.
- **Content source of truth:** `SharedContent/` — saint JSON (EN + ES), images, categories. 50 saints. Attribution to Loyola Press, FOCUS, Lifeteen, Ascension Press, Hallow, Catholic Encyclopedia.
- **Existing marketing assets:**
  - iOS: `docs/appstore/` (screenshots, submission info, feature copy)
  - Android: `docs/android/` (play-store-icon-512.png, play-store-feature-graphic-1024x500.png, 5 phone screenshots at 1080×2424)
- **App brand voice:** Preparing for Catholic Confirmation — reverent, accessible, encouraging. Audience is teens first, adults second.

## Learnings

### 2026-04-24 — Remotion scaffold + first treatments
- **Remotion version installed:** `4.0.451` (pinned across all `@remotion/*` packages — they MUST stay in lockstep; use `npm run upgrade` which wraps `remotion upgrade`, never `npm update` individual packages).
- **Scaffold approach:** skipped `npx create-video@latest` (it's interactive and its non-interactive flags keep changing). Hand-wrote `package.json`, `tsconfig.json`, `remotion.config.ts`, `src/index.ts` (registerRoot), `src/Root.tsx` (Composition), and a stub component. Cleaner + deterministic.
- **npm cache gotcha on this machine:** `/Users/jorge/.npm/_cacache/content-v2/sha512/74/` is root-owned (from a past sudo install). Workaround: `npm --cache ./.npm-cache install` inside `video/` — local cache is gitignored. Noted for future installs.
- **Key file paths:**
  - Project root: `video/`
  - Entry: `video/src/index.ts` → `video/src/Root.tsx`
  - Composition: `video/src/ConfirmationSaintsPromo.tsx` (1080×1080, 30fps, 900 frames)
  - Config: `video/remotion.config.ts` (codec=h264, pixelFormat=yuv420p for broad compatibility)
  - Render output: `video/out/` (gitignored)
  - Shippable MP4s: `docs/video/` (when ready)
- **Smoke render worked:** placeholder 30s MP4 rendered in ~30s to 1.6MB. `npx remotion render` on macOS requires no extra Chrome install (Remotion downloads a headless Chromium on first run).
- **Brand colors confirmed from `.squad/decisions.md`:** primary red `#B9161C` (icon gradient), purple accent `#4A148C` (legacy). Using red as the promo hero color.
- **Saint count reality check:** submission-info.md says 81 saints, history doc said 50 — the JSON has grown. Treatments reference **81 saints** to match current marketing copy.
- **Recommended treatment:** Treatment A ("Find Your Saint" — mosaic → saint card → app UI). See `.squad/decisions/inbox/galadriel-video-treatments.md`. Awaiting Jorge's approval + answers to 4 open questions (featured saint choice, wordmark style, music, stores badges).
- **Boundary respected:** did NOT touch iOS/Android/SharedContent. Only added `video/` and a small section to root `.gitignore`.

