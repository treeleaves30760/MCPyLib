# Installation Guide

This guide will help you complete the installation and setup of MCPyLib.

## System Requirements

### Python Package
- Python 3.11 or higher
- pip (Python package manager)

### Minecraft Server Plugin
- Minecraft Server (Spigot or Paper) version 26.1 or higher
- Java 25 or higher
- Building from source needs only a JDK 25: the repository includes the Maven Wrapper, so no separate Maven install is required

For Minecraft 1.21.x servers, keep using plugin 1.1.x.

## Installation Steps

### Step 1: Install Server Plugin

#### Method 1: Using Pre-compiled JAR File

1. Download `MCPyLib-Plugin-1.2.1.jar` from the Releases page
2. Copy the JAR file to your Minecraft server's `plugins` folder
3. Start or restart the Minecraft server

#### Method 2: Build from Source

```bash
cd ServerPlugin
./mvnw clean package   # Windows: mvnw.cmd clean package
```

After compilation, the JAR file will be at `ServerPlugin/target/MCPyLib-Plugin-1.2.1.jar`

Copy it to the server's `plugins` folder:
```bash
cp target/MCPyLib-Plugin-1.2.1.jar /path/to/minecraft/server/plugins/
```

### Step 2: Start Server and Get Token

1. Start the Minecraft server
2. Verify the plugin is loaded by running:
   ```
   /plugins
   ```
   You should see `MCPyLib` displayed in green

3. Get the authentication token. The plugin generates it on first start and prints it in the server console (`[MCPyLib] Authentication token: ...`). You can show it again with:
   ```
   /mcpylib token
   ```
4. Copy the displayed token for later use

### Step 3: Install Python Package

#### From PyPI (Recommended)

```bash
pip install mcpylib
```

Or with uv: `uv pip install mcpylib`

#### Development Mode Installation

If you want to modify the code or develop:

```bash
cd MCPyLib
pip install -e .
```

#### Standard Installation

```bash
cd MCPyLib
pip install .
```

#### Using uv Installation (Optional)

If you have uv installed:

```bash
cd MCPyLib
uv pip install -e .
```

### Step 4: Verify Installation

Create a test file `test.py`:

```python
from mcpylib import MCPyLib

# Replace with your token
mc = MCPyLib(
    ip="127.0.0.1",
    port=65535,
    token="YOUR_TOKEN_HERE"
)

# Test connection
try:
    mc.setblock(0, 64, 0, "minecraft:diamond_block")
    print("Success! Placed diamond block at coordinates (0, 64, 0)")
except Exception as e:
    print(f"Error: {e}")
```

Run the test:
```bash
python test.py
```

If successful, you should see a diamond block appear in the game!

## Advanced Configuration

### Configure Server Plugin

Edit `plugins/MCPyLib/config.yml` to adjust settings:

```yaml
server:
  port: 65535              # TCP server port
  host: '0.0.0.0'          # Bind address (0.0.0.0 = all interfaces)
  max-connections: 10      # Maximum concurrent connections (extra ones get "Server busy")
  idle-timeout-seconds: 300  # Close idle connections; clients reconnect automatically

security:
  token: ''                # Auto-generated authentication token
  require-token: true      # Whether token authentication is required

logging:
  log-connections: true    # Log connections
  log-commands: true       # Log executed commands
```

Reload configuration after modification:
```
/mcpylib reload
```

`/mcpylib reload` applies `require-token`, the logging options and `idle-timeout-seconds` (for new connections). Changes to `port`, `host`, `max-connections` and `token` only take effect after a server restart. To change the token without a restart, use `/mcpylib token regenerate`.

### Configure Local Connection (Recommended)

If you only run Python programs locally, it's recommended to set host to `127.0.0.1` for increased security:

```yaml
server:
  host: '127.0.0.1'
```

Restart the server after changing `host`.

### Configure Remote Connection

If you need to connect from other computers:

1. Set host to `0.0.0.0`
2. Configure firewall to allow the specified port (default 65535)
3. Keep token secure, don't share publicly
4. Consider changing the default port

