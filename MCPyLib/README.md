# MCPyLib - Python Client Library

Python client library for controlling Minecraft servers remotely.

## Installation

### From source

```bash
git clone <repository-url>
cd MCPyLib
pip install -e .
```

### Using pip (when published)

```bash
pip install mcpylib
```

## Quick Start

```python
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
setblock(x: int, y: int, z: int, block_name: str) -> int
```

Set a block at the specified coordinates.

**Parameters:**
- `x`, `y`, `z` (int): Block coordinates
- `block_name` (str): Block type (e.g., "minecraft:stone" or "stone")

**Returns:**
- `1` if successful

**Raises:**
- `ConnectionError`: Failed to connect to server
- `AuthenticationError`: Invalid token
- `CommandError`: Invalid coordinates or block type

**Example:**
```python
mc.setblock(100, 64, 200, "minecraft:diamond_block")
mc.setblock(101, 64, 200, "gold_block")  # "minecraft:" prefix is optional
```

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
