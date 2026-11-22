# Development Guide

This guide covers building, developing, and contributing to MCPyLib.

## Project Structure

```
MCPyLib/
├── MCPyLib/                    # Python client library
│   ├── src/mcpylib/
│   │   ├── __init__.py        # Package initialization, __version__
│   │   ├── client.py          # Main MCPyLib class
│   │   └── py.typed           # Type hints marker
│   ├── pyproject.toml         # Python project config (uv_build backend)
│   ├── uv.lock
│   └── README.md              # PyPI description
│
├── ServerPlugin/               # Minecraft server plugin
│   ├── src/main/java/com/mcpylib/plugin/
│   │   ├── MCPyLibPlugin.java      # Plugin entry point
│   │   ├── NetworkServer.java      # TCP server, connection limit
│   │   ├── ClientHandler.java      # Per-connection request loop
│   │   ├── CommandHandler.java     # Command processor (all actions)
│   │   ├── NMSBlockPlacer.java     # Fast bulk block writes for edit()
│   │   ├── CommandResult.java      # Result wrapper
│   │   ├── MCPyLibCommand.java     # In-game /mcpylib command
│   │   └── TokenManager.java       # Authentication
│   ├── src/main/resources/
│   │   ├── plugin.yml         # Plugin metadata
│   │   └── config.yml         # Default configuration
│   ├── scripts/
│   │   ├── check_paper_abi.py # Paper binary-compatibility check (CI)
│   │   └── live_smoke_test.py # End-to-end test against a live server
│   ├── .mvn/wrapper/          # Maven Wrapper settings
│   ├── mvnw, mvnw.cmd         # Maven Wrapper scripts
│   ├── pom.xml                # Maven build config
│   └── README.md
│
├── docs/                       # Documentation
│   ├── installation.md
│   ├── api-reference.md
│   ├── protocol.md
│   └── development.md
│
├── examples/                   # Example scripts (basics, buildings, games, benchmarks)
├── .github/workflows/ci.yml    # CI: plugin (Spigot + Paper API) and Python package
├── CHANGELOG.md
├── LICENSE
├── README.md                   # Main documentation
└── .gitignore
```

## Building from Source

### Prerequisites

**Python Library:**
- Python 3.11 or higher
- pip or uv

**Server Plugin:**
- Java 25 or higher (e.g. Eclipse Temurin 25)
- Maven is optional: the repo ships the Maven Wrapper (`./mvnw` / `mvnw.cmd`), which downloads Maven 3.9.16 on first use
- Paper or Spigot 26.1+ server for testing

### Build Python Library

#### Development Installation

```bash
cd MCPyLib
pip install -e .
```

Changes to the code will be reflected immediately.

#### Build Distribution

```bash
cd MCPyLib
python -m build
```

Creates packages in `dist/`:
- `mcpylib-1.2.2-py3-none-any.whl`
- `mcpylib-1.2.2.tar.gz`

Or with uv: `uv build`

#### Using uv

```bash
cd MCPyLib
uv pip install -e .
```

### Build Server Plugin

```bash
cd ServerPlugin
./mvnw clean package          # Windows: mvnw.cmd clean package
```

Output: `ServerPlugin/target/MCPyLib-Plugin-1.2.2.jar`

This creates a shaded JAR with all dependencies included. It is compiled
against the Spigot API and runs on both Spigot and Paper.

#### Paper / Spigot compatibility checks

Paper and Spigot have diverged slightly. In 26.x, for example,
`org.bukkit.GameRule` is an interface in Spigot but a class in Paper, and
`RegistryAware#getKeyOrThrow()` only exists in Spigot. A call that compiles
against one API can then fail on the other at runtime (`IncompatibleClassChangeError`
or `NoSuchMethodError`). CI runs these checks on every push:

```bash
./mvnw -Ppaper clean compile   # every API method used must exist in Paper
./mvnw clean package           # normal Spigot build
python -I scripts/check_paper_abi.py "$(which javap)" target/classes   ~/.m2/repository/io/papermc/paper/paper-api/<version>/paper-api-<version>.jar
```

#### NMS fast path

`NMSBlockPlacer` writes blocks straight into chunk sections for bulk `edit`
calls. It reaches server internals through reflection only, resolved once on
first use, so no BuildTools server jar is needed at compile time. Minecraft 26.1+
ships unobfuscated, so the same Mojang names work on Paper and Spigot. If any
lookup fails, the plugin logs a warning and falls back to the Bukkit API.

