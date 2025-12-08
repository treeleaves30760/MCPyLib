# Development Guide

This guide covers building, developing, and contributing to MCPyLib.

## Project Structure

```
minepy/
├── MCPyLib/                    # Python client library
│   ├── src/mcpylib/
│   │   ├── __init__.py        # Package initialization
│   │   ├── client.py          # Main MCPyLib class
│   │   └── py.typed           # Type hints marker
│   ├── pyproject.toml         # Python project config
│   └── README.md
│
├── ServerPlugin/               # Minecraft server plugin
│   ├── src/main/java/com/mcpylib/plugin/
│   │   ├── MCPyLibPlugin.java      # Plugin entry point
│   │   ├── NetworkServer.java      # TCP server
│   │   ├── ClientHandler.java      # Connection handler
│   │   ├── CommandHandler.java     # Command processor
│   │   ├── CommandResult.java      # Result wrapper
│   │   ├── MCPyLibCommand.java     # In-game commands
│   │   └── TokenManager.java       # Authentication
│   ├── src/main/resources/
│   │   ├── plugin.yml         # Plugin metadata
│   │   └── config.yml         # Default configuration
│   ├── pom.xml                # Maven build config
│   └── README.md
│
├── docs/                       # Documentation
│   ├── installation.md
│   ├── api-reference.md
│   ├── protocol.md
│   └── development.md
│
├── README.md                   # Main documentation
├── example.py                  # Example scripts
└── .gitignore
```

## Building from Source

### Prerequisites

**Python Library:**
- Python 3.11 or higher
- pip or uv

**Server Plugin:**
- Java 17 or higher
- Maven 3.6+
- Spigot/Paper server for testing

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
- `mcpylib-0.1.0-py3-none-any.whl`
- `mcpylib-0.1.0.tar.gz`

#### Using uv

```bash
cd MCPyLib
uv pip install -e .
```

### Build Server Plugin

```bash
cd ServerPlugin
mvn clean package
```

Output: `ServerPlugin/target/MCPyLib-Plugin-0.1.0.jar`

This creates a shaded JAR with all dependencies included.

#### Build Troubleshooting

**Java version issues:**
```bash
export JAVA_HOME=/path/to/jdk-17
mvn clean package
```

**Maven memory issues:**
```bash
export MAVEN_OPTS="-Xmx1024m"
mvn clean package
```

**Clean build:**
```bash
mvn clean install -U
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
   mvn clean package
   ```
3. Copy JAR to test server:
   ```bash
   cp target/MCPyLib-Plugin-0.1.0.jar /path/to/server/plugins/
   ```
4. Reload server:
   ```
   /reload confirm
   ```

### Testing Integration

1. Start Minecraft server with plugin
2. Get token: `/mcpylib token`
3. Run test script:
   ```python
   from mcpylib import MCPyLib

   mc = MCPyLib(token="your_token")
   mc.setblock(0, 64, 0, "diamond_block")
   print("Success!")
   ```
4. Verify in-game

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
     |  {success, data, error}   |
     |<--------------------------|
```

### Key Components

#### Python Client (`client.py`)

- **MCPyLib Class**: Main API interface
- **_send_command()**: Handles TCP communication
- **Custom Exceptions**: Error handling

#### Server Plugin

**MCPyLibPlugin.java**
- Plugin lifecycle management
- Initializes network server
- Manages configuration

**NetworkServer.java**
- TCP server implementation
- Thread pool for connections
- Connection management

**ClientHandler.java**
- Handles individual connections
- JSON parsing
- Token validation
- Routes commands to handler

**CommandHandler.java**
- Executes Minecraft commands
- Thread-safe execution on main thread
- Input validation
- Error handling

**TokenManager.java**
- Token generation (SecureRandom)
- Token validation
- Configuration persistence

## Adding New Commands

### 1. Define Protocol

Update `docs/protocol.md`:

```json
{
  "action": "new_command",
  "params": {
    "param1": "value",
    "param2": 123
  }
}
```

### 2. Implement Python Client

Edit `MCPyLib/src/mcpylib/client.py`:

```python
def new_command(self, param1: str, param2: int) -> Any:
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
    return self._send_command("new_command", params)
