# API Reference

Complete reference for the MCPyLib Python API.

## MCPyLib Class

### Constructor

```python
MCPyLib(ip="127.0.0.1", port=65535, token="", timeout=10.0)
```

Initialize a new MCPyLib client. No connection is made until the first command; after that the client keeps one persistent TCP connection and reuses it for every command.

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

**Connection behavior:**
- If the server closed the connection (restart, idle timeout), the next command reconnects automatically.
- If no reply arrives within `timeout`, the command raises `ConnectionError` and is **not** sent again, because it may still have run on the server.
- One client is not meant to be shared between threads; create one client per thread.

### close()

```python
close() -> None
```

Close the persistent connection. The client can still be used afterwards: the next command opens a new connection.

The client is also a context manager that calls `close()` on exit:

```python
with MCPyLib(token="your_token_here") as mc:
    mc.setblock(100, 64, 200, "minecraft:stone")
```

---

## Block Operations

### setblock()

```python
setblock(x: int, y: int, z: int, block_name: str, block_state: dict = None, nbt: dict = None) -> int
```

Place a block at the specified coordinates.

**Parameters:**
- `x` (int): X coordinate
- `y` (int): Y coordinate (vertical)
- `z` (int): Z coordinate
- `block_name` (str): Block type (e.g., `"minecraft:stone"` or `"stone"`). Item ids that are not placeable blocks (such as `"brick"`) are rejected.
- `block_state` (dict, optional): Block state properties, e.g. `{"facing": "north", "half": "top"}`. Every vanilla property works (`type`, `rotation`, `part`, `power`, `axis`, `waterlogged`, ...). Values may be strings, numbers or booleans. An unknown property raises `CommandError`.
- `nbt` (dict, optional): Block entity data. Only two kinds of keys are applied:
  - `"CustomName"`: plain-text name for containers (chest, barrel, furnace, ...)
  - `"Text1"` to `"Text4"`: the front lines of a sign

  Other keys (for example `"Items"`) are ignored.

**Returns:** `int` -- `1` if successful

**Raises:** `ConnectionError`, `AuthenticationError`, `CommandError`

**Example:**
```python
# Both formats work
mc.setblock(100, 64, 200, "minecraft:diamond_block")
mc.setblock(101, 64, 200, "gold_block")

# Block states
mc.setblock(100, 64, 201, "oak_stairs", block_state={"facing": "north", "half": "top"})
mc.setblock(100, 64, 202, "oak_slab", block_state={"type": "double"})

# A sign with text, and a named chest
mc.setblock(100, 64, 203, "oak_sign", block_state={"rotation": "0"},
            nbt={"Text1": "Hello", "Text2": "World", "Text3": "", "Text4": ""})
mc.setblock(100, 64, 204, "chest", block_state={"facing": "north"},
            nbt={"CustomName": "Treasure Chest"})
```

**Two-block blocks:** place both halves, in this order: a door's `half=upper` first and then `half=lower`, a bed's `part=head` first and then `part=foot`. In the other order the server's block update destroys the half placed first and the door or bed ends up broken.

```python
mc.setblock(100, 65, 205, "oak_door", block_state={"facing": "south", "half": "upper"})
mc.setblock(100, 64, 205, "oak_door", block_state={"facing": "south", "half": "lower"})
```

