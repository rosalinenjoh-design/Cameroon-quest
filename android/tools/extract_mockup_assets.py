"""Cuts the Cameroon Quest artwork out of design/mockup.png.png into Android drawables.

Run from the repository root:
    py android/tools/extract_mockup_assets.py

Coordinates are measured on the 1280x853 mockup. A larger export of the same
mockup layout is scaled automatically and gives sharper pictures.
"""
import argparse
from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter

REF_W, REF_H = 1280, 853
UPSCALE = 3

ROOT = Path(__file__).resolve().parents[2]
DEFAULT_MOCKUP = ROOT / "design" / "mockup.png.png"
DEFAULT_OUT = ROOT / "android" / "app" / "src" / "main" / "res" / "drawable-nodpi"

PORTRAITS = {
    "njoh": (326, 74, 438, 172), "bakama": (454, 74, 559, 172), "mbororo": (574, 74, 684, 172),
    "wouri": (699, 74, 809, 172), "nkem": (825, 74, 938, 172),
    "yeme": (326, 232, 438, 332), "zanga": (454, 232, 559, 332), "tenda": (574, 232, 684, 332),
    "foufou": (699, 232, 808, 332), "lea": (825, 232, 938, 332),
}
FACES = {
    "njoh": (363, 77, 427, 141), "bakama": (473, 77, 537, 141), "mbororo": (595, 77, 659, 141),
    "wouri": (718, 77, 782, 141), "nkem": (853, 77, 917, 141),
    "yeme": (343, 240, 407, 304), "zanga": (475, 240, 539, 304), "tenda": (595, 240, 659, 304),
    "foufou": (723, 240, 787, 304), "lea": (840, 240, 904, 304),
}
PICTURES = {
    "home_hero": (14, 484, 226, 594),
    "game_songo": (260, 490, 442, 559),
    "game_pirogue": (260, 578, 442, 647),
    "game_dochi": (260, 667, 442, 736),
    "quiz_grassfields": (697, 483, 860, 568),
}
WELCOME_HERO = (11, 4, 294, 246)
WELCOME_LOWER = (11, 238, 294, 372)
WELCOME_ERASE = [(58, 246, 254, 282), (68, 300, 240, 352)]
WELCOME_RATIO = 0.42
DETAIL_NJOH = (971, 9, 1100, 388)
DETAIL_BACK_BUTTON = (979, 12, 1009, 42)


class Mockup:
    def __init__(self, path):
        self.image = Image.open(path).convert("RGB")
        self.sx = self.image.width / REF_W
        self.sy = self.image.height / REF_H

    def crop(self, box):
        x0, y0, x1, y1 = box
        return self.image.crop((round(x0 * self.sx), round(y0 * self.sy), round(x1 * self.sx), round(y1 * self.sy)))

    def finish(self, image, factor=UPSCALE):
        scale = max(1.0, factor / self.sx)
        if scale > 1.0:
            image = image.resize((round(image.width * scale), round(image.height * scale)), Image.LANCZOS)
            image = image.filter(ImageFilter.UnsharpMask(radius=2, percent=70, threshold=2))
        return image


def erase_columns(image, rect, origin):
    """Fills a rectangle by blending, per column, the pixels just above and below it."""
    px = image.load()
    x0, y0, x1, y1 = (rect[0] - origin[0], rect[1] - origin[1], rect[2] - origin[0], rect[3] - origin[1])
    x0, y0 = max(0, x0), max(1, y0)
    x1, y1 = min(image.width, x1), min(image.height - 1, y1)
    for x in range(x0, x1):
        top, bottom = px[x, y0 - 1], px[x, y1]
        for y in range(y0, y1):
            t = (y - y0 + 1) / (y1 - y0 + 1)
            px[x, y] = tuple(round(top[i] * (1 - t) + bottom[i] * t) for i in range(3))


def welcome_background(mockup):
    hero = mockup.crop(WELCOME_HERO)
    lower = mockup.crop(WELCOME_LOWER)
    scale = lower.width / (WELCOME_LOWER[2] - WELCOME_LOWER[0])
    for rect in WELCOME_ERASE:
        scaled = tuple(round(v * scale) for v in rect)
        erase_columns(lower, scaled, (round(WELCOME_LOWER[0] * scale), round(WELCOME_LOWER[1] * scale)))
    lower = lower.filter(ImageFilter.GaussianBlur(8 * scale))
    height = round(hero.width / WELCOME_RATIO)
    seam = hero.height - round(10 * scale)
    canvas = Image.new("RGB", (hero.width, height))
    canvas.paste(lower.resize((hero.width, height - seam), Image.LANCZOS), (0, seam))
    mask = Image.new("L", hero.size, 255)
    draw = ImageDraw.Draw(mask)
    for y in range(seam, hero.height):
        draw.line([(0, y), (hero.width, y)], fill=round(255 * (hero.height - y) / (hero.height - seam)))
    canvas.paste(hero, (0, 0), mask)
    return canvas


def detail_portrait(mockup):
    portrait = mockup.crop(DETAIL_NJOH)
    scale = portrait.width / (DETAIL_NJOH[2] - DETAIL_NJOH[0])
    bx0, by0, bx1, by1 = (DETAIL_BACK_BUTTON[0] - DETAIL_NJOH[0], DETAIL_BACK_BUTTON[1] - DETAIL_NJOH[1],
                          DETAIL_BACK_BUTTON[2] - DETAIL_NJOH[0], DETAIL_BACK_BUTTON[3] - DETAIL_NJOH[1])
    box = tuple(round(v * scale) for v in (bx0, by0, bx1, by1))
    width = box[2] - box[0]
    patch = portrait.crop((box[2] + 2, box[1], box[2] + 2 + width, box[3]))
    mask = Image.new("L", patch.size, 0)
    ImageDraw.Draw(mask).ellipse((1, 1, patch.width - 2, patch.height - 2), fill=255)
    portrait.paste(patch, box[:2], mask.filter(ImageFilter.GaussianBlur(2 * scale)))
    return portrait


def backdrop(portrait):
    """Tiny, heavily blurred portrait used as a full-screen colour backdrop."""
    return portrait.resize((48, 104), Image.LANCZOS).filter(ImageFilter.GaussianBlur(5))


def save(image, out_dir, name, quality=90):
    path = out_dir / f"{name}.webp"
    image.save(path, "WEBP", quality=quality, method=6)
    print(f"  {path.name:28s} {image.width}x{image.height}")


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--mockup", type=Path, default=DEFAULT_MOCKUP)
    parser.add_argument("--out", type=Path, default=DEFAULT_OUT)
    args = parser.parse_args()

    mockup = Mockup(args.mockup)
    args.out.mkdir(parents=True, exist_ok=True)
    print(f"Mockup {mockup.image.width}x{mockup.image.height} -> {args.out}")

    save(mockup.finish(welcome_background(mockup)), args.out, "welcome_bg")
    save(mockup.finish(detail_portrait(mockup)), args.out, "detail_njoh")
    for name, box in PICTURES.items():
        save(mockup.finish(mockup.crop(box)), args.out, name)
    for name, box in PORTRAITS.items():
        portrait = mockup.crop(box)
        save(mockup.finish(portrait), args.out, f"char_{name}")
        save(backdrop(portrait), args.out, f"charbg_{name}", quality=80)
    for name, box in FACES.items():
        save(mockup.finish(mockup.crop(box)), args.out, f"avatar_{name}")


if __name__ == "__main__":
    main()
