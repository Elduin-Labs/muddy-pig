"""Draws the Muddy Pig's textures. Run from the repo root: python3 tools/textures.py

Everything is drawn from scratch here (no Mojang art), so the pictures can be changed by
editing this file and running it again.
"""
import math
import os
import struct
import zlib

ROOT = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources", "assets", "muddy_pig", "textures")


def write_png(path, pixels, w, h):
    raw = b"".join(b"\x00" + bytes(c for px in pixels[y * w:(y + 1) * w] for c in px) for y in range(h))

    def chunk(kind, data):
        return struct.pack(">I", len(data)) + kind + data + struct.pack(">I", zlib.crc32(kind + data) & 0xFFFFFFFF)

    png = b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", struct.pack(">IIBBBBB", w, h, 8, 6, 0, 0, 0))
    png += chunk(b"IDAT", zlib.compress(raw, 9)) + chunk(b"IEND", b"")
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "wb") as f:
        f.write(png)


def hash01(x, y, seed):
    n = (x * 374761393 + y * 668265263 + seed * 2147483647) & 0xFFFFFFFF
    n = ((n ^ (n >> 13)) * 1274126177) & 0xFFFFFFFF
    return ((n ^ (n >> 16)) & 0xFFFF) / 65535.0


def blobs(x, y, seed, scale=3.0):
    """Smooth lumpy noise, 0..1, for mud splotches."""
    fx, fy = x / scale, y / scale
    x0, y0 = math.floor(fx), math.floor(fy)
    tx, ty = fx - x0, fy - y0
    tx, ty = tx * tx * (3 - 2 * tx), ty * ty * (3 - 2 * ty)
    a, b = hash01(x0, y0, seed), hash01(x0 + 1, y0, seed)
    c, d = hash01(x0, y0 + 1, seed), hash01(x0 + 1, y0 + 1, seed)
    return (a + (b - a) * tx) + ((c + (d - c) * tx) - (a + (b - a) * tx)) * ty


CLEAR = (0, 0, 0, 0)

PINK = [(246, 184, 178, 255), (238, 165, 160, 255), (226, 146, 143, 255)]
SNOUT = (250, 202, 196, 255)
NOSTRIL = (150, 82, 86, 255)
EYE_WHITE = (250, 250, 250, 255)
EYE_BLACK = (28, 20, 24, 255)

WET = [(96, 64, 43, 255), (79, 52, 35, 255), (112, 78, 52, 255)]
WET_SHINE = (146, 110, 79, 255)
DRY = [(170, 144, 111, 255), (152, 127, 97, 255), (189, 165, 131, 255)]
CRACK = (114, 93, 70, 255)

PETAL = (240, 120, 176, 255)
PETAL_DARK = (205, 84, 146, 255)
CENTRE = (250, 214, 64, 255)
STEM = (76, 148, 52, 255)
LEAF = (100, 176, 66, 255)
BUD = (220, 104, 158, 255)
SEPAL = (64, 128, 46, 255)


def pink(x, y):
    r = hash01(x, y, 1)
    return PINK[0] if r < 0.35 else PINK[1] if r < 0.85 else PINK[2]


def mud(x, y, dried):
    r = hash01(x, y, 2)
    if dried:
        if hash01(x, y, 3) < 0.14:
            return CRACK
        return DRY[0] if r < 0.5 else DRY[1] if r < 0.8 else DRY[2]
    if hash01(x, y, 4) < 0.07:
        return WET_SHINE
    return WET[0] if r < 0.5 else WET[1] if r < 0.8 else WET[2]


def is_mud(x, y):
    """Where the mud is on the 64x64 pig layout. Same for wet and dried."""
    b = blobs(x, y, 7)
    # head (texOffs 0,0; 8x8x8): top 8..16,0..8  bottom 16..24,0..8
    #   right 0..8,8..16  front 8..16,8..16  left 16..24,8..16  back 24..32,8..16
    if 16 <= x < 24 and 0 <= y < 8:
        return True  # under the chin
    if 8 <= x < 16 and 0 <= y < 8:
        return b > 0.62  # a splat or two on top of the head
    if 8 <= x < 16 and 8 <= y < 16:
        r, c = y - 8, x - 8
        if r == 7:
            return True
        if r >= 5 and c in (0, 7):
            return True  # muddy cheeks
        return r <= 1 and 2 <= c <= 4  # a splash on the forehead
    if (0 <= x < 8 or 16 <= x < 32) and 8 <= y < 16:
        return (y - 8) >= 5 - 2 * b  # lower half of the sides and back of the head
    # snout (texOffs 16,16): stays pink, apart from one dot on top
    if 16 <= x < 26 and 16 <= y < 20:
        return (x, y) == (19, 16)
    # legs (texOffs 0,16): all mud
    if 0 <= x < 16 and 16 <= y < 26:
        return True
    # body (texOffs 28,8; 10x16x8, turned to lie flat)
    #   chest 36..46,8..16  rear 46..56,8..16
    #   right side 28..36 (back -> belly), belly 36..46, left side 46..54 (belly -> back), back 54..64
    if 36 <= x < 56 and 8 <= y < 16:
        return b > 0.35
    if 36 <= x < 46 and 16 <= y < 32:
        return True
    if 28 <= x < 36 and 16 <= y < 32:
        t = (x - 28 + 0.5) / 8.0  # 0 at the back, 1 at the belly
        return t > 0.55 - 0.35 * (b - 0.5) or b > 0.72
    if 46 <= x < 54 and 16 <= y < 32:
        t = 1.0 - (x - 46 + 0.5) / 8.0
        return t > 0.55 - 0.35 * (b - 0.5) or b > 0.72
    if 54 <= x < 64 and 16 <= y < 32:
        return b > 0.6
    return False


