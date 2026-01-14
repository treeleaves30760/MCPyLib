# build_cabin_30x30.py
# Python 3.11+
from __future__ import annotations
from mcpylib import MCPyLib
from dotenv import load_dotenv
import os

load_dotenv()


# ========== 你可能會改的設定 ==========
IP = "127.0.0.1"
PORT = 65535
TOKEN = os.getenv("SERVER_TOKEN")

PLAYER_NAME = os.getenv("PLAYER_NAME", "Steve")
DRY_RUN = False
# 建築參數
SIZE = 30
WALL_H = 6
PART_H = 3          # 內部隔間牆高度（避免和閣樓地板衝突）
LIVING_DEPTH = 8     # 南側客廳深度（格）
ROOF_LAYERS = 8

# 方塊
FOUNDATION = "cobblestone"
FLOOR = "oak_planks"
WALL = "oak_planks"
CORNER_LOG = "oak_log"
ROOF_STAIR = "spruce_stairs"
ROOF_RIDGE = "spruce_planks"
WINDOW = "glass_pane"
DOOR = "oak_door"
MARKER = "red_wool"

# 家具/裝飾
TORCH = "torch"
CRAFT = "crafting_table"
FURNACE = "furnace"
SMOKER = "smoker"
BARREL = "barrel"
CHEST = "chest"
BOOKSHELF = "bookshelf"
OAK_FENCE = "oak_fence"
OAK_SLAB = "oak_slab"
OAK_STAIRS = "oak_stairs"
CARPET = "white_carpet"
CAMPFIRE = "campfire"


def norm_box(a: int, b: int) -> tuple[int, int]:
    return (a, b) if a <= b else (b, a)


def get_origin(mc: MCPyLib, player: str, dx: int = 5, dz: int = 5) -> tuple[int, int, int]:
    """
    以玩家座標做基準：把 (x0,z0) 當作「西北角」，往 +X(東) +Z(南) 擴展 SIZE×SIZE
    Minecraft 座標慣例：+X=東、+Z=南
    """
    try:
        px, py, pz = mc.getPos(player)
        return int(px + dx), int(py), int(pz + dz)
    except:
        return 0, 64, 0


def outline_border(mc: MCPyLib, x0: int, y0: int, z0: int) -> None:
    """DRY_RUN：只畫 30×30 外框，並標示南側門口位置。"""
    x1, z1 = x0 + SIZE - 1, z0 + SIZE - 1
    mc.fill(x0, y0, z0, x1, y0, z0, MARKER)  # 北邊
    mc.fill(x0, y0, z1, x1, y0, z1, MARKER)  # 南邊
    mc.fill(x0, y0, z0, x0, y0, z1, MARKER)  # 西邊
    mc.fill(x1, y0, z0, x1, y0, z1, MARKER)  # 東邊

    # 南側中央門口提示（z1）
    door_x = x0 + SIZE // 2
    mc.setblock(door_x, y0, z1, "yellow_wool")
    mc.setblock(door_x, y0 + 1, z1, "yellow_wool")


def set_door(mc: MCPyLib, x: int, y: int, z: int, facing: str, hinge: str = "left") -> None:
    """放一扇直立門（2 格高）。"""
    mc.setblock(x, y, z, DOOR)


def carve_and_glass(mc: MCPyLib, xa: int, ya: int, za: int, xb: int, yb: int, zb: int) -> None:
    xlo, xhi = norm_box(xa, xb)
    ylo, yhi = norm_box(ya, yb)
    zlo, zhi = norm_box(za, zb)
    mc.fill(xlo, ylo, zlo, xhi, yhi, zhi, "air")
    mc.fill(xlo, ylo, zlo, xhi, yhi, zhi, WINDOW)


