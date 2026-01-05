# MCPyLib - Python Client Library

Python client library for controlling Minecraft servers remotely.

## Installation

### Using uv (Recommended)

[uv](https://github.com/astral-sh/uv) is a fast Python package manager that's 10-100x faster than pip.

```bash
# Install uv if you haven't already
curl -LsSf https://astral.sh/uv/install.sh | sh
# or on macOS: brew install uv

# Clone and install
git clone <repository-url>
cd MCPyLib
uv sync

# Run scripts with uv
uv run python your_script.py
```

### Using pip (Traditional method)

```bash
# Clone and install
git clone <repository-url>
cd MCPyLib
pip install -e .

# Or when published to PyPI
pip install mcpylib
```

### Why use uv?

- **Fast**: 10-100x faster than pip
- **Reliable**: Automatic dependency locking (uv.lock)
- **Simple**: One command to set up everything
- **Isolated**: Automatic virtual environment management

## Quick Start

### Create and run your first script

```python
# quickstart.py
from mcpylib import MCPyLib

# Initialize client
mc = MCPyLib(
    ip="127.0.0.1",      # Server IP
    port=65535,          # Server port
    token="your_token"   # Authentication token
)

# Set a diamond block
mc.setblock(100, 64, 200, "minecraft:diamond_block")

# Get block type
block = mc.getblock(100, 64, 200)
print(f"Block: {block}")  # Output: Block: minecraft:diamond_block

# Fill a 10x10x10 region with glass
count = mc.fill(0, 60, 0, 10, 70, 10, "minecraft:glass")
print(f"Filled {count} blocks")

# Get player position
pos = mc.getPos("Steve")
print(f"Player at: {pos}")  # Output: Player at: [100, 64, 200]
```

### Run the script

```bash
# With uv (recommended)
uv run python quickstart.py

# Or with pip
python quickstart.py
```

## API Reference

### MCPyLib Class

#### Constructor

```python
MCPyLib(ip="127.0.0.1", port=65535, token="", timeout=10.0)
```

**Parameters:**
- `ip` (str): Server IP address
- `port` (int): Server port
- `token` (str): Authentication token from server
- `timeout` (float): Socket timeout in seconds

#### Methods

##### setblock()

```python
setblock(x: int, y: int, z: int, block_name: str, block_state: dict = None, nbt: dict = None) -> int
```

Set a block at the specified coordinates with optional block state and NBT data.

**Parameters:**
- `x`, `y`, `z` (int): Block coordinates
- `block_name` (str): Block type (e.g., "minecraft:stone" or "stone")
- `block_state` (dict, optional): Block state properties like direction, rotation, etc.
- `nbt` (dict, optional): NBT data for block entities (signs, chests, etc.)

**Returns:**
- `1` if successful

**Raises:**
- `ConnectionError`: Failed to connect to server
- `AuthenticationError`: Invalid token
- `CommandError`: Invalid coordinates, block type, or state properties

**Example:**
```python
# Basic block placement
mc.setblock(100, 64, 200, "minecraft:diamond_block")
mc.setblock(101, 64, 200, "gold_block")  # "minecraft:" prefix is optional

# Place a stair facing north
mc.setblock(100, 64, 201, "oak_stairs", block_state={"facing": "north", "half": "bottom"})

# Place a fence gate facing east
mc.setblock(100, 64, 202, "oak_fence_gate", block_state={"facing": "east", "open": "false"})

# Place a log with horizontal axis
mc.setblock(100, 64, 203, "oak_log", block_state={"axis": "x"})

# Place a sign with text
mc.setblock(100, 64, 204, "oak_sign",
    block_state={"rotation": "0"},
    nbt={"Text1": "Hello", "Text2": "World", "Text3": "", "Text4": ""}
)

# Place a chest with custom name
mc.setblock(100, 64, 205, "chest",
    block_state={"facing": "north"},
    nbt={"CustomName": '{"text":"My Chest"}'}
)

# Place a waterlogged fence
mc.setblock(100, 64, 206, "oak_fence", block_state={"waterlogged": "true"})
```

**Common Block State Properties:**
- `facing`: Direction the block faces ("north", "south", "east", "west", "up", "down")
- `rotation`: Rotation for signs and banners ("0" to "15")
- `axis`: Axis for logs and pillars ("x", "y", "z")
- `half`: Upper or lower half ("top", "bottom")
- `shape`: Stair shape ("straight", "inner_left", "inner_right", "outer_left", "outer_right")
- `open`: Whether doors/gates are open ("true", "false")
- `powered`: Whether redstone components are powered ("true", "false")
- `waterlogged`: Whether block contains water ("true", "false")

##### getblock()

```python
getblock(x: int, y: int, z: int) -> str
```

Get the block type at coordinates.

**Parameters:**
- `x`, `y`, `z` (int): Block coordinates

**Returns:**
- Block type as string (e.g., "minecraft:stone")

**Raises:**
- `ConnectionError`: Failed to connect to server
- `AuthenticationError`: Invalid token
- `CommandError`: Invalid coordinates

**Example:**
```python
block = mc.getblock(100, 64, 200)
print(block)  # "minecraft:diamond_block"
```

##### fill()

```python
fill(x1: int, y1: int, z1: int, x2: int, y2: int, z2: int, block_name: str) -> int
```

Fill a region with the specified block type.

**Parameters:**
- `x1`, `y1`, `z1` (int): Starting coordinates
- `x2`, `y2`, `z2` (int): Ending coordinates
- `block_name` (str): Block type

**Returns:**
- Number of blocks affected

**Raises:**
- `ConnectionError`: Failed to connect to server
- `AuthenticationError`: Invalid token
- `CommandError`: Invalid coordinates or block type

**Example:**
```python
# Fill a 10x10x10 cube with glass
count = mc.fill(0, 60, 0, 10, 70, 10, "minecraft:glass")
print(f"Filled {count} blocks")  # "Filled 1331 blocks"
```

##### getPos()

```python
getPos(username: str) -> List[int]
```

Get a player's current position.

**Parameters:**
- `username` (str): Player's Minecraft username (case-sensitive)

**Returns:**
- List of `[x, y, z]` coordinates (integers)

**Raises:**
- `ConnectionError`: Failed to connect to server
- `AuthenticationError`: Invalid token
- `CommandError`: Player not found or not online

**Example:**
```python
pos = mc.getPos("Steve")
x, y, z = pos
print(f"Steve is at ({x}, {y}, {z})")
```

##### teleport()

```python
teleport(username: str, x: float, y: float, z: float, yaw: float = None, pitch: float = None) -> bool
```

Teleport a player to specified coordinates.

**Parameters:**
- `username` (str): Player's Minecraft username
- `x`, `y`, `z` (float): Target coordinates
- `yaw` (float, optional): Rotation yaw angle
- `pitch` (float, optional): Rotation pitch angle

**Returns:**
- `True` if successful

**Raises:**
- `ConnectionError`: Failed to connect to server
- `AuthenticationError`: Invalid token
- `CommandError`: Player not found or teleport fails

**Example:**
```python
# Teleport to coordinates
mc.teleport("Steve", 100, 64, 200)

# Teleport with rotation
mc.teleport("Steve", 100, 64, 200, yaw=90.0, pitch=0.0)
```

##### gamemode()

```python
gamemode(username: str, mode: str) -> bool
```

Change a player's game mode.

**Parameters:**
- `username` (str): Player's Minecraft username
- `mode` (str): Game mode ("survival", "creative", "adventure", "spectator")

**Returns:**
- `True` if successful

**Raises:**
- `ConnectionError`: Failed to connect to server
- `AuthenticationError`: Invalid token
- `CommandError`: Player not found or invalid game mode

**Example:**
```python
mc.gamemode("Steve", "creative")
mc.gamemode("Alex", "survival")
mc.gamemode("Bob", "spectator")
```

##### time()

```python
time(action: str, value: int = None) -> int
```

Control world time.

**Parameters:**
- `action` (str): Action to perform ("set", "add", "query")
- `value` (int, optional): Time value (0-24000) for "set" or "add" actions

**Returns:**
- Current world time (0-24000)

**Raises:**
- `ConnectionError`: Failed to connect to server
- `AuthenticationError`: Invalid token
- `CommandError`: Invalid action or value out of range

**Example:**
```python
# Set time to day (1000)
mc.time("set", 1000)

# Add 6000 ticks
mc.time("add", 6000)

# Query current time
current_time = mc.time("query")
print(f"Current time: {current_time}")
```

##### weather()

```python
weather(condition: str, duration: int = None) -> bool
```

Control world weather.

**Parameters:**
- `condition` (str): Weather condition ("clear", "rain", "thunder")
- `duration` (int, optional): Duration in seconds

**Returns:**
- `True` if successful

**Raises:**
- `ConnectionError`: Failed to connect to server
- `AuthenticationError`: Invalid token
- `CommandError`: Invalid weather condition

**Example:**
```python
# Clear weather
mc.weather("clear")

# Rain for 10 minutes (600 seconds)
mc.weather("rain", duration=600)

# Thunder storm for 5 minutes
mc.weather("thunder", duration=300)
```

##### give()

```python
give(username: str, item: str, amount: int = 1) -> bool
```

Give items to a player.

**Parameters:**
- `username` (str): Player's Minecraft username
- `item` (str): Item type (e.g., "minecraft:diamond" or "diamond")
- `amount` (int): Quantity (1-64, default: 1)

**Returns:**
- `True` if successful

**Raises:**
- `ConnectionError`: Failed to connect to server
- `AuthenticationError`: Invalid token
- `CommandError`: Player not found, invalid item, or amount out of range

**Example:**
```python
# Give 10 diamonds
mc.give("Steve", "minecraft:diamond", 10)

# Give 1 diamond sword (amount defaults to 1)
mc.give("Steve", "diamond_sword")

# Give 64 stone blocks
mc.give("Alex", "stone", 64)
```

##### summon()

```python
summon(entity_type: str, x: float, y: float, z: float) -> str
```

Summon an entity at specified coordinates.

**Parameters:**
- `entity_type` (str): Entity type (e.g., "minecraft:zombie", "pig", "creeper")
- `x`, `y`, `z` (float): Spawn coordinates

**Returns:**
- Entity UUID as string

**Raises:**
- `ConnectionError`: Failed to connect to server
- `AuthenticationError`: Invalid token
- `CommandError`: Invalid entity type or coordinates

**Example:**
```python
# Summon a zombie
uuid = mc.summon("minecraft:zombie", 100, 64, 200)

# Summon a pig (namespace prefix optional)
uuid = mc.summon("pig", 150, 70, 250)

# Summon a creeper
uuid = mc.summon("creeper", 200, 64, 300)
```

##### kill()

```python
kill(selector: str) -> int
```

Remove entities from the world.

**Parameters:**
- `selector` (str): Entity selector:
  - `"all"` - Kill all entities
  - Entity type (e.g., `"zombie"`, `"creeper"`) - Kill all of that type
  - `"player:username"` - Kill a specific player

**Returns:**
- Number of entities killed

**Raises:**
- `ConnectionError`: Failed to connect to server
- `AuthenticationError`: Invalid token
- `CommandError`: Invalid selector or player not found

**Example:**
```python
# Kill all entities
count = mc.kill("all")
print(f"Killed {count} entities")

# Kill all zombies
zombie_count = mc.kill("zombie")

# Kill all creepers
creeper_count = mc.kill("creeper")

# Kill a specific player
mc.kill("player:Steve")
```

##### edit()

```python
edit(x: int, y: int, z: int, blocks: List[List[List]]) -> int
```

High-performance bulk block editing (like WorldEdit). Place large structures with mixed block types in a single operation.

**Parameters:**
- `x`, `y`, `z` (int): Starting coordinates
- `blocks` (List[List[List]]): 3D array of blocks [x][y][z], where each element can be:
  - `str`: Block name (e.g., "stone", "minecraft:glass")
  - `None`: Skip this position (leave unchanged)
  - `dict`: Complex block with state and NBT:
    ```python
    {
        "block": "minecraft:chest",
        "block_state": {"facing": "north"},  # Optional
        "nbt": {"CustomName": '{"text":"Storage"}'}  # Optional
    }
    ```

**Returns:**
- Number of blocks placed

**Raises:**
- `ConnectionError`: Failed to connect to server
- `AuthenticationError`: Invalid token
- `CommandError`: Invalid block data or coordinates

**Example:**
```python
# Simple 2x2x2 cube
blocks = [
    [["stone", "glass"], ["stone", "glass"]],
    [["stone", "glass"], ["stone", "glass"]]
]
mc.edit(100, 64, 200, blocks)

# Mixed mode with complex blocks
blocks = [
    [[None, "stone"], ["glass", None]],
    [[{
        "block": "minecraft:chest",
        "block_state": {"facing": "north"},
        "nbt": {"CustomName": '{"text":"Loot"}'}
    }, "stone"], ["diamond_block", "gold_block"]]
]
mc.edit(100, 64, 200, blocks)

# Build a house
blocks = []
for x in range(10):
    x_layer = []
    for y in range(5):
        y_layer = [None] * 10
        x_layer.append(y_layer)
    blocks.append(x_layer)

# Floor
for x in range(10):
    for z in range(10):
        blocks[x][0][z] = "stone"

# Walls
for y in range(1, 4):
    for x in range(10):
        blocks[x][y][0] = "cobblestone"
        blocks[x][y][9] = "cobblestone"
    for z in range(10):
        blocks[0][y][z] = "cobblestone"
        blocks[9][y][z] = "cobblestone"

# Roof
for x in range(10):
    for z in range(10):
        blocks[x][4][z] = "oak_planks"

count = mc.edit(100, 64, 200, blocks)
print(f"House built: {count} blocks")
```

**Performance Notes:**
- Use `edit()` for structures with multiple different block types
- Use `fill()` for large areas of the same block
- Much faster than calling `setblock()` repeatedly
- Supports thousands of blocks in a single operation

##### clone()

```python
clone(x1: int, y1: int, z1: int, x2: int, y2: int, z2: int, dest_x: int, dest_y: int, dest_z: int) -> int
```

Clone a region of blocks to a new location.

**Parameters:**
- `x1`, `y1`, `z1` (int): Source region start coordinates
- `x2`, `y2`, `z2` (int): Source region end coordinates
- `dest_x`, `dest_y`, `dest_z` (int): Destination coordinates

**Returns:**
- Number of blocks cloned

**Raises:**
- `ConnectionError`: Failed to connect to server
- `AuthenticationError`: Invalid token
- `CommandError`: Region too large (max 32,768 blocks)

**Example:**
```python
# Clone a 10x10x10 region
count = mc.clone(0, 64, 0, 10, 74, 10, 20, 64, 0)
print(f"Cloned {count} blocks")

# Copy a house from one location to another
mc.clone(100, 64, 100, 110, 74, 110, 200, 64, 200)
```

## Exception Handling

MCPyLib defines custom exceptions:

```python
from mcpylib import MCPyLib, MCPyLibError, ConnectionError, AuthenticationError, CommandError

try:
    mc = MCPyLib(token="wrong_token")
    mc.setblock(100, 64, 200, "minecraft:stone")
except AuthenticationError as e:
    print(f"Authentication failed: {e}")
except ConnectionError as e:
    print(f"Connection failed: {e}")
except CommandError as e:
    print(f"Command failed: {e}")
except MCPyLibError as e:
    print(f"General error: {e}")
```

## Block Names

Block names can be specified with or without the "minecraft:" namespace prefix:

```python
# Both are valid
mc.setblock(0, 64, 0, "minecraft:stone")
mc.setblock(0, 64, 0, "stone")

# Common blocks
mc.setblock(0, 64, 0, "grass_block")
mc.setblock(0, 65, 0, "oak_log")
mc.setblock(0, 66, 0, "diamond_block")
mc.setblock(0, 67, 0, "glowstone")
```

See the [Minecraft Wiki](https://minecraft.fandom.com/wiki/Java_Edition_data_values) for a complete list of block IDs.

## Advanced Usage

### Custom Timeout

```python
# Set a longer timeout for slow connections
mc = MCPyLib(
    ip="192.168.1.100",
    port=65535,
    token="your_token",
    timeout=30.0  # 30 seconds
)
```

### Building Structures

```python
def build_cube(mc, x, y, z, size, block):
    """Build a hollow cube"""
    # Bottom and top
    mc.fill(x, y, z, x+size, y, z+size, block)
    mc.fill(x, y+size, z, x+size, y+size, z+size, block)

    # Sides
    mc.fill(x, y, z, x, y+size, z+size, block)
    mc.fill(x+size, y, z, x+size, y+size, z+size, block)
    mc.fill(x, y, z, x+size, y+size, z, block)
    mc.fill(x, y, z+size, x+size, y+size, z+size, block)

# Build a 10x10x10 glass cube
build_cube(mc, 0, 64, 0, 10, "glass")
```

### Following Players

```python
import time

def follow_player(mc, username, interval=1.0):
    """Track player movement"""
    try:
        while True:
            pos = mc.getPos(username)
            print(f"{username} is at {pos}")
            time.sleep(interval)
    except KeyboardInterrupt:
        print("Stopped tracking")
    except CommandError as e:
        print(f"Error: {e}")

follow_player(mc, "Steve")
```

## Requirements

- Python 3.11 or higher
- No external dependencies (uses only standard library)

## See Also

- [Main Documentation](../README.md)
- [Server Plugin Documentation](../ServerPlugin/README.md)
- [Communication Protocol](../PROTOCOL.md)
- [Example Scripts](../example.py)
