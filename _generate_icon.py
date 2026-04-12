#!/usr/bin/env python3
"""
Generate a 1024x1024 app icon for Confirmation Saints.

Design: Rich liturgical red radial gradient background, prominent white dove
(Holy Spirit) with wings spread upward, subtle golden glow/halo around the
dove, and small Pentecost flame accents.

Renders at 2x (2048x2048) then downscales with LANCZOS for anti-aliasing.
"""

import math
from PIL import Image, ImageDraw, ImageFilter

# --- Configuration ---
FINAL_SIZE = 1024
RENDER_SIZE = FINAL_SIZE * 2  # 2x for anti-aliasing
CENTER = RENDER_SIZE // 2
OUTPUT_PATH = "ios/CatholicSaints/Resources/Assets.xcassets/AppIcon.appiconset/app-icon-1024.png"

# Colors — liturgical Pentecost red palette
RED_DARK = (120, 10, 15)         # Deep edge red
RED_MID = (185, 22, 28)          # Rich confirmation red
RED_BRIGHT = (200, 30, 35)       # Bright center red
GOLD = (218, 175, 62)            # Rich gold
GOLD_LIGHT = (240, 215, 110)     # Light gold
WHITE = (255, 255, 255)
FLAME_ORANGE = (245, 140, 30)    # Flame accent
FLAME_YELLOW = (255, 210, 60)    # Inner flame


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
            t = t * t  # Ease-out for smoother gradient
            pixels[x, y] = lerp_color(inner_color, outer_color, t)


def draw_light_rays(draw, cx, cy, size, color_rgba):
    """Draw subtle rays of light radiating downward from the dove."""
    num_rays = 12
    ray_length = size * 0.95
    ray_width = max(2, int(size * 0.018))
    start_y = cy + size * 0.10  # rays start just below dove body

    for i in range(num_rays):
        # Spread rays in a downward fan (~140 degrees)
        angle = math.pi * 0.15 + (math.pi * 0.70) * (i / (num_rays - 1))
        x_end = cx + ray_length * math.cos(angle)
        y_end = start_y + ray_length * math.sin(angle)
        # Fade alpha toward edges
        center_dist = abs(i - (num_rays - 1) / 2) / ((num_rays - 1) / 2)
        alpha = int(color_rgba[3] * (1.0 - 0.6 * center_dist))
        ray_color = (color_rgba[0], color_rgba[1], color_rgba[2], alpha)
        draw.line([(cx, start_y), (x_end, y_end)],
                  fill=ray_color, width=ray_width)


def draw_dove(draw, cx, cy, size, color):
    """Draw a traditional Holy Spirit descending dove — frontal, symmetrical."""
    # Small compact body — an oval centered slightly below middle
    body_w = size * 0.14
    body_h = size * 0.20
    body_top = cy - body_h * 0.3
    draw.ellipse(
        [cx - body_w, body_top, cx + body_w, body_top + body_h * 2],
        fill=color
    )

    # Head — small circle at the top of the body
    head_r = size * 0.08
    head_cy = body_top - head_r * 0.3
    draw.ellipse(
        [cx - head_r, head_cy - head_r,
         cx + head_r, head_cy + head_r],
        fill=color
    )

    # Beak — small downward-pointing triangle (descending pose)
    beak_len = size * 0.05
    draw.polygon([
        (cx - beak_len * 0.4, head_cy + head_r * 0.6),
        (cx, head_cy + head_r + beak_len),
        (cx + beak_len * 0.4, head_cy + head_r * 0.6),
    ], fill=color)

    # Wings — large, sweeping upward and outward in a wide V/arc
    # Each wing is a smooth polygon curving from body up and outward
    wing_attach_y = body_top + body_h * 0.3  # attach near upper body

    # Left wing
    wing_pts_l = [
        (cx - body_w * 0.5, wing_attach_y),            # inner attach
        (cx - size * 0.12, wing_attach_y - size * 0.08),
        (cx - size * 0.28, wing_attach_y - size * 0.22),
        (cx - size * 0.42, wing_attach_y - size * 0.38),
        (cx - size * 0.52, wing_attach_y - size * 0.48),  # wingtip area
        (cx - size * 0.58, wing_attach_y - size * 0.50),
        (cx - size * 0.62, wing_attach_y - size * 0.48),  # outer tip
        (cx - size * 0.60, wing_attach_y - size * 0.42),
        (cx - size * 0.52, wing_attach_y - size * 0.32),
        (cx - size * 0.40, wing_attach_y - size * 0.20),
        (cx - size * 0.25, wing_attach_y - size * 0.08),
        (cx - body_w * 0.8, wing_attach_y + body_h * 0.4),  # lower attach
    ]
    draw.polygon(wing_pts_l, fill=color)

    # Right wing — perfect mirror
    wing_pts_r = [(2 * cx - px, py) for px, py in wing_pts_l]
    draw.polygon(wing_pts_r, fill=color)

    # Wing feather details — 5 long feather lines per wing for definition
    feather_w = max(2, int(size * 0.010))
    feather_color = color if len(color) == 3 else (
        color[0], color[1], color[2], min(255, color[3] + 20))

    for i in range(5):
        t = 0.25 + i * 0.15
        # Left feathers: lines from mid-wing radiating outward
        start_x = cx - size * (0.08 + t * 0.10)
        start_y = wing_attach_y - size * (0.02 + t * 0.06)
        end_x = cx - size * (0.30 + t * 0.28)
        end_y = wing_attach_y - size * (0.18 + t * 0.26)
        draw.line([(start_x, start_y), (end_x, end_y)],
                  fill=feather_color, width=feather_w)
        # Right feathers — mirror
        draw.line([(2 * cx - start_x, start_y), (2 * cx - end_x, end_y)],
                  fill=feather_color, width=feather_w)

    # Tail — symmetrical fan of feathers descending below the body
    tail_cy = body_top + body_h * 2
    tail_pts = [
        (cx - body_w * 0.6, tail_cy - body_h * 0.2),
        (cx - size * 0.12, tail_cy + size * 0.10),
        (cx - size * 0.06, tail_cy + size * 0.18),
        (cx, tail_cy + size * 0.22),  # center tail tip
        (cx + size * 0.06, tail_cy + size * 0.18),
        (cx + size * 0.12, tail_cy + size * 0.10),
        (cx + body_w * 0.6, tail_cy - body_h * 0.2),
    ]
    draw.polygon(tail_pts, fill=color)


