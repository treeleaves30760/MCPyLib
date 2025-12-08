# API Reference

Complete reference for the MCPyLib Python API.

## MCPyLib Class

### Constructor

```python
MCPyLib(ip="127.0.0.1", port=65535, token="", timeout=10.0)
```

Initialize a new MCPyLib client connection.

**Parameters:**
- `ip` (str, optional): Server IP address. Default: `"127.0.0.1"`
- `port` (int, optional): Server port. Default: `65535`
- `token` (str, required): Authentication token from server
- `timeout` (float, optional): Socket timeout in seconds. Default: `10.0`

**Example:**
```python
from mcpylib import MCPyLib

mc = MCPyLib(
    ip="127.0.0.1",
    port=65535,
    token="your_token_here",
    timeout=15.0
)
```

## Methods

### setblock()

```python
setblock(x: int, y: int, z: int, block_name: str) -> int
```

Place a block at the specified coordinates.

**Parameters:**
- `x` (int): X coordinate
- `y` (int): Y coordinate (vertical)
- `z` (int): Z coordinate
- `block_name` (str): Block type (e.g., `"minecraft:stone"` or `"stone"`)

**Returns:**
- `int`: `1` if successful

**Raises:**
- `ConnectionError`: Failed to connect to server
- `AuthenticationError`: Invalid or missing token
- `CommandError`: Invalid coordinates or block type

**Example:**
```python
# Both formats work
mc.setblock(100, 64, 200, "minecraft:diamond_block")
mc.setblock(101, 64, 200, "gold_block")

# Common blocks
mc.setblock(0, 64, 0, "stone")
mc.setblock(0, 65, 0, "glass")
mc.setblock(0, 66, 0, "oak_log")
```

### getblock()

```python
getblock(x: int, y: int, z: int) -> str
```

Get the block type at specified coordinates.

**Parameters:**
- `x` (int): X coordinate
- `y` (int): Y coordinate (vertical)
- `z` (int): Z coordinate

**Returns:**
- `str`: Block type identifier (e.g., `"minecraft:stone"`)

**Raises:**
- `ConnectionError`: Failed to connect to server
- `AuthenticationError`: Invalid or missing token
- `CommandError`: Invalid coordinates

**Example:**
```python
block = mc.getblock(100, 64, 200)
print(f"Block type: {block}")  # "minecraft:diamond_block"

# Check if a specific block exists
if mc.getblock(0, 64, 0) == "minecraft:air":
    print("No block at this location")
```

### fill()

```python
fill(x1: int, y1: int, z1: int, x2: int, y2: int, z2: int, block_name: str) -> int
```

Fill a rectangular region with the specified block type.

**Parameters:**
- `x1, y1, z1` (int): Starting corner coordinates
- `x2, y2, z2` (int): Ending corner coordinates
- `block_name` (str): Block type to fill with

**Returns:**
- `int`: Number of blocks affected

**Raises:**
- `ConnectionError`: Failed to connect to server
- `AuthenticationError`: Invalid or missing token
- `CommandError`: Invalid coordinates or block type

**Example:**
```python
# Fill a 10x10x10 cube with glass
count = mc.fill(0, 60, 0, 10, 70, 10, "glass")
print(f"Filled {count} blocks")

# Create a floor
mc.fill(0, 63, 0, 20, 63, 20, "stone")

# Build a wall
mc.fill(0, 64, 0, 0, 70, 20, "cobblestone")
```

**Note:** The order of coordinates doesn't matter - the function automatically determines the bounding box.

### getPos()

```python
getPos(username: str) -> List[int]
```

Get a player's current position.

**Parameters:**
- `username` (str): Player's Minecraft username (case-sensitive)

**Returns:**
- `List[int]`: List of `[x, y, z]` coordinates (integers, rounded down)

**Raises:**
- `ConnectionError`: Failed to connect to server
- `AuthenticationError`: Invalid or missing token
- `CommandError`: Player not found or not online

**Example:**
```python
# Get player position
pos = mc.getPos("Steve")
x, y, z = pos
print(f"Steve is at ({x}, {y}, {z})")

# Place a block at player's feet
pos = mc.getPos("Steve")
mc.setblock(pos[0], pos[1] - 1, pos[2], "diamond_block")

# Track player movement
import time
while True:
    pos = mc.getPos("Steve")
    print(f"Position: {pos}")
    time.sleep(1)
```

## Exceptions

MCPyLib defines custom exception classes for error handling.

### MCPyLibError

Base exception class for all MCPyLib errors.

```python
class MCPyLibError(Exception):
    """Base exception for MCPyLib"""
    pass
```

### ConnectionError

Raised when connection to server fails.

```python
class ConnectionError(MCPyLibError):
    """Failed to connect to server"""
    pass
```

