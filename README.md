# MCPyLib - Control Minecraft with Python

Control your Minecraft server remotely using Python! Build, break blocks, track player positions - all with simple Python code.

## Quick Start

### 1. Install Server Plugin

1. Download or compile `MCPyLib-Plugin-0.1.0.jar`
2. Place the JAR file in your Minecraft server's `plugins` folder
3. Start or restart your server
4. Run this command in-game or in server console:
   ```
   /mcpylib token
   ```
5. Copy the displayed token

### 2. Install Python Package

```bash
cd MCPyLib
pip install -e .
```

### 3. Start Using

```python
from mcpylib import MCPyLib

# Connect to server
mc = MCPyLib(
    ip="127.0.0.1",
    port=65535,
    token="YOUR_TOKEN"
)

# Place a diamond block
mc.setblock(100, 64, 200, "minecraft:diamond_block")

# Get block type
block = mc.getblock(100, 64, 200)
print(f"Block: {block}")

# Fill a region
mc.fill(100, 64, 200, 110, 70, 210, "minecraft:glass")

# Get player position
position = mc.getPos("PlayerName")
print(f"Position: {position}")
```

## Main Features

- **setblock(x, y, z, block_name)** - Place a block at coordinates
- **getblock(x, y, z)** - Get block type at coordinates
- **fill(x1, y1, z1, x2, y2, z2, block_name)** - Fill a region
- **getPos(player_name)** - Get player coordinates

## Usage Examples

### Build a Glass Cube
```python
from mcpylib import MCPyLib

mc = MCPyLib(token="YOUR_TOKEN")

# Build a 10x10x10 glass cube
for x in range(10):
    for y in range(10):
        for z in range(10):
            # Only place glass on edges
            if x == 0 or x == 9 or y == 0 or y == 9 or z == 0 or z == 9:
                mc.setblock(x, 64+y, z, "glass")
```

### Track Player Position
```python
import time
from mcpylib import MCPyLib

mc = MCPyLib(token="YOUR_TOKEN")

while True:
    try:
        pos = mc.getPos("Steve")
        print(f"Steve is at: {pos}")
        time.sleep(1)
    except KeyboardInterrupt:
        break
```

### Quick Build a Floor
```python
from mcpylib import MCPyLib

mc = MCPyLib(token="YOUR_TOKEN")

# Use fill to quickly build a 20x20 stone floor
count = mc.fill(0, 63, 0, 20, 63, 20, "stone")
print(f"Placed {count} blocks")
```

## Requirements

- **Python**: 3.11 or higher
- **Minecraft Server**: Spigot/Paper 1.20.1 or higher
- **Java**: 17 or higher (for running Minecraft server)

## Troubleshooting

### Cannot Connect to Server
- Check that server plugin is loaded (run `/plugins`)
- Verify port number is correct (default: 65535)
- Check firewall settings

### Authentication Failed
- Get correct token with `/mcpylib token`
- Ensure token is copied completely without extra spaces
- Check for no extra whitespace

### Player Not Found
- Verify player is online
- Player names are case-sensitive
- Check spelling is correct

## Documentation

- [Installation Guide](docs/installation.md) - Complete installation and setup steps
- [API Reference](docs/api-reference.md) - All available functions and parameters
- [Protocol](docs/protocol.md) - Technical implementation details
- [Development Guide](docs/development.md) - How to build and develop this project

## Example Scripts

See [example.py](example.py) for more example code.

## License

This project is provided for educational and development purposes.

## Author

Created by treeleaves30760

## Version

Current version: 0.1.0