#### Build Troubleshooting

**Java version issues** (`release version 25 not supported`):
```bash
export JAVA_HOME=/path/to/jdk-25
./mvnw clean package
```

**Maven memory issues:**
```bash
export MAVEN_OPTS="-Xmx1024m"
./mvnw clean package
```

**Clean build:**
```bash
./mvnw clean install -U
```

## Development Workflow

### Python Development

1. Make changes to `MCPyLib/src/mcpylib/`
2. Changes are immediately available (if installed with `-e`)
3. Test changes with example scripts
4. Run tests (if available):
   ```bash
   pytest
   ```

### Java Development

1. Make changes to `ServerPlugin/src/main/java/`
2. Rebuild plugin:
   ```bash
   cd ServerPlugin
   ./mvnw clean package
   ```
3. Copy JAR to test server:
   ```bash
   cp target/MCPyLib-Plugin-1.2.2.jar /path/to/server/plugins/
   ```
4. Restart the server (`/reload` is unsupported on modern Paper)

### Testing Integration

1. Start Minecraft server with plugin
2. Get token: `/mcpylib token` (it is also printed in the console at startup)
3. Run test script:
   ```python
   from mcpylib import MCPyLib

   mc = MCPyLib(token="your_token")
   mc.setblock(0, 64, 0, "diamond_block")
   print("Success!")
   ```
4. Verify in-game
5. For a full end-to-end check, run the live smoke test against a **throwaway**
   test server. It exercises about 60 commands, works with no players online,
   and modifies the world near (0, 100, 0):
   ```bash
   cd MCPyLib
   uv run python ../ServerPlugin/scripts/live_smoke_test.py <token>
   ```

## Architecture Overview

### Client-Server Architecture

```
Python Client              Minecraft Server
     |                           |
     |  1. TCP Connect           |
     |-------------------------->|
     |                           |
     |  2. JSON Request          |
     |  {token, action, params}  |
     |-------------------------->|
     |                           |
     |            3. Validate Token
     |            4. Execute on Main Thread
     |            5. Build Response
     |                           |
     |  6. JSON Response         |
     |  {success, data | error}  |
     |<--------------------------|
     |                           |
     |  (2-6 repeat on the same  |
     |   persistent connection)  |
```

See the [Protocol Documentation](protocol.md) for the wire format.

### Key Components

#### Python Client (`client.py`)

- **MCPyLib Class**: Main API interface
- **_send_command()**: Handles TCP communication over one persistent connection, reconnects when the server closed it, never re-sends after a timeout
- **Custom Exceptions**: Error handling

#### Server Plugin

**MCPyLibPlugin.java**
- Plugin lifecycle management
- Initializes network server
- Manages configuration

**NetworkServer.java**
- TCP server bound to `server.host:server.port`
- Thread pool with `server.max-connections` threads
- Rejects connections beyond the limit with a `Server busy` error

**ClientHandler.java**
- Handles one connection: reads newline-delimited requests until the client disconnects
- Closes connections idle for `server.idle-timeout-seconds`
- JSON parsing and token validation
- Runs each command on the main thread (`callSyncMethod(...).get()`) and writes the response

**CommandHandler.java**
- One static `handleXxx` method per action, dispatched by `handleCommand()`
- Always called on the server's main thread, so it can use the Bukkit API directly
- Parameter checks and error messages

**NMSBlockPlacer.java**
- Fast path for `bulkEdit`: writes plain blocks directly into chunk sections
- Resolved via reflection; falls back to the Bukkit API if unavailable

**TokenManager.java**
- Token generation (SecureRandom)
- Token validation
- Configuration persistence

## Adding New Commands

### 1. Define Protocol

Update `docs/protocol.md`:

```json
{
  "action": "newCommand",
  "params": {
    "param1": "value",
    "param2": 123
  }
}
```

### 2. Implement Python Client

Edit `MCPyLib/src/mcpylib/client.py`:

```python
def newCommand(self, param1: str, param2: int) -> int:
    """
    Description of the command.

    Args:
        param1: Description
        param2: Description

    Returns:
        Description of return value

    Raises:
        ConnectionError: ...
        AuthenticationError: ...
        CommandError: ...
    """
    params = {
        "param1": param1,
        "param2": param2
    }
    return self._send_command("newCommand", params)
```

### 3. Implement Server Handler

Edit `ServerPlugin/src/main/java/com/mcpylib/plugin/CommandHandler.java`.
`handleCommand()` switches on the **lower-cased** action name, so the `case`
label must be lower case:

