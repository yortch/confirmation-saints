#!/usr/bin/env python3
"""
Generate a 1024x1024 app icon for Confirmation Saints.

Design: Purple-to-indigo radial gradient background, golden halo circle,
white chi-rho symbol, subtle dove silhouette, golden accent dots.

Renders at 2x (2048x2048) then downscales for anti-aliasing.
"""

import math
from PIL import Image, ImageDraw

# --- Configuration ---
FINAL_SIZE = 1024
RENDER_SIZE = FINAL_SIZE * 2  # 2x for anti-aliasing
CENTER = RENDER_SIZE // 2
OUTPUT_PATH = "ios/CatholicSaints/Resources/Assets.xcassets/AppIcon.appiconset/app-icon-1024.png"

# Colors
PURPLE_DARK = (45, 13, 83)       # Deep purple
PURPLE_MID = (75, 25, 130)       # Mid purple
INDIGO = (55, 20, 110)           # Indigo
GOLD = (218, 175, 62)            # Rich gold
GOLD_LIGHT = (240, 210, 100)     # Light gold
WHITE = (255, 255, 255)
WHITE_SEMI = (255, 255, 255, 180)
GOLD_SEMI = (218, 175, 62, 40)


def lerp_color(c1, c2, t):
    """Linear interpolation between two RGB colors."""
    return tuple(int(a + (b - a) * t) for a, b in zip(c1, c2))


def draw_radial_gradient(img, center, radius, inner_color, outer_color):
    """Draw a radial gradient on an RGB image."""
    pixels = img.load()
    cx, cy = center
    for y in range(img.height):
        for x in range(img.width):
            dist = math.sqrt((x - cx) ** 2 + (y - cy) ** 2)
            t = min(dist / radius, 1.0)
            # Ease-out for smoother gradient
            t = t * t
            pixels[x, y] = lerp_color(inner_color, outer_color, t)


def draw_dove(draw, cx, cy, size, color):
    """Draw a stylized dove (Holy Spirit symbol) using simple shapes."""
    # Body - an ellipse tilted slightly
    body_w = size * 0.5
    body_h = size * 0.25
    draw.ellipse(
        [cx - body_w, cy - body_h, cx + body_w, cy + body_h],
        fill=color
    )

    # Left wing - arc upward
    wing_pts = [
        (cx - body_w * 0.3, cy),
        (cx - size * 0.9, cy - size * 0.7),
        (cx - size * 0.5, cy - size * 0.55),
        (cx - body_w * 0.1, cy - body_h * 0.5),
    ]
    draw.polygon(wing_pts, fill=color)

    # Right wing - arc upward (mirror)
    wing_pts_r = [
        (cx + body_w * 0.3, cy),
        (cx + size * 0.9, cy - size * 0.7),
        (cx + size * 0.5, cy - size * 0.55),
        (cx + body_w * 0.1, cy - body_h * 0.5),
    ]
    draw.polygon(wing_pts_r, fill=color)

    # Head - small circle
    head_r = size * 0.12
    draw.ellipse(
        [cx + body_w * 0.6, cy - head_r * 1.5,
         cx + body_w * 0.6 + head_r * 2, cy + head_r * 0.5],
        fill=color
    )

    # Tail feathers
    tail_pts = [
        (cx - body_w * 0.8, cy - body_h * 0.3),
        (cx - size * 0.75, cy + size * 0.1),
        (cx - body_w * 0.5, cy + body_h * 0.5),
    ]
    draw.polygon(tail_pts, fill=color)


def draw_cross(draw, cx, cy, arm_len, thickness, color):
    """Draw a simple cross."""
    half_t = thickness // 2
    # Vertical bar (slightly longer downward)
    draw.rectangle(
        [cx - half_t, cy - arm_len, cx + half_t, cy + arm_len * 1.2],
        fill=color
    )
    # Horizontal bar (slightly above center for traditional proportions)
    bar_y = cy - arm_len * 0.2
    draw.rectangle(
        [cx - arm_len * 0.75, bar_y - half_t,
         cx + arm_len * 0.75, bar_y + half_t],
        fill=color
    )


