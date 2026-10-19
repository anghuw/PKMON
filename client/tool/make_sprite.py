"""把「白底 + 黑框」的精灵源图，转成带透明通道的 PNG 立绘。

为什么需要这个脚本：精灵源图常常是截图或表情包（JPEG，没有透明通道，
四周还带一圈黑边）。直接丢进 assets/ 的话，卡片上会显示成一个白框。

用法：
    python tool/make_sprite.py <源图> <输出.png>

例：
    python tool/make_sprite.py 攻城狮.jpg assets/sprites/siege_lion.png

做了四件事：
  1. 自动识别并裁掉四周的黑色边框
  2. 从画面四边向内做连通填充，把白底涂成透明
     —— 用「连通」而不是「所有白色」，是为了保住立绘内部的白色
        （眼睛高光、白色道具），它们被描边围住，填不进去
  3. 删掉孤立的小碎块（黑框倒角的抗锯齿残留）
  4. 裁到实际内容，再留 3% 呼吸边，让立绘在各处的显示尺寸一致

注意：只适用于「白底」源图。如果背景是别的颜色，
把下面的 LIGHT 判据改成对应颜色即可。
"""

import sys
from collections import deque

from PIL import Image

# 「算作背景」的亮度下限：三个通道都 >= 200 才当成白底
LIGHT = 200
# 小于这么多像素的独立色块当成噪点丢掉
MIN_AREA = 50
# 内容四周保留的留白比例
PAD_RATIO = 0.03


def is_dark(px):
    return max(px) < 60


def detect_frame(im):
    """逐边向内推进，返回黑框的厚度 (左, 上, 右, 下)。没有黑框就是 0。"""
    w, h = im.size
    px = im.load()

    def row_dark(y, x0, x1, step):
        hits = total = 0
        x = x0
        while (x < x1) if step > 0 else (x > x1):
            total += 1
            if is_dark(px[x, y]):
                hits += 1
            x += step
        return total and hits / total > 0.95

    def col_dark(x, y0, y1, step):
        hits = total = 0
        y = y0
        while (y < y1) if step > 0 else (y > y1):
            total += 1
            if is_dark(px[x, y]):
                hits += 1
            y += step
        return total and hits / total > 0.95

    left = 0
    while left < w // 2 and col_dark(left, 0, h, 1):
        left += 1

    right = 0
    while right < w // 2 and col_dark(w - 1 - right, 0, h, 1):
        right += 1

    top = 0
    while top < h // 2 and row_dark(top, 0, w, 1):
        top += 1

    bottom = 0
    while bottom < h // 2 and row_dark(h - 1 - bottom, 0, w, 1):
        bottom += 1

    return left, top, right, bottom


def remove_background(im):
    """从四边连通填充，返回一张 RGBA 图。"""
    im = im.convert("RGB")
    w, h = im.size
    px = im.load()
    bg = bytearray(w * h)
    q = deque()

    def push(x, y):
        i = y * w + x
        if not bg[i] and min(px[x, y]) >= LIGHT:
            bg[i] = 1
            q.append((x, y))

    for x in range(w):
        push(x, 0)
        push(x, h - 1)
    for y in range(h):
        push(0, y)
        push(w - 1, y)

    while q:
        x, y = q.popleft()
        if x > 0:
            push(x - 1, y)
        if x < w - 1:
            push(x + 1, y)
        if y > 0:
            push(x, y - 1)
        if y < h - 1:
            push(x, y + 1)

    solid = bytearray(0 if b else 1 for b in bg)

    # 去掉孤立小碎块
    seen = bytearray(w * h)
    for sy in range(h):
        for sx in range(w):
            si = sy * w + sx
            if not solid[si] or seen[si]:
                continue
            comp, qq = [], deque([(sx, sy)])
            seen[si] = 1
            while qq:
                x, y = qq.popleft()
                comp.append((x, y))
                for nx, ny in ((x - 1, y), (x + 1, y), (x, y - 1), (x, y + 1)):
                    if 0 <= nx < w and 0 <= ny < h:
                        ni = ny * w + nx
                        if solid[ni] and not seen[ni]:
                            seen[ni] = 1
                            qq.append((nx, ny))
            if len(comp) < MIN_AREA:
                for x, y in comp:
                    solid[y * w + x] = 0

    out = im.convert("RGBA")
    out.putalpha(Image.frombytes("L", (w, h), bytes(255 if s else 0 for s in solid)))
    return out


def main():
    if len(sys.argv) != 3:
        print(__doc__)
        return 1

    src, dst = sys.argv[1], sys.argv[2]
    im = Image.open(src).convert("RGB")
    original = im.size

    left, top, right, bottom = detect_frame(im)
    if left or top or right or bottom:
        im = im.crop((left, top, im.width - right, im.height - bottom))

    out = remove_background(im)
    out = out.crop(out.getbbox())

    pad = int(max(out.size) * PAD_RATIO)
    canvas = Image.new("RGBA", (out.width + pad * 2, out.height + pad * 2), (0, 0, 0, 0))
    canvas.paste(out, (pad, pad))
    canvas.save(dst)

    print(f"源图 {original} → 裁掉黑框 {(left, top, right, bottom)} → 输出 {canvas.size} → {dst}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
