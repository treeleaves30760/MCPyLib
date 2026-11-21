# MCPyLib

A Python library for remotely controlling Minecraft servers through a TCP-based API. It consists of a Python client (`pip install mcpylib`) and a Paper/Spigot plugin (Minecraft 26.1+, Java 25).

```python
from mcpylib import MCPyLib

with MCPyLib(ip="127.0.0.1", port=65535, token="YOUR_TOKEN") as mc:
    mc.setblock(100, 64, 200, "minecraft:diamond_block")
    print(mc.getblock(100, 64, 200))  # minecraft:diamond_block
```

## Documentation

- [Installation Guide](installation.md) - Install the plugin and the Python package, configure the server
- [API Reference](api-reference.md) - Every client method with parameters, return values and examples
- [Protocol Specification](protocol.md) - The JSON-over-TCP protocol, for writing clients in other languages
- [Development Guide](development.md) - Building from source and adding new commands

More examples and the changelog are in the [GitHub repository](https://github.com/treeleaves30760/MCPyLib).
