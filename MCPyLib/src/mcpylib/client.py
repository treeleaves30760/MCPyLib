"""MCPyLib Client Implementation"""

import json
import socket
from typing import List, Optional


class MCPyLibError(Exception):
    """Base exception for MCPyLib errors"""
    pass


class ConnectionError(MCPyLibError):
    """Raised when connection to server fails"""
    pass


class AuthenticationError(MCPyLibError):
    """Raised when authentication fails"""
    pass


class CommandError(MCPyLibError):
    """Raised when a command execution fails"""
    pass


class MCPyLib:
    """Main client class for interacting with Minecraft server

    Args:
        ip: Server IP address (default: "127.0.0.1")
        port: Server port (default: 65535)
        token: Authentication token
        timeout: Socket timeout in seconds (default: 10)

    Example:
        >>> mc = MCPyLib(ip="127.0.0.1", port=65535, token="your_token")
        >>> mc.setblock(100, 64, 200, "minecraft:stone")
        1
        >>> mc.getblock(100, 64, 200)
        'minecraft:stone'
    """

    def __init__(
        self,
        ip: str = "127.0.0.1",
        port: int = 65535,
        token: str = "",
        timeout: float = 10.0
    ):
        """Initialize MCPyLib client

        Args:
            ip: Server IP address
            port: Server port
            token: Authentication token
            timeout: Socket timeout in seconds
        """
        self.ip = ip
        self.port = port
        self.token = token
        self.timeout = timeout
        self._socket: Optional[socket.socket] = None

    def _connect(self) -> socket.socket:
        """Establish connection to the server

        Returns:
            Connected socket

        Raises:
            ConnectionError: If connection fails
        """
        try:
            sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            sock.settimeout(self.timeout)
            sock.connect((self.ip, self.port))
            return sock
        except socket.error as e:
            raise ConnectionError(f"Failed to connect to {self.ip}:{self.port}: {e}")

    def _send_command(self, action: str, params: dict) -> dict:
        """Send a command to the server and get response

        Args:
            action: Command action name
            params: Command parameters

        Returns:
            Response data

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If command execution fails
        """
        # Prepare request
        request = {
            "token": self.token,
            "action": action,
            "params": params
        }

        # Connect and send
        sock = self._connect()
        try:
            # Send request
            message = json.dumps(request) + "\n"
            sock.sendall(message.encode("utf-8"))

            # Receive response
            buffer = b""
            while b"\n" not in buffer:
                chunk = sock.recv(4096)
                if not chunk:
                    raise ConnectionError("Connection closed by server")
                buffer += chunk

            # Parse response
            response_line = buffer.split(b"\n", 1)[0]
            response = json.loads(response_line.decode("utf-8"))

            # Check response
            if not response.get("success", False):
                error = response.get("error", "Unknown error")
                if "token" in error.lower():
                    raise AuthenticationError(error)
                raise CommandError(error)

            return response.get("data")

        finally:
            sock.close()

    def setblock(self, x: int, y: int, z: int, block_name: str) -> int:
        """Set a block at the specified coordinates

        Args:
            x: X coordinate
            y: Y coordinate
            z: Z coordinate
            block_name: Block type (e.g., "minecraft:stone")

        Returns:
            1 if successful

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If command execution fails

        Example:
            >>> mc.setblock(100, 64, 200, "minecraft:stone")
            1
        """
        params = {
            "x": x,
            "y": y,
            "z": z,
            "block": block_name
        }
        return self._send_command("setblock", params)

    def getblock(self, x: int, y: int, z: int) -> str:
        """Get the block type at the specified coordinates

        Args:
            x: X coordinate
            y: Y coordinate
            z: Z coordinate

        Returns:
            Block type (e.g., "minecraft:stone")

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If command execution fails

        Example:
            >>> mc.getblock(100, 64, 200)
            'minecraft:stone'
        """
        params = {
            "x": x,
            "y": y,
            "z": z
        }
        return self._send_command("getblock", params)

    def fill(
        self,
        x1: int, y1: int, z1: int,
        x2: int, y2: int, z2: int,
        block_name: str
    ) -> int:
        """Fill a region with the specified block

        Args:
            x1: Starting X coordinate
            y1: Starting Y coordinate
            z1: Starting Z coordinate
            x2: Ending X coordinate
            y2: Ending Y coordinate
            z2: Ending Z coordinate
            block_name: Block type (e.g., "minecraft:glass")

        Returns:
            Number of blocks affected

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If command execution fails

        Example:
            >>> mc.fill(100, 64, 200, 110, 74, 210, "minecraft:glass")
            1100
        """
        params = {
            "x1": x1,
            "y1": y1,
            "z1": z1,
            "x2": x2,
            "y2": y2,
            "z2": z2,
            "block": block_name
        }
        return self._send_command("fill", params)

    def getPos(self, username: str) -> List[int]:
        """Get the position of a player

        Args:
            username: Player username

        Returns:
            List of [x, y, z] coordinates

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If command execution fails (e.g., player not found)

        Example:
            >>> mc.getPos("Steve")
            [100, 64, 200]
        """
        params = {
            "username": username
        }
        return self._send_command("getPos", params)