```java
public static CommandResult handleCommand(MCPyLibPlugin plugin, String action, JsonObject params) {
    try {
        switch (action.toLowerCase()) {
            // ... existing cases
            case "newcommand":
                return handleNewCommand(params);
            default:
                return CommandResult.error("Unknown action: " + action);
        }
    } catch (Exception e) {
        return CommandResult.error("Error executing command: " + e.getMessage());
    }
}

private static CommandResult handleNewCommand(JsonObject params) {
    if (!params.has("param1") || !params.has("param2")) {
        return CommandResult.error("Missing parameters: param1, param2");
    }

    String param1 = params.get("param1").getAsString();
    int param2 = params.get("param2").getAsInt();

    try {
        // Already on the main thread: call the Bukkit API directly
        World world = Bukkit.getWorlds().get(0);
        int result = doSomething(world, param1, param2);
        return CommandResult.success(result);
    } catch (Exception e) {
        return CommandResult.error("Failed to run newCommand: " + e.getMessage());
    }
}
```

**Do not schedule work onto the main thread from a handler.** `ClientHandler`
already runs every handler on the main thread via
`Bukkit.getScheduler().callSyncMethod(...).get()`. Calling
`Bukkit.getScheduler().runTask(...)` or `callSyncMethod(...)` again and then
waiting for the result (`future.get()`) blocks the main thread on a task that
can only run on the main thread: the server freezes and the watchdog
eventually crashes it. To run a vanilla command, call
`Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd)` directly, as
`handleExec()` does.

Return values are serialized with Gson. Null values are omitted from the JSON,
so a map entry set to `null` is simply missing on the client side.

### 4. Update Documentation

- Add to API reference
- Add examples
- Update README if needed
- Add a CHANGELOG entry

## Configuration

### Python Client

Configured programmatically:

```python
mc = MCPyLib(
    ip="127.0.0.1",
    port=65535,
    token="your_token",
    timeout=10.0
)
```

### Server Plugin

Edit `plugins/MCPyLib/config.yml`:

```yaml
server:
  port: 65535
  host: '0.0.0.0'
  max-connections: 10
  idle-timeout-seconds: 300

security:
  token: 'auto-generated'
  require-token: true

logging:
  log-connections: true
  log-commands: true
```

`/mcpylib reload` re-reads the file, but `server.port`, `server.host`,
`server.max-connections` and the token are only applied at startup.
`require-token`, the logging options and `idle-timeout-seconds` (for new
connections) take effect after a reload.

Access in Java:

```java
int port = plugin.getConfig().getInt("server.port");
String host = plugin.getConfig().getString("server.host");
```

## Testing Strategy

### Unit Tests

**Python:**
```python
# tests/test_client.py
import unittest
from mcpylib import MCPyLib

class TestMCPyLib(unittest.TestCase):
    def test_setblock(self):
        # Mock socket connection
        # Test command serialization
        pass
```

**Java:**
```java
// src/test/java/com/mcpylib/plugin/CommandHandlerTest.java
@Test
public void testSetblock() {
    // Mock Bukkit API
    // Test command execution
}
```

### Integration Tests

