# Communication Protocol

This document describes the TCP-based JSON protocol used for communication between the Python client and Minecraft server plugin.

## Overview

MCPyLib uses a simple request-response protocol over TCP:
- Messages are JSON objects
- Each message is terminated with a newline character (`\n`)
- UTF-8 encoding
- Default port: 65535
- Connections are persistent: a client can send any number of requests over one connection. Each request gets exactly one response line, in order

## Connection Flow

```
Client                          Server
  |                               |
  |  1. TCP Connect               |
  |------------------------------>|
  |                               |
  |  2. Send JSON Request         |
  |------------------------------>|
  |                               |
  |       3. Check token          |
  |       4. Execute command on   |
  |          the main thread      |
  |       5. Build response       |
  |                               |
  |  6. Receive JSON Response     |
  |<------------------------------|
  |                               |
  |  Repeat 2-6 on the same       |
  |  connection as often as       |
  |  needed                       |
  |                               |
  |  7. Close Connection          |
  |------------------------------>|
```

Send one request, then wait for its response before sending the next one.

The server closes a connection by itself when:
- no request arrived for `server.idle-timeout-seconds` (default 300; `0` disables the timeout). The Python client notices this and reconnects on its next command.
- a request is valid JSON but has the wrong structure, for example `params` is not an object. The connection is dropped without a reply.

When all `server.max-connections` slots (default 10) are in use, the server still accepts a new connection, reads its first request line (waiting up to 2 seconds for it), replies with a busy error and closes it:

```json
{"success":false,"error":"Server busy: max-connections (10) reached. Close unused clients or raise server.max-connections.","data":null}
```

## Message Format

### Request Message

All requests follow this structure:

```json
{
  "token": "authentication_token",
  "action": "command_name",
  "params": {
    // command-specific parameters
  }
}
```

