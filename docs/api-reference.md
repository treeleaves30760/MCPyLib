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

---

## Block Operations

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

**Returns:** `int` -- `1` if successful

**Raises:** `ConnectionError`, `AuthenticationError`, `CommandError`

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

**Returns:** `str` -- Block type identifier (e.g., `"minecraft:stone"`)

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
# Fill a 10x10x10 cube with glass
count = mc.fill(0, 60, 0, 10, 70, 10, "glass")
print(f"Filled {count} blocks")

# Create a floor
mc.fill(0, 63, 0, 20, 63, 20, "stone")

# Build a wall
mc.fill(0, 64, 0, 0, 70, 20, "cobblestone")
```

**Note:** The order of coordinates doesn't matter -- the function automatically determines the bounding box.

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

**Example:**
```python
# Clone a 5x5x5 region to a new location
count = mc.clone(0, 64, 0, 5, 69, 5, 100, 64, 100)
print(f"Cloned {count} blocks")
```

### edit()

```python
edit(x: int, y: int, z: int, blocks: dict) -> int
```

Edit blocks in a region using a block mapping dictionary.

**Parameters:**
- `x` (int): Base X coordinate
- `y` (int): Base Y coordinate
- `z` (int): Base Z coordinate
- `blocks` (dict): Dictionary mapping relative positions to block types

**Returns:** `int` -- Number of blocks edited

**Example:**
```python
# Edit multiple blocks relative to a base position
blocks = {
    "0,0,0": "stone",
    "1,0,0": "oak_planks",
    "0,1,0": "glass"
}
count = mc.edit(100, 64, 200, blocks)
print(f"Edited {count} blocks")
```

---

## Player Control

### getPos()

```python
getPos(username: str) -> List[int]
```

Get a player's current position.

**Parameters:**
- `username` (str): Player's Minecraft username (case-sensitive)

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
- `amount` (int, optional): Number of items. Default: `1`

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

**Returns:** `dict` -- Experience information

**Example:**
```python
# Add 100 XP points
mc.experience("Steve", "add", 100)

# Set level to 30
mc.experience("Steve", "set", 30, target="levels")

# Query current XP
xp = mc.experience("Steve", "query")
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
- `slot` (int): Inventory slot number

**Returns:** `dict` -- Item information (type, count, etc.)

**Example:**
```python
item = mc.getItem("Steve", 0)
print(f"Item in slot 0: {item}")
```

### setItem()

```python
setItem(username: str, slot: int, item: str, amount: int = 1) -> bool
```

Set an item in a player's inventory slot.

**Parameters:**
- `username` (str): Player's Minecraft username
- `slot` (int): Inventory slot number
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

Get or set the world time.

**Parameters:**
- `action` (str): Action to perform (`"set"`, `"add"`, `"query"`)
- `value` (int, optional): Time value (in ticks, or keywords like `"day"`, `"night"`)

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
- `rule` (str): Game rule name (e.g., `"doDaylightCycle"`, `"keepInventory"`)
- `value` (str, optional): Value to set. If `None`, returns current value.

**Returns:** `str` -- Current value of the game rule

**Example:**
```python
# Disable daylight cycle
mc.gamerule("doDaylightCycle", "false")

# Enable keep inventory
mc.gamerule("keepInventory", "true")

# Query a rule
value = mc.gamerule("mobGriefing")
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

Broadcast a message to all players.

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

Send a raw JSON text message to a player.

**Parameters:**
- `username` (str): Target player's username (or `"@a"` for all players)
- `json_text` (str): JSON-formatted text component

**Returns:** `bool` -- `True` if successful

**Example:**
```python
mc.tellraw("@a", '{"text":"Hello!","color":"gold","bold":true}')