def draw_chi_rho(draw, cx, cy, size, color, thickness):
    """Draw a Chi-Rho (☧) monogram — overlapping X and P."""
    # The P (Rho) - vertical line with a loop at top
    half_t = thickness // 2

    # Vertical stroke of P
    draw.rectangle(
        [cx - half_t, cy - size * 0.85, cx + half_t, cy + size],
        fill=color
    )

    # P loop (arc at top right) - approximate with ellipse
    loop_r = size * 0.32
    loop_cy = cy - size * 0.55
    # Draw arc as a thick outline
    for r_offset in range(-half_t, half_t + 1):
        draw.arc(
            [cx - r_offset, loop_cy - loop_r,
             cx + loop_r * 2 + r_offset, loop_cy + loop_r],
            start=-90, end=90,
            fill=color, width=thickness
        )

    # X (Chi) - two diagonal lines through center
    x_len = size * 0.7
    offset_y = cy + size * 0.05

    # Diagonal top-left to bottom-right
    for i in range(-half_t, half_t + 1):
        draw.line(
            [(cx - x_len + i, offset_y - x_len),
             (cx + x_len + i, offset_y + x_len)],
            fill=color, width=thickness
        )

    # Diagonal top-right to bottom-left
    for i in range(-half_t, half_t + 1):
        draw.line(
            [(cx + x_len + i, offset_y - x_len),
             (cx - x_len + i, offset_y + x_len)],
            fill=color, width=thickness
        )


def draw_accent_dots(draw, cx, cy, radius, count, dot_r, color):
    """Draw small accent dots in a circle."""
    for i in range(count):
        angle = (2 * math.pi * i / count) - math.pi / 2
        x = cx + radius * math.cos(angle)
        y = cy + radius * math.sin(angle)
        draw.ellipse(
            [x - dot_r, y - dot_r, x + dot_r, y + dot_r],
            fill=color
        )


def main():
    # --- Background: radial gradient ---
    img = Image.new("RGB", (RENDER_SIZE, RENDER_SIZE), PURPLE_DARK)
    draw_radial_gradient(img, (CENTER, CENTER), RENDER_SIZE * 0.75,
                         PURPLE_MID, PURPLE_DARK)

    # Convert to RGBA for layering
    img = img.convert("RGBA")
    draw = ImageDraw.Draw(img)

    # --- Subtle outer glow ring ---
    glow_r = int(RENDER_SIZE * 0.42)
    for i in range(30, 0, -1):
        alpha = int(8 * (i / 30))
        r = glow_r + i * 4
        color = (218, 175, 62, alpha)
        draw.ellipse(
            [CENTER - r, CENTER - r, CENTER + r, CENTER + r],
            outline=color, width=3
        )

    # --- Golden halo circle ---
    halo_r = int(RENDER_SIZE * 0.34)
    halo_width = int(RENDER_SIZE * 0.018)
    draw.ellipse(
        [CENTER - halo_r, CENTER - halo_r,
         CENTER + halo_r, CENTER + halo_r],
        outline=GOLD, width=halo_width
    )

    # Inner thin gold ring
    inner_r = halo_r - int(RENDER_SIZE * 0.025)
    draw.ellipse(
        [CENTER - inner_r, CENTER - inner_r,
         CENTER + inner_r, CENTER + inner_r],
        outline=GOLD_LIGHT, width=int(RENDER_SIZE * 0.005)
    )

    # --- Dove silhouette (subtle, behind cross) ---
    dove_layer = Image.new("RGBA", (RENDER_SIZE, RENDER_SIZE), (0, 0, 0, 0))
    dove_draw = ImageDraw.Draw(dove_layer)
    dove_size = RENDER_SIZE * 0.18
    dove_cy = CENTER - RENDER_SIZE * 0.18
    draw_dove(dove_draw, CENTER, dove_cy, dove_size, (255, 255, 255, 55))
    img = Image.alpha_composite(img, dove_layer)
    draw = ImageDraw.Draw(img)

    # --- Chi-Rho symbol ---
    symbol_size = int(RENDER_SIZE * 0.18)
    symbol_thickness = int(RENDER_SIZE * 0.022)
    draw_chi_rho(draw, CENTER, CENTER + int(RENDER_SIZE * 0.02),
                 symbol_size, WHITE, symbol_thickness)

    # --- Accent dots around halo ---
    dot_radius = halo_r + int(RENDER_SIZE * 0.06)
    draw_accent_dots(draw, CENTER, CENTER, dot_radius, 12,
                     int(RENDER_SIZE * 0.008), GOLD_LIGHT)

    # --- Small cross at very top (above dove) ---
    small_cross_y = CENTER - int(RENDER_SIZE * 0.36)
    small_cross_size = int(RENDER_SIZE * 0.035)
    small_thickness = int(RENDER_SIZE * 0.008)
    draw_cross(draw, CENTER, small_cross_y, small_cross_size,
               small_thickness, GOLD)

    # --- Downscale for anti-aliasing ---
    img = img.convert("RGB")
    img = img.resize((FINAL_SIZE, FINAL_SIZE), Image.LANCZOS)

    img.save(OUTPUT_PATH, "PNG")
    print(f"✅ Icon saved to {OUTPUT_PATH}")
    print(f"   Size: {img.size[0]}x{img.size[1]}")


if __name__ == "__main__":
    main()
