"""
Example usage of MCPyLib

This script demonstrates how to use the MCPyLib library to control
a Minecraft server remotely.
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
    # Example 1: Get player position
    print("\nGetting player position...")
    username = os.getenv("PLAYER_NAME", "Player")
    position = mc.getPos(username)  # [x, y, z]
    print(f"Player {username} position: {position}")

    # Example 2: Set a single block
    print("Setting a block...")
    result = mc.setblock(position[0], position[1] - 1,
                         position[2], "minecraft:diamond_block")
    print(f"Block set: {result}")

    # Example 3: Get block type at a location
    print("\nGetting block type...")
    block_type = mc.getblock(position[0], position[1] - 1, position[2])
    print(f"Block type: {block_type}")

    # Example 4: Fill a region with blocks
    print("\nFilling a region...")
    blocks_affected = mc.fill(position[0] + 1, position[1] - 1, position[2] + 1,
                              position[0] + 10, position[1] +
                              5, position[2] + 10,
                              "minecraft:glass")
    print(f"Blocks affected: {blocks_affected}")


except Exception as e:
    print(f"Error: {e}")