mc.tellraw("Steve", '[{"text":"Click "},{"text":"here","color":"blue","underlined":true,"clickEvent":{"action":"run_command","value":"/spawn"}}]')
```

### title()

```python
title(username: str, title: str = "", subtitle: str = "", fade_in: int = 10, stay: int = 70, fade_out: int = 20) -> bool
```

Display a title on a player's screen.

**Parameters:**
- `username` (str): Target player's username (or `"@a"` for all)
- `title` (str, optional): Main title text
- `subtitle` (str, optional): Subtitle text
- `fade_in` (int, optional): Fade-in time in ticks. Default: `10`
- `stay` (int, optional): Stay time in ticks. Default: `70`
- `fade_out` (int, optional): Fade-out time in ticks. Default: `20`

**Returns:** `bool` -- `True` if successful

**Example:**
```python
mc.title("@a", title="Welcome!", subtitle="Enjoy the server")
mc.title("Steve", title="Level Up!", fade_in=5, stay=40, fade_out=10)
```

### playsound()

```python
playsound(username: str, sound: str, source: str = "master", x: float = None, y: float = None, z: float = None, volume: float = 1.0, pitch: float = 1.0) -> bool
```

Play a sound for a player.

**Parameters:**
- `username` (str): Target player's username
- `sound` (str): Sound identifier (e.g., `"minecraft:entity.experience_orb.pickup"`)
- `source` (str, optional): Sound category. Default: `"master"`
- `x, y, z` (float, optional): Sound position. Defaults to player's location.
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
- `source` (str, optional): Sound category to stop

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

Spawn particles at a location.

**Parameters:**
- `particle_type` (str): Particle type (e.g., `"flame"`, `"heart"`, `"explosion"`)
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
spawnpoint(username: str, x: int = None, y: int = None, z: int = None) -> bool
```

Set a player's individual spawn point.

**Parameters:**
- `username` (str): Player's username
- `x, y, z` (int, optional): Spawn coordinates. If omitted, uses the player's current position.

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

Set the world spawn point.

**Parameters:**
- `x, y, z` (int): Spawn coordinates

**Returns:** `bool` -- `True` if successful

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

Manage the world border.

**Parameters:**
- `action` (str): Action to perform (`"get"`, `"set"`, `"add"`, `"center"`, `"damage"`, `"warning"`)
- `value` (float, optional): Size or amount depending on action
- `time` (int, optional): Transition time in seconds (for `"set"` and `"add"`)
- `x, z` (float, optional): Center coordinates (for `"center"`)

**Returns:** `dict` -- World border information

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

Force-load or unload chunks.

**Parameters:**
- `action` (str): Action to perform (`"add"`, `"remove"`)
- `x` (int): Chunk X coordinate
- `z` (int): Chunk Z coordinate

**Returns:** `bool` -- `True` if successful

**Example:**
```python
# Force-load a chunk
mc.forceload("add", 0, 0)

# Remove force-load
mc.forceload("remove", 0, 0)
```

---

## Entity Control

### summon()

```python
summon(entity_type: str, x: float, y: float, z: float) -> str
```

Summon an entity at the specified location.

**Parameters:**
- `entity_type` (str): Entity type (e.g., `"zombie"`, `"minecraft:cow"`)
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

Kill entities matching a selector.

**Parameters:**
- `selector` (str): Target selector (e.g., `"@e[type=zombie]"`, a UUID, or a player name)

**Returns:** `int` -- Number of entities killed

**Example:**
```python
# Kill all zombies
count = mc.kill("@e[type=zombie]")
print(f"Killed {count} zombies")

# Kill a specific entity by UUID
mc.kill("a1b2c3d4-e5f6-7890-abcd-ef1234567890")
```

### getEntityPos()

```python
getEntityPos(entity_uuid: str) -> dict
```

Get the position of an entity by its UUID.

**Parameters:**
- `entity_uuid` (str): Entity UUID

**Returns:** `dict` -- Position with `x`, `y`, `z` keys

**Example:**
```python
pos = mc.getEntityPos("a1b2c3d4-e5f6-7890-abcd-ef1234567890")
print(f"Entity at ({pos['x']}, {pos['y']}, {pos['z']})")
```

### getEntityStatus()

```python
getEntityStatus(entity_uuid: str) -> dict
```

Get the status information of an entity.

**Parameters:**
- `entity_uuid` (str): Entity UUID

**Returns:** `dict` -- Entity status (health, type, custom name, etc.)

