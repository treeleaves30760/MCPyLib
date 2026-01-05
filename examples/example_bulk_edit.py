"""
Example demonstrating the high-performance bulk edit feature.

This example shows how to use mc.edit() to quickly build large structures,
similar to WorldEdit plugin functionality.

All structures will be built around the player's current position.
"""

from mcpylib import MCPyLib
import time
from dotenv import load_dotenv
import os

# Load environment variables from .env file
load_dotenv()


# Initialize client
mc = MCPyLib(
    ip=os.getenv("SERVER_IP"),
    port=65535,
    token=os.getenv("SERVER_TOKEN")
)

print("=== MCPyLib Bulk Edit Examples ===\n")

# Get player position
player_name = input("Enter your Minecraft username: ").strip()
if not player_name:
    print("No username provided. Using default username from environment...")
    player_name = os.getenv("PLAYER_NAME", "Player")

try:
    pos = mc.getPos(player_name)
    base_x, base_y, base_z = pos
    print(f"✓ Found player '{player_name}' at ({base_x}, {base_y}, {base_z})")
    print(f"✓ All structures will be built around your position\n")
except Exception as e:
    print(f"✗ Error: Could not find player '{player_name}': {e}")
    print("✗ Make sure the player is online and the username is correct.")
    exit(1)

# Example 1: Simple 3x3x3 cube with different materials
print("1. Building a simple 3x3x3 cube...")
blocks = [
    # X layer 0
    [
        ["stone", "stone", "stone"],        # Y=0
        ["stone", "air", "stone"],          # Y=1
        ["stone", "stone", "stone"]         # Y=2
    ],
    # X layer 1
    [
        ["stone", "air", "stone"],
        ["air", "air", "air"],
        ["stone", "air", "stone"]
    ],
    # X layer 2
    [
        ["stone", "stone", "stone"],
        ["stone", "air", "stone"],
        ["stone", "stone", "stone"]
    ]
]

# Build 5 blocks in front of player
x, y, z = base_x + 5, base_y, base_z
print(f"   Building at ({x}, {y}, {z})")
start_time = time.time()
count = mc.edit(x, y, z, blocks)
elapsed = time.time() - start_time
print(f"   ✓ Placed {count} blocks in {elapsed:.3f} seconds\n")

# Example 2: Colorful tower (5x10x5)
print("2. Building a colorful tower (5x10x5)...")
blocks = []
colors = ["white_concrete", "orange_concrete", "magenta_concrete",
          "light_blue_concrete", "yellow_concrete", "lime_concrete",
          "pink_concrete", "gray_concrete", "light_gray_concrete", "cyan_concrete"]

for x in range(5):
    x_layer = []
    for y in range(10):
        y_layer = []
        for z in range(5):
            # Use different colors for each height level
            if (x == 0 or x == 4 or z == 0 or z == 4):
                y_layer.append(colors[y])
            else:
                y_layer.append("air")
        x_layer.append(y_layer)
    blocks.append(x_layer)

# Build 15 blocks to the right of player
x, y, z = base_x, base_y, base_z + 15
print(f"   Building at ({x}, {y}, {z})")
start_time = time.time()
count = mc.edit(x, y, z, blocks)
elapsed = time.time() - start_time
print(f"   ✓ Placed {count} blocks in {elapsed:.3f} seconds\n")

# Example 3: House with complex blocks (chests, signs, etc.)
print("3. Building a house with complex blocks...")
blocks = [
    # Floor and walls (10x5x10)
]

# Initialize with None
for x in range(10):
    x_layer = []
    for y in range(5):
        y_layer = [None] * 10
        x_layer.append(y_layer)
    blocks.append(x_layer)

# Floor (stone)
for x in range(10):
    for z in range(10):
        blocks[x][0][z] = "stone"

# Walls (cobblestone)
for y in range(1, 4):
    for x in range(10):
        blocks[x][y][0] = "cobblestone"
        blocks[x][y][9] = "cobblestone"
    for z in range(10):
        blocks[0][y][z] = "cobblestone"
        blocks[9][y][z] = "cobblestone"

# Door (leave air)
blocks[5][1][0] = "air"
blocks[5][2][0] = "air"

# Windows (glass)
blocks[3][2][0] = "glass"
blocks[7][2][0] = "glass"
blocks[0][2][3] = "glass"
blocks[0][2][7] = "glass"

# Roof (oak planks)
for x in range(10):
    for z in range(10):
        blocks[x][4][z] = "oak_planks"

# Add a chest with custom name
blocks[2][1][2] = {
    "block": "minecraft:chest",
    "block_state": {"facing": "north"},
    "nbt": {"CustomName": '{"text":"Storage Chest"}'}
}

