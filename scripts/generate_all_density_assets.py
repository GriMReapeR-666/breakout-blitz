import math
import os
import sys
from PIL import Image, ImageDraw, ImageFont

sys.stdout.reconfigure(encoding='utf-8')

def create_tactical_candy_icon(size: int, is_round: bool = False) -> Image.Image:
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)

    center = size / 2.0
    radius = size * 0.46

    # 1. Background
    if is_round:
        draw.ellipse(
            [center - radius, center - radius, center + radius, center + radius],
            fill=(13, 17, 23, 255),
            outline=(33, 150, 243, 200),
            width=max(2, int(size * 0.015))
        )
    else:
        corner = size * 0.22
        draw.rounded_rectangle(
            [size * 0.04, size * 0.04, size * 0.96, size * 0.96],
            radius=corner,
            fill=(13, 17, 23, 255),
            outline=(33, 150, 243, 200),
            width=max(2, int(size * 0.015))
        )

    # 2. Tactical Grid
    grid_color = (25, 35, 48, 140)
    grid_step = size * 0.12
    for x in range(int(size * 0.1), int(size * 0.9), int(grid_step)):
        draw.line([(x, size * 0.15), (x, size * 0.85)], fill=grid_color, width=1)
    for y in range(int(size * 0.15), int(size * 0.85), int(grid_step)):
        draw.line([(size * 0.1, y), (size * 0.9, y)], fill=grid_color, width=1)

    # 3. Reticle Brackets
    bracket_col = (0, 230, 118, 230)
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

    # 4. Bearish Candle (Candy Magenta)
    c1_x = center - size * 0.16
    c1_w = size * 0.09
    c1_wick_top = size * 0.36
    c1_wick_bot = size * 0.68
    c1_body_top = size * 0.42
    c1_body_bot = size * 0.62
    wick_thick = max(2, int(size * 0.014))
    draw.line([(c1_x, c1_wick_top), (c1_x, c1_wick_bot)], fill=(255, 64, 129, 210), width=wick_thick)
    draw.rounded_rectangle(
        [c1_x - c1_w/2, c1_body_top, c1_x + c1_w/2, c1_body_bot],
        radius=size * 0.02,
        fill=(255, 64, 129, 255),
        outline=(255, 128, 171, 255),
        width=max(1, int(size * 0.01))
    )

    # 5. Bullish Candle (Candy Neon Green)
    c2_x = center + size * 0.12
    c2_w = size * 0.11
    c2_wick_top = size * 0.22
    c2_wick_bot = size * 0.76
    c2_body_top = size * 0.28
    c2_body_bot = size * 0.58
    draw.line([(c2_x, c2_wick_top), (c2_x, c2_wick_bot)], fill=(0, 230, 118, 230), width=wick_thick)
    draw.rounded_rectangle(
        [c2_x - c2_w/2, c2_body_top, c2_x + c2_w/2, c2_body_bot],
        radius=size * 0.025,
        fill=(0, 230, 118, 255),
        outline=(185, 246, 202, 255),
        width=max(1, int(size * 0.012))
    )

    # 6. Target Gold Diamond
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

