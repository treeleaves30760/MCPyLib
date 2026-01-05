# MCPyLib Server Plugin

A Spigot/Bukkit plugin that enables remote control of Minecraft servers via the MCPyLib Python library.

## Features

- TCP server for handling Python client connections
- Token-based authentication
- Support for block manipulation commands
- Player position tracking
- Configurable settings

## Requirements

- Minecraft Server (Spigot/Paper) 1.20.1+
- Java 17+
- Maven (for building)

## Building

Build the plugin using Maven:

```bash
mvn clean package
```

The compiled JAR will be in `target/MCPyLib-Plugin-0.1.0.jar`

## Installation

1. Copy the JAR file to your server's `plugins` folder
2. Start or restart your server
3. The plugin will generate a configuration file and authentication token

## Configuration

Edit `plugins/MCPyLib/config.yml`:

```yaml
server:
  port: 65535           # TCP server port
  host: '0.0.0.0'       # Bind address
  max-connections: 10   # Max concurrent connections

security:
  token: ''             # Auto-generated token
  require-token: true   # Enable token authentication

logging:
  log-connections: true # Log incoming connections
  log-commands: true    # Log executed commands
```

## Commands

- `/mcpylib` - Show plugin information
- `/mcpylib token` - Display current authentication token
- `/mcpylib token regenerate` - Generate new token
- `/mcpylib reload` - Reload configuration
- `/mcpylib status` - Show server status

## Permissions

- `mcpylib.admin` - Full access to all commands (default: op)
- `mcpylib.use` - Allow using the API (default: true)

## Getting the Token

After installing the plugin, run this command in-game or in the console:

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

### Block Operations
- `setblock` - Set a block at coordinates with optional state and NBT
- `getblock` - Get block type at coordinates
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

See [PROTOCOL.md](../PROTOCOL.md) for detailed API documentation.

## Security Notes

- Keep your token secret
- Use firewall rules to restrict access to the TCP port
- Consider changing the default port
- Use `host: '127.0.0.1'` to only allow local connections

## Troubleshooting

### Plugin won't load
- Check server console for errors
- Verify Java version (17+ required)
- Ensure Spigot/Paper version is compatible

### Can't connect from Python
- Check firewall settings
- Verify server is running and listening on correct port
- Check token is correct
- Look at server logs for connection errors

## License

This plugin is provided as-is for educational and development purposes.
