# MCPyLib

A Python library for remotely controlling Minecraft servers through a TCP-based API.

## Overview

MCPyLib enables programmatic control of Minecraft servers via Python. It consists of two components:

- **Python Client Library** - Provides a clean API for server interaction
- **Bukkit/Spigot Plugin** - Handles server-side command execution

## Quick Start

### Server Setup

1. Place `MCPyLib-Plugin-0.1.0.jar` in your server's `plugins` folder
2. Start or restart your Minecraft server
3. Generate an authentication token:

   ```bash
   /mcpylib token
   ```

4. Copy the displayed token for client authentication

### Client Installation

```bash
cd MCPyLib
pip install -e .
```

### Basic Usage

```python
from mcpylib import MCPyLib

# Initialize client
mc = MCPyLib(
    ip="127.0.0.1",
    port=65535,
    token="YOUR_TOKEN"
)

# Place a block
mc.setblock(100, 64, 200, "minecraft:diamond_block")

# Query block type
block = mc.getblock(100, 64, 200)  # Returns: "minecraft:diamond_block"

# Fill region
mc.fill(100, 64, 200, 110, 70, 210, "minecraft:glass")
```

## API Reference

### Block Operations

**`setblock(x, y, z, block_name, block_state=None, nbt=None)`**

- Place a block with optional state and NBT data
- Supports block states (e.g., stairs orientation, fence gate status)
- Supports NBT data (e.g., chest names, sign text)

```python
# Basic placement
mc.setblock(100, 64, 200, "minecraft:stone")

# With block state
mc.setblock(100, 64, 200, "minecraft:oak_stairs",
    block_state={"facing": "north", "half": "bottom"})

# With NBT data
mc.setblock(100, 64, 200, "minecraft:chest",
    nbt={"CustomName": '{"text":"Storage"}'})
```

**`getblock(x, y, z)`**

- Returns block type at specified coordinates

**`fill(x1, y1, z1, x2, y2, z2, block_name)`**

- Fills a cuboid region with specified block
- Returns number of blocks affected

**`clone(x1, y1, z1, x2, y2, z2, dest_x, dest_y, dest_z)`**

- Clones a region to a new location
- Maximum 32,768 blocks per operation

### Player Control

**`getPos(username)`**

- Returns player coordinates as `[x, y, z]`

**`teleport(username, x, y, z, yaw=None, pitch=None)`**

- Teleports player to coordinates with optional rotation

**`gamemode(username, mode)`**

- Changes player gamemode
- Valid modes: `survival`, `creative`, `adventure`, `spectator`

**`give(username, item, amount=1)`**

- Gives items to player (1-64 per operation)

### World Control

**`time(action, value=None)`**

- Controls world time
- Actions: `set` (0-24000), `add`, `query`

**`weather(condition, duration=None)`**

- Sets weather condition
- Conditions: `clear`, `rain`, `thunder`
- Duration in seconds (optional)

### Entity Control

**`summon(entity_type, x, y, z)`**

- Summons entity at coordinates
- Returns entity UUID

**`kill(selector)`**

- Removes entities matching selector
- Selectors: `all`, entity type (e.g., `zombie`), `player:username`
- Returns number of entities killed

## Examples

### Advanced Block Placement

```python
# Create a waterlogged fence
mc.setblock(100, 64, 200, "minecraft:oak_fence",
    block_state={"waterlogged": "true"})

# Place a sign with text
mc.setblock(100, 64, 200, "minecraft:oak_sign",
    block_state={"rotation": "0"},
    nbt={
        "Text1": "Welcome",
        "Text2": "to MCPyLib",
        "Text3": "",
        "Text4": ""
    })
```

### Build Automation

```python
# Create a stone platform
blocks_placed = mc.fill(0, 63, 0, 20, 63, 20, "minecraft:stone")
print(f"Platform complete: {blocks_placed} blocks")

# Clone a structure
mc.clone(0, 64, 0, 10, 74, 10, 50, 64, 50)
```

### Player Monitoring

```python
import time

while True:
    try:
        pos = mc.getPos("Steve")
        print(f"Position: {pos}")
        time.sleep(1)
    except KeyboardInterrupt:
        break
```

See [`example.py`](example.py) and [`example_advanced.py`](example_advanced.py) for complete examples.

## Requirements

| Component        | Requirement          |
| ---------------- | -------------------- |
| Python           | 3.11+                |
| Minecraft Server | Spigot/Paper 1.20.1+ |
| Java             | 17+                  |

## Error Handling

The library raises specific exceptions for different error conditions:

- `ConnectionError` - Network connectivity issues
- `AuthenticationError` - Invalid token
- `CommandError` - Invalid command parameters or execution failure

```python
try:
    mc.setblock(100, 64, 200, "invalid_block")
except CommandError as e:
    print(f"Command failed: {e}")
```

## Troubleshooting

| Issue                 | Solution                                                 |
| --------------------- | -------------------------------------------------------- |
| Connection refused    | Verify plugin is loaded (`/plugins`), check port 65535   |
| Authentication failed | Regenerate token with `/mcpylib token`                   |
| Player not found      | Ensure player is online, check username case             |

## Documentation

- [Installation Guide](docs/installation.md)
- [API Reference](docs/api-reference.md)
- [Protocol Specification](docs/protocol.md)
- [Development Guide](docs/development.md)

## Project Information

**Version:** 0.1.0
**Author:** treeleaves30760
**License:** Educational and development use
