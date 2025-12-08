# Communication Protocol

This document describes the TCP-based JSON protocol used for communication between the Python client and Minecraft server plugin.

## Overview

MCPyLib uses a simple request-response protocol over TCP:
- Messages are JSON objects
- Each message is terminated with a newline character (`\n`)
- UTF-8 encoding
- Default port: 65535

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
  |       3. Authenticate         |
  |       4. Execute Command      |
  |       5. Build Response       |
  |                               |
  |  6. Receive JSON Response     |
  |<------------------------------|
  |                               |
  |  7. Close Connection          |
  |------------------------------>|
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
- `token` (string): Authentication token for security
- `action` (string): Command to execute (`setblock`, `getblock`, `fill`, `getPos`)
- `params` (object): Command-specific parameters

### Response Message

All responses follow this structure:

```json
{
  "success": true,
  "data": "result_data",
  "error": null
}
```

Or on error:

```json
{
  "success": false,
  "data": null,
  "error": "error_message"
}
```

**Fields:**
- `success` (boolean): Whether the command succeeded
- `data` (any): Result data (type depends on command)
- `error` (string|null): Error message if failed, null otherwise

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
    "block": "minecraft:stone"
  }
}
```

**Parameters:**
- `x` (integer): X coordinate
- `y` (integer): Y coordinate
- `z` (integer): Z coordinate
- `block` (string): Block type identifier

**Response (Success):**
```json
{
  "success": true,
  "data": 1,
  "error": null
}
```

**Response (Error):**
```json
{
  "success": false,
  "data": null,
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
  "data": "minecraft:stone",
  "error": null
}
```

**Response (Error):**
```json
{
  "success": false,
  "data": null,
  "error": "Coordinates out of world bounds"
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
  "data": 1210,
  "error": null
}
```

The `data` field contains the number of blocks affected.

**Response (Error):**
```json
{
  "success": false,
  "data": null,
  "error": "Region too large (max 32768 blocks)"
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
- `username` (string): Player's Minecraft username (case-sensitive)

**Response (Success):**
```json
{
  "success": true,
  "data": [100, 64, 200],
  "error": null
}
```

The `data` field contains an array of `[x, y, z]` coordinates as integers.

**Response (Error):**
```json
{
  "success": false,
  "data": null,
  "error": "Player not found: Steve"
}
```

## Error Handling

### Error Types

The server can return various error messages:

**Authentication Errors:**
- `Invalid token` - Token is incorrect or missing
- `Token required` - Authentication is enabled but no token provided

**Command Errors:**
- `Unknown command: [action]` - Invalid action specified
- `Missing required parameter: [param]` - Required parameter not provided
- `Invalid parameter type: [param]` - Parameter has wrong type

**Execution Errors:**
- `Player not found: [username]` - Player is offline or doesn't exist
- `Invalid block type: [block]` - Block type doesn't exist
- `Coordinates out of world bounds` - Coordinates are invalid
- `Region too large` - Fill region exceeds size limit

### Client Error Handling

The Python client maps server errors to exceptions:

```
"Invalid token" -> AuthenticationError
"Token required" -> AuthenticationError
Connection failed -> ConnectionError
Other errors -> CommandError
```

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
- Base64 URL-encoded
- Stored in `config.yml`
- Retrieved with `/mcpylib token`

### Disabling Authentication

Authentication can be disabled in `config.yml`:

```yaml
security:
  require-token: false
```

**Warning:** Only disable authentication in trusted environments.

## Network Details

### Connection Settings

- **Protocol:** TCP
- **Default Port:** 65535
- **Default Host:** `0.0.0.0` (all interfaces)
- **Encoding:** UTF-8
- **Message Delimiter:** Newline (`\n`)

### Connection Lifecycle

Each command creates a new connection:

1. Client opens TCP connection
2. Client sends JSON request + newline
3. Server processes request
4. Server sends JSON response + newline
5. Connection closed

### Timeouts

- **Client Timeout:** Default 10 seconds (configurable)
- **Server Timeout:** Per connection configured in plugin

## Performance Considerations

### Command Execution

- Commands execute on server's main thread
- Ensures thread-safety with Bukkit API
- May have slight latency during high server load

### Large Operations

- `fill` command has size limits to prevent server lag
- Consider breaking large operations into smaller chunks
- Use `fill` instead of multiple `setblock` calls for better performance

### Connection Pooling

Current implementation creates new connection per command:
- Simple and reliable
- Some overhead for connection setup
- Sufficient for most use cases

Future versions may implement persistent connections for better performance.

## Security Considerations

### Authentication

- Always use tokens in production
- Regenerate tokens periodically
- Never commit tokens to version control
- Use environment variables for tokens

### Network Security

- Bind to `127.0.0.1` for local-only access
- Use firewall rules to restrict access
- Consider running behind reverse proxy for SSL/TLS
- Limit `max-connections` in config

### Input Validation

Server validates all inputs:
- Token authentication
- Parameter type checking
- Coordinate bounds checking
- Block type validation
- Username validation

## Future Enhancements

Potential protocol improvements:

- **WebSocket Support** - Persistent bidirectional connections
- **Batch Commands** - Multiple commands in single request
- **Event Subscriptions** - Server-initiated messages for events
- **Compression** - Reduce bandwidth for large operations
- **SSL/TLS** - Encrypted connections
- **Protocol Versioning** - Support for protocol evolution

## Example Implementation

### Python Client Example

```python
import socket
import json

def send_command(ip, port, token, action, params):
    # Create socket
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.settimeout(10.0)

    try:
        # Connect
        sock.connect((ip, port))

        # Build request
        request = {
            "token": token,
            "action": action,
            "params": params
        }

        # Send request
        message = json.dumps(request) + "\n"
        sock.sendall(message.encode('utf-8'))

        # Receive response
        response = sock.recv(4096).decode('utf-8')
        result = json.loads(response)

        return result

    finally:
        sock.close()
```

### Java Server Example

```java
// Parse request
JsonObject request = JsonParser.parseString(line).getAsJsonObject();
String token = request.get("token").getAsString();
String action = request.get("action").getAsString();
JsonObject params = request.getAsJsonObject("params");

// Validate token
if (!tokenManager.validateToken(token)) {
    return errorResponse("Invalid token");
}

// Execute command
CommandResult result = commandHandler.execute(action, params);

// Build response
JsonObject response = new JsonObject();
response.addProperty("success", result.isSuccess());
response.add("data", result.getData());
response.addProperty("error", result.getError());

// Send response
writer.write(response.toString() + "\n");
writer.flush();
```

## See Also

- [API Reference](api-reference.md) - Python API documentation
- [Installation Guide](installation.md) - Setup instructions
- [Development Guide](development.md) - Implementation details