```

### 3. Implement Server Handler

Edit `ServerPlugin/src/main/java/com/mcpylib/plugin/CommandHandler.java`:

```java
public CommandResult execute(String action, JsonObject params) {
    return switch (action) {
        case "new_command" -> handleNewCommand(params);
        // ... existing cases
        default -> CommandResult.error("Unknown command: " + action);
    };
}

private CommandResult handleNewCommand(JsonObject params) {
    try {
        String param1 = params.get("param1").getAsString();
        int param2 = params.get("param2").getAsInt();

        // Execute on main thread
        CompletableFuture<Result> future = new CompletableFuture<>();
        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                // Your implementation here
                Object result = doSomething(param1, param2);
                future.complete(Result.success(result));
            } catch (Exception e) {
                future.complete(Result.error(e.getMessage()));
            }
        });

        Result result = future.get(10, TimeUnit.SECONDS);
        return result.isSuccess()
            ? CommandResult.success(result.getData())
            : CommandResult.error(result.getError());

    } catch (Exception e) {
        return CommandResult.error("Error: " + e.getMessage());
    }
}
```

### 4. Update Documentation

- Add to API reference
- Add examples
- Update README if needed

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

security:
  token: 'auto-generated'
  require-token: true

logging:
  log-connections: true
  log-commands: true
```

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
2. Run Python test suite
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
print(f"Fill 1100 blocks: {elapsed:.2f}s")
```

## Dependencies

### Python Library

**Runtime:** None (pure Python stdlib)

**Development:**
```toml
[build-system]
requires = ["setuptools>=61.0"]
build-backend = "setuptools.build_meta"
```

### Server Plugin

**Runtime:**
- Spigot/Paper API 1.20.1+ (provided scope)
- Gson 2.10.1 (shaded)

**Build:**
```xml
<dependencies>
    <dependency>
        <groupId>org.spigotmc</groupId>
        <artifactId>spigot-api</artifactId>
        <version>1.20.1-R0.1-SNAPSHOT</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>com.google.code.gson</groupId>
        <artifactId>gson</artifactId>
        <version>2.10.1</version>
    </dependency>
</dependencies>
```

## Version Management

Keep versions synchronized across:

1. **Python:** `MCPyLib/pyproject.toml`
   ```toml
   version = "0.1.0"
   ```

2. **Java:** `ServerPlugin/pom.xml`
   ```xml
   <version>0.1.0</version>
   ```

3. **Plugin:** `ServerPlugin/src/main/resources/plugin.yml`
   ```yaml
   version: '0.1.0'
   ```

## Release Process

### 1. Update Version

Update version in all three files above.

### 2. Build Both Components

```bash
# Python
cd MCPyLib
python -m build

# Java
cd ServerPlugin
mvn clean package
```

### 3. Test

Test both components together on test server.

### 4. Create Release

```bash
git tag -a v0.1.0 -m "Release version 0.1.0"
git push origin v0.1.0
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

1. **New connection per command** - Consider connection pooling
2. **Synchronous execution** - Commands wait for server main thread
3. **JSON parsing overhead** - Minimal with Gson

### Optimization Opportunities

1. **Persistent Connections**
   - Reuse TCP connections
   - Implement connection pooling
   - Reduce connection overhead

2. **Batch Commands**
   - Multiple commands in single request
   - Reduce network round trips

3. **Async Python API**
   - Non-blocking operations
   - Better for concurrent scripts

4. **Protocol Compression**
   - Reduce bandwidth for large operations

## Security Considerations

### Token Security

- Store tokens securely
- Use environment variables
- Never commit tokens to git
- Rotate tokens regularly

### Network Security

- Bind to localhost for local-only access
- Use firewall rules
- Consider SSL/TLS proxy
- Limit max connections

### Input Validation

Server validates:
- Token authentication
- Parameter types
- Coordinate bounds
- Block types
- Usernames

## Future Enhancements

### Planned Features

- [ ] WebSocket support
- [ ] Event subscriptions
- [ ] Batch command execution
- [ ] Connection pooling
- [ ] More Minecraft operations
- [ ] Player inventory management
- [ ] Entity manipulation
- [ ] World management

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
- [Main README](../README.md)
