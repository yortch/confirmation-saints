# Confirmation Saints — Promo Video (Remotion)

Remotion project for producing promotional videos for the Confirmation Saints
app. Isolated from the iOS / Android app code — nothing in here ships to
users' devices.

## Quickstart

```bash
cd video
npm install
npm start         # Remotion Studio (interactive preview in browser)
npm run render    # Renders MP4 → out/ConfirmationSaintsPromo.mp4
```

## Composition: `ConfirmationSaintsPromo`

- **Size:** 1080×1080 (square, LinkedIn-first)
- **FPS:** 30
- **Duration:** 30s (900 frames)
- **Codec:** H.264, yuv420p

### Scene timeline (Treatment A — "Find Your Saint")

| # | Scene | Frames | Time | What happens |
|---|-------|--------|------|--------------|
| 1 | `HookScene` | 0–120 | 0–4.0s | Red radial burst. Hook: "Preparing for Confirmation?" → Promise: "100+ saints. One is yours." |
| 2 | `MosaicScene` | 120–420 | 4.0–14.0s | 3-column parallax mosaic of 22 saint portraits with rotating tag chips ("martyr", "young", "mystic"…) |
| 3 | `SaintCardScene` | 420–540 | 14.0–18.0s | Hero card resolves: St. Carlo Acutis — portrait, years, feast day, patronage, quote |
| 4 | `AppTourScene` | 540–810 | 18.0–27.0s | Phone frame showing saint-detail screenshot + captions ("Bios. Quotes. Feast days. Offline."), then triptych of list/explore/about screens + "English + Español." |
| 5 | `EndCard` | 810–900 | 27.0–30.0s | App icon + wordmark "Confirmation Saints" + "Free · Offline · Bilingual" + App Store & Google Play badges |

Files under `src/scenes/` — edit individually without touching the top-level
`ConfirmationSaintsPromo.tsx` composer.

## Asset sources (all under `public/`)

| Folder | Source | Notes |
|--------|--------|-------|
| `public/saints/` | `SharedContent/images/*.jpg` | 22 portraits. All "Public domain, via Wikimedia Commons" per `SharedContent/saints/saints-en.json`. |
| `public/screenshots/` | `docs/android/phone-screenshot-*.png` | Android screenshots, 1080×2424. |
| `public/icons/app-icon-1024.png` | `ios/CatholicSaints/Resources/Assets.xcassets/AppIcon.appiconset/app-icon-1024.png` | App icon, 1024×1024. |
| `public/icons/app-icon-512.png` | `docs/android/play-store-icon-512.png` | Fallback / smaller icon. |
| `public/badges/app-store-badge.svg` | `https://tools.applemediaservices.com/api/badges/download-on-the-app-store/black/en-us` | Official Apple badge, SVG. |
| `public/badges/google-play-badge.png` | `https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png` | Official Google Play badge, PNG. |

> **Refresh assets:** re-run the `cp` block in `.squad/decisions/inbox/galadriel-video-final.md`
> or re-execute the curl commands above to pull the latest official badges.

## Audio (⚠️ manual step for Jorge)

The composition auto-detects `public/audio/track.mp3`. If it's present, it's
mounted via Remotion's `<Audio>`. If absent, the video renders silent (no
error, no blocker).

**Tone brief:** reverent but modern; uplifting build; warm resolution on the
end card; subtle SFX for mosaic reveal, card transition, final lockup.

### Recommended royalty-free tracks (Jorge to download + drop into `public/audio/track.mp3`)

1. **"Sanctuary"** — Pixabay Music (search "sacred uplifting cinematic")
   → https://pixabay.com/music/search/sacred%20uplifting/ — pick one ~30s, warm piano + strings.
2. **"Divinity"** by Keys of Moon — free with attribution
   → https://soundcloud.com/keysofmoon — ambient strings, reverent build.
3. **YouTube Audio Library** — filter for "Inspirational / Cinematic / No attribution required"
   → https://studio.youtube.com/ → Audio Library. Good safe-bet default.

After downloading: save as `public/audio/track.mp3` (exact path), then
`npm run render`. No code changes needed — the composition picks it up
automatically via the HEAD-check in `ConfirmationSaintsPromo.tsx`.

If SFX are added separately, drop them at `public/audio/sfx-*.mp3` and add
matching `<Audio src startFrom endAt />` entries in the composition.

## Store badges

Fetched directly from the official endpoints above on the first render pass.
Guidelines respected:

- Apple: black "Download on the App Store" badge, rendered at natural aspect
  ratio (no distortion), 72px tall in the 1080×1080 frame.
- Google Play: standard "Get it on Google Play" badge, rendered at natural
  aspect ratio, 72px tall.

**When Android goes live in the Play Store** (currently in review), no changes
needed — the badge already links conceptually to the store listing; audience
just needs to see it in the video.

## Commands

| Command | What it does |
|---------|--------------|
| `npm start` | Opens Remotion Studio (live preview, scrubbing) |
| `npm run render` | Renders to `out/ConfirmationSaintsPromo.mp4` (H.264, ~24MB) |
| `npm run upgrade` | Bumps all `@remotion/*` packages together |

## Project layout

```
video/
├── package.json
├── tsconfig.json
├── remotion.config.ts
├── src/
│   ├── index.ts                       # registerRoot entry
│   ├── Root.tsx                       # Composition registry
│   ├── theme.ts                       # Brand colors + easings
│   ├── data.ts                        # Saint list + hero (Carlo) data
│   ├── ConfirmationSaintsPromo.tsx    # Top-level composition (Series of 5 scenes)
│   └── scenes/
│       ├── HookScene.tsx
│       ├── MosaicScene.tsx
│       ├── SaintCardScene.tsx
│       ├── AppTourScene.tsx
│       └── EndCard.tsx
├── public/                            # Static assets (loaded via staticFile())
│   ├── saints/       (22 JPGs)
│   ├── screenshots/  (4 PNGs)
│   ├── icons/        (app icon 1024 + 512)
│   ├── badges/       (App Store SVG + Google Play PNG)
│   └── audio/        (empty — drop track.mp3 here)
└── out/                               # MP4 renders (gitignored)
```

## Delivery

- **In-progress renders:** `video/out/` (gitignored).
- **Approved-to-ship MP4:** copy to `docs/video/ConfirmationSaintsPromo.mp4`.

## Notes for future agents

- **Keep `@remotion/*` versions in lockstep.** Use `npm run upgrade`
  (`remotion upgrade`), never `npm update` individually.
- **Saint portrait attribution** must be preserved in any derivative work
  (policy in `.squad/decisions.md`). Current video doesn't display individual
  attributions on-screen because all images share the same
  "Public domain, via Wikimedia Commons" line — if we ever use a CC-BY image,
  add attribution in the end-card fine print.
- **Creative decisions** (treatments, scene intent, open questions) are
  tracked in `.squad/decisions/inbox/galadriel-video-*.md`, not this README.