def create_feature_graphic(width: int = 1024, height: int = 500) -> Image.Image:
    # Google Play Store 1024x500 Feature Graphic
    img = Image.new("RGBA", (width, height), (13, 17, 23, 255))
    draw = ImageDraw.Draw(img)

    # Background gradient & grid
    for x in range(0, width, 40):
        draw.line([(x, 0), (x, height)], fill=(20, 28, 40, 150), width=1)
    for y in range(0, height, 40):
        draw.line([(0, y), (width, y)], fill=(20, 28, 40, 150), width=1)

    # Glow accents
    draw.ellipse([width * 0.6, -100, width * 1.1, height * 0.8], fill=(0, 230, 118, 20))
    draw.ellipse([width * 0.05, height * 0.4, width * 0.5, height * 1.2], fill=(255, 64, 129, 20))

    # High-tech candlestick wave across graphic
    base_y = height * 0.55
    points = [
        (60, base_y + 40), (120, base_y + 20), (180, base_y + 60), (240, base_y - 20),
        (300, base_y - 60), (360, base_y - 40), (420, base_y - 100), (480, base_y - 70),
        (540, base_y - 130), (600, base_y - 110), (660, base_y - 160), (720, base_y - 140),
        (780, base_y - 190), (840, base_y - 170), (900, base_y - 220), (960, base_y - 200)
    ]
    # Connect with smooth glowing neon line
    for i in range(len(points) - 1):
        draw.line([points[i], points[i+1]], fill=(0, 230, 118, 200), width=3)

    # Draw candle bodies along the wave
    for i, pt in enumerate(points[::2]):
        is_bull = (i % 2 == 0)
        c_col = (0, 230, 118, 240) if is_bull else (255, 64, 129, 240)
        c_x, c_y = pt
        draw.line([(c_x, c_y - 30), (c_x, c_y + 30)], fill=c_col, width=2)
        draw.rounded_rectangle([c_x - 10, c_y - 18, c_x + 10, c_y + 18], radius=4, fill=c_col)

    # Tactical HUD Overlay Text
    draw.text((80, 80), "BACKTEST BLITZ", fill=(255, 255, 255, 255))
    draw.text((80, 120), "TACTICAL TRADING SIMULATOR · ULTRA-FLUID NATIVE ENGINE", fill=(0, 230, 118, 255))
    draw.text((80, 150), "COMMUNITY TOP INDICATORS · PRO DUEL ARENA · ZERO GAMBLING SIMULATION", fill=(160, 175, 200, 255))

    # Corner brackets
    draw.line([(40, 60), (40, 40), (60, 40)], fill=(0, 230, 118, 255), width=2)
    draw.line([(width - 60, 40), (width - 40, 40), (width - 40, 60)], fill=(0, 230, 118, 255), width=2)
    draw.line([(40, height - 60), (40, height - 40), (60, height - 40)], fill=(0, 230, 118, 255), width=2)
    draw.line([(width - 60, height - 40), (width - 40, height - 40), (width - 40, height - 60)], fill=(0, 230, 118, 255), width=2)

    return img

def main():
    res_base = r"c:\Users\User\OneDrive\Desktop\selfmade game projects\setupcandy\app\src\main\res"
    
    # 1. Density Map for Launcher Icons
    densities = {
        "mipmap-mdpi": 48,
        "mipmap-hdpi": 72,
        "mipmap-xhdpi": 96,
        "mipmap-xxhdpi": 144,
        "mipmap-xxxhdpi": 192
    }

    for folder, sz in densities.items():
        out_d = os.path.join(res_base, folder)
        os.makedirs(out_d, exist_ok=True)
        
        # Standard Squircle Icon
        ico_std = create_tactical_candy_icon(sz, is_round=False)
        p_std = os.path.join(out_d, "ic_launcher.png")
        ico_std.save(p_std, "PNG")
        
        # Round Icon
        ico_round = create_tactical_candy_icon(sz, is_round=True)
        p_round = os.path.join(out_d, "ic_launcher_round.png")
        ico_round.save(p_round, "PNG")
        
        print(f"Generated {folder}: std ({sz}x{sz}), round ({sz}x{sz})")

    # 2. Google Play Store 512x512 Store Listing Icon
    drawable_d = os.path.join(res_base, "drawable")
    os.makedirs(drawable_d, exist_ok=True)
    
    play_icon_512 = create_tactical_candy_icon(512, is_round=False)
    p_play = os.path.join(drawable_d, "play_store_512.png")
    play_icon_512.save(p_play, "PNG")
    print(f"Generated Google Play Store Icon (512x512): {p_play}")

    # 3. Google Play Store 1024x500 Feature Graphic
    feature_graphic = create_feature_graphic(1024, 500)
    p_feat = os.path.join(drawable_d, "feature_graphic_1024x500.png")
    feature_graphic.save(p_feat, "PNG")
    print(f"Generated Google Play Feature Graphic (1024x500): {p_feat}")

if __name__ == "__main__":
    main()