**Common causes:**
- Server is not running
- Wrong IP or port
- Firewall blocking connection
- Plugin not loaded

### AuthenticationError

Raised when authentication fails.

```python
class AuthenticationError(MCPyLibError):
    """Authentication failed"""
    pass
```

**Common causes:**
- Wrong token
- Token has extra whitespace
- Token was regenerated on server
- Authentication disabled in config

### CommandError

Raised when a command fails to execute.

```python
class CommandError(MCPyLibError):
    """Command execution failed"""
    pass
```

**Common causes:**
- Invalid coordinates (out of world bounds)
- Invalid block type
- Player not found
- Player not online

### Exception Handling Example

```python
from mcpylib import MCPyLib, MCPyLibError, ConnectionError, AuthenticationError, CommandError

try:
    mc = MCPyLib(token="your_token")
    mc.setblock(100, 64, 200, "minecraft:stone")

except ConnectionError as e:
    print(f"Cannot connect to server: {e}")
    print("Check that server is running and plugin is loaded")

except AuthenticationError as e:
    print(f"Authentication failed: {e}")
    print("Get the correct token with /mcpylib token")

except CommandError as e:
    print(f"Command failed: {e}")
    print("Check coordinates and block type are valid")

except MCPyLibError as e:
    print(f"MCPyLib error: {e}")
```

## Block Names

Block names can be specified with or without the `minecraft:` namespace prefix.

### Common Block Types

```python
# Building blocks
"stone", "cobblestone", "brick", "stone_bricks"
"oak_planks", "spruce_planks", "birch_planks"
"glass", "white_concrete", "black_concrete"

# Natural blocks
"dirt", "grass_block", "sand", "gravel"
"oak_log", "oak_leaves", "water", "lava"

# Ores and minerals
"coal_ore", "iron_ore", "gold_ore", "diamond_ore"
"coal_block", "iron_block", "gold_block", "diamond_block"

# Decorative blocks
"glowstone", "sea_lantern", "redstone_lamp"
"bookshelf", "crafting_table", "furnace"
```

For a complete list of block IDs, see the [Minecraft Wiki](https://minecraft.fandom.com/wiki/Java_Edition_data_values).

## Advanced Usage

### Connection Pooling

MCPyLib creates a new connection for each command. For better performance with many commands:

```python
# Execute multiple commands quickly
mc = MCPyLib(token="your_token")

blocks_to_place = [
    (0, 64, 0, "stone"),
    (1, 64, 0, "stone"),
    (2, 64, 0, "stone"),
]

for x, y, z, block in blocks_to_place:
    mc.setblock(x, y, z, block)
```

### Custom Timeout

For slow connections or complex operations:

```python
# Set longer timeout for slow connections
mc = MCPyLib(
    ip="192.168.1.100",
    token="your_token",
    timeout=30.0  # 30 seconds
)

# Large fill operations may take longer
mc.fill(0, 0, 0, 100, 100, 100, "stone")
```

### Building Complex Structures

```python
def build_pyramid(mc, x, y, z, size, block):
    """Build a pyramid structure"""
    for level in range(size):
        mc.fill(
            x + level, y + level, z + level,
            x + size - level - 1, y + level, z + size - level - 1,
            block
        )

# Build a 20-block tall pyramid
build_pyramid(mc, 0, 64, 0, 20, "sandstone")
```

### Player Tracking

```python
def track_player(mc, username, duration=60):
    """Track player for specified duration"""
    import time

    start_time = time.time()
    positions = []

    while time.time() - start_time < duration:
        try:
            pos = mc.getPos(username)
            positions.append(pos)
            print(f"{username} at {pos}")
            time.sleep(1)
        except CommandError:
            print(f"Player {username} not found")
            break

    return positions

# Track Steve for 60 seconds
history = track_player(mc, "Steve", 60)
```

### Error Recovery

```python
def safe_setblock(mc, x, y, z, block, retries=3):
    """Set block with automatic retry"""
    import time

    for attempt in range(retries):
        try:
            mc.setblock(x, y, z, block)
            return True
        except ConnectionError:
            if attempt < retries - 1:
                print(f"Retry {attempt + 1}/{retries}")
                time.sleep(1)
            else:
                print("Failed after all retries")
                return False
```

## Performance Tips

1. **Use fill() for large areas** - Much faster than individual setblock() calls
2. **Batch operations** - Group multiple commands together
3. **Handle errors gracefully** - Implement retry logic for network errors
4. **Adjust timeout** - Increase for slow connections or large operations
5. **Use appropriate block names** - Both formats work, but consistency helps

## See Also

- [Installation Guide](installation.md) - Setup and configuration
- [Protocol Documentation](protocol.md) - Technical details
- [Development Guide](development.md) - Contributing and building
- [Example Scripts](../example.py) - More code examples
