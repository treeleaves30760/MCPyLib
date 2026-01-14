"""
Advanced example usage of MCPyLib demonstrating block state and NBT features

This script shows how to use the new block_state and nbt parameters
with the setblock command.
"""

from mcpylib import MCPyLib
from dotenv import load_dotenv
import os

# Load environment variables from .env file
load_dotenv()

# Initialize the client
mc = MCPyLib(
    ip=os.getenv("SERVER_IP"),
    port=65535,
    token=os.getenv("SERVER_TOKEN")
)

try:
    print("=== Example 1: Basic Block Placement ===")
    mc.setblock(100, 64, 200, "minecraft:stone")
    print("Placed a stone block at (100, 64, 200)")

    print("\n=== Example 2: Block with Direction (Stairs) ===")
    # Place oak stairs facing north on the bottom half
    mc.setblock(
        100, 64, 201,
        "minecraft:oak_stairs",
        block_state={"facing": "north", "half": "bottom"}
    )
    print("Placed oak stairs facing north at (100, 64, 201)")

    print("\n=== Example 3: Block with Direction (Fence Gate) ===")
    # Place a fence gate facing east
    mc.setblock(
        100, 64, 202,
        "minecraft:oak_fence_gate",
        block_state={"facing": "east", "open": "false"}
    )
    print("Placed closed fence gate facing east at (100, 64, 202)")

    print("\n=== Example 4: Log with Axis ===")
    # Place a horizontal log (axis: x)
    mc.setblock(
        100, 64, 203,
        "minecraft:oak_log",
        block_state={"axis": "x"}
    )
    print("Placed oak log with X axis at (100, 64, 203)")

    print("\n=== Example 5: Sign with Text ===")
    # Place a sign with custom text
    mc.setblock(
        100, 64, 204,
        "minecraft:oak_sign",
        block_state={"rotation": "0"},
        nbt={
            "Text1": "Welcome to",
            "Text2": "MCPyLib",
            "Text3": "Demo",
            "Text4": ""
        }
    )
    print("Placed sign with custom text at (100, 64, 204)")

    print("\n=== Example 6: Chest with Custom Name ===")
    # Place a chest with a custom name
    mc.setblock(
        100, 64, 205,
        "minecraft:chest",
        block_state={"facing": "north"},
        nbt={"CustomName": '{"text":"Treasure Chest"}'}
    )
    print("Placed chest with custom name at (100, 64, 205)")

    print("\n=== Example 7: Complex Stair Configuration ===")
    # Create a corner stair
    mc.setblock(
        100, 64, 206,
        "minecraft:stone_brick_stairs",
        block_state={
            "facing": "north",
            "half": "top",
            "shape": "outer_right"
        }
    )
    print("Placed complex stair configuration at (100, 64, 206)")

    print("\n=== Example 8: Waterlogged Block ===")
    # Place a waterlogged fence
    mc.setblock(
        100, 64, 207,
        "minecraft:oak_fence",
        block_state={"waterlogged": "true"}
    )
    print("Placed waterlogged fence at (100, 64, 207)")

    print("\n=== All examples completed successfully! ===")

except Exception as e:
    print(f"Error: {e}")
