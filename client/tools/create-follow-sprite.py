"""Create the replacement follow marker. Requires Python 3 and Pillow."""

from pathlib import Path

from PIL import Image, ImageDraw


output = Path(__file__).resolve().parents[1] / "src/sprites/peasant_follow.png"
image = Image.new("RGBA", (72, 72), (0, 0, 0, 0))
draw = ImageDraw.Draw(image)

# The peasant occupies roughly x=24..48, y=22..47 in the original frames.
# Keep the centre transparent and use pixel-aligned brackets for crisp rendering.
corners = (
    ((18, 26), (18, 16), (28, 16)),
    ((44, 16), (54, 16), (54, 26)),
    ((18, 44), (18, 54), (28, 54)),
    ((44, 54), (54, 54), (54, 44)),
)
for corner in corners:
    draw.line(corner, fill=(51, 31, 9, 230), width=5)
for corner in corners:
    draw.line(corner, fill=(255, 206, 64, 255), width=3)

output.parent.mkdir(parents=True, exist_ok=True)
image.save(output)
print(output)
