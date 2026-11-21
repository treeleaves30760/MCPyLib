# MCPyLib

[![PyPI Downloads](https://static.pepy.tech/personalized-badge/mcpylib?period=total&units=INTERNATIONAL_SYSTEM&left_color=BLACK&right_color=GREEN&left_text=downloads)](https://pepy.tech/projects/mcpylib)
<br>
A Python library for remotely controlling Minecraft servers through a TCP-based API.

## Overview

MCPyLib enables programmatic control of Minecraft servers via Python. It consists of two components:

- **Python Client Library** - Provides a clean API for server interaction
- **Paper/Spigot Plugin** - Handles server-side command execution with high-performance bulk operations

### Key Features

- **High-Performance Bulk Editing** - Build large structures quickly with WorldEdit-like performance
- **Block Operations** - Set, get, fill, clone, read regions and bulk edit blocks, with block states and basic block entity data (container names, sign text)
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

## AI Integration

Let AI agents build in Minecraft for you:

- **[MCPyLib-mcp](https://github.com/treeleaves30760/MCPyLib-mcp)**: an MCP (Model Context Protocol) server, so Claude Code, Claude Desktop, Cursor, VS Code and other MCP clients can call MCPyLib as tools:
  ```bash
  claude mcp add mcpylib -e MCPYLIB_TOKEN=<token> -- uvx --from git+https://github.com/treeleaves30760/MCPyLib-mcp mcpylib-mcp
  ```
- **[MCPyLib-skills](https://github.com/treeleaves30760/MCPyLib-skills)**: Agent Skills that teach AI coding agents the MCPyLib API, building recipes and mini-game patterns:
  ```
  /plugin marketplace add treeleaves30760/MCPyLib-skills
  ```
- **Website**: https://treeleaves30760.github.io/MCPyLib-website/ (docs, showcase, and `llms.txt` for agents)

## Quick Start

### Server Setup

1. Place `MCPyLib-Plugin-1.2.0.jar` in your server's `plugins` folder
2. Start or restart your Minecraft server
3. The plugin generates an authentication token on first start and prints it in the server console. You can show it again at any time:

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

# The client reuses one connection for all commands; close it when you are done
mc.close()
```

The client also works as a context manager, which closes the connection for you:

```python
with MCPyLib(token="YOUR_TOKEN") as mc:
    mc.setblock(100, 64, 200, "minecraft:stone")
```

## API Reference

MCPyLib provides **72 commands** across 18 categories, plus `close()` for the connection. See the [API Reference](docs/api-reference.md) for full details.

### Block Operations (6)

| Method | Parameters | Description |
| ------ | ---------- | ----------- |
| `setblock` | `x, y, z, block_name, block_state=None, nbt=None` | Place a block with optional block state and NBT data |
| `getblock` | `x, y, z` | Get the block type at coordinates |
| `getblocks` | `x1, y1, z1, x2, y2, z2` | Read a region as a 3D list `[x][y][z]` of block types (the layout `edit` uses) |
| `fill` | `x1, y1, z1, x2, y2, z2, block_name` | Fill a cuboid region with a block type |
| `clone` | `x1, y1, z1, x2, y2, z2, dest_x, dest_y, dest_z` | Clone a region to a new location (max 32,768 blocks) |
| `edit` | `x, y, z, blocks` | High-performance bulk block editing with a 3D list `[x][y][z]` |

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
| `effect` | `username, effect_type, duration=30, amplifier=0, hide_particles=False` | Apply a status effect to a player (duration in seconds) |
| `clearEffect` | `username, effect_type=None` | Remove a specific effect or all effects from a player |
| `clear` | `username, item=None, max_count=-1` | Clear items from a player's inventory (`-1` = no limit); returns the number removed |
| `experience` | `username, action, amount=0, target="points"` | Add, set, or query player experience (`target`: `"points"` or `"levels"`) |
| `enchant` | `username, enchantment, level=1` | Enchant the item in the player's main hand |

### Inventory Management (2)

| Method | Parameters | Description |
| ------ | ---------- | ----------- |
| `getItem` | `username, slot` | Get item data from a specific inventory slot |
| `setItem` | `username, slot, item, amount=1` | Set an item in a specific inventory slot |

### World Settings (5)

| Method | Parameters | Description |
| ------ | ---------- | ----------- |
| `time` | `action, value=None` | Control world time (`set` 0-24000, `add`, `query`) |
| `weather` | `condition, duration=None` | Set weather (`clear`, `rain`, `thunder`) with optional duration in seconds |
| `difficulty` | `level` | Set world difficulty (`peaceful`, `easy`, `normal`, `hard`) |
| `gamerule` | `rule, value=None` | Get or set a game rule (`keep_inventory` style names; legacy `keepInventory` names also work) |
| `defaultgamemode` | `mode` | Set the default gamemode for new players |

### Chat & Display (6)

| Method | Parameters | Description |
| ------ | ---------- | ----------- |
| `say` | `message` | Broadcast a message to all players |
| `tell` | `username, message` | Send a private message to a player |
| `tellraw` | `username, json_text` | Send a JSON text component (passed as a string) to a player or selector such as `@a` |
| `title` | `username, title="", subtitle="", fade_in=10, stay=70, fade_out=20` | Display a title and subtitle (times in ticks) |
| `playsound` | `username, sound, source="master", x=None, y=None, z=None, volume=1.0, pitch=1.0` | Play a sound for a player |
| `stopsound` | `username, sound=None, source=None` | Stop one sound, or all sounds, for a player |

### Particle & Spawn (3)

| Method | Parameters | Description |
| ------ | ---------- | ----------- |
| `particle` | `particle_type, x, y, z, count=1, dx=0, dy=0, dz=0, speed=0` | Create particle effects at a location |
| `spawnpoint` | `username, x=None, y=None, z=None` | Set a player's spawn point (default: current position) |
| `setworldspawn` | `x, y, z` | Set the world's default spawn point |

### World Border & Chunks (2)

| Method | Parameters | Description |
| ------ | ---------- | ----------- |
| `worldborder` | `action, value=None, time=None, x=None, z=None` | Manage the world border (`get`, `set`, `add`, `center`, `damage`, `warning`) |
| `forceload` | `action, x, z` | Force-load (`add`), unload (`remove`) or `query` the chunk that contains block `x, z` |

### Entity Control (10)

| Method | Parameters | Description |
| ------ | ---------- | ----------- |
| `summon` | `entity_type, x, y, z` | Summon an entity at coordinates, returns UUID |
| `kill` | `selector` | Remove entities: `"all"` (every non-player entity), an entity type, or `"player:<name>"` |
| `getEntityPos` | `entity_uuid` | Get entity position as a dict `{x, y, z, yaw, pitch}` |
| `getEntityStatus` | `entity_uuid` | Get entity status (type, health, AI, position, velocity, etc.) |
| `teleportEntity` | `entity_uuid, x, y, z, yaw=None, pitch=None` | Teleport an entity to coordinates |
| `setEntityVelocity` | `entity_uuid, vx, vy, vz` | Set an entity's velocity vector (blocks/tick, clamped to ±10) |
| `setEntityRotation` | `entity_uuid, yaw, pitch` | Set an entity's rotation |
| `setEntityAI` | `entity_uuid, enabled` | Enable or disable a living entity's AI |
| `setEntityTarget` | `entity_uuid, target_uuid=None` | Set or clear a mob's attack target |
| `removeEntity` | `entity_uuid` | Remove a specific entity by UUID (not players) |

### Entity Equipment (2)

| Method | Parameters | Description |
| ------ | ---------- | ----------- |
| `getEntityEquipment` | `entity_uuid` | Get equipped items (empty slots are left out) |
| `setEntityEquipment` | `entity_uuid, equipment, drop_chances=None` | Equip items from a dict with keys `helmet`, `chestplate`, `leggings`, `boots`, `main_hand`, `off_hand` |

### Entity Interaction (3)

| Method | Parameters | Description |
| ------ | ---------- | ----------- |
| `damage` | `entity_uuid, amount, source_uuid=None` | Deal damage to a living entity, optionally caused by another entity |
| `ride` | `passenger_uuid, vehicle_uuid=None` | Make an entity ride another entity, or dismount it |
| `attribute` | `entity_uuid, attribute_name, action="get", value=None` | Get or set an attribute's base value (`action`: `"get"` or `"set"`) |

### Entity Tags (3)

| Method | Parameters | Description |
| ------ | ---------- | ----------- |
| `addTag` | `entity_uuid, tag` | Add a scoreboard tag to an entity |
| `removeTag` | `entity_uuid, tag` | Remove a scoreboard tag from an entity |
| `getTags` | `entity_uuid` | Get all scoreboard tags on an entity |

### Villager (3)

| Method | Parameters | Description |
| ------ | ---------- | ----------- |
| `getVillagerData` | `villager_uuid` | Get villager profession, level, and trade data |
| `setVillagerProfession` | `villager_uuid, profession` | Set a villager's profession (e.g. `"librarian"`) |
| `setVillagerTrades` | `villager_uuid, trades` | Replace a villager's trades (`buy1`, optional `buy2`, `sell` as `{"item", "amount"}`) |

### Scoreboard (5)

| Method | Parameters | Description |
| ------ | ---------- | ----------- |
| `addObjective` | `name, criteria, display_name=None` | Create a new scoreboard objective |
| `removeObjective` | `name` | Remove a scoreboard objective |
| `setScore` | `objective, player, score` | Set a player's (or any entry's) score for an objective |
| `getScore` | `objective, player` | Get a player's score for an objective (`0` if never set) |
| `setDisplaySlot` | `slot, objective=None` | Show an objective in a slot (`sidebar`, `below_name`, `player_list`); `None` clears it |

### Teams (1)

| Method | Parameters | Description |
| ------ | ---------- | ----------- |
| `team` | `action, name=None, members=None, option=None, value=None, display_name=None` | Manage teams (`add`, `remove`, `join`, `leave`, `modify`, `list`) |

### Boss Bar (1)

| Method | Parameters | Description |
| ------ | ---------- | ----------- |
| `bossbar` | `action, bar_id, title=None, color=None, style=None, progress=None, visible=None, username=None` | Manage boss bars (`add`, `remove`, `set`, `addplayer`, `removeplayer`, `get`, `list`) |

### World Generation (7)

| Method | Parameters | Description |
| ------ | ---------- | ----------- |
| `locate` | `structure, x=None, z=None` | Find the nearest structure (keys such as `village_plains`) |
| `loot` | `loot_table, x, y, z` | Drop the loot of a loot table at a location |
| `fillbiome` | `x1, y1, z1, x2, y2, z2, biome, filter_biome=None` | Fill a region with a specific biome |
| `placeFeature` | `feature, x=None, y=None, z=None` | Place a configured feature at a location |
| `placeStructure` | `structure, x=None, y=None, z=None` | Place a structure at a location |
| `placeJigsaw` | `pool, target, max_depth, x=None, y=None, z=None` | Place a jigsaw structure at a location |
| `placeTemplate` | `template, x=None, y=None, z=None, rotation="none", mirror="none", integrity=1.0, seed=0` | Place a structure template with optional rotation, mirroring and integrity |

### Server & Utility (4)

| Method | Parameters | Description |
| ------ | ---------- | ----------- |
| `exec` | `command` | Execute any server command as the console |
| `spreadplayers` | `center_x, center_z, spread_distance, max_range, usernames` | Spread players randomly around a point |
| `list` | | Get a list of online players |
| `advancement` | `username, action, advancement_key` | Grant, revoke, or query a player advancement |

### Good to Know

- Block, entity and world commands act on the server's main world (the first world loaded, normally the overworld).
- Player names are matched case-insensitively. `getPos`, `teleport`, `gamemode`, `give` and `kill("player:...")` need the full name; most other player commands also accept the start of a name (`"Ste"` for `"Steve"`). `tellraw` and `spreadplayers` hand the names to the vanilla command.
- Entities can only be found by UUID while their chunk is loaded. Without a player nearby, call `mc.forceload("add", x, z)` first.
- Calls that run a vanilla command (`exec`, `tellraw`, `fillbiome`, `placeFeature`, `placeStructure`, `placeJigsaw`, `placeTemplate`, `spreadplayers`) return as soon as the command was dispatched. Vanilla error messages such as "No player was found" only appear in the server console.

## Examples

### Advanced Block Placement

```python
# Create a waterlogged fence
mc.setblock(100, 64, 200, "minecraft:oak_fence",
    block_state={"waterlogged": "true"})

# Place a sign with text (rotation 0-15, 0 = facing south)
mc.setblock(100, 64, 201, "minecraft:oak_sign",
    block_state={"rotation": "0"},
    nbt={
        "Text1": "Welcome",
        "Text2": "to MCPyLib",
        "Text3": "",
        "Text4": ""
    })

# Two-block blocks: place the door's upper half first, then the lower half
mc.setblock(100, 65, 202, "minecraft:oak_door",
    block_state={"facing": "south", "half": "upper"})
mc.setblock(100, 64, 202, "minecraft:oak_door",
    block_state={"facing": "south", "half": "lower"})
```

### Build Automation

```python
# Create a stone platform
blocks_placed = mc.fill(0, 63, 0, 20, 63, 20, "minecraft:stone")
print(f"Platform complete: {blocks_placed} blocks")  # 441 (21 x 21)

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

# Add a chest with a custom name (plain text)
blocks[2][1][2] = {
    "block": "minecraft:chest",
    "block_state": {"facing": "north"},
    "nbt": {"CustomName": "Storage"}
}

# Place the entire structure in one operation
count = mc.edit(100, 64, 200, blocks)
print(f"House built: {count} blocks placed")  # None entries are not counted
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
mc.experience("Steve", "add", 10, target="levels")

# Query current experience
xp = mc.experience("Steve", "query")
print(f"Player level: {xp['level']}")  # xp = {'level': ..., 'points': ..., 'progress': ...}

# Enchant the held item
mc.enchant("Steve", "minecraft:sharpness", level=5)
```

### Chat & Titles

```python
import json

# Broadcast a message
mc.say("Server restarting in 5 minutes!")

# Send a private message
mc.tell("Steve", "You have been selected for a quest!")

# Send formatted JSON text: json_text is a string, using the 26.x component format
mc.tellraw("Steve", json.dumps([
    {"text": "Click ", "color": "white"},
    {"text": "[HERE]", "color": "green", "bold": True,
     "click_event": {"action": "run_command", "command": "/help"}}
]))

# Display a title with subtitle (fade times in ticks)
mc.title("Steve", "Welcome!", "Enjoy your stay", fade_in=20, stay=60, fade_out=20)

# Play a sound
mc.playsound("Steve", "minecraft:entity.ender_dragon.growl")

# Actionbar text has no dedicated method: run the vanilla command
mc.exec('title Steve actionbar {"text":"Health: 20/20"}')
```

### Scoreboard & Teams

```python
# Create a scoreboard objective
mc.addObjective("kills", "playerKillCount", "Player Kills")

# Display it on the sidebar
mc.setDisplaySlot("sidebar", "kills")

# Set and get scores (objective first, then the player name)
mc.setScore("kills", "Steve", 15)
score = mc.getScore("kills", "Steve")
print(f"Steve's kills: {score}")

# Create and configure a team (one option per "modify" call)
mc.team("add", "red_team", display_name="Red Team")
mc.team("modify", "red_team", option="color", value="red")
mc.team("modify", "red_team", option="prefix", value="[RED] ")
mc.team("join", "red_team", members=["Steve", "Alex"])

# Clean up
mc.team("remove", "red_team")
mc.removeObjective("kills")
```

### Boss Bars

```python
import time

# Create a boss bar (colors and styles use Bukkit names, e.g. SEGMENTED_10)
mc.bossbar("add", "my_bar", title="Dungeon Progress", color="red", style="segmented_10")

# Show it to a player
mc.bossbar("addplayer", "my_bar", username="Steve")

# Update progress over time (0.0 to 1.0)
for i in range(101):
    mc.bossbar("set", "my_bar", progress=i / 100)
    time.sleep(0.5)

# Remove the boss bar
mc.bossbar("remove", "my_bar")
```

### Entity Interaction

```python
# Entities are only reachable while their chunk is loaded:
# force-load it when no player is nearby
mc.forceload("add", 100, 200)

# Summon and control a zombie
uuid = mc.summon("minecraft:zombie", 100, 64, 200)

# Get entity info
status = mc.getEntityStatus(uuid)
print(f"Entity health: {status['health']}")

# Equip the entity
mc.setEntityEquipment(uuid, {
    "helmet": "minecraft:diamond_helmet",
    "main_hand": "minecraft:diamond_sword",
})

# Launch the entity upward
mc.setEntityVelocity(uuid, 0, 1.5, 0)

# Disable AI (no movement, no gravity) and position the entity
mc.setEntityAI(uuid, False)
mc.teleportEntity(uuid, 105, 64, 205, yaw=90, pitch=0)

# Tag entities for later reference
mc.addTag(uuid, "arena_mob")
tags = mc.getTags(uuid)
print(f"Tags: {tags}")

# Deal damage to the entity, caused by another entity
pig_uuid = mc.summon("minecraft:pig", 100, 64, 200)
mc.damage(uuid, 5.0, source_uuid=pig_uuid)

# Make one entity ride another (passenger first, then vehicle)
mc.ride(uuid, pig_uuid)

# Clean up
mc.removeEntity(uuid)
mc.removeEntity(pig_uuid)
mc.forceload("remove", 100, 200)
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

The examples read `SERVER_IP`, `SERVER_TOKEN` and `PLAYER_NAME` from a `.env` file: copy [`examples/.env.example`](examples/.env.example) to `examples/.env` and fill it in.

## Requirements

| Component        | Requirement          |
| ---------------- | -------------------- |
| Python           | 3.11+                |
| Minecraft Server | Spigot/Paper 26.1+ (tested build target: 26.3) |
| Java             | 25+                  |

## Error Handling

The library raises specific exceptions for different error conditions:

- `ConnectionError` - Network connectivity issues, or no reply within `timeout`. A timed-out command is not re-sent, because it may still have run on the server
- `AuthenticationError` - Invalid token
- `CommandError` - Invalid command parameters or execution failure, including `Server busy` when all connection slots are in use

```python
from mcpylib import CommandError

try:
    mc.setblock(100, 64, 200, "invalid_block")
except CommandError as e:
    print(f"Command failed: {e}")  # Command failed: Invalid block type: invalid_block
```

## Troubleshooting

| Issue                 | Solution                                                 |
| --------------------- | -------------------------------------------------------- |
| Connection refused    | Verify plugin is loaded (`/plugins`), check `server.port` and `server.host` in `plugins/MCPyLib/config.yml` |
| Authentication failed | Show the current token with `/mcpylib token`; `/mcpylib token regenerate` creates a new one |
| Player not found      | Ensure the player is online and check the spelling (names are not case-sensitive) |
| Entity not found      | The entity's chunk is not loaded: call `mc.forceload("add", x, z)` or keep a player nearby |
| Server busy           | All `server.max-connections` slots are taken: `close()` unused clients or raise the limit |

## Documentation

- [Installation Guide](docs/installation.md)
- [API Reference](docs/api-reference.md)
- [Protocol Specification](docs/protocol.md)
- [Development Guide](docs/development.md)
- [Changelog](CHANGELOG.md)
- [Website](https://treeleaves30760.github.io/MCPyLib-website/)

## Project Information

**Version:** 1.2.0
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
