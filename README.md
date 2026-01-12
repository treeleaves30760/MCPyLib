# MCPyLib

A Python library for remotely controlling Minecraft servers through a TCP-based API.

## Overview

MCPyLib enables programmatic control of Minecraft servers via Python. It consists of two components:

- **Python Client Library** - Provides a clean API for server interaction
- **Bukkit/Spigot Plugin** - Handles server-side command execution with high-performance bulk operations

### Key Features

- **High-Performance Bulk Editing** - Build large structures quickly with WorldEdit-like performance
- **Block Placement** - Set individual blocks with state and NBT support
- **Player Control** - Teleport, change gamemode, give items
- **World Management** - Control time, weather, and entities
- **Region Operations** - Fill, clone, and bulk edit regions

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

Install MCPyLib using pip:

```bash
pip install mcpylib
```

Or using uv (faster alternative):

```bash
uv pip install mcpylib
```

<details>
<summary><b>Want faster package management? Try uv!</b></summary>

[uv](https://github.com/astral-sh/uv) is a blazing-fast Python package manager (10-100x faster than pip).

**Install uv:**

```bash
# On macOS/Linux
curl -LsSf https://astral.sh/uv/install.sh | sh

# On macOS with Homebrew
brew install uv

# On Windows
powershell -c "irm https://astral.sh/uv/install.ps1 | iex"
```

**Install MCPyLib with uv:**

```bash
uv pip install mcpylib
```

**Why use uv?**
- Fast: 10-100x faster than pip
- Reliable: Automatic dependency locking
- Simple: One command to set up everything
- Isolated: Automatic virtual environment management

</details>

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

# Fast bulk edit (like WorldEdit)
blocks = [
    [["stone", "glass"], ["stone", "glass"]],
    [["stone", "glass"], ["stone", "glass"]]
]
mc.edit(100, 64, 200, blocks)
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

**`edit(x, y, z, blocks)`**

- High-performance bulk block editing (like WorldEdit)
- Place large structures with mixed block types in a single operation
- Accepts 3D array where each element can be:
  - String for simple blocks (e.g., "stone")
  - None to skip positions
  - Dict for complex blocks with state/NBT
- Ideal for building large, complex structures quickly

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

### High-Performance Bulk Editing

```python
# Build a simple house (10x5x10) with different materials
blocks = []

# Initialize structure
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

# Windows (glass)
blocks[3][2][0] = "glass"
blocks[7][2][0] = "glass"

# Roof
for x in range(10):
    for z in range(10):
        blocks[x][4][z] = "oak_planks"

# Add a chest with custom name
blocks[2][1][2] = {
    "block": "minecraft:chest",
    "block_state": {"facing": "north"},
    "nbt": {"CustomName": '{"text":"Storage"}'}
}

# Place the entire structure in one operation
count = mc.edit(100, 64, 200, blocks)
print(f"House built: {count} blocks placed")
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

See the [`examples`](examples/) directory for complete examples including:
- [`example.py`](examples/example.py) - Basic operations
- [`example_advanced.py`](examples/example_advanced.py) - Advanced block placement with states and NBT
- [`example_bulk_edit.py`](examples/example_bulk_edit.py) - High-performance bulk editing

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

**Version:** 1.0.0
**Author:** treeleaves30760
**PyPI:** https://pypi.org/project/mcpylib/
**Repository:** https://github.com/treeleaves30760/MCPyLib

## License

This project is licensed under a **Non-Commercial License with Commercial Exception**.

### Non-Commercial Use

You are free to use, modify, and distribute this software for **non-commercial purposes**, including:
- Personal projects and learning
- Academic research (without tuition or fees)
- Open-source projects
- Non-profit organizations

### Commercial Use

**Commercial use requires a separate commercial license.**

Commercial use includes:
- Use in any business or for-profit organization
- Use in **paid educational services, courses, or training programs**
- Use in educational institutions where tuition or fees are charged
- Integration into commercial products or services
- Any use that generates revenue

### Getting a Commercial License

To obtain a commercial license, please contact:

**Email:** treeleaves30760@gmail.com

For complete license terms, see the [LICENSE](LICENSE) file.

---

**Note:** This software is provided "as is" without warranty of any kind. See the LICENSE file for full terms and conditions.
