# Confirmation Saints — Promo Video (Remotion)

Remotion project for producing promotional videos for the Confirmation Saints app.
Isolated from the iOS/Android app code — nothing in here ships to users' devices.

## Quickstart

```bash
cd video
npm install
npm start         # opens Remotion Studio in the browser (interactive preview)
npm run render    # renders MP4 to out/
```

## Current composition

| ID | Size | FPS | Duration | Purpose |
|----|------|-----|----------|---------|
| `ConfirmationSaintsPromo` | 1080×1080 | 30 | 30s (900 frames) | LinkedIn square promo |

Currently the composition renders a **branded placeholder** (red gradient + title
lockup) until the treatment is approved. See the treatment plan at
`../.squad/decisions/inbox/galadriel-video-treatments.md` — Treatment A is
recommended.

## Render outputs

MP4s render to `video/out/` (gitignored). Finished, shippable MP4s are copied to
`docs/video/` when ready to publish.

## Stack

- **Remotion** `4.0.451` (pinned) — React + TypeScript video framework
- **React** 19
- **TypeScript** 5.8
- **@remotion/google-fonts** for web-safe typographic control

## Project layout

```
video/
├── package.json
├── tsconfig.json
├── remotion.config.ts
├── src/
│   ├── index.ts                       # registerRoot entry
│   ├── Root.tsx                       # Composition registry
│   └── ConfirmationSaintsPromo.tsx    # The 30s square promo
├── out/                               # render output (gitignored)
└── README.md
```

## Assets

- Saint portraits: `../SharedContent/images/` (read-only reference; preserve
  attribution in any overlays per team policy).
- App screenshots: `../docs/android/phone-screenshot-*.png`,
  `../docs/appstore/*.png`.
- Icon / feature graphic: `../docs/android/play-store-icon-512.png`,
  `../docs/android/play-store-feature-graphic-1024x500.png`.

If you reference assets from outside `video/`, copy them into `video/public/`
(Remotion's static asset folder) or use `staticFile()` with files placed there.

## Commands

| Command | What it does |
|---------|--------------|
| `npm start` | Opens Remotion Studio (live preview, scrubbing, prop editing) |
| `npm run render` | Renders `ConfirmationSaintsPromo` to `out/ConfirmationSaintsPromo.mp4` (H.264) |
| `npm run upgrade` | Bumps all Remotion packages together (use `remotion upgrade`, not `npm update`) |

## Notes for future agents

- **Always upgrade Remotion packages together** via `npm run upgrade` — mixed
  versions break the bundler.
- Treatments and creative decisions live in `.squad/decisions/` (team-visible),
  not in this README.
