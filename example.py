"""
Example usage of MCPyLib

This script demonstrates how to use the MCPyLib library to control
a Minecraft server remotely.
"""

from MCPyLib.src.mcpylib import MCPyLib

# Initialize the client
# Replace 'YOUR_TOKEN_HERE' with your actual token from the server
mc = MCPyLib(
    ip="127.0.0.1",
    port=65535,
    token="YOUR_TOKEN_HERE"
)

try:
    # Example 1: Set a single block
    print("Setting a block...")
    result = mc.setblock(100, 64, 200, "minecraft:diamond_block")
    print(f"Block set: {result}")

    # Example 2: Get block type at a location
    print("\nGetting block type...")
    block_type = mc.getblock(100, 64, 200)
    print(f"Block type: {block_type}")

    # Example 3: Fill a region with blocks
    print("\nFilling a region...")
    blocks_affected = mc.fill(100, 64, 200, 110, 70, 210, "minecraft:glass")
    print(f"Blocks affected: {blocks_affected}")

    # Example 4: Get player position
    print("\nGetting player position...")
    username = "YourMinecraftUsername"  # Replace with actual username
    position = mc.getPos(username)
    print(f"Player {username} position: {position}")

except Exception as e:
    print(f"Error: {e}")
