"""Sand cache mini-game demo.

Builds a fixed-height tower next to the player. A diamond block is set one
block above foot height as the anchor, walls and cap are built to a fixed
height, and 20–60 sand are stacked inside. The topmost sand is always red sand;
lower layers are normal sand. A full 3x3 ring of air at player height surrounds
the shaft so sand can be dug from any direction, and the block right under the
windows is a diamond block.
"""

import os
import random
from typing import Tuple

from dotenv import load_dotenv
from mcpylib import MCPyLib


load_dotenv()


Coordinate = Tuple[int, int, int]


def build_sand_cache(mc: MCPyLib, anchor: Coordinate, sand_min: int = 20, sand_max: int = 60) -> None:
    """Create the wrapped sand stack; topmost sand is always red."""
    sand_count = random.randint(sand_min, sand_max)

    x, y, z = anchor
    base_y = y  # first block above the anchor block
    tower_height = sand_max  # keep exterior a fixed height
    top_y = base_y + tower_height

    # Build four stone-brick walls around the 1x1 sand shaft up to fixed height.
    mc.fill(x - 1, base_y, z - 1, x - 1, top_y, z + 1, "minecraft:stone_bricks")
    mc.fill(x + 1, base_y, z - 1, x + 1, top_y, z + 1, "minecraft:stone_bricks")
    mc.fill(x - 1, base_y, z - 1, x + 1, top_y, z - 1, "minecraft:stone_bricks")
    mc.fill(x - 1, base_y, z + 1, x + 1, top_y, z + 1, "minecraft:stone_bricks")

    # Leave a full 3x3 ring of air at player level to mine sand from any side.
    window_y = base_y + 1
    for dx in (-1, 0, 1):
        for dz in (-1, 0, 1):
            if dx == 0 and dz == 0:
                continue
            mc.setblock(x + dx, window_y, z + dz, "minecraft:air")

    # Base block under the windows is diamond; sand starts above it.
    mc.setblock(x, base_y, z, "minecraft:diamond_block")

    # Cap the top so players cannot peek.
    mc.fill(x - 1, top_y + 1, z - 1, x + 1, top_y + 1, z + 1, "minecraft:stone_bricks")

    # Stack sand; topmost sand is red, others sand. Leave air above if sand_count < tower height.
    for idx in range(sand_count):
        is_top = idx == sand_count - 1
        block = "minecraft:red_sand" if is_top else "minecraft:sand"
        mc.setblock(x, base_y + 1 + idx, z, block)
    if sand_count < tower_height:
        mc.fill(x, base_y + 1 + sand_count, z, x, top_y, z, "minecraft:air")

    print(
        f"Sand cache built at {(x, y, z)} with {sand_count} layers; top layer is red sand.")


def main() -> None:
    mc = MCPyLib(
        ip=os.getenv("SERVER_IP", "127.0.0.1"),
        port=65535,
        token=os.getenv("SERVER_TOKEN", ""),
    )

    username = os.getenv("PLAYER_NAME", "Player")
    position = mc.getPos(username)
    print(f"Player {username} position: {position}")

    # Place anchor two blocks east of the player, one block above foot height.
    anchor = (int(round(position[0])) + 2,
              int(round(position[1])), int(round(position[2])))
    build_sand_cache(mc, anchor)


if __name__ == "__main__":
    try:
        main()
    except Exception as exc:  # Broad catch to surface MCPyLib errors nicely during demos.
        print(f"Error: {exc}")
