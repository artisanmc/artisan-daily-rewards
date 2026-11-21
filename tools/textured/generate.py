"""Draws the images of the `textured` variant (menus for players with the resource pack).

    python3 tools/textured/generate.py

Writes src/main/resources/module/assets/{textures,glyphs}/daily_*.png and their .yml descriptors.
Pillow only.
Vanilla palette (generic_54); no text in the images: the texts are title text layers, translated.
"""
from pathlib import Path

from PIL import Image, ImageDraw

OUT = Path(__file__).resolve().parents[2] / "src/main/resources/module/assets"

CLEAR = (0, 0, 0, 0)
OUTLINE = (0, 0, 0, 255)
VBG = (198, 198, 198, 255)
VHI = (255, 255, 255, 255)
VSH = (85, 85, 85, 255)
VSLOT = (139, 139, 139, 255)
VSLOT_SH = (55, 55, 55, 255)
GOLD = (244, 200, 66, 255)
GOLD_D = (190, 132, 28, 255)
GOLD_H = (255, 238, 150, 255)
W, H = 176, 222


def px(img, x, y, c):
    if 0 <= x < img.width and 0 <= y < img.height:
        img.putpixel((x, y), c)


def rect(img, x0, y0, x1, y1, c):  # inclusive
    ImageDraw.Draw(img).rectangle([x0, y0, x1, y1], fill=c)


def bevel(img, x0, y0, x1, y1, fill, hi, sh):
    rect(img, x0, y0, x1, y1, fill)
    rect(img, x0, y0, x1 - 1, y0, hi)
    rect(img, x0, y0, x0, y1 - 1, hi)
    rect(img, x0 + 1, y1, x1, y1, sh)
    rect(img, x1, y0 + 1, x1, y1, sh)