def used(x, y):
    """Pixels the pig model actually shows."""
    return (8 <= x < 24 and 0 <= y < 8) or (0 <= x < 32 and 8 <= y < 16) or \
        (0 <= x < 16 and 16 <= y < 26) or (16 <= x < 26 and 16 <= y < 20) or \
        (36 <= x < 56 and 8 <= y < 16) or (28 <= x < 64 and 16 <= y < 32)


# Flowers are 6x7 cards. '.' is see-through.
OPEN_FLOWER = [
    ".pPPp.",
    "pPYYPp",
    "PYYYYP",
    "pPYYPp",
    ".pSSp.",
    ".LSSL.",
    "..SS..",
]
CLOSED_BUD = [
    "......",
    "..bb..",
    "..bB..",
    "..bb..",
    "..ss..",
    ".LSSL.",
    "..SS..",
]
FLOWER_COLOURS = {"P": PETAL, "p": PETAL_DARK, "Y": CENTRE, "S": STEM, "L": LEAF, "b": BUD, "B": PETAL, "s": SEPAL}


def draw_card(pixels, sprite, u, v, dried_bud=False):
    # the card's front and back faces sit side by side: (u, v) and (u + 6, v)
    for side in (0, 6):
        for r, row in enumerate(sprite):
            for c, ch in enumerate(row):
                if ch == ".":
                    continue
                col = FLOWER_COLOURS[ch]
                if dried_bud and ch in "SLs":
                    col = (112, 128, 70, 255)  # a bit droopy and dull while the mud is dry
                pixels[(v + r) * 64 + u + side + c] = col


def pig_texture(dried):
    px = [CLEAR] * (64 * 64)
    for y in range(64):
        for x in range(64):
            if used(x, y):
                px[y * 64 + x] = mud(x, y, dried) if is_mud(x, y) else pink(x, y)
    # face: eyes two pixels tall, just above the snout
    for r in (2, 3):
        y = 8 + r
        px[y * 64 + 8 + 1] = EYE_WHITE
        px[y * 64 + 8 + 2] = EYE_BLACK
        px[y * 64 + 8 + 5] = EYE_BLACK
        px[y * 64 + 8 + 6] = EYE_WHITE
    # snout front (17..21, 17..20) with nostrils
    for y in range(17, 20):
        for x in range(17, 21):
            px[y * 64 + x] = SNOUT
    px[18 * 64 + 17] = NOSTRIL
    px[18 * 64 + 20] = NOSTRIL
    draw_card(px, OPEN_FLOWER, 0, 40)
    draw_card(px, CLOSED_BUD, 16, 40, dried_bud=dried)
    return px


def spawn_egg():
    w = h = 16
    px = [CLEAR] * (w * h)
    inside = set()
    for y in range(h):
        for x in range(w):
            fy = (y + 0.5 - 9.0) / 6.8
            rx = 5.3 * (1.0 - 0.22 * max(0.0, -fy))  # pointier at the top
            fx = (x + 0.5 - 8.0) / rx
            if fx * fx + fy * fy <= 1.0 and y >= 2:
                inside.add((x, y))
    for (x, y) in inside:
        lit = (x + y) < 12
        if y >= 10 - 2 * blobs(x, y, 11, 2.0) or blobs(x, y, 5, 2.0) > 0.78:
            col = WET[2] if lit else WET[0]
        else:
            col = PINK[0] if lit else PINK[1]
        if not ((x + 1, y) in inside and (x, y + 1) in inside):
            col = WET[1] if y >= 9 else PINK[2]
        px[y * w + x] = col
    # outline
    for y in range(h):
        for x in range(w):
            if (x, y) in inside:
                continue
            if any((x + dx, y + dy) in inside for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                px[y * w + x] = (58, 36, 26, 255)
    # the flower on top
    for (x, y, col) in [(9, 0, PETAL), (8, 1, PETAL), (9, 1, CENTRE), (10, 1, PETAL), (9, 2, PETAL), (9, 3, STEM)]:
        px[y * w + x] = col
    return px, w, h


if __name__ == "__main__":
    write_png(os.path.join(ROOT, "entity", "muddy_pig", "muddy_pig.png"), pig_texture(False), 64, 64)
    write_png(os.path.join(ROOT, "entity", "muddy_pig", "dried_muddy_pig.png"), pig_texture(True), 64, 64)
    egg, w, h = spawn_egg()
    write_png(os.path.join(ROOT, "item", "muddy_pig_spawn_egg.png"), egg, w, h)
    print("done")