**Example:**
```python
status = mc.getEntityStatus("a1b2c3d4-e5f6-7890-abcd-ef1234567890")
print(f"Health: {status['health']}, Type: {status['type']}")
```

### teleportEntity()

```python
teleportEntity(entity_uuid: str, x: float, y: float, z: float, yaw: float = None, pitch: float = None) -> bool
```

Teleport an entity to the specified coordinates.

**Parameters:**
- `entity_uuid` (str): Entity UUID
- `x, y, z` (float): Destination coordinates
- `yaw` (float, optional): Horizontal rotation
- `pitch` (float, optional): Vertical rotation

**Returns:** `bool` -- `True` if successful

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
- `vx` (float): X velocity
- `vy` (float): Y velocity
- `vz` (float): Z velocity

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
- `yaw` (float): Horizontal rotation (-180 to 180)
- `pitch` (float): Vertical rotation (-90 to 90)

**Returns:** `bool` -- `True` if successful

**Example:**
```python
mc.setEntityRotation("a1b2c3d4-...", 90.0, 0.0)
```

### setEntityAI()

```python
setEntityAI(entity_uuid: str, enabled: bool) -> bool
```

Enable or disable an entity's AI.

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

Set or clear an entity's attack target.

**Parameters:**
- `entity_uuid` (str): Entity UUID
- `target_uuid` (str, optional): Target entity UUID. If `None`, clears the target.

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

Remove an entity from the world.

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
- `entity_uuid` (str): Target entity UUID
- `amount` (float): Damage amount (in half-hearts)
- `source_uuid` (str, optional): Source entity UUID (for attribution)

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

**Returns:** `bool` -- `True` if successful

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

Get or modify an entity's attribute.

**Parameters:**
- `entity_uuid` (str): Entity UUID
- `attribute_name` (str): Attribute name (e.g., `"generic.max_health"`, `"generic.movement_speed"`)
- `action` (str, optional): Action (`"get"`, `"set"`, `"add"`, `"remove"`). Default: `"get"`
- `value` (float, optional): Value for set/add actions

**Returns:** `dict` -- Attribute information

**Example:**
```python
# Get max health
health = mc.attribute("a1b2c3d4-...", "generic.max_health")
print(f"Max health: {health}")

# Set max health to 40 (20 hearts)
mc.attribute("a1b2c3d4-...", "generic.max_health", action="set", value=40.0)
```

---

## Entity Equipment

### getEntityEquipment()

```python
getEntityEquipment(entity_uuid: str) -> dict
```

Get the equipment of an entity.

**Parameters:**
- `entity_uuid` (str): Entity UUID

**Returns:** `dict` -- Equipment in each slot (head, chest, legs, feet, mainhand, offhand)

**Example:**
```python
equipment = mc.getEntityEquipment("a1b2c3d4-...")
print(f"Head: {equipment.get('head')}")
print(f"Mainhand: {equipment.get('mainhand')}")
```

### setEntityEquipment()

```python
setEntityEquipment(entity_uuid: str, equipment: dict, drop_chances: dict = None) -> bool
```

Set the equipment of an entity.

**Parameters:**
- `entity_uuid` (str): Entity UUID
- `equipment` (dict): Dictionary mapping slot names to item types
- `drop_chances` (dict, optional): Dictionary mapping slot names to drop chance (0.0 to 1.0)

**Returns:** `bool` -- `True` if successful

