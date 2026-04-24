# Galadriel — History

## Core Context

- **Project:** confirmation-saints — a Catholic Confirmation companion app helping candidates (primarily teens, also adults) find a patron saint.
- **User:** Jorge Balderas
- **Primary stack:** iOS (Swift/SwiftUI) and Android (Kotlin/Compose) — shipped. iOS v1.0.1 is live in the App Store; Android is in Google Play review.
- **My stack:** Remotion (React + TypeScript) for promotional video — isolated in a `video/` subdirectory at repo root.
- **Content source of truth:** `SharedContent/` — saint JSON (EN + ES), images, categories. **81 saints** (verified 2026-04-24 via `jq`/`python` on `saints-en.json`; matches `docs/appstore/submission-info.md`). Attribution to Loyola Press, FOCUS, Lifeteen, Ascension Press, Hallow, Catholic Encyclopedia.
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

### 2026-04-24 — Treatment A fully built + rendered
- **Final composition** `ConfirmationSaintsPromo` broken into 5 sub-components under `src/scenes/` — HookScene, MosaicScene, SaintCardScene, AppTourScene, EndCard. Composed via Remotion's `<Series>` at the top level. Maintainable: scene timing changes only need `durationInFrames` tweaks in `ConfirmationSaintsPromo.tsx`.
- **Render performance:** 30s @ 1080×1080 @ 30fps rendered in **25 seconds** on Jorge's Mac (M-series, macOS). 24 MB output (H.264, yuv420p). Well under the 5-min budget.
- **Mosaic performance trick:** used 2 vertically stacked copies of each column + modulo-looped offset so the column appears to scroll infinitely without teleporting. Keeps <Img> count stable (48 Imgs total across 3 columns × 8 tiles × 2 copies) and avoids frame-by-frame DOM thrash.
- **Audio handling:** `<Audio>` mounted behind a `useEffect` HEAD-check on `public/audio/track.mp3`. Composition renders silent if the file doesn't exist — no hard dependency, no broken render for the audio-optional case.
- **Font loading:** `@remotion/google-fonts/CormorantGaramond` + `Inter`. Called at module-import time (outside component) so they're ready before first frame.
- **Saint count reconciliation:** Jorge flagged possible "50 vs 81" mismatch. Verified via Python on the JSON: **81 saints** is correct and matches `docs/appstore/submission-info.md`. Video uses "81". Updated Core Context above.
- **Store badge fetch:** Apple via `tools.applemediaservices.com/api/badges/...` (returns SVG directly, no auth). Google Play via `play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png` (direct PNG). Both endpoints are curl-friendly.
- **npm cache workaround still needed** on this machine (root-owned `~/.npm/_cacache/`). Keep using `npm --cache ./.npm-cache` for installs.
- **Carlo Acutis data:** his `id` is `carlo-acutis`, feast day `10-12`, quote "The Eucharist is my highway to heaven." Pulled verbatim into `video/src/data.ts` — if JSON updates, `data.ts` should be resynced (there's no build-time pipe).
- **Deliverables checked in:** `video/out/ConfirmationSaintsPromo.mp4` (24 MB, gitignored per policy). Ready to promote to `docs/video/` once Jorge approves.

