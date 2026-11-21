# MCPyLib Server Plugin

A Paper/Spigot plugin that enables remote control of Minecraft servers via the MCPyLib Python library.

## Features

- TCP server for handling Python client connections (persistent connections, configurable bind address and connection limit)
- Token-based authentication
- 72 commands: blocks (including fast bulk editing), players, entities, villagers, scoreboards, teams, boss bars, world settings and world generation
- Configurable settings

## Requirements

- Minecraft Server (Spigot/Paper) 26.1+
- Java 25+
- For building: only a JDK 25 (the Maven Wrapper `./mvnw` downloads Maven itself)

## Building

Build the plugin with the Maven Wrapper:

```bash
./mvnw clean package   # Windows: mvnw.cmd clean package
```

The compiled JAR will be in `target/MCPyLib-Plugin-1.2.0.jar`

## Installation

1. Copy the JAR file to your server's `plugins` folder
2. Start or restart your server
3. The plugin will generate a configuration file and authentication token (the token is printed in the server console)

## Configuration

Edit `plugins/MCPyLib/config.yml`:

```yaml
server:
  port: 65535           # TCP server port
  host: '0.0.0.0'       # Bind address
  max-connections: 10   # Max concurrent connections (extra ones get "Server busy")
  idle-timeout-seconds: 300  # Close idle connections (0 = never)

security:
  token: ''             # Auto-generated token
  require-token: true   # Enable token authentication

logging:
  log-connections: true # Log incoming connections
  log-commands: true    # Log executed commands
```

`port`, `host`, `max-connections` and `token` are read when the plugin starts, so restart the server after changing them. `/mcpylib reload` applies the other settings.

## Commands

- `/mcpylib` - Show plugin information
- `/mcpylib token` - Display current authentication token
- `/mcpylib token regenerate` - Generate new token
- `/mcpylib reload` - Reload configuration (not the network settings or the token; see above)
- `/mcpylib status` - Show server status

Aliases: `/mcpy`, `/mcp`.

## Permissions

- `mcpylib.admin` - Required for all `/mcpylib` commands (default: op)
- `mcpylib.use` - Declared (default: true) but not checked: API access is controlled by the token only

## Getting the Token

The token is printed in the server console when the plugin starts. You can also run this command in-game (as an operator) or in the console:

```
/mcpylib token
```

Copy the token and use it in your Python scripts.

## Using the API

After installing the plugin, you can control your server using the Python client library:

```bash
# Install the Python client with uv (recommended)
cd ../MCPyLib
uv sync

# Run your scripts
uv run python your_script.py
```

See [MCPyLib README](../MCPyLib/README.md) for complete client documentation.

## Supported API Commands

A selection of the 72 actions; all of them are listed in [docs/protocol.md](../docs/protocol.md#all-supported-actions).

### Block Operations
- `setblock` - Set a block at coordinates with optional state and NBT
- `getblock` - Get block type at coordinates
- `getblocks` - Get all block types in a region as a 3D array
- `fill` - Fill a region with the same block type
- `bulkEdit` - High-performance bulk editing with mixed block types (like WorldEdit)
- `clone` - Clone a region to a new location

### Player Control
- `getPos` - Get player position
- `teleport` - Teleport player to coordinates
- `gamemode` - Change player gamemode
- `give` - Give items to player

### World Management
- `time` - Control world time
- `weather` - Control weather conditions

### Entity Control
- `summon` - Summon entities
- `kill` - Remove entities

See the [protocol documentation](../docs/protocol.md) for detailed API documentation.

## Security Notes

- Keep your token secret: it allows any command, including arbitrary console commands via `exec`
- Use firewall rules to restrict access to the TCP port
- Consider changing the default port
- Use `host: '127.0.0.1'` to only allow local connections

## Troubleshooting

### Plugin won't load
- Check server console for errors
- Verify Java version (25+ required)
- Ensure Spigot/Paper version is compatible (26.1+; use plugin 1.1.x for 1.21.x servers)

### Can't connect from Python
- Check firewall settings
- Verify server is running and listening on correct port (and that `host` is not `127.0.0.1` when connecting from another machine)
- Check token is correct
- Look at server logs for connection errors
- `Server busy` errors mean all `max-connections` slots are taken; close unused clients or raise the limit

## License

This plugin is provided as-is for educational and development purposes.