1. Start test server
2. Run `ServerPlugin/scripts/live_smoke_test.py` (see [Testing Integration](#testing-integration))
3. Verify results in-game
4. Check server logs

### Performance Tests

```python
import time
from mcpylib import MCPyLib

mc = MCPyLib(token="token")

# Test setblock performance
start = time.time()
for i in range(100):
    mc.setblock(i, 64, 0, "stone")
elapsed = time.time() - start
print(f"100 setblocks: {elapsed:.2f}s")

# Test fill performance
start = time.time()
mc.fill(0, 64, 0, 10, 74, 10, "stone")
elapsed = time.time() - start
print(f"Fill 1331 blocks (11x11x11): {elapsed:.2f}s")
```

`examples/benchmarks/fill_vs_edit.py` compares `fill()` and `edit()` on regions of several sizes.

## Dependencies

### Python Library

**Runtime:** `python-dotenv>=1.2.4`. The client itself only uses the standard library; python-dotenv is there for loading `.env` files in scripts such as the examples.

**Build:**
```toml
[build-system]
requires = ["uv_build>=0.11.1,<0.12.0"]
build-backend = "uv_build"
```

### Server Plugin

**Runtime:**
- Spigot/Paper API 26.1+ (provided scope, built against 26.3)
- Gson 2.15.0 (shaded)

**Build:**
```xml
<dependencies>
    <dependency>
        <!-- spigot-api by default; the "paper" profile (-Ppaper) uses
             io.papermc.paper:paper-api 26.3.build.257-stable instead -->
        <groupId>org.spigotmc</groupId>
        <artifactId>spigot-api</artifactId>
        <version>26.3-R0.1-SNAPSHOT</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>com.google.code.gson</groupId>
        <artifactId>gson</artifactId>
        <version>2.15.0</version>
    </dependency>
</dependencies>
```

Build plugins: maven-compiler-plugin 3.16.0 (`release` 25) and maven-shade-plugin 3.6.2.

## Version Management

Keep versions synchronized across:

1. **Python:** `MCPyLib/pyproject.toml`
   ```toml
   version = "1.2.2"
   ```

2. **Python package:** `MCPyLib/src/mcpylib/__init__.py`
   ```python
   __version__ = "1.2.2"
   ```

3. **Java:** `ServerPlugin/pom.xml`
   ```xml
   <version>1.2.2</version>
   ```

`ServerPlugin/src/main/resources/plugin.yml` does not need editing: its
`version: '${project.version}'` is filled in from `pom.xml` at build time.

## Release Process

### 1. Update Version

Update the version in the files above, and add a section to `CHANGELOG.md`.

### 2. Build Both Components

```bash
# Python
cd MCPyLib
python -m build

# Java
cd ServerPlugin
./mvnw clean package
```

### 3. Test

Test both components together on test server.

### 4. Create Release

```bash
git tag -a v1.2.2 -m "Release version 1.2.2"
git push origin v1.2.2
```

### 5. Distribute

- Upload Python package to PyPI (optional)
- Attach JAR to GitHub release
- Update documentation

## Contributing

### Code Style

**Python:**
- Follow PEP 8
- Use type hints
- Add docstrings

**Java:**
- Follow Google Java Style Guide
- Add JavaDoc comments
- Use meaningful variable names

### Pull Request Process

1. Fork the repository
2. Create feature branch
3. Make changes
4. Add tests
5. Update documentation
6. Submit pull request

### Commit Messages

```
feat: Add new command for entity manipulation
fix: Correct token validation logic
docs: Update API reference
test: Add integration tests for fill command
```

## Performance Optimization

### Current Bottlenecks

1. **Synchronous execution** - Every command waits for the server main thread, and commands from all clients run one after another
2. **One request at a time per connection** - Each command is a full network round trip
3. **Large responses** - `getblocks` returns one string per block, uncompressed
4. **JSON parsing overhead** - Minimal with Gson

### Optimization Opportunities

1. **Batch Commands**
   - Multiple commands in single request
   - Reduce network round trips

2. **Async Python API**
   - Non-blocking operations
   - Better for concurrent scripts

3. **Response Compression**
   - Compress large replies such as `getblocks` (bulk `edit` requests are already compressed)

## Security Considerations

### Token Security

- Store tokens securely
- Use environment variables
- Never commit tokens to git
- Rotate tokens regularly
- Remember that a token grants console-level access through `exec`

### Network Security

- Bind to localhost for local-only access
- Use firewall rules
- Use an SSH tunnel or VPN across untrusted networks (traffic is not encrypted)
- Limit max connections

### Input Validation

Server validates:
- Token authentication
- Presence of required parameters
- Block, item, entity and other registry names
- UUID format and a few value ranges

It does not check coordinate bounds or player permissions (the `mcpylib.use`
permission declared in `plugin.yml` is not enforced). Wrongly typed parameters
fail with `Error executing command: ...`.

## Future Enhancements

### Planned Features

- [ ] WebSocket support
- [ ] Event subscriptions
- [ ] Batch command execution
- [x] Persistent connections (v1.2.2)
- [x] More Minecraft operations (72 commands in v1.2.2)
- [x] Player inventory management (getItem, setItem, clear, enchant)
- [x] Entity manipulation (damage, ride, attribute, tags)
- [x] World management (worldborder, forceload, fillbiome, place)

### API Evolution

Consider protocol versioning for future changes:

```json
{
  "version": "2.0",
  "token": "...",
  "action": "...",
  "params": {...}
}
```

## Getting Help

- Read documentation in `docs/`
- Check example scripts
- Review source code
- Open GitHub issue

## See Also

- [Installation Guide](installation.md)
- [API Reference](api-reference.md)
- [Protocol Documentation](protocol.md)
- [Main README](https://github.com/treeleaves30760/MCPyLib#readme)