## Plugin Commands

### Basic Commands

- `/mcpylib` - Display plugin information
- `/mcpylib token` - Display current authentication token
- `/mcpylib status` - Display server status
- `/mcpylib reload` - Reload configuration (see [Configure Server Plugin](#configure-server-plugin) for what it applies)

`/mcpy` and `/mcp` are aliases of `/mcpylib`. All commands work in-game and in the server console.

### Token Management

- `/mcpylib token` - View current token
- `/mcpylib token regenerate` - Regenerate token (old token will become invalid)

### Permissions

- `mcpylib.admin` - Required for all `/mcpylib` commands (default: OP)
- `mcpylib.use` - Declared in `plugin.yml` (default: everyone) but currently not checked. Python clients are not Minecraft players: access to the API is controlled by the token alone, so anyone with the token has full access

## Troubleshooting

### Plugin Won't Load

**Symptoms**: Running `/plugins` doesn't show MCPyLib, or it appears in red

**Solutions**:
1. Check server logs (`logs/latest.log`) for error messages
2. Verify Java version is 25 or higher: `java -version`
3. Confirm server version is Spigot/Paper 26.1+
4. Re-download or recompile the plugin

### Python Package Won't Install

**Symptoms**: Errors when running `pip install`

**Solutions**:
1. Verify Python version: `python --version` (requires 3.11+)
2. Update pip: `pip install --upgrade pip`
3. Use a virtual environment:
   ```bash
   python -m venv venv
   source venv/bin/activate  # Linux/Mac
   venv\Scripts\activate     # Windows
   pip install -e MCPyLib/
   ```

### Cannot Connect to Server

**Symptoms**: Python program shows `ConnectionError`

**Solutions**:
1. Verify plugin is loaded: `/plugins`
2. Check port configuration: view `server.port` in `config.yml`
3. Check firewall settings
4. Verify IP address is correct (use `127.0.0.1` for localhost)
5. Check server logs for error messages

### Authentication Failed

**Symptoms**: Python program shows `AuthenticationError`

**Solutions**:
1. Get the correct token with `/mcpylib token`
2. Ensure token is copied without extra spaces or line breaks
3. If you edited `security.token` in `config.yml` by hand, restart the server: `/mcpylib reload` does not reload the token
4. Try regenerating token: `/mcpylib token regenerate`

### Server Busy

**Symptoms**: Python program shows `CommandError: Server busy: max-connections (10) reached...`

**Solutions**:
1. Close clients you no longer use (`mc.close()` or a `with` block); every open client holds one connection
2. Idle connections are freed automatically after `server.idle-timeout-seconds` (default 300)
3. Raise `server.max-connections` in `config.yml` and restart the server

### Port Already In Use

**Symptoms**: Server startup shows "port already in use"

**Solutions**:
1. Change `server.port` in `config.yml` to a different value (e.g., 65534)
2. Restart the server
3. Use the same port number in your Python program

## Upgrading

### Upgrade Server Plugin

1. Download the new version of the JAR file
2. Stop the Minecraft server
3. Replace the old JAR file in the `plugins` folder
4. Start the server
5. Token and configuration files will be automatically preserved (settings added in newer versions, such as `idle-timeout-seconds`, use their defaults until you add them to your `config.yml`)

Plugin 1.2.1 requires Paper or Spigot 26.1+ on Java 25; stay on 1.1.x for 1.21.x servers.

### Upgrade Python Package

```bash
pip install --upgrade mcpylib
```

If you installed from a git checkout:

```bash
cd MCPyLib
git pull
pip install --upgrade -e .
```

## Next Steps

After installation, check out:
- [API Reference](api-reference.md) - Learn about all available functions
- [README](https://github.com/treeleaves30760/MCPyLib#readme) - View usage examples
- [examples](https://github.com/treeleaves30760/MCPyLib/tree/main/examples) - More example code (copy `examples/.env.example` to `examples/.env` first)