Some properties are recalculated by the game when a neighboring block changes (for example a stair's `shape` follows the stairs next to it).

### getblock()

```python
getblock(x: int, y: int, z: int) -> str
```

Get the block type at specified coordinates.

**Parameters:**
- `x` (int): X coordinate
- `y` (int): Y coordinate (vertical)
- `z` (int): Z coordinate

**Returns:** `str` -- Block type identifier (e.g., `"minecraft:stone"`). Block states are not included.

**Raises:** `ConnectionError`, `AuthenticationError`, `CommandError`

**Example:**
```python
block = mc.getblock(100, 64, 200)
print(f"Block type: {block}")  # "minecraft:diamond_block"

# Check if a specific block exists
if mc.getblock(0, 64, 0) == "minecraft:air":
    print("No block at this location")
```

### getblocks()

```python
getblocks(x1: int, y1: int, z1: int, x2: int, y2: int, z2: int) -> List[List[List[str]]]
```

Get all blocks in a rectangular region as a 3D array. The returned array uses `[x][y][z]` indexing, matching the format used by `edit()`.

**Parameters:**
- `x1` (int): Starting X coordinate
- `y1` (int): Starting Y coordinate (vertical)
- `z1` (int): Starting Z coordinate
- `x2` (int): Ending X coordinate
- `y2` (int): Ending Y coordinate (vertical)
- `z2` (int): Ending Z coordinate

**Returns:** `List[List[List[str]]]` -- 3D array of block type identifiers

**Raises:** `ConnectionError`, `AuthenticationError`, `CommandError`

**Example:**
```python
# Read a 3x3x3 region
blocks = mc.getblocks(100, 64, 200, 102, 66, 202)
print(blocks[0][0][0])  # "minecraft:stone"

# Read, modify, and write back with edit()
blocks = mc.getblocks(100, 64, 200, 110, 68, 210)
for x in range(len(blocks)):
    for y in range(len(blocks[0])):
        for z in range(len(blocks[0][0])):
            if blocks[x][y][z] == "minecraft:stone":
                blocks[x][y][z] = "minecraft:diamond_block"
mc.edit(100, 64, 200, blocks)
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

**Returns:** `int` -- Number of blocks affected

**Raises:** `ConnectionError`, `AuthenticationError`, `CommandError`

**Example:**
```python
# Fill an 11x11x11 cube with glass (both corners are included)
count = mc.fill(0, 60, 0, 10, 70, 10, "glass")
print(f"Filled {count} blocks")  # Filled 1331 blocks

# Create a floor
mc.fill(0, 63, 0, 20, 63, 20, "stone")

# Build a wall
mc.fill(0, 64, 0, 0, 70, 20, "cobblestone")
```

**Note:** The order of coordinates doesn't matter -- the function automatically determines the bounding box. Blocks are placed without block updates (sand does not fall, for example). There is no size limit, but the fill runs on the server's main thread, so a very large region pauses the server while it runs; prefer `edit()` for big volumes.

### clone()

```python
clone(x1: int, y1: int, z1: int, x2: int, y2: int, z2: int, dest_x: int, dest_y: int, dest_z: int) -> int
```

Clone a rectangular region of blocks to a new location.

**Parameters:**
- `x1, y1, z1` (int): First corner of the source region
- `x2, y2, z2` (int): Second corner of the source region
- `dest_x, dest_y, dest_z` (int): Destination corner coordinates

**Returns:** `int` -- Number of blocks cloned

**Raises:** `CommandError` if the source region is larger than 32,768 blocks

**Example:**
```python
# Clone a 6x6x6 region to a new location (both corners are included)
count = mc.clone(0, 64, 0, 5, 69, 5, 100, 64, 100)
print(f"Cloned {count} blocks")  # Cloned 216 blocks
```

**Note:** `dest_x, dest_y, dest_z` is where the source region's minimum corner ends up. Block types and states are copied; block entity contents (chest items, sign text) are not.

### edit()

```python
edit(x: int, y: int, z: int, blocks: List[List[List]]) -> int
```

Place a whole 3D region of blocks in a single request, much faster than calling `setblock()` repeatedly (like WorldEdit). The client compresses the array (palette + run-length encoding + zlib), and the server writes plain blocks straight into the chunk sections when it can (falling back to the Bukkit API otherwise).

**Parameters:**
- `x` (int): X coordinate of the region's minimum corner
- `y` (int): Y coordinate of the region's minimum corner
- `z` (int): Z coordinate of the region's minimum corner
- `blocks` (list): 3D list indexed `blocks[x][y][z]` (the same layout `getblocks()` returns). Each entry is one of:
  - `str`: block name, e.g. `"stone"` or `"minecraft:glass"`
  - `None`: leave this position unchanged
  - `dict`: `{"block": "minecraft:chest", "block_state": {...}, "nbt": {...}}`, where `block_state` and `nbt` are optional and work as in `setblock()`

**Returns:** `int` -- Number of blocks placed (`None` entries are not counted)

**Raises:** `ConnectionError`, `AuthenticationError`, `CommandError` (for example an invalid block name in the palette)

**Example:**
```python
# A 2x2x2 cube: x layers, then y rows, then z values
blocks = [
    [["stone", "glass"], ["stone", "glass"]],
    [["stone", "glass"], ["stone", "glass"]]
]
count = mc.edit(100, 64, 200, blocks)
print(f"Placed {count} blocks")  # Placed 8 blocks

# Mix plain names, skipped positions and blocks with state/NBT
blocks = [
    [[None, "stone"], ["glass", None]],
    [[{
        "block": "minecraft:chest",
        "block_state": {"facing": "north"},
        "nbt": {"CustomName": "Loot"}
    }, "stone"], ["diamond_block", "gold_block"]]
]
mc.edit(100, 64, 200, blocks)  # 6

# Build an empty 10x5x10 array and fill parts of it
size_x, size_y, size_z = 10, 5, 10
blocks = [[[None] * size_z for _ in range(size_y)] for _ in range(size_x)]
for x in range(size_x):
    for z in range(size_z):
        blocks[x][0][z] = "stone"        # floor
        blocks[x][4][z] = "oak_planks"   # roof
mc.edit(100, 64, 200, blocks)  # 200
```

**Note:** Plain blocks are written without block updates. Entries with `block_state` or `nbt` are applied in a second pass with block updates, in `x`, `y`, `z` order, so place two-block blocks such as doors and beds with `setblock()` instead (see [setblock()](#setblock)).

---

## Player Control

Player names are matched case-insensitively. `getPos()`, `teleport()`, `gamemode()`, `give()` and `kill("player:...")` need the full name. The other player commands look players up with Bukkit's `getPlayer()`, which also accepts the start of a name (`"Ste"` finds `"Steve"`; the closest match wins), except `tellraw()` and `spreadplayers()`, which pass the names to the vanilla command. An unknown or offline player raises `CommandError` (`Player not found: <name>`).

### getPos()

```python
getPos(username: str) -> List[int]
```

Get a player's current position.

**Parameters:**
- `username` (str): Player's Minecraft username (the full name; case does not matter)

**Returns:** `List[int]` -- List of `[x, y, z]` coordinates (integers, rounded down)

**Raises:** `ConnectionError`, `AuthenticationError`, `CommandError`

**Example:**
```python
# Get player position
pos = mc.getPos("Steve")
x, y, z = pos
print(f"Steve is at ({x}, {y}, {z})")

# Place a block at player's feet
pos = mc.getPos("Steve")
mc.setblock(pos[0], pos[1] - 1, pos[2], "diamond_block")
```

### teleport()

```python
teleport(username: str, x: float, y: float, z: float, yaw: float = None, pitch: float = None) -> bool
```

Teleport a player to the specified coordinates.

**Parameters:**
- `username` (str): Player's Minecraft username
- `x` (float): Destination X coordinate
- `y` (float): Destination Y coordinate
- `z` (float): Destination Z coordinate
- `yaw` (float, optional): Horizontal rotation (-180 to 180)
- `pitch` (float, optional): Vertical rotation (-90 to 90)

**Returns:** `bool` -- `True` if successful

**Example:**
```python
# Simple teleport
mc.teleport("Steve", 100, 64, 200)

# Teleport with facing direction
mc.teleport("Steve", 100, 64, 200, yaw=90.0, pitch=0.0)
```

### gamemode()

```python
gamemode(username: str, mode: str) -> bool
```

Set a player's game mode.

**Parameters:**
- `username` (str): Player's Minecraft username
- `mode` (str): Game mode (`"survival"`, `"creative"`, `"adventure"`, `"spectator"`)

**Returns:** `bool` -- `True` if successful

**Example:**
```python
mc.gamemode("Steve", "creative")
mc.gamemode("Steve", "survival")
```

### give()

```python
give(username: str, item: str, amount: int = 1) -> bool
```

Give an item to a player.

**Parameters:**
- `username` (str): Player's Minecraft username
- `item` (str): Item type (e.g., `"diamond_sword"`, `"minecraft:golden_apple"`)
- `amount` (int, optional): Number of items, 1-64. Default: `1`

**Returns:** `bool` -- `True` if successful

**Example:**
```python
mc.give("Steve", "diamond_sword")
mc.give("Steve", "golden_apple", 64)
```

---

## Player Effects & Status

### effect()

```python
effect(username: str, effect_type: str, duration: int = 30, amplifier: int = 0, hide_particles: bool = False) -> bool
```

Apply a status effect to a player.

**Parameters:**
- `username` (str): Player's Minecraft username
- `effect_type` (str): Effect type (e.g., `"speed"`, `"strength"`, `"night_vision"`)
- `duration` (int, optional): Duration in seconds. Default: `30`
- `amplifier` (int, optional): Effect level (0 = level I). Default: `0`
- `hide_particles` (bool, optional): Whether to hide particles. Default: `False`

**Returns:** `bool` -- `True` if successful

**Example:**
```python
mc.effect("Steve", "speed", duration=60, amplifier=2)
mc.effect("Steve", "night_vision", duration=300, hide_particles=True)
```

### clearEffect()

```python
clearEffect(username: str, effect_type: str = None) -> bool
```

Remove status effects from a player.

**Parameters:**
- `username` (str): Player's Minecraft username
- `effect_type` (str, optional): Specific effect to clear. If `None`, clears all effects.

**Returns:** `bool` -- `True` if successful

**Example:**
```python
# Clear a specific effect
mc.clearEffect("Steve", "speed")

# Clear all effects
mc.clearEffect("Steve")
```

### clear()

```python
clear(username: str, item: str = None, max_count: int = -1) -> int
```

Clear items from a player's inventory.

**Parameters:**
- `username` (str): Player's Minecraft username
- `item` (str, optional): Specific item to clear. If `None`, clears all items.
- `max_count` (int, optional): Maximum number of items to remove. `-1` for all. Default: `-1`

**Returns:** `int` -- Number of items removed

**Example:**
```python
# Clear all items
mc.clear("Steve")

# Clear specific item
mc.clear("Steve", "dirt")

# Clear at most 10 cobblestone
mc.clear("Steve", "cobblestone", 10)
```

### experience()

```python
experience(username: str, action: str, amount: int = 0, target: str = "points") -> dict
```

Manage a player's experience points or levels.

**Parameters:**
- `username` (str): Player's Minecraft username
- `action` (str): Action to perform (`"add"`, `"set"`, `"query"`)
- `amount` (int, optional): Amount of XP. Default: `0`
- `target` (str, optional): Target type (`"points"` or `"levels"`). Default: `"points"`

**Returns:** `dict` -- `{"level": int, "points": int, "progress": float}` after the action (`points` is the player's total experience, `progress` the fill of the XP bar from 0.0 to 1.0)

**Note:** `"set"` with `target="points"` only moves the XP bar within the current level (`amount` divided by the points needed for the next level, clamped to 0-1). Use `target="levels"` to set the level.

**Example:**
```python
# Add 100 XP points
mc.experience("Steve", "add", 100)

# Set level to 30
mc.experience("Steve", "set", 30, target="levels")

# Query current XP
xp = mc.experience("Steve", "query")
print(f"Level {xp['level']}")
```

### enchant()

```python
enchant(username: str, enchantment: str, level: int = 1) -> bool
```

Enchant the item a player is currently holding.

**Parameters:**
- `username` (str): Player's Minecraft username
- `enchantment` (str): Enchantment type (e.g., `"sharpness"`, `"efficiency"`)
- `level` (int, optional): Enchantment level. Default: `1`

**Returns:** `bool` -- `True` if successful

**Example:**
```python
mc.enchant("Steve", "sharpness", 5)
mc.enchant("Steve", "unbreaking", 3)
```

---

## Inventory Management

### getItem()

```python
getItem(username: str, slot: int) -> dict
```

Get information about an item in a player's inventory slot.

**Parameters:**
- `username` (str): Player's Minecraft username
- `slot` (int): Inventory slot number: 0-8 hotbar, 9-35 main inventory, 36-39 armor, 40 off-hand

**Returns:** `dict` -- `{"item": "minecraft:diamond_sword", "amount": 1}`, plus `"enchantments"` (e.g. `{"sharpness": 5}`) when the item has any. An empty slot returns `{"amount": 0}` without an `"item"` key.

**Example:**
```python
item = mc.getItem("Steve", 0)
print(f"Item in slot 0: {item.get('item')} x{item['amount']}")
```

### setItem()

```python
setItem(username: str, slot: int, item: str, amount: int = 1) -> bool
```

Set an item in a player's inventory slot.

**Parameters:**
- `username` (str): Player's Minecraft username
- `slot` (int): Inventory slot number (see [getItem()](#getitem))
- `item` (str): Item type
- `amount` (int, optional): Stack size. Default: `1`

**Returns:** `bool` -- `True` if successful

**Example:**
```python
mc.setItem("Steve", 0, "diamond_sword")
mc.setItem("Steve", 1, "golden_apple", 64)
```

---

## World Settings

### time()

```python
time(action: str, value: int = None) -> int
```

Get or set the world time (of the main world).

**Parameters:**
- `action` (str): Action to perform (`"set"`, `"add"`, `"query"`)
- `value` (int, optional): Time in ticks, required for `"set"` (0-24000) and `"add"`. Keywords such as `"day"` are not accepted; use `1000` for day, `6000` for noon, `13000` for night.

**Returns:** `int` -- Current world time in ticks

**Example:**
```python
# Set time to day
mc.time("set", 1000)

# Add 6000 ticks
mc.time("add", 6000)

# Query current time
current = mc.time("query")
print(f"Current time: {current}")
```

### weather()

```python
weather(condition: str, duration: int = None) -> bool
```

Set the weather condition.

**Parameters:**
- `condition` (str): Weather type (`"clear"`, `"rain"`, `"thunder"`)
- `duration` (int, optional): Duration in seconds

**Returns:** `bool` -- `True` if successful

**Example:**
```python
mc.weather("clear")
mc.weather("rain", duration=600)
mc.weather("thunder", duration=300)
```

### difficulty()

```python
difficulty(level: str) -> bool
```

Set the world difficulty.

**Parameters:**
- `level` (str): Difficulty level (`"peaceful"`, `"easy"`, `"normal"`, `"hard"`)

**Returns:** `bool` -- `True` if successful

**Example:**
```python
mc.difficulty("hard")
```

### gamerule()

```python
gamerule(rule: str, value: str = None) -> str
```

Get or set a game rule.

**Parameters:**
- `rule` (str): Game rule name. Minecraft 1.21.11+ uses snake_case registry names (e.g., `"keep_inventory"`, `"minecraft:mob_griefing"`); legacy camelCase names such as `"keepInventory"` are still accepted
- `value` (str, optional): Value to set. If `None`, returns current value.

**Returns:** `str` -- Current value of the game rule

**Example:**
```python
# Disable daylight cycle
mc.gamerule("doDaylightCycle", "false")

# Enable keep inventory
mc.gamerule("keep_inventory", "true")

# Query a rule
value = mc.gamerule("mob_griefing")
print(f"mobGriefing: {value}")
```

### defaultgamemode()

```python
defaultgamemode(mode: str) -> bool
```

Set the default game mode for new players.

**Parameters:**
- `mode` (str): Game mode (`"survival"`, `"creative"`, `"adventure"`, `"spectator"`)

**Returns:** `bool` -- `True` if successful

**Example:**
```python
mc.defaultgamemode("survival")
```

---

## Chat, Display & Sound

### say()

```python
say(message: str) -> bool
```

Broadcast a message to all players. It is shown as `[Server] <message>`.

**Parameters:**
- `message` (str): Message to broadcast

**Returns:** `bool` -- `True` if successful

**Example:**
```python
mc.say("Hello, everyone!")
mc.say("The event starts in 5 minutes!")
```

### tell()

```python
tell(username: str, message: str) -> bool
```

Send a private message to a specific player.

**Parameters:**
- `username` (str): Target player's username
- `message` (str): Message to send

**Returns:** `bool` -- `True` if successful

**Example:**
```python
mc.tell("Steve", "You have been selected for the quest!")
```

### tellraw()

```python
tellraw(username: str, json_text: str) -> bool
```

Send a raw JSON text message to a player. The plugin runs the vanilla `tellraw` command, so selectors work too.

**Parameters:**
- `username` (str): Target player's username, or a target selector such as `"@a"`
- `json_text` (str): Text component as a JSON **string** (not a dict or list; use `json.dumps()`). Minecraft 26.x uses the newer snake_case component keys, e.g. `click_event` with `"command"` (or `"url"` for `open_url`) instead of `clickEvent` with `"value"`.

**Returns:** `bool` -- `True` once the command was dispatched. If no player matches, vanilla only reports "No player was found" in the server console. Malformed JSON raises `CommandError`.

**Example:**
```python
import json

mc.tellraw("@a", '{"text":"Hello!","color":"gold","bold":true}')

mc.tellraw("Steve", json.dumps([
    {"text": "Click "},
    {"text": "here", "color": "blue", "underlined": True,
     "click_event": {"action": "run_command", "command": "/help"}}
]))
```

### title()

```python
title(username: str, title: str = "", subtitle: str = "", fade_in: int = 10, stay: int = 70, fade_out: int = 20) -> bool
```

Display a title on a player's screen.

**Parameters:**
- `username` (str): Target player's username (one player; selectors such as `"@a"` are not supported)
- `title` (str, optional): Main title text (plain text, not JSON)
- `subtitle` (str, optional): Subtitle text (plain text, not JSON)
- `fade_in` (int, optional): Fade-in time in ticks. Default: `10`
- `stay` (int, optional): Stay time in ticks. Default: `70`
- `fade_out` (int, optional): Fade-out time in ticks. Default: `20`

**Returns:** `bool` -- `True` if successful

**Example:**
```python
mc.title("Steve", title="Welcome!", subtitle="Enjoy the server")
mc.title("Steve", title="Level Up!", fade_in=5, stay=40, fade_out=10)

# All online players
for player in mc.list()["players"]:
    mc.title(player["name"], title="Welcome!")

# Actionbar text: use the vanilla command
mc.exec('title Steve actionbar {"text":"Health: 20/20"}')
```

### playsound()

```python
playsound(username: str, sound: str, source: str = "master", x: float = None, y: float = None, z: float = None, volume: float = 1.0, pitch: float = 1.0) -> bool
```

Play a sound for a player.

**Parameters:**
- `username` (str): Target player's username
- `sound` (str): Sound identifier (e.g., `"minecraft:entity.experience_orb.pickup"`). An unknown sound plays nothing; it does not raise an error.
- `source` (str, optional): Sound category (`master`, `music`, `record`, `weather`, `block`, `hostile`, `neutral`, `player`, `ambient`, `voice`). Unknown values fall back to `master`. Default: `"master"`
- `x, y, z` (float, optional): Sound position (all three are needed). Defaults to player's location.
- `volume` (float, optional): Volume level. Default: `1.0`
- `pitch` (float, optional): Pitch modifier. Default: `1.0`

**Returns:** `bool` -- `True` if successful

**Example:**
```python
mc.playsound("Steve", "minecraft:entity.experience_orb.pickup")
mc.playsound("Steve", "minecraft:block.note_block.pling", volume=0.5, pitch=2.0)
```

### stopsound()

```python
stopsound(username: str, sound: str = None, source: str = None) -> bool
```

Stop a sound playing for a player.

**Parameters:**
- `username` (str): Target player's username
- `sound` (str, optional): Sound to stop. If `None`, stops all sounds.
- `source` (str, optional): Category of `sound`. Only used together with `sound`; without a sound, all sounds stop.

**Returns:** `bool` -- `True` if successful

**Example:**
```python
# Stop a specific sound
mc.stopsound("Steve", "minecraft:music.game")

# Stop all sounds
mc.stopsound("Steve")
```

---

## Particle & Spawn

### particle()

```python
particle(particle_type: str, x: float, y: float, z: float, count: int = 1, dx: float = 0, dy: float = 0, dz: float = 0, speed: float = 0) -> bool
```

Spawn particles at a location in the main world.

**Parameters:**
- `particle_type` (str): Particle type (e.g., `"flame"`, `"heart"`, `"explosion"`). Particles that need extra data (`"dust"`, `"block"`, `"item"`, ...) are not supported and raise `CommandError`.
- `x, y, z` (float): Center position
- `count` (int, optional): Number of particles. Default: `1`
- `dx, dy, dz` (float, optional): Spread in each axis. Default: `0`
- `speed` (float, optional): Particle speed. Default: `0`

**Returns:** `bool` -- `True` if successful

**Example:**
```python
mc.particle("flame", 100, 65, 200, count=50, dx=1, dy=1, dz=1, speed=0.1)
mc.particle("heart", 100, 67, 200, count=10)
```

### spawnpoint()

```python
spawnpoint(username: str, x: float = None, y: float = None, z: float = None) -> bool
```

Set a player's individual spawn point.

**Parameters:**
- `username` (str): Player's username
- `x, y, z` (float, optional): Spawn coordinates (all three, in the player's world). If omitted, uses the player's current position.

**Returns:** `bool` -- `True` if successful

**Example:**
```python
mc.spawnpoint("Steve", 100, 64, 200)

# Set to player's current position
mc.spawnpoint("Steve")
```

### setworldspawn()

```python
setworldspawn(x: int, y: int, z: int) -> bool
```

Set the spawn point of the main world.

**Parameters:**
- `x, y, z` (int): Spawn coordinates

**Returns:** `bool` -- `True` if the server accepted the new spawn location

**Example:**
```python
mc.setworldspawn(0, 64, 0)
```

---

## World Border & Chunks

### worldborder()

```python
worldborder(action: str, value: float = None, time: int = None, x: float = None, z: float = None) -> dict
```

Manage the world border of the main world.

**Parameters:**
- `action` (str): Action to perform:
  - `"get"`: read all settings
  - `"set"`: set the size to `value` (blocks), over `time` seconds if given
  - `"add"`: grow (or shrink, with a negative `value`) the size by `value`, over `time` seconds if given
  - `"center"`: move the center to `x, z`
  - `"damage"`: set the damage per block outside the border to `value`
  - `"warning"`: set the warning distance to `value` (blocks) and/or the warning time to `time` (seconds)
- `value` (float, optional): Size or amount depending on action
- `time` (int, optional): Transition time in seconds (`"set"`, `"add"`) or warning time (`"warning"`)
- `x, z` (float, optional): Center coordinates (for `"center"`)

**Returns:** `dict` -- For `"get"`: `size`, `center_x`, `center_z`, `damage_amount`, `damage_buffer`, `warning_distance`, `warning_time`. The other actions return only the values they changed (e.g. `{"size": 500.0}`).

**Example:**
```python
# Set world border size
mc.worldborder("set", value=1000)

# Gradually shrink over 60 seconds
mc.worldborder("set", value=500, time=60)

# Move center
mc.worldborder("center", x=100, z=200)

# Get current border info
info = mc.worldborder("get")
```

### forceload()

```python
forceload(action: str, x: int, z: int) -> bool
```

Force-load or unload the chunk that contains a block position (in the main world). Force-loaded chunks stay loaded without players nearby, which entity commands need: an entity in an unloaded chunk cannot be found by UUID.

**Parameters:**
- `action` (str): Action to perform (`"add"`, `"remove"`, `"query"`)
- `x` (int): Block X coordinate (converted to the chunk coordinate `x >> 4`)
- `z` (int): Block Z coordinate (converted to the chunk coordinate `z >> 4`)

**Returns:** `bool` -- `True` for `"add"` and `"remove"`; for `"query"`, whether the chunk is force-loaded

**Example:**
```python
# Force-load the chunk containing block (100, 200), i.e. chunk (6, 12)
mc.forceload("add", 100, 200)
print(mc.forceload("query", 100, 200))  # True

# Remove force-load
mc.forceload("remove", 100, 200)
```

---

## Entity Control

Entities are addressed by the UUID string that `summon()` returns. The server can only find an entity while its chunk is loaded, so without a player nearby, call [`forceload("add", x, z)`](#forceload) before summoning. Unknown UUIDs raise `CommandError` (`Entity not found: <uuid>`), malformed ones `Invalid UUID format: <uuid>`.

### summon()

```python
summon(entity_type: str, x: float, y: float, z: float) -> str
```

Summon an entity at the specified location in the main world.

**Parameters:**
- `entity_type` (str): Entity type (e.g., `"zombie"`, `"minecraft:cow"`). Players and non-spawnable types are rejected.
- `x, y, z` (float): Spawn coordinates

**Returns:** `str` -- UUID of the summoned entity

**Example:**
```python
uuid = mc.summon("zombie", 100, 64, 200)
print(f"Spawned zombie with UUID: {uuid}")

mc.summon("minecraft:cow", 105, 64, 200)
```

### kill()

```python
kill(selector: str) -> int
```

Remove entities from the main world. This is not the vanilla `/kill`: only three selector forms are understood.

**Parameters:**
- `selector` (str): One of:
  - `"all"`: every non-player entity, including dropped items, armor stands, item frames, paintings and boats
  - an entity type such as `"zombie"` or `"minecraft:cow"`: all entities of that type
  - `"player:<name>"`: sets that player's health to 0

  Vanilla selectors such as `"@e[type=zombie]"` and UUIDs are rejected with `CommandError` (use [removeEntity()](#removeentity) for a single entity).

**Returns:** `int` -- Number of entities removed. `"all"` and entity types remove at most 1000 entities per call, and only entities in loaded chunks. Removed mobs drop no loot.

**Example:**
```python
# Remove all zombies
count = mc.kill("zombie")
print(f"Removed {count} zombies")

# Kill a player
mc.kill("player:Steve")
```

### getEntityPos()

```python
getEntityPos(entity_uuid: str) -> dict
```

Get the position of an entity by its UUID.

**Parameters:**
- `entity_uuid` (str): Entity UUID

**Returns:** `dict` -- `{"x": float, "y": float, "z": float, "yaw": float, "pitch": float}`

**Example:**
```python
pos = mc.getEntityPos("a1b2c3d4-e5f6-7890-abcd-ef1234567890")
print(f"Entity at ({pos['x']}, {pos['y']}, {pos['z']}), facing yaw {pos['yaw']}")
```

### getEntityStatus()

```python
getEntityStatus(entity_uuid: str) -> dict
```

Get the status information of an entity.

**Parameters:**
- `entity_uuid` (str): Entity UUID

**Returns:** `dict` with these keys:
- `uuid`, `type` (e.g. `"minecraft:zombie"`), `world`
- `is_valid`, `is_dead`
- `position` (`{x, y, z}`) and `velocity` (`{x, y, z}`)
- `custom_name`: only present if the entity has a custom name
- `health`, `max_health`, `has_ai`: only present for living entities

Keys whose value would be null are left out by the server, so read the optional ones with `.get()`.

**Example:**
```python
status = mc.getEntityStatus("a1b2c3d4-e5f6-7890-abcd-ef1234567890")
print(f"Health: {status.get('health')}, Type: {status['type']}")
```

### teleportEntity()

```python
teleportEntity(entity_uuid: str, x: float, y: float, z: float, yaw: float = None, pitch: float = None) -> bool
```

Teleport an entity to the specified coordinates.

**Parameters:**
- `entity_uuid` (str): Entity UUID
- `x, y, z` (float): Destination coordinates (in the entity's current world)
- `yaw` (float, optional): Horizontal rotation
- `pitch` (float, optional): Vertical rotation

`yaw` and `pitch` are only applied when both are given; otherwise the entity keeps its rotation.

**Returns:** `bool` -- `True` if the teleport succeeded

**Example:**
```python
mc.teleportEntity("a1b2c3d4-e5f6-7890-abcd-ef1234567890", 100, 64, 200)
mc.teleportEntity("a1b2c3d4-e5f6-7890-abcd-ef1234567890", 100, 64, 200, yaw=90.0, pitch=0.0)
```

### setEntityVelocity()

```python
setEntityVelocity(entity_uuid: str, vx: float, vy: float, vz: float) -> bool
```

Set an entity's velocity vector.

**Parameters:**
- `entity_uuid` (str): Entity UUID
- `vx` (float): X velocity (blocks per tick, clamped to ±10)
- `vy` (float): Y velocity (blocks per tick, clamped to ±10)
- `vz` (float): Z velocity (blocks per tick, clamped to ±10)

Mobs with AI disabled do not move, so velocity has no visible effect on them.

**Returns:** `bool` -- `True` if successful

**Example:**
```python
# Launch entity upward
mc.setEntityVelocity("a1b2c3d4-...", 0, 2.0, 0)

# Push entity forward and up
mc.setEntityVelocity("a1b2c3d4-...", 1.0, 0.5, 0)
```

### setEntityRotation()

```python
setEntityRotation(entity_uuid: str, yaw: float, pitch: float) -> bool
```

Set an entity's rotation.

**Parameters:**
- `entity_uuid` (str): Entity UUID
- `yaw` (float): Horizontal rotation (0 = south, 90 = west, 180 = north, 270 or -90 = east)
- `pitch` (float): Vertical rotation (-90 = up, 0 = level, 90 = down)

**Returns:** `bool` -- `True` if successful

**Example:**
```python
mc.setEntityRotation("a1b2c3d4-...", 90.0, 0.0)
```

### setEntityAI()

```python
setEntityAI(entity_uuid: str, enabled: bool) -> bool
```

Enable or disable a living entity's AI. Without AI a mob stands still like a statue (no movement, no gravity). Non-living entities raise `CommandError`.

**Parameters:**
- `entity_uuid` (str): Entity UUID
- `enabled` (bool): `True` to enable AI, `False` to disable

**Returns:** `bool` -- `True` if successful

**Example:**
```python
# Freeze a zombie in place
mc.setEntityAI("a1b2c3d4-...", False)

# Re-enable AI
mc.setEntityAI("a1b2c3d4-...", True)
```

### setEntityTarget()

```python
setEntityTarget(entity_uuid: str, target_uuid: str = None) -> bool
```

Set or clear a mob's attack target.

**Parameters:**
- `entity_uuid` (str): UUID of a mob (an entity that can have a target)
- `target_uuid` (str, optional): UUID of a living entity to target. If `None`, clears the target.

**Returns:** `bool` -- `True` if successful

**Example:**
```python
# Make a zombie target a specific entity
mc.setEntityTarget("zombie-uuid", "player-uuid")

# Clear target
mc.setEntityTarget("zombie-uuid")
```

### removeEntity()

```python
removeEntity(entity_uuid: str) -> bool
```

Remove an entity from the world (without death animation or drops). Players cannot be removed.

**Parameters:**
- `entity_uuid` (str): Entity UUID

**Returns:** `bool` -- `True` if successful

**Example:**
```python
mc.removeEntity("a1b2c3d4-e5f6-7890-abcd-ef1234567890")
```

### damage()

```python
damage(entity_uuid: str, amount: float, source_uuid: str = None) -> bool
```

Deal damage to an entity.

**Parameters:**
- `entity_uuid` (str): Target entity UUID (must be a living entity)
- `amount` (float): Damage amount (in half-hearts)
- `source_uuid` (str, optional): Source entity UUID (for attribution). If that entity cannot be found, the damage is dealt without a source.

**Returns:** `bool` -- `True` if successful

**Example:**
```python
# Deal 5 hearts of damage
mc.damage("a1b2c3d4-...", 10.0)

# Deal damage attributed to another entity
mc.damage("target-uuid", 4.0, source_uuid="attacker-uuid")
```

### ride()

```python
ride(passenger_uuid: str, vehicle_uuid: str = None) -> bool
```

Make an entity ride another entity, or dismount.

**Parameters:**
- `passenger_uuid` (str): Passenger entity UUID
- `vehicle_uuid` (str, optional): Vehicle entity UUID. If `None`, dismounts the passenger.

**Returns:** `bool` -- `True` if the entity was mounted (or dismounted)

**Example:**
```python
# Mount an entity on another
mc.ride("passenger-uuid", "horse-uuid")

# Dismount
mc.ride("passenger-uuid")
```

### attribute()

```python
attribute(entity_uuid: str, attribute_name: str, action: str = "get", value: float = None) -> dict
```

Get or set a living entity's attribute.

**Parameters:**
- `entity_uuid` (str): Entity UUID
- `attribute_name` (str): Attribute name, e.g. `"max_health"`, `"minecraft:movement_speed"`, `"attack_damage"`. Legacy names such as `"generic.max_health"` or `"GENERIC_MAX_HEALTH"` are accepted too.
- `action` (str, optional): `"get"` or `"set"`. Default: `"get"`
- `value` (float, optional): New base value, required for `"set"`

**Returns:** `dict` -- `{"base_value": float, "value": float}` (`value` includes modifiers from equipment and effects)

**Example:**
```python
# Get max health
health = mc.attribute("a1b2c3d4-...", "max_health")
print(f"Max health: {health['value']}")  # {'base_value': 20.0, 'value': 20.0}

# Set max health to 40 (20 hearts)
mc.attribute("a1b2c3d4-...", "max_health", action="set", value=40.0)
```

---

## Entity Equipment

### getEntityEquipment()

```python
getEntityEquipment(entity_uuid: str) -> dict
```

Get the equipment of a living entity.

**Parameters:**
- `entity_uuid` (str): Entity UUID

**Returns:** `dict` -- Item id per slot, with the keys `helmet`, `chestplate`, `leggings`, `boots`, `main_hand` and `off_hand`. Empty slots are left out, so an unequipped zombie returns `{}`.

**Example:**
```python
equipment = mc.getEntityEquipment("a1b2c3d4-...")
print(f"Helmet: {equipment.get('helmet')}")      # e.g. "minecraft:diamond_helmet" or None
print(f"Main hand: {equipment.get('main_hand')}")
```

### setEntityEquipment()

```python
setEntityEquipment(entity_uuid: str, equipment: dict, drop_chances: dict = None) -> bool
```

Set the equipment of a living entity. Only the slots you pass are changed.

**Parameters:**
- `entity_uuid` (str): Entity UUID
- `equipment` (dict): Item type per slot. Slot keys: `helmet`, `chestplate`, `leggings`, `boots`, `main_hand`, `off_hand`. Other keys (such as `head` or `mainhand`) are silently ignored.
- `drop_chances` (dict, optional): Drop chance per slot (0.0 to 1.0), same keys

**Returns:** `bool` -- `True` if successful

**Example:**
```python
mc.setEntityEquipment("a1b2c3d4-...", {
    "helmet": "diamond_helmet",
    "chestplate": "diamond_chestplate",
    "leggings": "diamond_leggings",
    "boots": "diamond_boots",
    "main_hand": "diamond_sword"
})

# With drop chances
mc.setEntityEquipment("a1b2c3d4-...", {
    "main_hand": "diamond_sword"
}, drop_chances={
    "main_hand": 0.5
})
```

---

## Entity Tags

### addTag()

```python
addTag(entity_uuid: str, tag: str) -> bool
```

Add a scoreboard tag to an entity.

**Parameters:**
- `entity_uuid` (str): Entity UUID
- `tag` (str): Tag name

**Returns:** `bool` -- `True` if the tag was added, `False` if the entity already had it

**Example:**
```python
mc.addTag("a1b2c3d4-...", "boss")
mc.addTag("a1b2c3d4-...", "friendly")
```

### removeTag()

```python
removeTag(entity_uuid: str, tag: str) -> bool
```

Remove a scoreboard tag from an entity.

**Parameters:**
- `entity_uuid` (str): Entity UUID
- `tag` (str): Tag name

**Returns:** `bool` -- `True` if the tag was removed, `False` if the entity did not have it

**Example:**
```python
mc.removeTag("a1b2c3d4-...", "boss")
```

### getTags()

```python
getTags(entity_uuid: str) -> List[str]
```

Get all scoreboard tags on an entity.

**Parameters:**
- `entity_uuid` (str): Entity UUID

**Returns:** `List[str]` -- List of tag names

**Example:**
```python
tags = mc.getTags("a1b2c3d4-...")
print(f"Tags: {tags}")
```

---

## Villager

### getVillagerData()

```python
getVillagerData(villager_uuid: str) -> dict
```

Get data about a villager (profession, level, trades).

**Parameters:**
- `villager_uuid` (str): Villager entity UUID

**Returns:** `dict` -- `{"profession": "LIBRARIAN", "level": 1, "trades": [...]}`. Each trade has `buy1`, `sell` (both `{"item": "minecraft:emerald", "amount": 10}`), `uses` and `max_uses`, plus `buy2` when the trade has a second ingredient.

**Example:**
```python
data = mc.getVillagerData("villager-uuid")
print(f"Profession: {data['profession']}")
print(f"Level: {data['level']}")
for trade in data["trades"]:
    print(trade["buy1"], trade.get("buy2"), "->", trade["sell"])
```

### setVillagerProfession()

```python
setVillagerProfession(villager_uuid: str, profession: str) -> bool
```

Set a villager's profession.

**Parameters:**
- `villager_uuid` (str): Villager entity UUID
- `profession` (str): Profession type, case-insensitive, with or without `minecraft:` (`armorer`, `butcher`, `cartographer`, `cleric`, `farmer`, `fisherman`, `fletcher`, `leatherworker`, `librarian`, `mason`, `nitwit`, `none`, `shepherd`, `toolsmith`, `weaponsmith`)

**Returns:** `bool` -- `True` if successful

**Example:**
```python
mc.setVillagerProfession("villager-uuid", "librarian")
```

### setVillagerTrades()

```python
setVillagerTrades(villager_uuid: str, trades: list) -> bool
```

Replace all of a villager's trades with custom ones.

**Parameters:**
- `villager_uuid` (str): Villager entity UUID
- `trades` (list): List of trade dicts, each with:
  - `buy1` (required): first ingredient, `{"item": "emerald", "amount": 5}`
  - `buy2` (optional): second ingredient, same format
  - `sell` (required): result item, same format
  - `max_uses` (optional): uses before the trade locks. Default: `10`
  - `experience_reward` (optional): whether trading gives XP. Default: `True`

  `amount` defaults to `1` when omitted.

**Returns:** `bool` -- `True` if successful

**Example:**
```python
trades = [
    {
        "buy1": {"item": "emerald", "amount": 1},
        "sell": {"item": "diamond", "amount": 1},
    },
    {
        "buy1": {"item": "emerald", "amount": 5},
        "buy2": {"item": "book", "amount": 1},
        "sell": {"item": "enchanted_book", "amount": 1},
        "max_uses": 5,
    },
]
mc.setVillagerTrades("villager-uuid", trades)
```

---

## Scoreboard

### addObjective()

```python
addObjective(name: str, criteria: str, display_name: str = None) -> bool
```

Create a new scoreboard objective.

**Parameters:**
- `name` (str): Objective name (internal identifier)
- `criteria` (str): Criteria type (e.g., `"dummy"`, `"playerKillCount"`, `"health"`)
- `display_name` (str, optional): Display name shown to players. Defaults to `name`.

Objectives live on the server's main scoreboard. Adding a name that already exists raises `CommandError`.

**Returns:** `bool` -- `True` if successful

**Example:**
```python
mc.addObjective("kills", "playerKillCount", "Player Kills")
mc.addObjective("score", "dummy", "Score")
```

### removeObjective()

```python
removeObjective(name: str) -> bool
```

Remove a scoreboard objective.

**Parameters:**
- `name` (str): Objective name

**Returns:** `bool` -- `True` if successful

**Example:**
```python
mc.removeObjective("kills")
```

### setScore()

```python
setScore(objective: str, player: str, score: int) -> bool
```

Set a player's score for an objective. Note the argument order: objective first, then the player.

**Parameters:**
- `objective` (str): Objective name
- `player` (str): Player name, or any other score holder name (the player does not need to be online)
- `score` (int): Score value

**Returns:** `bool` -- `True` if successful

**Example:**
```python
mc.setScore("score", "Steve", 100)
mc.setScore("kills", "Alex", 5)
```

### getScore()

```python
getScore(objective: str, player: str) -> int
```

Get a player's score for an objective.

**Parameters:**
- `objective` (str): Objective name
- `player` (str): Player name

**Returns:** `int` -- Score value (`0` if the score was never set)

**Example:**
```python
score = mc.getScore("score", "Steve")
print(f"Steve's score: {score}")
```

### setDisplaySlot()

```python
setDisplaySlot(slot: str, objective: str = None) -> bool
```

Set or clear the objective displayed in a scoreboard slot.

**Parameters:**
- `slot` (str): Display slot, case-insensitive: `"sidebar"`, `"below_name"`, `"player_list"`, or a team-colored sidebar such as `"sidebar_team_red"`. The vanilla spellings `"list"` and `"belowName"` are rejected.
- `objective` (str, optional): Objective to display. If `None`, clears the slot.

**Returns:** `bool` -- `True` if successful

**Example:**
```python
# Display kills on sidebar
mc.setDisplaySlot("sidebar", "kills")

# Show health below player names
mc.setDisplaySlot("below_name", "health")

# Clear sidebar
mc.setDisplaySlot("sidebar")
```

---

## Teams

### team()

```python
team(action: str, name: str = None, members: list = None, option: str = None, value: str = None, display_name: str = None) -> dict
```

Manage scoreboard teams on the main scoreboard.

**Parameters:**
- `action` (str): Action:
  - `"add"`: create team `name` (optionally with `display_name`); returns `{"name": ...}`
  - `"remove"`: delete team `name`; returns `{"removed": True}`
  - `"join"` / `"leave"`: add or remove `members`; returns `{"added": n}` / `{"removed_count": n}`
  - `"modify"`: set one `option` to `value`; returns `{"modified": True}`
  - `"list"`: returns `{"teams": [{"name", "display_name", "members", "size"}, ...]}`
- `name` (str, optional): Team name (required for every action except `"list"`)
- `members` (list, optional): Entry names, usually player names (for `"join"` and `"leave"`)
- `option` (str, optional): Option for `"modify"`, case-insensitive: `displayname`, `color`, `friendlyfire`, `seefriendlyinvisibles`, `prefix`, `suffix`
- `value` (str, optional): Value for the option: a chat color name such as `"red"` for `color`, `"true"`/`"false"` for the two boolean options, text for the others
- `display_name` (str, optional): Team display name (for `"add"`)

**Returns:** `dict` -- Operation result, see `action`

**Example:**
```python
# Create a team
mc.team("add", name="red", display_name="Red Team")

# Add players
mc.team("join", name="red", members=["Steve", "Alex"])

# Set team color
mc.team("modify", name="red", option="color", value="red")

# Disable friendly fire
mc.team("modify", name="red", option="friendlyfire", value="false")

# List teams
teams = mc.team("list")

# Remove a team
mc.team("remove", name="red")
```

---

## Boss Bar

### bossbar()

```python
bossbar(action: str, bar_id: str, title: str = None, color: str = None, style: str = None, progress: float = None, visible: bool = None, username: str = None) -> dict
```

Manage boss bars displayed to players. The bars are created through the Bukkit API and kept in the plugin's memory: they are not vanilla `/bossbar` bars, and they disappear when the server restarts.

**Parameters:**
- `action` (str): Action:
  - `"add"`: create a bar (`title` defaults to `bar_id`); returns `{"id": ...}`
  - `"set"`: change `title`, `color`, `style`, `progress` and/or `visible`; returns `{"updated": True}`
  - `"addplayer"` / `"removeplayer"`: show or hide the bar for `username`
  - `"get"`: returns `{"id", "title", "color", "style", "progress", "visible", "players"}`
  - `"list"`: returns `{"bars": [ids]}`
  - `"remove"`: delete the bar
- `bar_id` (str): Boss bar identifier. It is required for every action; for `"list"` any value works.
- `title` (str, optional): Display title
- `color` (str, optional): Bar color, case-insensitive: `"pink"`, `"blue"`, `"red"`, `"green"`, `"yellow"`, `"purple"`, `"white"`
- `style` (str, optional): Bar style (Bukkit names, case-insensitive): `"solid"`, `"segmented_6"`, `"segmented_10"`, `"segmented_12"`, `"segmented_20"`. The vanilla names (`"progress"`, `"notched_10"`, ...) do not work.
- `progress` (float, optional): Progress value (0.0 to 1.0; clamped)
- `visible` (bool, optional): Whether the bar is visible
- `username` (str, optional): Player for `"addplayer"` / `"removeplayer"`

An unknown color or style is ignored by `"add"` (the bar is created WHITE / SOLID) but raises `CommandError` in `"set"`.

**Returns:** `dict` -- Operation result, see `action`

**Example:**
```python
# Create a boss bar
mc.bossbar("add", "my_bar", title="Event Progress")

# Configure appearance
mc.bossbar("set", "my_bar", color="red", style="segmented_10", progress=0.75)

# Show to a player
mc.bossbar("addplayer", "my_bar", username="Steve")

# Update progress
mc.bossbar("set", "my_bar", progress=1.0)

# List bars, then remove one
print(mc.bossbar("list", ""))  # {'bars': ['my_bar']}
mc.bossbar("remove", "my_bar")
```

---

## World Generation & Exploration

### locate()

```python
locate(structure: str, x: float = None, z: float = None) -> dict
```

Locate the nearest structure in the main world, searching up to 100 chunks from the origin.

**Parameters:**
- `structure` (str): Structure key, with or without `minecraft:` (e.g., `"village_plains"`, `"village_desert"`, `"desert_pyramid"`, `"ancient_city"`, `"trial_chambers"`). Structure tags such as `"village"` are not accepted.
- `x, z` (float, optional): Search origin (both or neither). Defaults to world spawn.

**Returns:** `dict` -- `{"found": True, "x": int, "y": int, "z": int}`, or `{"found": False}` if nothing was found in range. The coordinates point at the structure's chunk; `y` is not a ground height (usually `0`).

**Example:**
```python
village = mc.locate("village_plains")
if village["found"]:
    print(f"Nearest plains village at ({village['x']}, {village['z']})")
```

### loot()

```python
loot(loot_table: str, x: float, y: float, z: float) -> int
```

Roll a loot table once and drop the items at a location in the main world.

**Parameters:**
- `loot_table` (str): Loot table identifier, with or without `minecraft:` (e.g., `"chests/simple_dungeon"`)
- `x, y, z` (float): Position to spawn loot

**Returns:** `int` -- Number of item stacks dropped

**Example:**
```python
mc.loot("minecraft:chests/simple_dungeon", 100, 64, 200)
```

### fillbiome()

```python
fillbiome(x1: int, y1: int, z1: int, x2: int, y2: int, z2: int, biome: str, filter_biome: str = None) -> bool
```

Fill a region with a specific biome.

**Parameters:**
- `x1, y1, z1` (int): First corner coordinates
- `x2, y2, z2` (int): Second corner coordinates
- `biome` (str): Biome type (e.g., `"plains"`, `"desert"`, `"jungle"`)
- `filter_biome` (str, optional): Only replace this biome type

**Returns:** `bool` -- `True` once the command was dispatched

**Example:**
```python
# Convert a 16x16x16 region to desert
mc.fillbiome(100, 60, 200, 115, 75, 215, "desert")

# Replace only plains with jungle
mc.fillbiome(100, 60, 200, 115, 75, 215, "jungle", filter_biome="plains")
```

**Vanilla-backed commands:** `fillbiome()`, `placeFeature()`, `placeStructure()`, `placeJigsaw()` and `placeTemplate()` run the vanilla command of the same name as the server console. They return `True` once the command was dispatched; the vanilla result (for example "That position is not loaded" or a too-large region) only appears in the server console, so check the result in the world. The target area must be loaded (keep a player nearby or use `forceload()`). Without coordinates, the command runs at the world spawn. An unknown biome or feature id raises `CommandError`; an unknown structure, template or template pool is only reported in the console.

### placeFeature()

```python
placeFeature(feature: str, x: int = None, y: int = None, z: int = None) -> bool
```

Place a configured feature at a location.

**Parameters:**
- `feature` (str): Configured feature identifier (e.g., `"minecraft:oak"`, `"minecraft:lake_lava"`)
- `x, y, z` (int, optional): Placement coordinates (all three, or none)

**Returns:** `bool` -- `True` once the command was dispatched (see [vanilla-backed commands](#fillbiome))

**Example:**
```python
mc.placeFeature("minecraft:oak", 100, 64, 200)
```

### placeStructure()

```python
placeStructure(structure: str, x: int = None, y: int = None, z: int = None) -> bool
```

Place a structure at a location.

**Parameters:**
- `structure` (str): Structure identifier (e.g., `"minecraft:village_plains"`)
- `x, y, z` (int, optional): Placement coordinates (all three, or none)

**Returns:** `bool` -- `True` once the command was dispatched (see [vanilla-backed commands](#fillbiome))

**Example:**
```python
mc.placeStructure("minecraft:village_plains", 200, 64, 200)
```

### placeJigsaw()

```python
placeJigsaw(pool: str, target: str, max_depth: int, x: int = None, y: int = None, z: int = None) -> bool
```

Place a jigsaw structure using a template pool.

**Parameters:**
- `pool` (str): Template pool identifier
- `target` (str): Target jigsaw block name
- `max_depth` (int): Maximum generation depth (1-20)
- `x, y, z` (int, optional): Placement coordinates (all three, or none)

**Returns:** `bool` -- `True` once the command was dispatched (see [vanilla-backed commands](#fillbiome))

**Example:**
```python
mc.placeJigsaw("minecraft:village/plains/town_centers", "minecraft:bottom", 7, 100, 64, 200)
```

### placeTemplate()

```python
placeTemplate(template: str, x: int = None, y: int = None, z: int = None, rotation: str = "none", mirror: str = "none", integrity: float = 1.0, seed: int = 0) -> bool
```

Place a structure template at a location.

**Parameters:**
- `template` (str): Template identifier
- `x, y, z` (int, optional): Placement coordinates
- `rotation` (str, optional): Rotation (`"none"`, `"clockwise_90"`, `"180"`, `"counterclockwise_90"`). Default: `"none"`
- `mirror` (str, optional): Mirror mode (`"none"`, `"front_back"`, `"left_right"`). Default: `"none"`
- `integrity` (float, optional): Structural integrity (0.0 to 1.0). Default: `1.0`
- `seed` (int, optional): Random seed for integrity. Default: `0`

**Returns:** `bool` -- `True` once the command was dispatched (see [vanilla-backed commands](#fillbiome))

**Example:**
```python
mc.placeTemplate("minecraft:village/plains/houses/plains_small_house_1", 100, 64, 200)

# Place with rotation and partial integrity (ruins effect)
mc.placeTemplate("minecraft:village/plains/houses/plains_small_house_1",
                 100, 64, 200, rotation="clockwise_90", integrity=0.6)
```

### advancement()

```python
advancement(username: str, action: str, advancement_key: str) -> bool
```

Grant, revoke or query an advancement of a player.

**Parameters:**
- `username` (str): Player's username
- `action` (str): Action (`"grant"`, `"revoke"`, `"query"`)
- `advancement_key` (str): Advancement identifier, with or without `minecraft:` (e.g., `"story/mine_stone"`)

**Returns:** `bool` -- `True` for `"grant"` and `"revoke"`; for `"query"`, whether the player has completed the advancement

**Example:**
```python
mc.advancement("Steve", "grant", "minecraft:story/mine_stone")
print(mc.advancement("Steve", "query", "story/mine_stone"))  # True
mc.advancement("Steve", "revoke", "minecraft:story/mine_stone")
```

---

## Server & Utility

### exec()

```python
exec(command: str) -> bool
```

Execute any server command (vanilla or from a plugin) as the server console. Useful for everything that has no dedicated method.

**Parameters:**
- `command` (str): Command string; a leading `/` is optional

**Returns:** `bool` -- `True` if the server found and ran the command, `False` if no such command exists. The command's own output, including vanilla errors such as "No entity was found", only appears in the server console. Malformed arguments can raise `CommandError`.

**Example:**
```python
mc.exec("say Hello from Python!")
mc.exec("/tp Steve 100 64 200")
mc.exec('title Steve actionbar {"text":"Hi"}')
```

**Security note:** `exec()` gives every token holder full console access to the server.

### list()

```python
list() -> dict
```

Get a list of online players and server info.

**Returns:** `dict` -- `{"online_count": int, "max_players": int, "players": [{"name": str, "uuid": str, "gamemode": "SURVIVAL"}, ...]}`

**Example:**
```python
info = mc.list()
print(f"Players online: {info['online_count']}/{info['max_players']}")
for player in info["players"]:
    print(player["name"], player["gamemode"])
```

### spreadplayers()

```python
spreadplayers(center_x: float, center_z: float, spread_distance: float, max_range: float, usernames: list) -> bool
```

Spread players randomly across a region, using the vanilla `spreadplayers` command (teams are not kept together). Like the other vanilla-backed commands, it returns `True` once dispatched; failures such as unknown players only appear in the server console.

**Parameters:**
- `center_x` (float): Center X coordinate
- `center_z` (float): Center Z coordinate
- `spread_distance` (float): Minimum distance between players
- `max_range` (float): Maximum spread range from center
- `usernames` (list): List of player usernames to spread

**Returns:** `bool` -- `True` if successful

**Example:**
```python
mc.spreadplayers(0, 0, 10, 100, ["Steve", "Alex", "Notch"])
```

---

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
- No reply within `timeout` seconds. The command is not sent again, because it may still have run on the server; raise `timeout` for slow operations

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
- Token was regenerated on server (`/mcpylib token regenerate`)

The client raises `AuthenticationError` for any server error message that contains the word "token" (the server replies `Invalid token`).

### CommandError

Raised when a command fails to execute.

```python
class CommandError(MCPyLibError):
    """Command execution failed"""
    pass
```

**Common causes:**
- Invalid block type, or an item id used as a block (`Invalid block type: brick (this is an item, not a placeable block)`)
- Player not found or not online (`Player not found: Steve`)
- Entity not found, usually because its chunk is not loaded (`Entity not found: <uuid>`)
- Missing or wrongly typed parameters (`Missing parameter: ...`, `Error executing command: ...`)
- All connection slots in use (`Server busy: max-connections (10) reached. ...`)

The exception message is the server's error text.

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

---

## Block Names

Block names can be specified with or without the `minecraft:` namespace prefix. They must be placeable blocks: item ids such as `"brick"` (the item) are rejected, use `"bricks"` (the block).

### Common Block Types

```python
# Building blocks
"stone", "cobblestone", "bricks", "stone_bricks"
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

For a complete list of block IDs, see the [Minecraft Wiki](https://minecraft.wiki/w/Java_Edition_data_values).

---

## Advanced Usage

### Persistent Connection

MCPyLib keeps one TCP connection open and reuses it for every command, so many small commands in a row are cheap. Reuse one client instead of creating a new one per command, and close it when you are done:

```python
with MCPyLib(token="your_token") as mc:
    blocks_to_place = [
        (0, 64, 0, "stone"),
        (1, 64, 0, "stone"),
        (2, 64, 0, "stone"),
    ]

    for x, y, z, block in blocks_to_place:
        mc.setblock(x, y, z, block)
```

The server allows `server.max-connections` (default 10) connections at once and closes connections that stay idle for `server.idle-timeout-seconds` (default 300). The client reconnects automatically after an idle close; a client that cannot get a slot receives a `CommandError` starting with `Server busy`.

### Custom Timeout

For slow connections or complex operations:

```python
# Set longer timeout for slow connections
mc = MCPyLib(
    ip="192.168.1.100",
    token="your_token",
    timeout=30.0  # 30 seconds
)

# Large fill operations may take longer (about a million blocks here)
mc.fill(0, 0, 0, 100, 100, 100, "stone")
```

If a command times out, `ConnectionError` is raised and the command is not repeated: it may still finish on the server.

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

### Entity Army Example

```python
import time

def spawn_army(mc, entity_type, x, y, z, count=10, spacing=2):
    """Spawn a group of entities in a grid formation"""
    uuids = []
    for i in range(count):
        row = i // 5
        col = i % 5
        uuid = mc.summon(entity_type, x + col * spacing, y, z + row * spacing)
        uuids.append(uuid)
    return uuids

# Keep the chunk loaded so the entities can be found later
mc.forceload("add", 100, 200)

# Spawn 10 zombies in formation
uuids = spawn_army(mc, "zombie", 100, 64, 200)

# Equip them all
for uuid in uuids:
    mc.setEntityEquipment(uuid, {
        "main_hand": "iron_sword",
        "helmet": "iron_helmet"
    })
    mc.setEntityAI(uuid, False)  # Freeze in place
```

### Scoreboard Game Example

```python
def setup_kill_tracker(mc):
    """Set up a kill tracking scoreboard"""
    mc.addObjective("kills", "playerKillCount", "Kills")
    mc.setDisplaySlot("sidebar", "kills")
    mc.say("Kill tracking enabled! Check the sidebar.")

def setup_teams(mc, players):
    """Divide players into two teams"""
    mc.team("add", name="red", display_name="Red Team")
    mc.team("add", name="blue", display_name="Blue Team")
    mc.team("modify", name="red", option="color", value="red")
    mc.team("modify", name="blue", option="color", value="blue")

    mid = len(players) // 2
    mc.team("join", name="red", members=players[:mid])
    mc.team("join", name="blue", members=players[mid:])

setup_kill_tracker(mc)
setup_teams(mc, ["Steve", "Alex", "Notch", "Jeb"])
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
    """Set block with automatic retry (safe because setblock can run twice)"""
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

---

## Performance Tips

1. **Use fill() for large areas** -- Much faster than individual setblock() calls
2. **Use edit() for mixed blocks** -- One request for a whole 3D region of different blocks
3. **Use clone() to duplicate structures** -- Faster than rebuilding block by block
4. **Reuse one client** -- It keeps its connection open between commands
5. **Handle errors gracefully** -- Retry network errors only for commands that are safe to run twice (a timed-out command may already have run)
6. **Adjust timeout** -- Increase for slow connections or large operations
7. **Use exec() for unsupported commands** -- Fallback for any Minecraft command not yet wrapped
8. **Keep entity UUIDs** -- Store the UUIDs returned by summon(); entities can only be addressed by UUID
9. **Use appropriate block names** -- Both formats work, but consistency helps

---

## See Also

- [Installation Guide](installation.md) - Setup and configuration
- [Protocol Documentation](protocol.md) - Technical details
- [Development Guide](development.md) - Contributing and building
- [Example Scripts](https://github.com/treeleaves30760/MCPyLib/tree/main/examples) - More code examples
