#!/usr/bin/env python3
"""
Generate a 1024x1024 app icon for Confirmation Saints.

Design: Rich liturgical red radial gradient background, white dove in classic
Pentecost style (descending with wings spread wide, facing right), a prominent
single flame of the Holy Spirit above the dove, subtle golden glow/halo, and
gold accent dots.

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
    num_rays = 14
    ray_length = size * 0.95
    ray_width = max(2, int(size * 0.016))
    start_y = cy + size * 0.05

    for i in range(num_rays):
        angle = math.pi * 0.12 + (math.pi * 0.76) * (i / (num_rays - 1))
        x_end = cx + ray_length * math.cos(angle)
        y_end = start_y + ray_length * math.sin(angle)
        center_dist = abs(i - (num_rays - 1) / 2) / ((num_rays - 1) / 2)
        alpha = int(color_rgba[3] * (1.0 - 0.6 * center_dist))
        ray_color = (color_rgba[0], color_rgba[1], color_rgba[2], alpha)
        draw.line([(cx, start_y), (x_end, y_end)],
                  fill=ray_color, width=ray_width)


def draw_dove_pentecost(draw, cx, cy, size, color):
    """Draw a classic Pentecost dove — descending with wings spread wide, facing right.

    Inspired by traditional Holy Spirit iconography: clean iconic silhouette,
    broad graceful wings sweeping upward, body angled in gentle descent.
    """
    s = size  # shorthand

    # Dove center is shifted up slightly so the flame + dove compose well
    dy = cy - s * 0.02

    # --- Body: plump rounded dove body ---
    body_pts = [
        (cx + s * 0.16, dy + s * 0.04),    # front chest
        (cx + s * 0.12, dy - s * 0.02),    # upper chest
        (cx + s * 0.04, dy - s * 0.05),    # neck/back of head
        (cx - s * 0.06, dy - s * 0.04),    # upper back
        (cx - s * 0.14, dy + s * 0.00),    # mid-back
        (cx - s * 0.18, dy + s * 0.06),    # lower back
        (cx - s * 0.16, dy + s * 0.12),    # under-tail
        (cx - s * 0.08, dy + s * 0.15),    # belly back
        (cx + s * 0.02, dy + s * 0.15),    # belly mid
        (cx + s * 0.10, dy + s * 0.12),    # belly front
        (cx + s * 0.15, dy + s * 0.08),    # lower chest
    ]
    draw.polygon(body_pts, fill=color)

    # --- Head: round ---
    head_cx = cx + s * 0.12
    head_cy = dy + s * 0.01
    head_r = s * 0.060
    draw.ellipse(
        [head_cx - head_r, head_cy - head_r,
         head_cx + head_r, head_cy + head_r],
        fill=color
    )

    # --- Beak: small pointed triangle ---
    beak_len = s * 0.050
    draw.polygon([
        (head_cx + head_r * 0.6, head_cy - s * 0.008),
        (head_cx + head_r + beak_len, head_cy + s * 0.006),
        (head_cx + head_r * 0.6, head_cy + s * 0.016),
    ], fill=color)

    # --- Right wing (upper/front): broad, sweeps up-right with curved edge ---
    # The wing is wide — like a fan shape from the body up and outward
    rw_pts = [
        (cx + s * 0.04, dy - s * 0.04),       # attach at upper body
        (cx + s * 0.06, dy - s * 0.08),       # inner leading edge
        (cx + s * 0.10, dy - s * 0.14),
        (cx + s * 0.15, dy - s * 0.20),
        (cx + s * 0.21, dy - s * 0.26),
        (cx + s * 0.28, dy - s * 0.30),       # mid leading edge
        (cx + s * 0.35, dy - s * 0.32),
        (cx + s * 0.42, dy - s * 0.32),       # tip leading edge
        (cx + s * 0.46, dy - s * 0.30),       # wingtip
        (cx + s * 0.44, dy - s * 0.26),       # tip trailing edge
        (cx + s * 0.38, dy - s * 0.20),
        (cx + s * 0.32, dy - s * 0.14),       # mid trailing edge
        (cx + s * 0.26, dy - s * 0.08),
        (cx + s * 0.20, dy - s * 0.03),
        (cx + s * 0.14, dy + s * 0.02),       # trailing edge meets body
        (cx + s * 0.08, dy + s * 0.04),       # back to body
    ]
    draw.polygon(rw_pts, fill=color)

    # --- Left wing (rear/back): broad, sweeps up-left ---
    lw_pts = [
        (cx - s * 0.04, dy - s * 0.03),       # attach at upper back
        (cx - s * 0.06, dy - s * 0.07),
        (cx - s * 0.10, dy - s * 0.13),
        (cx - s * 0.15, dy - s * 0.19),
        (cx - s * 0.21, dy - s * 0.25),
        (cx - s * 0.28, dy - s * 0.29),       # mid leading edge
        (cx - s * 0.35, dy - s * 0.31),
        (cx - s * 0.42, dy - s * 0.31),       # tip area
        (cx - s * 0.46, dy - s * 0.29),       # wingtip
        (cx - s * 0.44, dy - s * 0.25),       # tip trailing edge
        (cx - s * 0.38, dy - s * 0.19),
        (cx - s * 0.32, dy - s * 0.13),
        (cx - s * 0.26, dy - s * 0.07),
        (cx - s * 0.20, dy - s * 0.02),
        (cx - s * 0.14, dy + s * 0.03),       # trailing edge meets body
        (cx - s * 0.08, dy + s * 0.05),       # back to body
    ]
    draw.polygon(lw_pts, fill=color)

    # --- Tail: fan of feathers extending behind-left ---
    tail_pts = [
        (cx - s * 0.16, dy + s * 0.06),       # base top
        (cx - s * 0.24, dy + s * 0.01),
        (cx - s * 0.30, dy - s * 0.01),
        (cx - s * 0.35, dy + s * 0.02),       # tip
        (cx - s * 0.33, dy + s * 0.08),
        (cx - s * 0.28, dy + s * 0.13),
        (cx - s * 0.22, dy + s * 0.14),
        (cx - s * 0.16, dy + s * 0.12),       # base bottom
    ]
    draw.polygon(tail_pts, fill=color)

    # --- Eye ---
    eye_r = s * 0.010
    eye_x = head_cx + head_r * 0.30
    eye_y = head_cy - head_r * 0.10
    draw.ellipse(
        [eye_x - eye_r, eye_y - eye_r,
         eye_x + eye_r, eye_y + eye_r],
        fill=(180, 30, 30, 200)
    )


def draw_large_flame(draw, cx, cy, width, height, outer_color, inner_color):
    """Draw a large prominent Pentecost flame — the central Holy Spirit fire."""
    # Outer flame — teardrop shape
    outer_pts = [
        (cx, cy - height),                          # tip
        (cx + width * 0.15, cy - height * 0.75),
        (cx + width * 0.35, cy - height * 0.45),
        (cx + width * 0.45, cy - height * 0.15),
        (cx + width * 0.40, cy + height * 0.08),
        (cx + width * 0.25, cy + height * 0.15),
        (cx, cy + height * 0.18),                   # base
        (cx - width * 0.25, cy + height * 0.15),
        (cx - width * 0.40, cy + height * 0.08),
        (cx - width * 0.45, cy - height * 0.15),
        (cx - width * 0.35, cy - height * 0.45),
        (cx - width * 0.15, cy - height * 0.75),
    ]
    draw.polygon(outer_pts, fill=outer_color)

    # Inner flame — brighter, narrower
    ih = height * 0.65
    iw = width * 0.50
    inner_pts = [
        (cx, cy - ih),
        (cx + iw * 0.12, cy - ih * 0.70),
        (cx + iw * 0.30, cy - ih * 0.35),
        (cx + iw * 0.38, cy - ih * 0.05),
        (cx + iw * 0.25, cy + ih * 0.15),
        (cx, cy + ih * 0.20),
        (cx - iw * 0.25, cy + ih * 0.15),
        (cx - iw * 0.38, cy - ih * 0.05),
        (cx - iw * 0.30, cy - ih * 0.35),
        (cx - iw * 0.12, cy - ih * 0.70),
    ]
    draw.polygon(inner_pts, fill=inner_color)

    # Core — white-hot center
    ch = height * 0.35
    cw = width * 0.22
    core_pts = [
        (cx, cy - ch),
        (cx + cw * 0.3, cy - ch * 0.3),
        (cx + cw * 0.2, cy + ch * 0.15),
        (cx, cy + ch * 0.2),
        (cx - cw * 0.2, cy + ch * 0.15),
        (cx - cw * 0.3, cy - ch * 0.3),
    ]
    draw.polygon(core_pts, fill=(255, 245, 200, 180))


def main():
    # --- Background: rich red radial gradient ---
    img = Image.new("RGB", (RENDER_SIZE, RENDER_SIZE), RED_DARK)
    draw_radial_gradient(img, (CENTER, CENTER), RENDER_SIZE * 0.75,
                         RED_BRIGHT, RED_DARK)

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
    glow_r = int(RENDER_SIZE * 0.36)
    dove_cy = CENTER - int(RENDER_SIZE * 0.01)
    for i in range(50, 0, -1):
        alpha = int(7 * (i / 50))
        r = glow_r + i * 7
        glow_draw.ellipse(
            [CENTER - r, dove_cy - r,
             CENTER + r, dove_cy + r],
            fill=(240, 210, 100, alpha)
        )
    img = Image.alpha_composite(img, glow_layer)
    draw = ImageDraw.Draw(img)

    # --- Thin gold halo circle around the dove area ---
    halo_r = int(RENDER_SIZE * 0.44)
    draw.ellipse(
        [CENTER - halo_r, dove_cy - halo_r,
         CENTER + halo_r, dove_cy + halo_r],
        outline=(218, 175, 62, 55), width=int(RENDER_SIZE * 0.006)
    )

    # --- Light rays radiating from behind the dove ---
    rays_layer = Image.new("RGBA", (RENDER_SIZE, RENDER_SIZE), (0, 0, 0, 0))
    rays_draw = ImageDraw.Draw(rays_layer)
    dove_size = RENDER_SIZE * 0.62
    draw_light_rays(rays_draw, CENTER, dove_cy, dove_size,
                    (255, 235, 160, 40))
    rays_blur = rays_layer.filter(ImageFilter.GaussianBlur(radius=18))
    img = Image.alpha_composite(img, rays_blur)
    img = Image.alpha_composite(img, rays_layer)
    draw = ImageDraw.Draw(img)

    # --- Main dove (white, classic Pentecost style facing right) ---
    dove_layer = Image.new("RGBA", (RENDER_SIZE, RENDER_SIZE), (0, 0, 0, 0))
    dove_draw = ImageDraw.Draw(dove_layer)
    # Shift dove down slightly to make room for the flame above
    dove_cy_actual = dove_cy + int(RENDER_SIZE * 0.07)
    draw_dove_pentecost(dove_draw, CENTER, dove_cy_actual, dove_size, WHITE)

    # Soft glow effect
    dove_glow = dove_layer.copy()
    dove_glow = dove_glow.filter(ImageFilter.GaussianBlur(radius=18))
    glow_data = dove_glow.split()
    dove_glow = Image.merge("RGBA", (
        glow_data[0], glow_data[1], glow_data[2],
        glow_data[3].point(lambda p: int(p * 0.35))
    ))
    img = Image.alpha_composite(img, dove_glow)
    img = Image.alpha_composite(img, dove_layer)
    draw = ImageDraw.Draw(img)

    # --- Single prominent Pentecost flame above the dove ---
    flame_layer = Image.new("RGBA", (RENDER_SIZE, RENDER_SIZE), (0, 0, 0, 0))
    flame_draw = ImageDraw.Draw(flame_layer)
    flame_cx = CENTER
    flame_cy = dove_cy_actual - int(RENDER_SIZE * 0.22)
    flame_w = int(RENDER_SIZE * 0.13)
    flame_h = int(RENDER_SIZE * 0.18)
    draw_large_flame(flame_draw, flame_cx, flame_cy, flame_w, flame_h,
                     FLAME_ORANGE, FLAME_YELLOW)

    # Soft flame glow
    flame_glow = flame_layer.copy()
    flame_glow = flame_glow.filter(ImageFilter.GaussianBlur(radius=24))
    glow_data = flame_glow.split()
    flame_glow = Image.merge("RGBA", (
        glow_data[0], glow_data[1], glow_data[2],
        glow_data[3].point(lambda p: int(p * 0.4))
    ))
    img = Image.alpha_composite(img, flame_glow)
    img = Image.alpha_composite(img, flame_layer)
    draw = ImageDraw.Draw(img)

    # --- Small gold accent dots in an arc above the dove ---
    arc_r = int(RENDER_SIZE * 0.44)
    dot_r = int(RENDER_SIZE * 0.005)
    for i in range(7):
        angle = math.pi + (math.pi * (i + 1) / 8)
        dx = CENTER + arc_r * math.cos(angle)
        dy = dove_cy + arc_r * math.sin(angle)
        draw.ellipse(
            [dx - dot_r, dy - dot_r, dx + dot_r, dy + dot_r],
            fill=(240, 215, 110, 130)
        )

    # --- Downscale for anti-aliasing ---
    img = img.convert("RGB")
    img = img.resize((FINAL_SIZE, FINAL_SIZE), Image.LANCZOS)

    img.save(OUTPUT_PATH, "PNG")
    print(f"✅ Icon saved to {OUTPUT_PATH}")
    print(f"   Size: {img.size[0]}x{img.size[1]}")


if __name__ == "__main__":
    main()