def slot_xy(slot):
    return 7 + (slot % 9) * 18, 17 + (slot // 9) * 18


def vslot(img, x, y):
    """An 18x18 vanilla slot whose top-left pixel is (x, y)."""
    rect(img, x, y, x + 17, y + 17, VSLOT)
    rect(img, x, y, x + 16, y, VSLOT_SH)
    rect(img, x, y, x, y + 16, VSLOT_SH)
    rect(img, x + 1, y + 17, x + 17, y + 17, VHI)
    rect(img, x + 17, y + 1, x + 17, y + 17, VHI)


def sprite(rows, colors):
    t = Image.new("RGBA", (len(rows[0]), len(rows)), CLEAR)
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch in colors:
                t.putpixel((x, y), colors[ch])
    return t


SNOW = ["...WW...", ".W.BB.W.", "..WBBW..", "WBBWWBBW", "WBBWWBBW", "..WBBW..", ".W.BB.W.", "...WW..."]


def background(freeze_field):
    """The whole window (vanilla_texture: false): panel, the 28 day slots, the player's inventory."""
    img = Image.new("RGBA", (W, H), CLEAR)
    rect(img, 3, 0, W - 4, H - 1, OUTLINE); rect(img, 0, 3, W - 1, H - 4, OUTLINE)
    rect(img, 1, 1, W - 2, H - 2, OUTLINE)
    rect(img, 2, 1, W - 3, H - 2, VBG); rect(img, 1, 2, W - 2, H - 3, VBG)
    rect(img, 3, 1, W - 5, 2, VHI); rect(img, 1, 3, 2, H - 5, VHI); px(img, 3, 3, VHI)
    rect(img, 4, H - 3, W - 3, H - 2, VSH); rect(img, W - 3, 4, W - 2, H - 4, VSH); px(img, W - 4, H - 4, VSH)
    if freeze_field:  # slots 7-8: a vanilla text box with a snowflake; the count is a title text layer
        fx0, fy0 = slot_xy(7)[0], 18
        fx1, fy1 = slot_xy(8)[0] + 17, 33
        rect(img, fx0, fy0, fx1, fy1, (160, 160, 160, 255))
        rect(img, fx0 + 1, fy0 + 1, fx1 - 1, fy1 - 1, OUTLINE)
        img.alpha_composite(sprite(SNOW, {"W": (220, 244, 255, 255), "B": (110, 190, 235, 255)}), (fx0 + 4, fy0 + 4))
    gx0, gy0 = slot_xy(10)
    for r in range(4):
        for c in range(7):
            vslot(img, gx0 + c * 18, gy0 + r * 18)
    for r in range(3):
        for c in range(9):
            vslot(img, 7 + c * 18, 139 + r * 18)
    for c in range(9):
        vslot(img, 7 + c * 18, 197)
    return img


def tile(base, rim, rim_hi, rim_sh):
    t = Image.new("RGBA", (16, 16), CLEAR)
    bevel(t, 1, 1, 14, 14, base, rim_hi, rim_sh)
    ImageDraw.Draw(t).rectangle([0, 0, 15, 15], outline=rim)
    for c in ((0, 0), (15, 0), (0, 15), (15, 15)):
        t.putpixel(c, CLEAR)
    return t


def day_claim():  # a gift waiting: gold tile, red box
    t = tile((255, 222, 110, 255), GOLD_D, GOLD_H, (214, 150, 40, 255))
    d = ImageDraw.Draw(t)
    d.rectangle([4, 7, 11, 12], fill=(206, 50, 46, 255))
    d.rectangle([3, 5, 12, 7], fill=(232, 72, 60, 255))
    d.rectangle([7, 5, 8, 12], fill=(255, 236, 140, 255))
    d.rectangle([3, 5, 12, 5], fill=(250, 110, 90, 255))
    for p in ((5, 3), (6, 4), (10, 3), (9, 4)):
        t.putpixel(p, (232, 72, 60, 255))
    d.rectangle([4, 12, 11, 12], fill=(140, 28, 30, 255))
    return t


def day_claimed():  # done: green check
    t = tile((176, 200, 140, 255), (88, 112, 64, 255), (214, 230, 186, 255), (130, 156, 100, 255))
    for (x, y) in ((4, 8), (5, 9), (6, 10), (7, 9), (8, 8), (9, 7), (10, 6), (11, 5)):
        for dy in (0, 1):
            t.putpixel((x, y + dy), (40, 120, 50, 255))
    return t


def day_today_claimed():  # today, claimed: the check with a gold rim
    t = day_claimed()
    ImageDraw.Draw(t).rectangle([0, 0, 15, 15], outline=GOLD)
    for c in ((0, 0), (15, 0), (0, 15), (15, 15)):
        t.putpixel(c, CLEAR)
    return t


def day_missed():  # a red cross
    t = tile((196, 170, 150, 255), (120, 70, 60, 255), (226, 206, 190, 255), (160, 128, 110, 255))
    for i in range(4, 12):
        for (x, y) in ((i, i), (i, 15 - i)):
            t.putpixel((x, y), (178, 40, 40, 255))
            if x + 1 < 15:
                t.putpixel((x + 1, y), (130, 26, 26, 255))
    return t


def day_freeze():  # covered by a freeze: ice with a snowflake
    t = tile((168, 220, 240, 255), (60, 130, 170, 255), (226, 248, 255, 255), (110, 180, 210, 255))
    for i in range(3, 13):
        t.putpixel((7, i), VHI); t.putpixel((i, 7), VHI)
    for i in range(-3, 4):
        t.putpixel((7 + i, 7 + i), (220, 245, 255, 255)); t.putpixel((7 + i, 7 - i), (220, 245, 255, 255))
    t.putpixel((7, 7), (60, 130, 170, 255))
    return t


def day_before():  # before the first counted day: a faint empty frame
    t = Image.new("RGBA", (16, 16), CLEAR)
    d = ImageDraw.Draw(t)
    d.rectangle([2, 2, 13, 13], outline=(100, 100, 100, 200))
    for x in range(4, 12, 2):
        t.putpixel((x, 8), (100, 100, 100, 180))
    return t


def arrow(left, enabled, w=18, h=36):
    t = Image.new("RGBA", (w, h), CLEAR)
    d = ImageDraw.Draw(t)
    cx, cy = w // 2, h // 2
    hw, hh = min(w, h * 2) // 2 - 3, h // 2 - 3
    if left:
        pts = [(cx + hw // 2, cy - hh), (cx - hw // 2 - 2, cy), (cx + hw // 2, cy + hh)]
    else:
        pts = [(cx - hw // 2, cy - hh), (cx + hw // 2 + 2, cy), (cx - hw // 2, cy + hh)]
    d.polygon(pts, fill=GOLD if enabled else (150, 150, 150, 255), outline=OUTLINE if enabled else (110, 110, 110, 255))
    return t


def close():
    t = Image.new("RGBA", (16, 16), CLEAR)
    for i in range(3, 13):
        for (x, y) in ((i, i), (i, 15 - i)):
            for dx in (0, 1):
                t.putpixel((min(15, x + dx), y), (220, 60, 50, 255))
    for i in range(3, 13):
        t.putpixel((i, i), (255, 120, 100, 255))
    return t


def trophy():
    t = Image.new("RGBA", (16, 16), CLEAR)
    d = ImageDraw.Draw(t)
    d.rectangle([4, 2, 11, 8], fill=GOLD)
    d.rectangle([4, 2, 11, 2], fill=GOLD_H)
    d.rectangle([2, 3, 3, 6], outline=GOLD_D)
    d.rectangle([12, 3, 13, 6], outline=GOLD_D)
    d.rectangle([5, 8, 10, 9], fill=GOLD_D)
    d.rectangle([7, 9, 8, 11], fill=GOLD_D)
    d.rectangle([4, 12, 11, 13], fill=(170, 90, 220, 255))
    d.rectangle([5, 3, 5, 6], fill=GOLD_H)
    return t


def back():  # back to the calendar: a small gold arrow pointing left
    return arrow(True, True, 16, 16)


def claim(active):
    """The claim button over 3 slots (54x18): vanilla-style, green or greyed. Its label is a title text."""
    t = Image.new("RGBA", (54, 18), CLEAR)
    if active:
        fill, hi, sh = (60, 133, 39, 255), (108, 192, 76, 255), (30, 82, 20, 255)
    else:
        fill, hi, sh = (112, 112, 112, 255), (150, 150, 150, 255), (74, 74, 74, 255)
    d = ImageDraw.Draw(t)
    d.rectangle([1, 1, 52, 16], fill=OUTLINE)
    d.rectangle([2, 2, 51, 15], fill=fill)
    d.rectangle([2, 2, 51, 3], fill=hi); d.rectangle([2, 2, 2, 14], fill=hi)
    d.rectangle([2, 14, 51, 15], fill=sh); d.rectangle([51, 3, 51, 15], fill=sh)
    return t


# Title glyphs (assets/glyphs): the windows, drawn by a `background` title layer from the window's top
# left (ascent 13 puts the top row at chest-y 0). Codepoints are only hints: the pack moves a glyph
# whose codepoint another project already uses.
GLYPHS = {
    "daily_background": (background(True), 0xE7A0),
    "daily_background_plain": (background(False), 0xE7A1),
}

# Image buttons (assets/textures).
IMAGES = {
    "daily_day_claim": day_claim(),
    "daily_day_claimed": day_claimed(),
    "daily_day_today_claimed": day_today_claimed(),
    "daily_day_missed": day_missed(),
    "daily_day_freeze": day_freeze(),
    "daily_day_before": day_before(),
    "daily_prev": arrow(True, True),
    "daily_prev_off": arrow(True, False),
    "daily_next": arrow(False, True),
    "daily_next_off": arrow(False, False),
    "daily_claim": claim(True),
    "daily_claim_off": claim(False),
    "daily_milestones": trophy(),
    "daily_back": back(),
    "daily_close": close(),
    # 1x1 transparent: a hover area with no picture (the streak, the freezes).
    "daily_blank": Image.new("RGBA", (1, 1), CLEAR),
}


def main():
    textures, glyphs = OUT / "textures", OUT / "glyphs"
    textures.mkdir(parents=True, exist_ok=True)
    glyphs.mkdir(parents=True, exist_ok=True)
    for name, img in IMAGES.items():
        img.save(textures / f"{name}.png", optimize=True)
        (textures / f"{name}.yml").write_text(f"width: {img.width}\nheight: {img.height}\nusages: [button]\n")
    for name, (img, codepoint) in GLYPHS.items():
        img.save(glyphs / f"{name}.png", optimize=True)
        (glyphs / f"{name}.yml").write_text(
            f"codepoint: {codepoint}\nascent: 13\nwidth: {img.width}\nheight: {img.height}\nusages: [background]\n")
    print(len(IMAGES) + len(GLYPHS), "images in", OUT)


if __name__ == "__main__":
    main()