def draw_flame(draw, cx, cy, width, height, outer_color, inner_color):
    """Draw a stylized flame tongue using overlapping tear-drop shapes."""
    # Outer flame
    outer_pts = [
        (cx, cy - height),
        (cx + width * 0.45, cy - height * 0.35),
        (cx + width * 0.35, cy + height * 0.1),
        (cx, cy + height * 0.15),
        (cx - width * 0.35, cy + height * 0.1),
        (cx - width * 0.45, cy - height * 0.35),
    ]
    draw.polygon(outer_pts, fill=outer_color)

    # Inner flame — smaller, brighter
    inner_h = height * 0.55
    inner_w = width * 0.45
    inner_pts = [
        (cx, cy - inner_h),
        (cx + inner_w * 0.4, cy - inner_h * 0.25),
        (cx + inner_w * 0.3, cy + inner_h * 0.15),
        (cx, cy + inner_h * 0.2),
        (cx - inner_w * 0.3, cy + inner_h * 0.15),
        (cx - inner_w * 0.4, cy - inner_h * 0.25),
    ]
    draw.polygon(inner_pts, fill=inner_color)


def main():
    # --- Background: rich red radial gradient ---
    img = Image.new("RGB", (RENDER_SIZE, RENDER_SIZE), RED_DARK)
    draw_radial_gradient(img, (CENTER, CENTER), RENDER_SIZE * 0.75,
                         RED_BRIGHT, RED_DARK)

    # Convert to RGBA for layering
    img = img.convert("RGBA")
    draw = ImageDraw.Draw(img)

    # --- Subtle vignette darkening at edges ---
    vignette = Image.new("RGBA", (RENDER_SIZE, RENDER_SIZE), (0, 0, 0, 0))
    vig_draw = ImageDraw.Draw(vignette)
    for i in range(40, 0, -1):
        alpha = int(3 * i)
        r = int(RENDER_SIZE * 0.52) + i * 8
        vig_draw.ellipse(
            [CENTER - r, CENTER - r, CENTER + r, CENTER + r],
            outline=(0, 0, 0, alpha), width=8
        )
    img = Image.alpha_composite(img, vignette)
    draw = ImageDraw.Draw(img)

    # --- Golden glow behind the dove ---
    glow_layer = Image.new("RGBA", (RENDER_SIZE, RENDER_SIZE), (0, 0, 0, 0))
    glow_draw = ImageDraw.Draw(glow_layer)
    glow_r = int(RENDER_SIZE * 0.28)
    for i in range(50, 0, -1):
        alpha = int(6 * (i / 50))
        r = glow_r + i * 6
        glow_draw.ellipse(
            [CENTER - r, CENTER - r - int(RENDER_SIZE * 0.04),
             CENTER + r, CENTER + r - int(RENDER_SIZE * 0.04)],
            fill=(240, 210, 100, alpha)
        )
    img = Image.alpha_composite(img, glow_layer)
    draw = ImageDraw.Draw(img)

    # --- Thin gold halo circle around the dove area ---
    halo_r = int(RENDER_SIZE * 0.30)
    halo_cy = CENTER - int(RENDER_SIZE * 0.02)
    draw.ellipse(
        [CENTER - halo_r, halo_cy - halo_r,
         CENTER + halo_r, halo_cy + halo_r],
        outline=(218, 175, 62, 60), width=int(RENDER_SIZE * 0.006)
    )

    # --- Light rays radiating from behind the dove ---
    rays_layer = Image.new("RGBA", (RENDER_SIZE, RENDER_SIZE), (0, 0, 0, 0))
    rays_draw = ImageDraw.Draw(rays_layer)
    dove_size = RENDER_SIZE * 0.42
    dove_cy = CENTER - int(RENDER_SIZE * 0.02)
    draw_light_rays(rays_draw, CENTER, dove_cy, dove_size,
                    (255, 235, 160, 45))
    rays_blur = rays_layer.filter(ImageFilter.GaussianBlur(radius=18))
    img = Image.alpha_composite(img, rays_blur)
    img = Image.alpha_composite(img, rays_layer)
    draw = ImageDraw.Draw(img)

    # --- Main dove (white, prominent, centered) ---
    dove_layer = Image.new("RGBA", (RENDER_SIZE, RENDER_SIZE), (0, 0, 0, 0))
    dove_draw = ImageDraw.Draw(dove_layer)
    draw_dove(dove_draw, CENTER, dove_cy, dove_size, WHITE)

    # Slight blur on a copy for soft glow effect, then composite both
    dove_glow = dove_layer.copy()
    dove_glow = dove_glow.filter(ImageFilter.GaussianBlur(radius=12))
    # Reduce glow opacity
    glow_data = dove_glow.split()
    dove_glow = Image.merge("RGBA", (
        glow_data[0], glow_data[1], glow_data[2],
        glow_data[3].point(lambda p: int(p * 0.35))
    ))
    img = Image.alpha_composite(img, dove_glow)
    img = Image.alpha_composite(img, dove_layer)
    draw = ImageDraw.Draw(img)

    # --- Pentecost flame accents (small tongues of fire below/around dove) ---
    flame_layer = Image.new("RGBA", (RENDER_SIZE, RENDER_SIZE), (0, 0, 0, 0))
    flame_draw = ImageDraw.Draw(flame_layer)
    flame_base_y = CENTER + int(RENDER_SIZE * 0.18)
    flame_w = int(RENDER_SIZE * 0.035)
    flame_h = int(RENDER_SIZE * 0.065)

    # Seven flames — recalling the seven gifts of the Holy Spirit
    flame_positions = [
        (CENTER - int(RENDER_SIZE * 0.18), flame_base_y),
        (CENTER - int(RENDER_SIZE * 0.12), flame_base_y + int(RENDER_SIZE * 0.02)),
        (CENTER - int(RENDER_SIZE * 0.06), flame_base_y + int(RENDER_SIZE * 0.03)),
        (CENTER, flame_base_y + int(RENDER_SIZE * 0.035)),
        (CENTER + int(RENDER_SIZE * 0.06), flame_base_y + int(RENDER_SIZE * 0.03)),
        (CENTER + int(RENDER_SIZE * 0.12), flame_base_y + int(RENDER_SIZE * 0.02)),
        (CENTER + int(RENDER_SIZE * 0.18), flame_base_y),
    ]

    for i, (fx, fy) in enumerate(flame_positions):
        # Vary sizes slightly for organic feel
        scale = 0.85 + 0.15 * math.sin(i * 1.2)
        draw_flame(flame_draw, fx, fy, int(flame_w * scale),
                   int(flame_h * scale), FLAME_ORANGE, FLAME_YELLOW)

    img = Image.alpha_composite(img, flame_layer)
    draw = ImageDraw.Draw(img)

    # --- Small gold accent dots in an arc above the dove ---
    arc_r = int(RENDER_SIZE * 0.35)
    arc_cy = CENTER - int(RENDER_SIZE * 0.02)
    dot_r = int(RENDER_SIZE * 0.006)
    for i in range(7):
        angle = math.pi + (math.pi * (i + 1) / 8)
        dx = CENTER + arc_r * math.cos(angle)
        dy = arc_cy + arc_r * math.sin(angle)
        draw.ellipse(
            [dx - dot_r, dy - dot_r, dx + dot_r, dy + dot_r],
            fill=(240, 215, 110, 140)
        )

    # --- Downscale for anti-aliasing ---
    img = img.convert("RGB")
    img = img.resize((FINAL_SIZE, FINAL_SIZE), Image.LANCZOS)

    img.save(OUTPUT_PATH, "PNG")
    print(f"✅ Icon saved to {OUTPUT_PATH}")
    print(f"   Size: {img.size[0]}x{img.size[1]}")


if __name__ == "__main__":
    main()
