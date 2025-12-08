# MCPyLib: Minecraft with Python

We can use python to run the command and send to minecraft server with plugin. MCPyLib is a plugin for spigot and module for python.

## Usage

User can run the python code locally with MCPyLib module.

Initial the class

```python
mc = MCPyLib(ip=127.0.0.1, port=65536, token=TOKEN)
```

User can use below method:

- mc.setblock(x: int, y: int, z: int, block_name: str) -> int
- mc.getblock(x: int, y: int, z: int) -> str
- mc.fill(x_start: int, y_start: int, z_start: int, block_name: str) -> int
- mc.getPos(username: str) -> List[int, int, int]

## Auth

When user install the plugin, we will generate a token. User need to use the token for class initial.
