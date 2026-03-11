# MCPyLib

[![PyPI Downloads](https://static.pepy.tech/personalized-badge/mcpylib?period=total&units=INTERNATIONAL_SYSTEM&left_color=BLACK&right_color=GREEN&left_text=downloads)](https://pepy.tech/projects/mcpylib)
<br>
A Python library for remotely controlling Minecraft servers through a TCP-based API.

## Overview

MCPyLib enables programmatic control of Minecraft servers via Python. It consists of two components:

- **Python Client Library** - Provides a clean API for server interaction
- **Bukkit/Spigot Plugin** - Handles server-side command execution with high-performance bulk operations

### Key Features

- **High-Performance Bulk Editing** - Build large structures quickly with WorldEdit-like performance
- **Block Operations** - Set, get, fill, clone, and bulk edit blocks with state and NBT support
- **Player Control** - Teleport, change gamemode, give items, apply effects, manage experience
- **Inventory Management** - Get and set items in player inventories
- **Entity Control** - Summon, kill, teleport, set velocity/rotation, manage AI and targeting for 10+ entity commands
- **Entity Equipment & Interaction** - Equip entities, deal damage, manage riding, and modify attributes
- **Entity Tags** - Add, remove, and query entity tags
- **Villager System** - Query villager data, set professions, and configure custom trades
- **Scoreboard & Teams** - Full scoreboard objective management, score tracking, and team control
- **Boss Bars** - Create and manage custom boss bars
- **Chat & Display** - Send messages, raw JSON text, titles, play and stop sounds
- **World Settings** - Control time, weather, difficulty, game rules, and default gamemode
- **World Generation** - Locate structures, generate loot, fill biomes, and place features/structures/jigsaws/templates
- **World Border & Chunks** - Manage world border and force-load chunks
- **Particles & Spawn** - Create particle effects and set spawn points
- **Server Utilities** - Execute raw commands, spread players, list online players, manage advancements

## Quick Start

### Server Setup

1. Place `MCPyLib-Plugin-1.1.0.jar` in your server's `plugins` folder
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

MCPyLib provides **71 commands** across 18 categories.

### Block Operations (5)

| Method | Parameters | Description |
| ------ | ---------- | ----------- |
| `setblock` | `x, y, z, block_name, block_state=None, nbt=None` | Place a block with optional state and NBT data |
| `getblock` | `x, y, z` | Get the block type at coordinates |
| `fill` | `x1, y1, z1, x2, y2, z2, block_name` | Fill a cuboid region with a block type |
| `clone` | `x1, y1, z1, x2, y2, z2, dest_x, dest_y, dest_z` | Clone a region to a new location (max 32,768 blocks) |
| `edit` | `x, y, z, blocks` | High-performance bulk block editing with a 3D array |

### Player Control (4)

| Method | Parameters | Description |
| ------ | ---------- | ----------- |
| `getPos` | `username` | Get player coordinates as `[x, y, z]` |
| `teleport` | `username, x, y, z, yaw=None, pitch=None` | Teleport player to coordinates with optional rotation |
| `gamemode` | `username, mode` | Set player gamemode (`survival`, `creative`, `adventure`, `spectator`) |
| `give` | `username, item, amount=1` | Give items to a player (1-64 per operation) |

### Player Effects & Inventory (5)

| Method | Parameters | Description |
| ------ | ---------- | ----------- |
| `effect` | `username, effect_id, duration=30, amplifier=0, hide_particles=False` | Apply a status effect to a player |
| `clearEffect` | `username, effect_id=None` | Remove a specific effect or all effects from a player |
| `clear` | `username, item=None, max_count=None` | Clear items from a player's inventory |
| `experience` | `username, action, amount, type="points"` | Add, set, or query player experience (points or levels) |
| `enchant` | `username, enchantment, level=1` | Enchant the item the player is holding |

### Inventory Management (2)

| Method | Parameters | Description |
| ------ | ---------- | ----------- |
| `getItem` | `username, slot` | Get item data from a specific inventory slot |
| `setItem` | `username, slot, item, amount=1, nbt=None` | Set an item in a specific inventory slot |

### World Settings (5)

| Method | Parameters | Description |
| ------ | ---------- | ----------- |
| `time` | `action, value=None` | Control world time (`set`, `add`, `query`) |
| `weather` | `condition, duration=None` | Set weather (`clear`, `rain`, `thunder`) with optional duration |
| `difficulty` | `level` | Set server difficulty (`peaceful`, `easy`, `normal`, `hard`) |
| `gamerule` | `rule, value=None` | Get or set a game rule |
| `defaultgamemode` | `mode` | Set the default gamemode for new players |

### Chat & Display (6)

| Method | Parameters | Description |
| ------ | ---------- | ----------- |
| `say` | `message` | Broadcast a message to all players |
| `tell` | `username, message` | Send a private message to a player |
| `tellraw` | `username, json_text` | Send a raw JSON text message to a player |
| `title` | `username, title_type, text, fade_in=None, stay=None, fade_out=None` | Display a title, subtitle, or actionbar text |
| `playsound` | `sound, source, username, x=None, y=None, z=None, volume=1, pitch=1` | Play a sound for a player |
| `stopsound` | `username, source=None, sound=None` | Stop playing sounds for a player |

### Particle & Spawn (3)

| Method | Parameters | Description |
| ------ | ---------- | ----------- |
| `particle` | `particle_type, x, y, z, dx=0, dy=0, dz=0, speed=0, count=1` | Create particle effects at a location |
| `spawnpoint` | `username, x=None, y=None, z=None` | Set a player's spawn point |
| `setworldspawn` | `x, y, z` | Set the world's default spawn point |

### World Border & Chunks (2)

| Method | Parameters | Description |
| ------ | ---------- | ----------- |
| `worldborder` | `action, value=None, time=None` | Manage the world border (set, add, get, center, damage, warning) |
| `forceload` | `action, x1, z1, x2=None, z2=None` | Force-load or unload chunks |

### Entity Control (10)

| Method | Parameters | Description |
| ------ | ---------- | ----------- |
| `summon` | `entity_type, x, y, z` | Summon an entity at coordinates, returns UUID |
| `kill` | `selector` | Remove entities matching a selector |
| `getEntityPos` | `uuid` | Get entity coordinates as `[x, y, z]` |
| `getEntityStatus` | `uuid` | Get entity status (health, type, custom name, etc.) |
| `teleportEntity` | `uuid, x, y, z, yaw=None, pitch=None` | Teleport an entity to coordinates |
| `setEntityVelocity` | `uuid, vx, vy, vz` | Set an entity's velocity vector |
| `setEntityRotation` | `uuid, yaw, pitch` | Set an entity's rotation |
| `setEntityAI` | `uuid, enabled` | Enable or disable an entity's AI |
| `setEntityTarget` | `uuid, target_uuid` | Set an entity's attack target |
| `removeEntity` | `uuid` | Remove a specific entity by UUID |

### Entity Equipment (2)

| Method | Parameters | Description |
| ------ | ---------- | ----------- |
| `getEntityEquipment` | `uuid` | Get all equipment slots for an entity |
| `setEntityEquipment` | `uuid, slot, item, nbt=None` | Set equipment in a specific slot (head, chest, legs, feet, mainhand, offhand) |

### Entity Interaction (3)

| Method | Parameters | Description |
| ------ | ---------- | ----------- |
| `damage` | `uuid, amount, damage_type=None` | Deal damage to an entity |
| `ride` | `uuid, vehicle_uuid` | Make an entity ride another entity |
| `attribute` | `uuid, attribute_name, value=None, modifier=None` | Get or modify entity attributes |

### Entity Tags (3)

| Method | Parameters | Description |
| ------ | ---------- | ----------- |
| `addTag` | `uuid, tag` | Add a scoreboard tag to an entity |
| `removeTag` | `uuid, tag` | Remove a scoreboard tag from an entity |
| `getTags` | `uuid` | Get all scoreboard tags on an entity |

### Villager (3)

| Method | Parameters | Description |
| ------ | ---------- | ----------- |
| `getVillagerData` | `uuid` | Get villager profession, level, and trade data |
| `setVillagerProfession` | `uuid, profession` | Set a villager's profession |
| `setVillagerTrades` | `uuid, trades` | Configure custom trades for a villager |

### Scoreboard (5)

| Method | Parameters | Description |
| ------ | ---------- | ----------- |
| `addObjective` | `name, criteria, display_name=None` | Create a new scoreboard objective |
| `removeObjective` | `name` | Remove a scoreboard objective |
| `setScore` | `player, objective, value` | Set a player's score for an objective |
| `getScore` | `player, objective` | Get a player's score for an objective |
| `setDisplaySlot` | `slot, objective=None` | Set which objective is displayed in a slot (sidebar, list, belowName) |

### Teams (1)

| Method | Parameters | Description |
| ------ | ---------- | ----------- |
| `team` | `action, name=None, **kwargs` | Manage teams (add, remove, join, leave, modify options) |

### Boss Bar (1)

| Method | Parameters | Description |
| ------ | ---------- | ----------- |
| `bossbar` | `action, bar_id=None, **kwargs` | Manage boss bars (add, remove, set value/max/color/style/players) |

### World Generation (7)

| Method | Parameters | Description |
| ------ | ---------- | ----------- |
| `locate` | `structure_type` | Find the nearest structure of a given type |
| `loot` | `action, **kwargs` | Generate and give loot from loot tables |
| `fillbiome` | `x1, y1, z1, x2, y2, z2, biome` | Fill a region with a specific biome |
| `placeFeature` | `feature, x, y, z` | Place a configured feature at a location |
| `placeStructure` | `structure, x, y, z` | Place a structure at a location |
| `placeJigsaw` | `pool, target, max_depth, x, y, z` | Place a jigsaw structure at a location |
| `placeTemplate` | `template, x, y, z, rotation=None, mirror=None` | Place a structure template with optional rotation and mirroring |

### Server & Utility (4)

| Method | Parameters | Description |
| ------ | ---------- | ----------- |
| `exec` | `command` | Execute a raw server command |
| `spreadplayers` | `x, z, spread_distance, max_range, respect_teams, targets` | Spread players randomly across an area |
| `list` | | Get a list of online players |
| `advancement` | `action, username, advancement=None` | Grant, revoke, or query player advancements |

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

### Effects & Experience

```python
# Give a player night vision for 60 seconds
mc.effect("Steve", "minecraft:night_vision", duration=60, amplifier=0)

# Give speed boost with hidden particles
mc.effect("Steve", "minecraft:speed", duration=120, amplifier=2, hide_particles=True)

# Clear all effects from a player
mc.clearEffect("Steve")

# Add 10 experience levels
mc.experience("Steve", "add", 10, type="levels")

# Query current experience
xp = mc.experience("Steve", "query", 0, type="levels")
print(f"Player level: {xp}")

# Enchant the held item
mc.enchant("Steve", "minecraft:sharpness", level=5)
```

### Chat & Titles

```python
# Broadcast a message
mc.say("Server restarting in 5 minutes!")

# Send a private message
mc.tell("Steve", "You have been selected for a quest!")

# Send formatted JSON text
mc.tellraw("Steve", [
    {"text": "Click ", "color": "white"},
    {"text": "[HERE]", "color": "green", "bold": True,
     "clickEvent": {"action": "run_command", "value": "/spawn"}}
])

# Display a title with subtitle
mc.title("Steve", "title", "Welcome!")
mc.title("Steve", "subtitle", "Enjoy your stay")
mc.title("Steve", "times", None, fade_in=20, stay=60, fade_out=20)

# Play a sound
mc.playsound("minecraft:entity.ender_dragon.growl", "master", "Steve")

# Display actionbar text
mc.title("Steve", "actionbar", "Health: 20/20")
```

### Scoreboard & Teams

```python
# Create a scoreboard objective
mc.addObjective("kills", "playerKillCount", "Player Kills")

# Display it on the sidebar
mc.setDisplaySlot("sidebar", "kills")

# Set and get scores
mc.setScore("Steve", "kills", 15)
score = mc.getScore("Steve", "kills")
print(f"Steve's kills: {score}")

# Create and configure a team
mc.team("add", "red_team")
mc.team("modify", "red_team", color="red", prefix="[RED] ")
mc.team("join", "red_team", members=["Steve", "Alex"])

# Clean up
mc.team("remove", "red_team")
mc.removeObjective("kills")
```

### Boss Bars

```python
# Create a boss bar
mc.bossbar("add", "my_bar", name="Dungeon Progress")

# Configure the boss bar
mc.bossbar("set", "my_bar", value=50, max=100)
mc.bossbar("set", "my_bar", color="red", style="segmented_10")

# Show it to specific players
mc.bossbar("set", "my_bar", players=["Steve", "Alex"])

# Update progress over time
import time
for i in range(101):
    mc.bossbar("set", "my_bar", value=i)
    time.sleep(0.5)

# Remove the boss bar
mc.bossbar("remove", "my_bar")
```

### Entity Interaction

```python
# Summon and control a zombie
uuid = mc.summon("minecraft:zombie", 100, 64, 200)

# Get entity info
status = mc.getEntityStatus(uuid)
print(f"Entity health: {status['health']}")

# Disable AI and position the entity
mc.setEntityAI(uuid, False)
mc.teleportEntity(uuid, 105, 64, 205, yaw=90, pitch=0)

# Equip the entity
mc.setEntityEquipment(uuid, "head", "minecraft:diamond_helmet")
mc.setEntityEquipment(uuid, "mainhand", "minecraft:diamond_sword")

# Launch the entity upward
mc.setEntityVelocity(uuid, 0, 1.5, 0)

# Tag entities for later reference
mc.addTag(uuid, "arena_mob")
tags = mc.getTags(uuid)
print(f"Tags: {tags}")

# Deal damage to the entity
mc.damage(uuid, 5.0, "minecraft:player_attack")

# Make one entity ride another
pig_uuid = mc.summon("minecraft:pig", 100, 64, 200)
mc.ride(uuid, pig_uuid)

# Clean up
mc.removeEntity(uuid)
mc.removeEntity(pig_uuid)
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
- [`basics/01_getting_started.py`](examples/basics/01_getting_started.py) - Basic operations
- [`basics/02_block_states_and_nbt.py`](examples/basics/02_block_states_and_nbt.py) - Advanced block placement with states and NBT
- [`basics/03_entity_control.py`](examples/basics/03_entity_control.py) - Entity control and management
- [`buildings/build_cabin.py`](examples/buildings/build_cabin.py) - Build a log cabin
- [`buildings/build_structures.py`](examples/buildings/build_structures.py) - Build structures programmatically
- [`buildings/build_maze.py`](examples/buildings/build_maze.py) - Generate and build a maze
- [`benchmarks/fill_vs_edit.py`](examples/benchmarks/fill_vs_edit.py) - Performance comparison of fill vs edit
- [`games/sand_cache_game.py`](examples/games/sand_cache_game.py) - A sand cache mini-game

## Requirements

| Component        | Requirement          |
| ---------------- | -------------------- |
| Python           | 3.11+                |
| Minecraft Server | Spigot/Paper 1.21.4+ |
| Java             | 21+                  |

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

**Version:** 1.1.0
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
