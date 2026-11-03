import math
import os
import sys
from PIL import Image, ImageDraw, ImageFilter

sys.stdout.reconfigure(encoding='utf-8')

def create_tactical_candy_icon(size: int, is_round: bool = False) -> Image.Image:
    # 1. Base Image RGBA
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    # Coordinates
    center = size / 2.0
    radius = size * 0.46

    # 2. Background Shape
    if is_round:
        draw.ellipse(
            [center - radius, center - radius, center + radius, center + radius],
            fill=(13, 17, 23, 255),
            outline=(33, 150, 243, 180),
            width=max(2, int(size * 0.015))
        )
    else:
        # Rounded rectangle squircle
        corner = size * 0.22
        draw.rounded_rectangle(
            [size * 0.04, size * 0.04, size * 0.96, size * 0.96],
            radius=corner,
            fill=(13, 17, 23, 255),
            outline=(33, 150, 243, 180),
            width=max(2, int(size * 0.015))
        )

    # 3. Radial Glow & Tactical Grid
    grid_color = (25, 35, 48, 120)
    grid_step = size * 0.12
    for x in range(int(size * 0.1), int(size * 0.9), int(grid_step)):
        draw.line([(x, size * 0.15), (x, size * 0.85)], fill=grid_color, width=1)
    for y in range(int(size * 0.15), int(size * 0.85), int(grid_step)):
        draw.line([(size * 0.1, y), (size * 0.9, y)], fill=grid_color, width=1)

    # 4. Tactical Reticle Brackets (Esports feel)
    bracket_col = (0, 230, 118, 220) # Neon Bull Green
    b_len = size * 0.12
    b_margin = size * 0.16
    b_thick = max(2, int(size * 0.018))
    # Top-Left
    draw.line([(b_margin, b_margin + b_len), (b_margin, b_margin)], fill=bracket_col, width=b_thick)
    draw.line([(b_margin, b_margin), (b_margin + b_len, b_margin)], fill=bracket_col, width=b_thick)
    # Top-Right
    draw.line([(size - b_margin - b_len, b_margin), (size - b_margin, b_margin)], fill=bracket_col, width=b_thick)
    draw.line([(size - b_margin, b_margin), (size - b_margin, b_margin + b_len)], fill=bracket_col, width=b_thick)
    # Bottom-Left
    draw.line([(b_margin, size - b_margin - b_len), (b_margin, size - b_margin)], fill=bracket_col, width=b_thick)
    draw.line([(b_margin, size - b_margin), (b_margin + b_len, size - b_margin)], fill=bracket_col, width=b_thick)
    # Bottom-Right
    draw.line([(size - b_margin - b_len, size - b_margin), (size - b_margin, size - b_margin)], fill=bracket_col, width=b_thick)
    draw.line([(size - b_margin, size - b_margin), (size - b_margin, size - b_margin - b_len)], fill=bracket_col, width=b_thick)

    # 5. Candy Theme Candlestick Bars
    # Bar 1 (Bearish Pullback): Candy Magenta/Purple
    c1_x = center - size * 0.16
    c1_w = size * 0.09
    c1_wick_top = size * 0.36
    c1_wick_bot = size * 0.68
    c1_body_top = size * 0.42
    c1_body_bot = size * 0.62
    wick_thick = max(2, int(size * 0.014))
    draw.line([(c1_x, c1_wick_top), (c1_x, c1_wick_bot)], fill=(255, 64, 129, 200), width=wick_thick)
    draw.rounded_rectangle(
        [c1_x - c1_w/2, c1_body_top, c1_x + c1_w/2, c1_body_bot],
        radius=size * 0.02,
        fill=(255, 64, 129, 255),
        outline=(255, 128, 171, 255),
        width=max(1, int(size * 0.01))
    )

    # Bar 2 (Bullish Ignition Breakout): Electric Candy Lime Green / Emerald
    c2_x = center + size * 0.12
    c2_w = size * 0.11
    c2_wick_top = size * 0.22
    c2_wick_bot = size * 0.76
    c2_body_top = size * 0.28
    c2_body_bot = size * 0.58
    draw.line([(c2_x, c2_wick_top), (c2_x, c2_wick_bot)], fill=(0, 230, 118, 220), width=wick_thick)
    draw.rounded_rectangle(
        [c2_x - c2_w/2, c2_body_top, c2_x + c2_w/2, c2_body_bot],
        radius=size * 0.025,
        fill=(0, 230, 118, 255),
        outline=(185, 246, 202, 255),
        width=max(1, int(size * 0.012))
    )

    # 6. Center Tactical Lightning / Target Diamond
    target_x = center + size * 0.12
    target_y = c2_wick_top
    d_size = size * 0.04
    draw.polygon(
        [(target_x, target_y - d_size), (target_x + d_size, target_y),
         (target_x, target_y + d_size), (target_x - d_size, target_y)],
        fill=(255, 215, 0, 255),
        outline=(255, 255, 255, 255)
    )

    return img

def main():
    out_dir = r"c:\Users\User\OneDrive\Desktop\selfmade game projects\setupcandy\app\src\main\res\mipmap-xxxhdpi"
    os.makedirs(out_dir, exist_ok=True)

    # Standard Square Squircle Icon (192x192)
    icon_std = create_tactical_candy_icon(192, is_round=False)
    p_std = os.path.join(out_dir, "ic_launcher.png")
    icon_std.save(p_std, "PNG")
    print(f"Generated original icon: {p_std} ({os.path.getsize(p_std)} bytes)")

    # Round Maskable Icon (512x512)
    icon_round = create_tactical_candy_icon(512, is_round=True)
    p_round = os.path.join(out_dir, "ic_launcher_round.png")
    icon_round.save(p_round, "PNG")
    print(f"Generated original round icon: {p_round} ({os.path.getsize(p_round)} bytes)")

if __name__ == "__main__":
    main()