def build_interior(mc: MCPyLib, x0: int, y0: int, z0: int) -> None:
    """內部隔間 + 閣樓 + 家具配置。"""
    x1, z1 = x0 + SIZE - 1, z0 + SIZE - 1
    ix0, ix1 = x0 + 1, x1 - 1
    iz0, iz1 = z0 + 1, z1 - 1

    door_x = x0 + SIZE // 2
    corridor_x0, corridor_x1 = door_x - 1, door_x + 1

    # 客廳深度 LIVING_DEPTH（南側開放區），隔間牆放在 z_div
    z_div = iz1 - LIVING_DEPTH  # 這條線以北是房間區，以南是客廳區（含門口）
    part_y1 = y0 + 1
    part_y2 = y0 + PART_H

    # 1) 走道兩側隔間牆（房間區）
    # 西側走道牆 x = corridor_x0 - 1
    wx = corridor_x0 - 1
    ex = corridor_x1 + 1
    mc.fill(wx, part_y1, iz0, wx, part_y2, z_div - 1, WALL)
    mc.fill(ex, part_y1, iz0, ex, part_y2, z_div - 1, WALL)

    # 2) 客廳/房間分隔橫牆（在 z_div），中間留走道洞、左右留房門洞
    mc.fill(ix0, part_y1, z_div, ix1, part_y2, z_div, WALL)

    # 走道洞（3 格寬）
    mc.fill(corridor_x0, part_y1, z_div, corridor_x1, part_y2, z_div, "air")

    # 房門洞（各 1 格寬）
    west_door_x = wx
    east_door_x = ex
    west_door_z = z_div - 2
    east_door_z = z_div - 2

    # 在走道牆上挖門洞
    mc.fill(west_door_x, part_y1, west_door_z,
            west_door_x, part_y1 + 1, west_door_z, "air")
    mc.fill(east_door_x, part_y1, east_door_z,
            east_door_x, part_y1 + 1, east_door_z, "air")

    # 放內門（可不喜歡門就把下面兩行註解）
    set_door(mc, west_door_x, part_y1, west_door_z,
             facing="west", hinge="left")
    set_door(mc, east_door_x, part_y1, east_door_z,
             facing="east", hinge="right")

    # 3) 閣樓地板（覆蓋房間區 + 分隔牆上方），y = y0+4
    loft_y = y0 + 4
    mc.fill(ix0, loft_y, iz0, ix1, loft_y, z_div, FLOOR)

    # 閣樓前緣護欄（在 z_div，留走道口）
    rail_y = loft_y + 1
    mc.fill(ix0, rail_y, z_div, ix1, rail_y, z_div, OAK_FENCE)
    mc.fill(corridor_x0, rail_y, z_div, corridor_x1, rail_y, z_div, "air")

    # 4) 通往閣樓的樓梯（放在客廳區，沿走道往北上去）
    # 3 段樓梯：最後一段剛好通到 y0+4 的閣樓地板
    stair_x = door_x  # 放在走道中央
    start_z = z_div + 2
    for i in range(3):
        mc.setblock(
            stair_x, y0 + 1 + i, start_z - i,
            OAK_STAIRS,
            block_state={"facing": "north", "half": "bottom"}
        )
    # 樓梯上方清空避免卡頭
    mc.fill(stair_x - 1, y0 + 2, z_div, stair_x +
            1, loft_y + 2, z_div + 3, "air")

    # 5) 地毯/照明（客廳）
    mc.fill(door_x - 2, y0 + 1, z_div + 2, door_x + 2, y0 + 1, iz1 - 2, CARPET)
    mc.setblock(door_x - 6, y0 + 3, iz1 - 2, TORCH)
    mc.setblock(door_x + 6, y0 + 3, iz1 - 2, TORCH)

    # 6) 餐桌（客廳）
    table_z = z_div + 4
    mc.setblock(door_x - 1, y0 + 1, table_z, OAK_FENCE)
    mc.setblock(door_x + 1, y0 + 1, table_z, OAK_FENCE)
    mc.fill(door_x - 1, y0 + 2, table_z, door_x + 1, y0 + 2, table_z, OAK_SLAB)

    # 7) 西側：廚房/工作間（房間區西半邊，x: ix0..wx-1）
    # 工作台/熔爐/煙燻爐/桶子
    wk_x = ix0 + 2
    wk_z = iz0 + 2
    mc.setblock(wk_x, y0 + 1, wk_z, CRAFT)
    mc.setblock(wk_x + 1, y0 + 1, wk_z, FURNACE,
                block_state={"facing": "south"})
    mc.setblock(wk_x + 2, y0 + 1, wk_z, SMOKER,
                block_state={"facing": "south"})
    mc.setblock(wk_x, y0 + 1, wk_z + 1, BARREL,
                block_state={"facing": "south"})
    mc.setblock(wk_x + 1, y0 + 1, wk_z + 1, CHEST,
                block_state={"facing": "south"})

    mc.setblock(ix0 + 1, y0 + 3, iz0 + 1, TORCH)

    # 8) 東側：臥室（房間區東半邊，x: ex+1..ix1）
    bed_foot_x = ix1 - 2
    bed_z = iz0 + 4
    # 床（兩格）：foot + head
    mc.setblock(bed_foot_x, y0 + 1, bed_z, "white_bed",
                block_state={"facing": "west"})
    mc.setblock(ix1 - 2, y0 + 1, bed_z + 2, CHEST,
                block_state={"facing": "west"})
    mc.setblock(ix1 - 1, y0 + 3, iz0 + 1, TORCH)

    # 9) 走道書櫃（房間區走道北側）
    mc.fill(corridor_x0, y0 + 1, iz0 + 6,
            corridor_x1, y0 + 2, iz0 + 6, BOOKSHELF)

    # 10) 閣樓簡單配置（床墊區/收納）
    # 閣樓上放兩個箱子和火把
    mc.setblock(ix0 + 2, loft_y + 1, iz0 + 2, CHEST,
                block_state={"facing": "south"})
    mc.setblock(ix0 + 3, loft_y + 1, iz0 + 2, CHEST,
                block_state={"facing": "south"})
    mc.setblock(door_x, loft_y + 2, iz0 + 3, TORCH)


