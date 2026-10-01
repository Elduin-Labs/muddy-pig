"""Draws the mod icon: a muddy pig face with its flower. Run from the repo root: python3 tools/icon.py

A 32x32 grid of letters, scaled up 8x with no smoothing so it stays pixel art.
Colours come from the pig's own textures in textures.py.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(__file__))
from textures import write_png, PINK, SNOUT, NOSTRIL, EYE_WHITE, EYE_BLACK, WET, WET_SHINE, PETAL, PETAL_DARK, CENTRE, STEM, LEAF

GRID = [
    "..GGGGGGGGGGGGGGGGGGGGGGGGGGGG..",
    ".GggggggggggggggggFFFFggggggggG.",
    "GggggggggggggggggFffffFggggggggG",
    "GgggggggggggggggFffyyffFgggggggG",
    "GgggggggggggggggFfyyyyfFgggggggG",
    "GgggggggggggggggFffyyffFgggggggG",
    "GggggggggggggggggFffffFggggggggG",
    "GgggggggggggggggggFFFFgggggggggG",
    "GggggggggggggggggggttggggggggggG",
    "GggggggggggggggggLLttLLggggggggG",
    "GgggggggggggggggggLttLgggggggggG",
    "GggggggggggggggggggttggggggggggG",
    "GggggooooooooooooooooooooooggggG",
    "GggggoPPPPPPPPmmmPPPPPPPPqoggggG",
    "GggggoPpppppplmmmmmppppppqoggggG",
    "GggggoPppppppppmmppppppppqoggggG",
    "GggggoPppppppppppppppppppqoggggG",
    "GggggoPpwkppppppppppppkwpqoggggG",
    "GggggoPpwkppppppppppppkwpqoggggG",
    "GggggoPppppppppppppppppppqoggggG",
    "GggggoPpppppssssssssppppmqoggggG",
    "GggggoPpppppsssssssspppmmmoggggG",
    "GggggompppppsnssssnsppmmmmoggggG",
    "GggggommppppsnssssnsppmlmmoggggG",
    "GggggommmpppsssssssspmmmmmoggggG",
    "GggggomlmmpppppppppppmmMmmoggggG",
    "GggggommmmmpppppppppmmmmmmoggggG",
    "GggggommMmmmmpmmmpmmmmlmmmoggggG",
    "GggggommemmmmmmmlmmmmmmeMmoggggG",
    "GggggooooooooooooooooooooooggggG",
    ".GggggggggggggggggggggggggggggG.",
    "..GGGGGGGGGGGGGGGGGGGGGGGGGGGG..",
]

PAL = {
    "g": (108, 172, 76, 255), "G": (74, 128, 52, 255),
    "o": (120, 70, 66, 255),
    "P": PINK[0], "p": PINK[1], "q": PINK[2],
    "s": SNOUT, "n": NOSTRIL, "w": EYE_WHITE, "k": EYE_BLACK,
    "m": WET[0], "M": WET[1], "l": WET[2], "e": WET_SHINE,
    "f": PETAL, "F": PETAL_DARK, "y": CENTRE, "t": STEM, "L": LEAF,
}


def build(grid, scale):
    h, w = len(grid), len(grid[0])
    assert all(len(r) == w for r in grid), [i for i, r in enumerate(grid) if len(r) != w]
    out = []
    for y in range(h * scale):
        for x in range(w * scale):
            ch = grid[y // scale][x // scale]
            out.append((0, 0, 0, 0) if ch == "." else PAL[ch])
    return out, w * scale, h * scale


if __name__ == "__main__":
    here = os.path.dirname(__file__)
    px, w, h = build(GRID, 8)
    write_png(os.path.join(here, "..", "src", "main", "resources", "assets", "icon.png"), px, w, h)
    if len(sys.argv) > 1:  # also write a small copy, to judge it at the size people see it
        small, sw, sh = build(GRID, 2)
        write_png(sys.argv[1], small, sw, sh)
    print("done")
