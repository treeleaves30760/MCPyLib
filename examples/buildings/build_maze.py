# build_maze_40x40.py
# Python 3.11+
from __future__ import annotations

import os
import random
from typing import List, Optional, Tuple

from dotenv import load_dotenv
from mcpylib import MCPyLib


def generate_maze_map(
    width: int,
    depth: int,
    corridor_width: int = 1,
    wall_width: int = 1,
    seed: Optional[int] = None
) -> List[List[bool]]:
    """
    回傳 walls[x][z]：True=牆、False=通路
    使用 DFS 迷宮生成算法，支援可變的走道和牆寬度。

    Args:
        width: 迷宮總寬度（方塊數）
        depth: 迷宮總深度（方塊數）
        corridor_width: 走道寬度（方塊數）
        wall_width: 牆寬度（方塊數）
        seed: 隨機種子
    """
    cell_size = corridor_width + wall_width
    min_size = wall_width + cell_size + wall_width

    if width < min_size or depth < min_size:
        raise ValueError(f"迷宮尺寸太小，至少需要 {min_size}x{min_size}")

    rng = random.Random(seed)

    # 全部先設牆
    walls = [[True for _ in range(depth)] for _ in range(width)]

    # 計算可以容納多少個 cell（每個 cell 包含走道和牆）
    cell_size = corridor_width + wall_width
    cell_w = (width - wall_width) // cell_size
    cell_d = (depth - wall_width) // cell_size
    visited = [[False for _ in range(cell_d)] for _ in range(cell_w)]

    def cell_to_block(cx: int, cz: int) -> Tuple[int, int]:
        # 對應到迷宮方塊座標（走道的起始位置）
        return wall_width + cell_size * cx, wall_width + cell_size * cz

    def carve_cell(cx: int, cz: int) -> None:
        """在指定 cell 位置挖出走道"""
        bx, bz = cell_to_block(cx, cz)
        # 挖出 corridor_width x corridor_width 的走道
        for dx in range(corridor_width):
            for dz in range(corridor_width):
                if bx + dx < width and bz + dz < depth:
                    walls[bx + dx][bz + dz] = False

    # 起點
    stack: List[Tuple[int, int]] = [(0, 0)]
    visited[0][0] = True
    carve_cell(0, 0)

    # DFS 挖通路
    while stack:
        cx, cz = stack[-1]
        neighbors = []
        for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            nx, nz = cx + dx, cz + dz
            if 0 <= nx < cell_w and 0 <= nz < cell_d and not visited[nx][nz]:
                neighbors.append((nx, nz, dx, dz))

        if not neighbors:
            stack.pop()
            continue

        nx, nz, dx, dz = rng.choice(neighbors)

        # 打通當前 cell 與下一個 cell 中間的牆
        bx, bz = cell_to_block(cx, cz)
        # 計算牆的位置（在兩個走道之間）
        if dx != 0:  # 水平方向
            wall_x = bx + (corridor_width if dx > 0 else -wall_width)
            wall_z = bz
            # 挖通牆，寬度為 corridor_width
            for dz_offset in range(corridor_width):
                for dx_offset in range(wall_width):
                    wx = wall_x + dx_offset
                    wz = wall_z + dz_offset
                    if 0 <= wx < width and 0 <= wz < depth:
                        walls[wx][wz] = False
        else:  # 垂直方向
            wall_x = bx
            wall_z = bz + (corridor_width if dz > 0 else -wall_width)
            # 挖通牆，寬度為 corridor_width
            for dx_offset in range(corridor_width):
                for dz_offset in range(wall_width):
                    wx = wall_x + dx_offset
                    wz = wall_z + dz_offset
                    if 0 <= wx < width and 0 <= wz < depth:
                        walls[wx][wz] = False

        visited[nx][nz] = True
        carve_cell(nx, nz)
        stack.append((nx, nz))

    # 開入口/出口
    # 入口：南側 z=0，從第一個走道位置 (cell 0,0)
    entrance_bx, entrance_bz = cell_to_block(0, 0)
    for dx in range(corridor_width):
        for dz in range(entrance_bz + corridor_width):  # 從 z=0 打通到走道
            if entrance_bx + dx < width and dz < depth:
                walls[entrance_bx + dx][dz] = False

    # 出口：北側 z=depth-1，從最後一個走道位置 (cell cell_w-1, cell_d-1)
    # 找最後一排中任何一個被訪問過的 cell
    exit_cell_x = None
    for cx in range(cell_w):
        if visited[cx][cell_d - 1]:
            exit_cell_x = cx
            break

    # 如果沒找到，用最後一個 cell
    if exit_cell_x is None:
        exit_cell_x = cell_w - 1

    exit_bx, exit_bz = cell_to_block(exit_cell_x, cell_d - 1)
    for dx in range(corridor_width):
        for dz in range(exit_bz, depth):  # 從走道打通到 z=depth-1
            if 0 <= exit_bx + dx < width and 0 <= dz < depth:
                walls[exit_bx + dx][dz] = False

    return walls