def build_shell(mc: MCPyLib, x0: int, y0: int, z0: int) -> None:
    """
    蓋外殼：清空 -> 地基 -> 地板 -> 牆 -> 門窗 -> 內裝 -> 屋頂
    (x0,z0) = 西北角，往 +X(東) +Z(南) 擴展
    """
    x1, z1 = x0 + SIZE - 1, z0 + SIZE - 1

    # 1) 清空施工範圍（含屋頂空間 + 一格外挑）
    clear_top = y0 + WALL_H + ROOF_LAYERS + 5
    mc.fill(x0 - 1, y0, z0 - 1, x1 + 1, clear_top, z1 + 1, "air")

    # 2) 地基 & 地板
    mc.fill(x0, y0 - 1, z0, x1, y0 - 1, z1, FOUNDATION)
    mc.fill(x0, y0, z0, x1, y0, z1, FLOOR)

    # 3) 外牆
    for yy in range(y0 + 1, y0 + WALL_H + 1):
        mc.fill(x0, yy, z0, x1, yy, z0, WALL)  # 北
        mc.fill(x0, yy, z1, x1, yy, z1, WALL)  # 南
        mc.fill(x0, yy, z0, x0, yy, z1, WALL)  # 西
        mc.fill(x1, yy, z0, x1, yy, z1, WALL)  # 東

    # 4) 角柱
    for (cx, cz) in [(x0, z0), (x0, z1), (x1, z0), (x1, z1)]:
        mc.fill(cx, y0 + 1, cz, cx, y0 + WALL_H, cz, CORNER_LOG)

    # 5) 主門（南側中央：z1）
    door_x = x0 + SIZE // 2
    mc.setblock(door_x, y0 + 1, z1, "air")
    mc.setblock(door_x, y0 + 2, z1, "air")
    set_door(mc, door_x, y0 + 1, z1, facing="south", hinge="left")

    # 6) 窗戶
    wy1, wy2 = y0 + 2, y0 + 3
    # 南牆（門左右）
    carve_and_glass(mc, door_x - 6, wy1, z1, door_x - 4, wy2, z1)
    carve_and_glass(mc, door_x + 4, wy1, z1, door_x + 6, wy2, z1)
    # 北牆（中間大窗）
    mid_x = x0 + SIZE // 2
    carve_and_glass(mc, mid_x - 2, wy1, z0, mid_x + 2, wy2, z0)
    # 西牆/東牆各兩窗
    carve_and_glass(mc, x0, wy1, z0 + 6, x0, wy2, z0 + 8)
    carve_and_glass(mc, x0, wy1, z1 - 8, x0, wy2, z1 - 6)

    carve_and_glass(mc, x1, wy1, z0 + 6, x1, wy2, z0 + 8)
    carve_and_glass(mc, x1, wy1, z1 - 8, x1, wy2, z1 - 6)

    # 7) 內部結構
    build_interior(mc, x0, y0, z0)

    # 8) 屋頂（雙坡，修正樓梯方向）
    # 北屋簷在 z0-1，南屋簷在 z1+1；向中心爬升
    roof_y0 = y0 + WALL_H + 1
    north_eave_z = z0 - 1
    south_eave_z = z1 + 1

    for i in range(ROOF_LAYERS):
        yy = roof_y0 + i
        nz = north_eave_z + i      # 從北往南靠近中心（z 變大）
        sz = south_eave_z - i      # 從南往北靠近中心（z 變小）

        # 北坡：要往南升高 => stairs facing south
        for xx in range(x0 - 1, x1 + 2):
            mc.setblock(xx, yy, nz, ROOF_STAIR, block_state={
                        "facing": "south", "half": "bottom"})

        # 南坡：要往北升高 => stairs facing north
        for xx in range(x0 - 1, x1 + 2):
            mc.setblock(xx, yy, sz, ROOF_STAIR, block_state={
                        "facing": "north", "half": "bottom"})

        # 山牆補齊（西/東面三角形）
        z_in1 = z0 + i
        z_in2 = z1 - i
        if z_in1 <= z_in2:
            mc.fill(x0, yy, z_in1, x0, yy, z_in2, WALL)
            mc.fill(x1, yy, z_in1, x1, yy, z_in2, WALL)

    # 屋脊（兩格寬）
    ridge_y = roof_y0 + ROOF_LAYERS
    ridge_z1 = z0 + (SIZE // 2) - 1
    ridge_z2 = z0 + (SIZE // 2)
    mc.fill(x0 - 1, ridge_y, ridge_z1, x1 + 1, ridge_y, ridge_z1, ROOF_RIDGE)
    mc.fill(x0 - 1, ridge_y, ridge_z2, x1 + 1, ridge_y, ridge_z2, ROOF_RIDGE)

    # 9) 把玩家傳到門口外
    try:
        mc.teleport(PLAYER_NAME, door_x + 0.5, y0 +
                    1, z1 + 2.5, yaw=180.0, pitch=0.0)
    except:
        pass


def main() -> None:
    mc = MCPyLib(ip=IP, port=PORT, token=TOKEN, timeout=10.0)

    x0, y0, z0 = get_origin(mc, PLAYER_NAME, dx=5, dz=5)
    print(f"Origin (NW corner) = ({x0}, {y0}, {z0})  size={SIZE}x{SIZE}")

    if DRY_RUN:
        outline_border(mc, x0, y0, z0)
        print("DRY_RUN=True：已用紅羊毛畫出 30x30 邊界，南側門口用黃羊毛標記。")
        print("確認位置OK後，把 DRY_RUN 改成 False 再執行一次就會整棟生成。")
        return

    build_shell(mc, x0, y0, z0)
    print("完成：30x30 小木屋（含內部隔間＋閣樓＋修正屋頂）已建造！")


if __name__ == "__main__":
    main()