**Fields:**
- `token` (string): Authentication token, checked on every request
- `action` (string): Command to execute (see [All Supported Actions](#all-supported-actions) for the full list). Action names are case-insensitive: `getPos`, `getpos` and `GETPOS` are the same action
- `params` (object, optional): Command-specific parameters. A missing `params` is treated as `{}`

### Response Message

A successful response carries `data`:

```json
{"success": true, "data": "result_data"}
```

An error response carries `error`:

```json
{"success": false, "error": "error_message"}
```

**Fields:**
- `success` (boolean): Whether the command succeeded
- `data` (any): Result data, only present on success (type depends on command)
- `error` (string): Error message, only present on failure

The server leaves out null values: a success response has no `error` key and an error response has no `data` key (the hand-written `Server busy` reply is the one exception and includes `"data":null`). The same applies inside `data`: object fields whose value would be null are omitted, for example `custom_name` in `getEntityStatus` for an entity without a custom name, or empty slots in `getEntityEquipment`.

## Commands

### setblock

Place a block at specified coordinates.

**Request:**
```json
{
  "token": "your_token",
  "action": "setblock",
  "params": {
    "x": 100,
    "y": 64,
    "z": 200,
    "block": "minecraft:oak_stairs",
    "block_state": {"facing": "north", "half": "top"}
  }
}
```

**Parameters:**
- `x` (integer): X coordinate
- `y` (integer): Y coordinate
- `z` (integer): Z coordinate
- `block` (string): Block type identifier (`minecraft:` prefix optional)
- `block_state` (object, optional): Block state properties; every vanilla property is accepted
- `nbt` (object, optional): Block entity data. Only `CustomName` (plain text, containers) and `Text1`-`Text4` (sign lines) are applied

**Response (Success):**
```json
{
  "success": true,
  "data": 1
}
```

**Response (Error):**
```json
{
  "success": false,
  "error": "Invalid block type: minecraft:invalid"
}
```

### getblock

Get the block type at specified coordinates.

**Request:**
```json
{
  "token": "your_token",
  "action": "getblock",
  "params": {
    "x": 100,
    "y": 64,
    "z": 200
  }
}
```

**Parameters:**
- `x` (integer): X coordinate
- `y` (integer): Y coordinate
- `z` (integer): Z coordinate

**Response (Success):**
```json
{
  "success": true,
  "data": "minecraft:stone"
}
```

**Response (Error):**
```json
{
  "success": false,
  "error": "Missing parameters: x, y, z"
}
```

### getblocks

Get all blocks in a rectangular region as a 3D array.

**Request:**
```json
{
  "token": "your_token",
  "action": "getblocks",
  "params": {
    "x1": 100,
    "y1": 64,
    "z1": 200,
    "x2": 102,
    "y2": 65,
    "z2": 202
  }
}
```

**Parameters:**
- `x1` (integer): Starting X coordinate
- `y1` (integer): Starting Y coordinate
- `z1` (integer): Starting Z coordinate
- `x2` (integer): Ending X coordinate
- `y2` (integer): Ending Y coordinate
- `z2` (integer): Ending Z coordinate

**Response (Success):**

The array is indexed `[x][y][z]`, starting at the minimum corner. Both corners are included, so this 3x2x3 region has 3 x-layers with 2 y-rows of 3 blocks each:

```json
{
  "success": true,
  "data": [
    [
      ["minecraft:stone", "minecraft:dirt", "minecraft:grass_block"],
      ["minecraft:air", "minecraft:air", "minecraft:air"]
    ],
    [
      ["minecraft:stone", "minecraft:stone", "minecraft:dirt"],
      ["minecraft:air", "minecraft:air", "minecraft:air"]
    ],
    [
      ["minecraft:stone", "minecraft:dirt", "minecraft:dirt"],
      ["minecraft:air", "minecraft:air", "minecraft:air"]
    ]
  ]
}
```

**Response (Error):**
```json
{
  "success": false,
  "error": "Missing parameters: x1, y1, z1, x2, y2, z2"
}
```

### fill

Fill a rectangular region with blocks.

**Request:**
```json
{
  "token": "your_token",
  "action": "fill",
  "params": {
    "x1": 100,
    "y1": 64,
    "z1": 200,
    "x2": 110,
    "y2": 74,
    "z2": 210,
    "block": "minecraft:glass"
  }
}
```

**Parameters:**
- `x1, y1, z1` (integers): First corner coordinates
- `x2, y2, z2` (integers): Second corner coordinates
- `block` (string): Block type to fill with

**Response (Success):**
```json
{
  "success": true,
  "data": 1331
}
```

The `data` field contains the number of blocks affected (11 x 11 x 11 = 1331; both corners are included). `fill` has no size limit.

**Response (Error):**
```json
{
  "success": false,
  "error": "Invalid block type: brick (this is an item, not a placeable block)"
}
```

### getPos

Get a player's current position.

**Request:**
```json
{
  "token": "your_token",
  "action": "getPos",
  "params": {
    "username": "Steve"
  }
}
```

**Parameters:**
- `username` (string): Player's Minecraft username (full name, case-insensitive)

**Response (Success):**
```json
{
  "success": true,
  "data": [100, 64, 200]
}
```

The `data` field contains an array of `[x, y, z]` coordinates as integers.

**Response (Error):**
```json
{
  "success": false,
  "error": "Player not found: Steve"
}
```

### All Supported Actions

The following table lists all 72 supported actions and their parameters:

| Action | Parameters | Response Data |
|--------|-----------|---------------|
| `setblock` | `x`, `y`, `z`, `block`, `block_state?`, `nbt?` | `1` (int) |
| `getblock` | `x`, `y`, `z` | Block type string |
| `getblocks` | `x1`, `y1`, `z1`, `x2`, `y2`, `z2` | 3D array of block type strings `[x][y][z]` |
| `fill` | `x1`, `y1`, `z1`, `x2`, `y2`, `z2`, `block` | Blocks affected (int) |
| `clone` | `x1`, `y1`, `z1`, `x2`, `y2`, `z2`, `dest_x`, `dest_y`, `dest_z` | Blocks cloned (int); max 32768 |
| `bulkEdit` | `x`, `y`, `z`, `blocks` (3D array) OR compressed format (see below) | Blocks placed (int) |
| `getPos` | `username` | `[x, y, z]` array |
| `teleport` | `username`, `x`, `y`, `z`, `yaw?`, `pitch?` | `true` |
| `gamemode` | `username`, `mode` | `true` |
| `give` | `username`, `item`, `amount?` (1-64, default 1) | `true` |
| `time` | `action` (`set`/`add`/`query`), `value?` | Current time (int) |
| `weather` | `condition` (`clear`/`rain`/`thunder`), `duration?` (seconds) | `true` |
| `exec` | `command` | `true`, or `false` if the command does not exist |
| `effect` | `username`, `effect`, `duration?` (seconds), `amplifier?`, `hide_particles?` | `true`/`false` |
| `clearEffect` | `username`, `effect?` | `true` |
| `clear` | `username`, `item?`, `max_count?` | Items removed (int) |
| `experience` | `username`, `action`, `amount`, `target?` | `{level, points, progress}` |
| `difficulty` | `level` | `true` |
| `gamerule` | `rule`, `value?` | Current value (string) |
| `say` | `message` | `true` |
| `tell` | `username`, `message` | `true` |
| `tellraw` | `username`, `json_text` | `true`/`false` (dispatch result) |
| `title` | `username`, `title?`, `subtitle?`, `fade_in?`, `stay?`, `fade_out?` | `true` |
| `playsound` | `username`, `sound`, `source?`, `x?`, `y?`, `z?`, `volume?`, `pitch?` | `true` |
| `stopsound` | `username`, `sound?`, `source?` | `true` |
| `particle` | `particle`, `x`, `y`, `z`, `count?`, `dx?`, `dy?`, `dz?`, `speed?` | `true` |
| `spawnpoint` | `username`, `x?`, `y?`, `z?` | `true` |
| `setWorldSpawn` | `x`, `y`, `z` | `true`/`false` |
| `worldborder` | `action`, `value?`, `time?`, `x?`, `z?` | Border info (dict) |
| `forceload` | `action` (`add`/`remove`/`query`), `x`, `z` (block coordinates) | `true`/`false` |
| `summon` | `entity_type`, `x`, `y`, `z` | Entity UUID (string) |
| `kill` | `selector` (`all`, an entity type, or `player:<name>`) | Entities removed (int) |
| `getEntityPos` | `uuid` | `{x, y, z, yaw, pitch}` |
| `getEntityStatus` | `uuid` | Entity status (dict) |
| `teleportEntity` | `uuid`, `x`, `y`, `z`, `yaw?`, `pitch?` | `true`/`false` |
| `setEntityVelocity` | `uuid`, `vx`, `vy`, `vz` | `true` |
| `setEntityRotation` | `uuid`, `yaw`, `pitch` | `true` |
| `setEntityAI` | `uuid`, `enabled` | `true` |
| `setEntityTarget` | `uuid`, `target_uuid?` | `true` |
| `removeEntity` | `uuid` | `true` |
| `getEntityEquipment` | `uuid` | Equipment slots (dict) |
| `setEntityEquipment` | `uuid`, `equipment`, `drop_chances?` | `true` |
| `damage` | `uuid`, `amount`, `source_uuid?` | `true` |
| `ride` | `passenger_uuid`, `vehicle_uuid?` | `true`/`false` |
| `attribute` | `uuid`, `attribute`, `action?` (`get`/`set`), `value?` | `{base_value, value}` |
| `addTag` | `uuid`, `tag` | `true`/`false` |
| `removeTag` | `uuid`, `tag` | `true`/`false` |
| `getTags` | `uuid` | Tags list |
| `getVillagerData` | `uuid` | Villager data (dict) |
| `setVillagerProfession` | `uuid`, `profession` | `true` |
| `setVillagerTrades` | `uuid`, `trades` (array) | `true` |
| `addObjective` | `name`, `criteria`, `display_name?` | `true` |
| `removeObjective` | `name` | `true` |
| `setScore` | `objective`, `player`, `score` | `true` |
| `getScore` | `objective`, `player` | Score (int) |
| `setDisplaySlot` | `slot`, `objective?` | `true` |
| `team` | `action`, `name?`, `members?`, `option?`, `value?`, `display_name?` | Team info (dict) |
| `bossbar` | `action`, `id`, `title?`, `color?`, `style?`, `progress?`, `visible?`, `username?` | Bar info (dict) |
| `enchant` | `username`, `enchantment`, `level?` | `true` |
| `getItem` | `username`, `slot` | Item info (dict) |
| `setItem` | `username`, `slot`, `item`, `amount?` | `true` |
| `locate` | `structure`, `x?`, `z?` | `{found, x?, y?, z?}` |
| `advancement` | `username`, `action`, `advancement` | `true`/`false` |
| `loot` | `loot_table`, `x`, `y`, `z` | Item stacks dropped (int) |
| `fillbiome` | `x1`, `y1`, `z1`, `x2`, `y2`, `z2`, `biome`, `filter_biome?` | `true`/`false` (dispatch result) |
| `placeFeature` | `feature`, `x?`, `y?`, `z?` | `true`/`false` (dispatch result) |
| `placeStructure` | `structure`, `x?`, `y?`, `z?` | `true`/`false` (dispatch result) |
| `placeJigsaw` | `pool`, `target`, `max_depth`, `x?`, `y?`, `z?` | `true`/`false` (dispatch result) |
| `placeTemplate` | `template`, `x?`, `y?`, `z?`, `rotation?`, `mirror?`, `integrity?`, `seed?` | `true`/`false` (dispatch result) |
| `spreadplayers` | `center_x`, `center_z`, `spread_distance`, `max_range`, `usernames` | `true`/`false` (dispatch result) |
| `defaultgamemode` | `mode` | `true` |
| `list` | (none) | `{online_count, max_players, players}` |

*Parameters marked with `?` are optional. The [API Reference](api-reference.md) describes each command in detail.*

**Notes:**
- Commands that are not tied to a player or entity (block commands, `summon`, `kill`, `particle`, `time`, `weather`, `worldborder`, `forceload`, ...) act on the server's main world, the first world loaded (normally the overworld).
- "Dispatch result" means the plugin runs a vanilla command as the console: `true` only says the command was found and run. Vanilla errors such as "No player was found" or "That position is not loaded" appear in the server console, not in the response. `exec` behaves the same way.
- `getPos`, `teleport`, `gamemode`, `give` and `kill` (`player:<name>`) need the full player name; the other player actions also accept the start of a name. Matching is case-insensitive.
- Entities can only be found by UUID while their chunk is loaded (use `forceload` when no player is nearby).

### bulkEdit (Compressed Format)

The `bulkEdit` command supports an optimized compressed encoding that drastically reduces payload size and improves server-side parsing performance. The Python client automatically uses this format.

**Request (Compressed):**
```json
{
  "token": "your_token",
  "action": "bulkEdit",
  "params": {
    "x": 100,
    "y": 64,
    "z": 200,
    "sizeX": 10,
    "sizeY": 5,
    "sizeZ": 10,
    "palette": [null, "stone", "cobblestone", {"block": "chest", "block_state": {"facing": "north"}}],
    "data": "eJxLzs8FAA...",
    "encoding": "palette_rle_zlib"
  }
}
```

**Compressed Format Parameters:**
- `x`, `y`, `z` (integers): Starting coordinates (same as legacy)
- `sizeX`, `sizeY`, `sizeZ` (integers): Dimensions of the 3D block array
- `palette` (array): Ordered list of unique block values. Each entry is one of:
  - `null` — skip position (leave unchanged)
  - `string` — block name (e.g., `"stone"`, `"minecraft:stone"`)
  - `object` — complex block with `block`, optional `block_state`, optional `nbt`
- `data` (string): Base64-encoded, zlib-compressed, RLE-encoded palette indices
- `encoding` (string): Must be `"palette_rle_zlib"`

**Encoding Pipeline:**
1. Flatten the 3D block array to 1D in x→y→z order (row-major)
2. Map each block to its palette index
3. RLE compress: consecutive identical indices become `count,index` pairs
4. Serialize RLE as comma-separated UTF-8 string (e.g., `"500,0,50,1,200,2"`)
5. zlib compress the string
6. Base64 encode the compressed bytes

The response `data` is the number of blocks placed; `null` positions are not counted.

**Format Detection:**
The server detects the format by checking for the `encoding` field. If absent, the legacy format is used (backward compatible): `blocks` is a 3D JSON array `[x][y][z]` whose entries are `null`, a block name string, or an object with `block`, optional `block_state` and optional `nbt`.

**Errors:** `Unsupported encoding: <name>`, `Invalid block type in palette[<i>]: <name>`, `Missing 'block' field in palette[<i>]`, `Invalid compressed block data (truncated or corrupt)`, and `Compressed block data expands beyond 256 MB`.

**Performance:**
When the server internals can be reached (resolved once at runtime via reflection, so no server jar is needed at build time; Minecraft 26.1+ ships unobfuscated so this works on both Paper and Spigot), the server uses direct chunk section writes for plain material blocks, bypassing Bukkit API overhead. Complex blocks with `block_state` or `nbt` fall back to Bukkit API. If NMS is unavailable, all blocks use Bukkit API.

## Error Handling

### Error Types

The server returns these error messages (exact text, with the parts in `<>` filled in):

**Request Errors:**
- `Invalid JSON` - The request line is not valid JSON. The connection stays open
- `Invalid token` - Token is incorrect or missing. The connection stays open
- `Unknown action: <action>` - Invalid action specified
- `Server busy: max-connections (<n>) reached. Close unused clients or raise server.max-connections.` - No free connection slot (see [Connection Flow](#connection-flow))

**Parameter Errors:**
- `Missing parameter: <name>` or `Missing parameters: <name>, <name>, ...` - A required parameter was not provided (the message lists the action's required parameters)
- `Error executing command: <details>` - An unexpected exception, usually a parameter of the wrong type (e.g. `Error executing command: For input string: "abc"`)

**Execution Errors (examples):**
- `Player not found: <username>` - Player is offline or doesn't exist
- `Entity not found: <uuid>` / `Invalid UUID format: <uuid>`
- `Invalid block type: <block>` - Block type doesn't exist; item ids get the suffix ` (this is an item, not a placeable block)`
- `Failed to set block state: Unsupported property '<key>=<value>' for <block>`
- `Region too large (max 32768 blocks): <volume>` - `clone` source region exceeds the limit
- `Invalid action: <action> (valid: ...)` - Unknown sub-action for actions such as `time`, `team` or `worldborder`
- `Failed to <operation>: <details>` - The command failed while running

### Client Error Handling

The Python client maps server errors to exceptions:

```
Error text contains "token" (any case)       -> AuthenticationError
Any other error response                     -> CommandError
Connection failed, closed, or timed out      -> ConnectionError
```

The client re-sends a request once only when a reused connection turns out to be closed before any byte of the reply arrived (for example after an idle timeout). A timed-out request is never re-sent, because it may already have run on the server.

## Authentication

### Token-Based Authentication

Every request must include a valid authentication token:

```json
{
  "token": "abc123def456...",
  "action": "setblock",
  "params": {...}
}
```

### Token Generation

Tokens are generated by the server plugin:
- 32 random bytes
- Base64 URL-encoded (43 characters, no padding)
- Generated on first start when `security.token` is empty, stored in `config.yml`, and printed in the server console (`Authentication token: ...`)
- Shown with `/mcpylib token`; replaced with `/mcpylib token regenerate` (the old token stops working immediately)

The plugin loads the token at startup. If you change `security.token` in `config.yml` by hand, restart the server: `/mcpylib reload` does not reload the token.

### Disabling Authentication

Authentication can be disabled in `config.yml`:

```yaml
security:
  require-token: false
```

This setting is read on every request, so it takes effect after `/mcpylib reload`.

**Warning:** Only disable authentication in trusted environments.

## Network Details

### Connection Settings

- **Protocol:** TCP
- **Default Port:** 65535 (`server.port`)
- **Default Host:** `0.0.0.0` (all interfaces; `server.host: 127.0.0.1` accepts local clients only)
- **Encoding:** UTF-8
- **Message Delimiter:** Newline (`\n`)
- **Max Concurrent Connections:** 10 (`server.max-connections`)
- **Idle Timeout:** 300 seconds (`server.idle-timeout-seconds`, `0` = never)

`server.port`, `server.host` and `server.max-connections` are only read when the plugin starts: restart the server after changing them.

### Connection Lifecycle

Connections are persistent:

1. Client opens TCP connection
2. Client sends JSON request + newline
3. Server processes request
4. Server sends JSON response + newline
5. Steps 2-4 repeat on the same connection
6. The client closes the connection, or the server closes it after the idle timeout

The Python client opens the connection with its first command, reuses it for all further commands, and reconnects transparently when it finds the connection closed.

### Timeouts

- **Client Timeout:** Default 10 seconds per socket operation (the `timeout` argument). On timeout the client raises `ConnectionError` and closes the connection
- **Server Idle Timeout:** `server.idle-timeout-seconds` (default 300). The plugin puts no time limit on a single command

## Performance Considerations

### Command Execution

- Each connection is served by its own thread, but every command executes on the server's main thread
- Ensures thread-safety with Bukkit API
- Commands from all clients run one after another; a long command (a huge `fill`, a large `getblocks`) pauses the server and makes other clients wait
- May have slight latency during high server load

### Large Operations

- `fill` has no size limit, so very large fills can stall the server for a while: split them or use `bulkEdit`
- `clone` is limited to 32,768 blocks
- `bulkEdit` (`edit()` in Python) is the fastest way to place many blocks
- `getblocks` returns one string per block, so large regions produce large responses
- Use `fill` or `bulkEdit` instead of many `setblock` calls for better performance

### Persistent Connections

The client keeps its connection open between commands, which avoids a TCP handshake per command. Each open connection occupies one of the `max-connections` slots until it is closed or times out, so close clients you no longer need (`mc.close()` or a `with` block).

## Security Considerations

### Authentication

- Always use tokens in production
- Regenerate tokens periodically
- Never commit tokens to version control
- Use environment variables for tokens
- Treat the token like a server console password: with `exec` it can run any command

### Network Security

- Bind to `127.0.0.1` for local-only access
- Use firewall rules to restrict access
- Traffic is plain TCP and not encrypted; across untrusted networks use an SSH tunnel or a VPN
- Limit `max-connections` in config
- The `mcpylib.use` permission in `plugin.yml` is not checked: API access is controlled by the token alone

### Input Validation

The server checks:
- The token, on every request
- That required parameters are present
- Names of blocks, items, entity types, effects, enchantments, attributes, particles and similar (unknown names are rejected; sound names are not checked)
- UUID format
- A few ranges: `time set` (0-24000), `give` amount (1-64), `clone` volume (32,768 blocks), inventory slot index; entity velocity is clamped to ±10 and boss bar progress to 0-1

It does not check coordinate bounds or player permissions. A parameter of the wrong type fails with `Error executing command: ...`.

## Future Enhancements

Potential protocol improvements:

- **WebSocket Support** - Bidirectional connections, e.g. for browser clients
- **Batch Commands** - Multiple commands in single request
- **Event Subscriptions** - Server-initiated messages for events
- **Compression for Responses** - Smaller `getblocks` replies (requests from `bulkEdit` are already compressed)
- **SSL/TLS** - Encrypted connections
- **Protocol Versioning** - Support for protocol evolution

## Example Implementation

### Python Client Example

A minimal client that keeps one connection open and sends several requests over it:

```python
import json
import socket

class SimpleClient:
    def __init__(self, ip, port, token):
        self.token = token
        self.sock = socket.create_connection((ip, port), timeout=10.0)
        self.reader = self.sock.makefile("r", encoding="utf-8")

    def send(self, action, params=None):
        request = {"token": self.token, "action": action, "params": params or {}}
        self.sock.sendall((json.dumps(request) + "\n").encode("utf-8"))
        line = self.reader.readline()  # exactly one response line per request
        if not line:
            raise ConnectionError("Connection closed by server")
        response = json.loads(line)
        if not response["success"]:
            raise RuntimeError(response["error"])
        return response.get("data")

    def close(self):
        self.reader.close()
        self.sock.close()

client = SimpleClient("127.0.0.1", 65535, "your_token")
client.send("setblock", {"x": 100, "y": 64, "z": 200, "block": "minecraft:stone"})
print(client.send("getblock", {"x": 100, "y": 64, "z": 200}))  # minecraft:stone
client.close()
```

Reading until the newline matters: responses such as `getblocks` can be much larger than a single `recv()`.

### Java Server Example

The plugin's `ClientHandler` (simplified):

```java
// One thread per connection: handle requests until the client disconnects
String line;
while ((line = in.readLine()) != null) {
    JsonObject request = gson.fromJson(line, JsonObject.class);  // "Invalid JSON" on failure

    // Validate token
    String token = request.has("token") ? request.get("token").getAsString() : "";
    if (!tokenManager.validateToken(token)) {
        sendError(out, "Invalid token");
        continue;  // the connection stays open
    }

    String action = request.has("action") ? request.get("action").getAsString() : "";
    JsonObject params = request.has("params") ? request.getAsJsonObject("params") : new JsonObject();

    // Execute the command on the server's main thread and wait for the result
    CommandResult result = Bukkit.getScheduler()
            .callSyncMethod(plugin, () -> CommandHandler.handleCommand(plugin, action, params))
            .get();

    // Build response (Gson leaves out null values)
    JsonObject response = new JsonObject();
    response.addProperty("success", result.isSuccess());
    if (result.isSuccess()) {
        response.add("data", gson.toJsonTree(result.getData()));
    } else {
        response.addProperty("error", result.getError());
    }
    out.println(gson.toJson(response));
}
```

## See Also

- [API Reference](api-reference.md) - Python API documentation
- [Installation Guide](installation.md) - Setup instructions
- [Development Guide](development.md) - Implementation details