# Add a sign
blocks[8][2][5] = {
    "block": "minecraft:oak_wall_sign",
    "block_state": {"facing": "west"},
    "nbt": {
        "Text1": "Welcome",
        "Text2": "to my",
        "Text3": "house!",
        "Text4": ""
    }
}

# Build 15 blocks to the left of player
x, y, z = base_x, base_y, base_z - 15
print(f"   Building at ({x}, {y}, {z})")
start_time = time.time()
count = mc.edit(x, y, z, blocks)
elapsed = time.time() - start_time
print(f"   ✓ Placed {count} blocks in {elapsed:.3f} seconds\n")

# Example 4: Performance comparison - Large platform
print("4. Performance comparison: Building a 50x1x50 platform...")

# Build 30 blocks behind player
x, y, z = base_x - 30, base_y, base_z

# Method 1: Using edit() - FAST
print("   Method 1: Using edit()...")
print(f"   Building at ({x}, {y}, {z})")
blocks = [[["stone" for _ in range(50)] for _ in range(1)] for _ in range(50)]
start_time = time.time()
count = mc.edit(x, y, z, blocks)
elapsed_edit = time.time() - start_time
print(f"      ✓ Placed {count} blocks in {elapsed_edit:.3f} seconds")

# Method 2: Using fill() - Also fast for uniform blocks
print("   Method 2: Using fill()...")
fill_x, fill_y, fill_z = base_x - 30, base_y, base_z + 60
print(f"   Building at ({fill_x}, {fill_y}, {fill_z})")
start_time = time.time()
count = mc.fill(fill_x, fill_y, fill_z, fill_x + 49, fill_y, fill_z + 49, "stone")
elapsed_fill = time.time() - start_time
print(f"      ✓ Placed {count} blocks in {elapsed_fill:.3f} seconds")

# Method 3: Using setblock() repeatedly - SLOW (commented out to save time)
# print("   Method 3: Using setblock() (100 blocks only)...")
# start_time = time.time()
# for x in range(10):
#     for z in range(10):
#         mc.setblock(200 + x, 64, 200 + z, "stone")
# elapsed_setblock = time.time() - start_time
# print(f"      Placed 100 blocks in {elapsed_setblock:.3f} seconds")
# print(f"      Estimated time for 2500 blocks: {elapsed_setblock * 25:.3f} seconds")

print(f"\n   Summary:")
print(f"      edit() is ideal for complex structures with different blocks")
print(f"      fill() is best for large uniform areas")
print(f"      setblock() should only be used for single blocks")

# Example 5: Pyramid
print("\n5. Building a pyramid (20x20x10)...")
blocks = []
base_size = 20
height = 10

for x in range(base_size):
    x_layer = []
    for y in range(height):
        y_layer = []
        # Calculate size at this height
        margin = y
        for z in range(base_size):
            if x >= margin and x < (base_size - margin) and z >= margin and z < (base_size - margin):
                # Alternate colors for visual effect
                if y % 2 == 0:
                    y_layer.append("sandstone")
                else:
                    y_layer.append("smooth_sandstone")
            else:
                y_layer.append(None)
        x_layer.append(y_layer)
    blocks.append(x_layer)

# Build 40 blocks in front and to the right of player
x, y, z = base_x + 40, base_y, base_z + 40
print(f"   Building at ({x}, {y}, {z})")
start_time = time.time()
count = mc.edit(x, y, z, blocks)
elapsed = time.time() - start_time
print(f"   ✓ Placed {count} blocks in {elapsed:.3f} seconds\n")

print("=== Examples Complete ===")
print(f"\n🎉 All structures have been built around your position!")
print(f"\n📍 Structure Locations:")
print(f"   1. 3x3x3 Cube:       ({base_x + 5}, {base_y}, {base_z})          [5 blocks in front]")
print(f"   2. Colorful Tower:   ({base_x}, {base_y}, {base_z + 15})        [15 blocks to the right]")
print(f"   3. House:            ({base_x}, {base_y}, {base_z - 15})        [15 blocks to the left]")
print(f"   4. Platform (edit):  ({base_x - 30}, {base_y}, {base_z})        [30 blocks behind]")
print(f"   5. Platform (fill):  ({base_x - 30}, {base_y}, {base_z + 60})   [30 blocks behind, 60 right]")
print(f"   6. Pyramid:          ({base_x + 40}, {base_y}, {base_z + 40})   [40 blocks diagonal]")
print(f"\n💡 Key takeaways:")
print("   • Use edit() for structures with multiple different block types")
print("   • Use fill() for large areas of the same block")
print("   • edit() supports mixed mode: simple strings or complex objects with state/NBT")
print("   • Set elements to None to skip positions and leave them unchanged")
print(f"\n👋 Thanks for trying MCPyLib's bulk edit feature!")