**Example:**
```python
mc.setEntityEquipment("a1b2c3d4-...", {
    "head": "diamond_helmet",
    "chest": "diamond_chestplate",
    "legs": "diamond_leggings",
    "feet": "diamond_boots",
    "mainhand": "diamond_sword"
})

# With drop chances
mc.setEntityEquipment("a1b2c3d4-...", {
    "mainhand": "diamond_sword"
}, drop_chances={
    "mainhand": 0.5
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

**Returns:** `bool` -- `True` if successful

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

**Returns:** `bool` -- `True` if successful

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

**Returns:** `dict` -- Villager data including profession, level, and trade information

**Example:**
```python
data = mc.getVillagerData("villager-uuid")
print(f"Profession: {data['profession']}")
print(f"Level: {data['level']}")
```

### setVillagerProfession()

```python
setVillagerProfession(villager_uuid: str, profession: str) -> bool
```

Set a villager's profession.

**Parameters:**
- `villager_uuid` (str): Villager entity UUID
- `profession` (str): Profession type (e.g., `"farmer"`, `"librarian"`, `"weaponsmith"`)

**Returns:** `bool` -- `True` if successful

**Example:**
```python
mc.setVillagerProfession("villager-uuid", "librarian")
```

### setVillagerTrades()

```python
setVillagerTrades(villager_uuid: str, trades: list) -> bool
```

Set a villager's custom trades.

**Parameters:**
- `villager_uuid` (str): Villager entity UUID
- `trades` (list): List of trade definitions

**Returns:** `bool` -- `True` if successful

**Example:**
```python
trades = [
    {"buy": "emerald", "buyAmount": 1, "sell": "diamond", "sellAmount": 1},
    {"buy": "emerald", "buyAmount": 5, "sell": "diamond_sword", "sellAmount": 1}
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
- `display_name` (str, optional): Display name shown to players

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

Set a player's score for an objective.

**Parameters:**
- `objective` (str): Objective name
- `player` (str): Player name
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

**Returns:** `int` -- Score value

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
- `slot` (str): Display slot (`"sidebar"`, `"list"`, `"belowName"`)
- `objective` (str, optional): Objective to display. If `None`, clears the slot.

**Returns:** `bool` -- `True` if successful

**Example:**
```python
# Display kills on sidebar
mc.setDisplaySlot("sidebar", "kills")

# Show health below player names
mc.setDisplaySlot("belowName", "health")

# Clear sidebar
mc.setDisplaySlot("sidebar")
```

---

## Teams

### team()

```python
team(action: str, name: str = None, members: list = None, option: str = None, value: str = None, display_name: str = None) -> dict
```

Manage scoreboard teams.

**Parameters:**
- `action` (str): Action (`"add"`, `"remove"`, `"join"`, `"leave"`, `"modify"`, `"list"`, `"empty"`)
- `name` (str, optional): Team name
- `members` (list, optional): List of player names (for `"join"`)
- `option` (str, optional): Option to modify (e.g., `"color"`, `"friendlyFire"`, `"nametagVisibility"`)
- `value` (str, optional): Value for the option
- `display_name` (str, optional): Team display name

**Returns:** `dict` -- Team information or operation result

**Example:**
```python
# Create a team
mc.team("add", name="red", display_name="Red Team")

# Add players
mc.team("join", name="red", members=["Steve", "Alex"])

# Set team color
mc.team("modify", name="red", option="color", value="red")

# Enable friendly fire
mc.team("modify", name="red", option="friendlyFire", value="true")

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

Manage boss bars displayed to players.

**Parameters:**
- `action` (str): Action (`"add"`, `"remove"`, `"set"`, `"get"`, `"list"`)
- `bar_id` (str): Boss bar identifier
- `title` (str, optional): Display title
- `color` (str, optional): Bar color (`"blue"`, `"green"`, `"pink"`, `"purple"`, `"red"`, `"white"`, `"yellow"`)
- `style` (str, optional): Bar style (`"progress"`, `"notched_6"`, `"notched_10"`, `"notched_12"`, `"notched_20"`)
- `progress` (float, optional): Progress value (0.0 to 1.0)
- `visible` (bool, optional): Whether the bar is visible
- `username` (str, optional): Player to add/remove from the bar

**Returns:** `dict` -- Boss bar information or operation result

**Example:**
```python
# Create a boss bar
mc.bossbar("add", "my_bar", title="Event Progress")

# Configure appearance
mc.bossbar("set", "my_bar", color="red", style="notched_10", progress=0.75)

# Show to a player
mc.bossbar("set", "my_bar", visible=True, username="Steve")

# Update progress
mc.bossbar("set", "my_bar", progress=1.0)

# Remove the bar
mc.bossbar("remove", "my_bar")
```

---

## World Generation & Exploration

### locate()

```python
locate(structure: str, x: int = None, z: int = None) -> dict
```

Locate the nearest structure.

**Parameters:**
- `structure` (str): Structure type (e.g., `"village"`, `"fortress"`, `"stronghold"`)
- `x, z` (int, optional): Search origin. Defaults to world spawn.

**Returns:** `dict` -- Structure location with `x`, `y`, `z` keys

**Example:**
```python
village = mc.locate("village")
print(f"Nearest village at ({village['x']}, {village['z']})")
```

### loot()

```python
loot(loot_table: str, x: float, y: float, z: float) -> int
```

Spawn loot from a loot table at a location.

**Parameters:**
- `loot_table` (str): Loot table identifier (e.g., `"minecraft:chests/simple_dungeon"`)
- `x, y, z` (float): Position to spawn loot

**Returns:** `int` -- Number of items spawned

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

**Returns:** `bool` -- `True` if successful

**Example:**
```python
# Convert a region to desert
mc.fillbiome(0, -64, 0, 100, 320, 100, "desert")

# Replace only plains with jungle
mc.fillbiome(0, -64, 0, 100, 320, 100, "jungle", filter_biome="plains")
```

### placeFeature()

```python
placeFeature(feature: str, x: int = None, y: int = None, z: int = None) -> bool
```

Place a configured feature at a location.

**Parameters:**
- `feature` (str): Feature identifier (e.g., `"minecraft:oak"`, `"minecraft:lake"`)
- `x, y, z` (int, optional): Placement coordinates

**Returns:** `bool` -- `True` if successful

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
- `x, y, z` (int, optional): Placement coordinates

**Returns:** `bool` -- `True` if successful

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
- `max_depth` (int): Maximum generation depth
- `x, y, z` (int, optional): Placement coordinates

**Returns:** `bool` -- `True` if successful

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

**Returns:** `bool` -- `True` if successful

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

Grant or revoke an advancement from a player.

**Parameters:**
- `username` (str): Player's username
- `action` (str): Action (`"grant"`, `"revoke"`)
- `advancement_key` (str): Advancement identifier (e.g., `"minecraft:story/mine_stone"`)

**Returns:** `bool` -- `True` if successful

**Example:**
```python
mc.advancement("Steve", "grant", "minecraft:story/mine_stone")
mc.advancement("Steve", "revoke", "minecraft:story/mine_stone")
```

---

## Server & Utility

### exec()

```python
exec(command: str) -> bool
```

Execute a raw Minecraft command on the server.

**Parameters:**
- `command` (str): Raw command string (without leading `/`)

**Returns:** `bool` -- `True` if successful

**Example:**
```python
mc.exec("say Hello from Python!")
mc.exec("tp Steve 100 64 200")
```

### list()

```python
list() -> dict
```

Get a list of online players and server info.

**Returns:** `dict` -- Player list and server information

**Example:**
```python
info = mc.list()
print(f"Players online: {info}")
```

### spreadplayers()

```python
spreadplayers(center_x: float, center_z: float, spread_distance: float, max_range: float, usernames: list) -> bool
```

Spread players randomly across a region.

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

---

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

---

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

# Spawn 10 zombies in formation
uuids = spawn_army(mc, "zombie", 100, 64, 200)

# Equip them all
for uuid in uuids:
    mc.setEntityEquipment(uuid, {
        "mainhand": "iron_sword",
        "head": "iron_helmet"
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

---

## Performance Tips

1. **Use fill() for large areas** -- Much faster than individual setblock() calls
2. **Use clone() to duplicate structures** -- Faster than rebuilding block by block
3. **Batch operations** -- Group multiple commands together
4. **Handle errors gracefully** -- Implement retry logic for network errors
5. **Adjust timeout** -- Increase for slow connections or large operations
6. **Use exec() for unsupported commands** -- Fallback for any Minecraft command not yet wrapped
7. **Cache entity UUIDs** -- Store UUIDs from summon() calls to avoid repeated lookups
8. **Use appropriate block names** -- Both formats work, but consistency helps

---

## See Also

- [Installation Guide](installation.md) - Setup and configuration
- [Protocol Documentation](protocol.md) - Technical details
- [Development Guide](development.md) - Contributing and building
- [Example Scripts](../example.py) - More code examples
