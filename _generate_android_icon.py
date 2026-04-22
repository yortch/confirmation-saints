#!/usr/bin/env python3
"""
Generate Android adaptive-icon assets from the iOS 1024x1024 app icon.

Output:
  android/app/src/main/res/mipmap-{m,h,xh,xxh,xxxh}dpi/
    ic_launcher.png           # legacy 1:1 full-bleed (API <26)
    ic_launcher_round.png     # legacy 1:1 full-bleed (API <26)
    ic_launcher_foreground.png  # adaptive foreground (108dp, 66dp safe zone)
    ic_splash.png             # splash screen icon (288dp, full-bleed)

The adaptive foreground centers the iOS icon at 60% scale so it stays inside
the mask crop (circle / squircle) across all launcher shapes. The 66dp safe
zone of the 108dp canvas ensures no cropping.

The splash icon is full-bleed (no padding) so the logo appears complete on
the system splash screen (androidx.core.splashscreen).

Background is the flat liturgical purple defined in res/values/colors.xml.

Run once: `python3 _generate_android_icon.py` from the repo root.
"""
from __future__ import annotations

from pathlib import Path

from PIL import Image

REPO = Path(__file__).resolve().parent
SRC_ICON = REPO / "ios/CatholicSaints/Resources/Assets.xcassets/AppIcon.appiconset/app-icon-1024.png"
OUT_ROOT = REPO / "android/app/src/main/res"

# Material adaptive-icon canvas is 108dp with a 66dp visible safe zone (circle).
# For a SQUARE icon to fit within a circular mask:
#   - Safe circle diameter: 66dp
#   - Max square side: 66dp / √2 ≈ 46.7dp ≈ 43.2% of 108dp canvas
#   - Use 43% scale for safety margin
# This ensures content survives circle/squircle/rounded-square/teardrop masks.
FOREGROUND_INNER_RATIO = 0.43

# Splash icon size (full-bleed, no mask)
SPLASH_DP = 288

# dp sizes per density bucket for legacy square icons.
LEGACY_DP = 48
DENSITIES = {
    "mdpi": 1.0,
    "hdpi": 1.5,
    "xhdpi": 2.0,
    "xxhdpi": 3.0,
    "xxxhdpi": 4.0,
}


def legacy_size(dp: int, scale: float) -> int:
    return int(round(dp * scale))


def adaptive_foreground(src: Image.Image, px: int) -> Image.Image:
    """Transparent 108dp canvas with the source icon centered at safe-zone scale."""
    canvas = Image.new("RGBA", (px, px), (0, 0, 0, 0))
    inner = int(round(px * FOREGROUND_INNER_RATIO))
    resized = src.resize((inner, inner), Image.LANCZOS)
    offset = ((px - inner) // 2, (px - inner) // 2)
    canvas.paste(resized, offset, resized)
    return canvas


def legacy_icon(src: Image.Image, px: int, rounded: bool) -> Image.Image:
    resized = src.resize((px, px), Image.LANCZOS)
    if not rounded:
        return resized
    mask = Image.new("L", (px, px), 0)
    from PIL import ImageDraw  # lazy import
    ImageDraw.Draw(mask).ellipse((0, 0, px, px), fill=255)
    out = Image.new("RGBA", (px, px), (0, 0, 0, 0))
    out.paste(resized, (0, 0), mask)
    return out


def main() -> None:
    if not SRC_ICON.exists():
        raise SystemExit(f"Missing iOS source icon: {SRC_ICON}")
    src = Image.open(SRC_ICON).convert("RGBA")

    for density, scale in DENSITIES.items():
        out_dir = OUT_ROOT / f"mipmap-{density}"
        out_dir.mkdir(parents=True, exist_ok=True)

        # Legacy icons (API <26)
        legacy_px = legacy_size(LEGACY_DP, scale)
        legacy_icon(src, legacy_px, rounded=False).save(out_dir / "ic_launcher.png")
        legacy_icon(src, legacy_px, rounded=True).save(out_dir / "ic_launcher_round.png")

        # Adaptive foreground: 108dp * density scale, with 60% safe-zone content
        adaptive_px = legacy_size(108, scale)
        adaptive_foreground(src, adaptive_px).save(out_dir / "ic_launcher_foreground.png")

        # Splash icon: full-bleed at 288dp (no adaptive padding)
        splash_px = legacy_size(SPLASH_DP, scale)
        src.resize((splash_px, splash_px), Image.LANCZOS).save(out_dir / "ic_splash.png")

    print("✅ Generated launcher + splash icons for", ", ".join(DENSITIES))
    print("   - ic_launcher.png / ic_launcher_round.png (legacy)")
    print("   - ic_launcher_foreground.png (adaptive, 66dp safe zone)")
    print("   - ic_splash.png (splash screen, full-bleed)")


if __name__ == "__main__":
    main()