def build_maze(
    mc: MCPyLib,
    origin_x: int,
    origin_y: int,
    origin_z: int,
    width: int = 40,
    depth: int = 40,
    wall_height: int = 5,
    corridor_width: int = 1,
    wall_width: int = 1,
    wall_block: str = "stone_bricks",
    floor_block: str = "stone_bricks",
    clear_extra: int = 3,
    seed: Optional[int] = None,
    dry_run: bool = True,
    mark_corners: bool = True,
) -> None:
    """
    origin_* 為迷宮西南角（最小 x, 最小 z），牆底 y=origin_y，地板 y=origin_y-1
    """
    x1, y_wall1, z1 = origin_x, origin_y, origin_z
    x2, y_wall2, z2 = origin_x + width - 1, origin_y + \
        wall_height - 1, origin_z + depth - 1

    y_clear_top = origin_y + wall_height + clear_extra

    # 估算方塊量（僅供顯示）
    approx_clear = width * depth * (wall_height + clear_extra + 1)
    approx_floor = width * depth
    approx_wall_max = width * depth * wall_height

    print("=== 迷宮建造資訊 ===")
    print(f"範圍 X: {x1}..{x2}  Z: {z1}..{z2}")
    print(f"牆 Y: {origin_y}..{y_wall2}（高度 {wall_height}）")
    print(f"地板 Y: {origin_y-1}")
    print(f"清空範圍 Y: {origin_y}..{y_clear_top}")
    print(f"走道寬度: {corridor_width}  牆寬度: {wall_width}")
    print(f"預估清空方塊: ~{approx_clear:,}")
    print(f"地板方塊: {approx_floor:,}")
    print(f"牆體最大方塊: <= {approx_wall_max:,}")
    print(f"seed={seed!r}, dry_run={dry_run}")

    if dry_run:
        print("（dry-run）未實際施工。加上 --apply 才會執行。")
        return

    # 1) 清場（牆以上留一點高度）
    mc.fill(x1, origin_y, z1, x2, y_clear_top, z2, "air")

    # 2) 鋪地板
    mc.fill(x1, origin_y - 1, z1, x2, origin_y - 1, z2, floor_block)

    # 3) 生成迷宮牆配置
    walls = generate_maze_map(
        width, depth, corridor_width, wall_width, seed=seed)

    # 4) 用 edit 一次性放牆（混合 None / 方塊）
    # blocks[x][y][z]
    blocks_3d: List[List[List[Optional[str]]]] = []
    for x in range(width):
        x_layer: List[List[Optional[str]]] = []
        for y in range(wall_height):
            y_layer: List[Optional[str]] = [None] * depth
            x_layer.append(y_layer)
        blocks_3d.append(x_layer)

    for x in range(width):
        for z in range(depth):
            if walls[x][z]:
                for y in range(wall_height):
                    blocks_3d[x][y][z] = wall_block
            # else: None（保持空氣）

    mc.edit(x1, origin_y, z1, blocks_3d)

    # 5) 角落標記（可選）
    if mark_corners:
        for (cx, cz) in ((x1, z1), (x2, z1), (x1, z2), (x2, z2)):
            mc.setblock(cx, origin_y, cz, "red_wool")


def _str_to_bool(val: Optional[str], default: bool = False) -> bool:
    if val is None:
        return default
    return str(val).strip().lower() in {"1", "true", "yes", "y", "on"}


def main() -> None:
    load_dotenv()

    ip = os.getenv("SERVER_IP", "127.0.0.1")
    port = int(os.getenv("SERVER_PORT", "65535"))
    token = os.getenv("SERVER_TOKEN", "your_token")
    username = os.getenv("PLAYER_NAME", "Steve")

    size = int(os.getenv("MAZE_SIZE", "81"))
    wall_height = int(os.getenv("MAZE_WALL_HEIGHT", "5"))
    corridor_width = int(os.getenv("MAZE_CORRIDOR_WIDTH", "2"))
    wall_width = int(os.getenv("MAZE_WALL_WIDTH", "2"))
    seed_env = os.getenv("MAZE_SEED")
    seed = int(seed_env) if seed_env not in (None, "") else None

    wall_block = os.getenv("MAZE_WALL_BLOCK", "stone_bricks")
    floor_block = os.getenv("MAZE_FLOOR_BLOCK", "stone_bricks")

    offset_x = int(os.getenv("MAZE_OFFSET_X", "2"))
    offset_z = int(os.getenv("MAZE_OFFSET_Z", "2"))
    mark_corners = _str_to_bool(os.getenv("MAZE_MARK_CORNERS"), True)
    apply_build = _str_to_bool(os.getenv("MAZE_APPLY"), True)

    mc = MCPyLib(ip=ip, port=port, token=token, timeout=10.0)

    px, py, pz = mc.getPos(username)
    origin_x = px + offset_x
    origin_y = py
    origin_z = pz + offset_z

    build_maze(
        mc=mc,
        origin_x=origin_x,
        origin_y=origin_y,
        origin_z=origin_z,
        width=size,
        depth=size,
        wall_height=wall_height,
        corridor_width=corridor_width,
        wall_width=wall_width,
        wall_block=wall_block,
        floor_block=floor_block,
        seed=seed,
        dry_run=not apply_build,
        mark_corners=mark_corners,
    )


if __name__ == "__main__":
    main()
