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

    def setblock(
        self,
        x: int,
        y: int,
        z: int,
        block_name: str,
        block_state: dict = None,
        nbt: dict = None
    ) -> int:
        """Set a block at the specified coordinates

        Args:
            x: X coordinate
            y: Y coordinate
            z: Z coordinate
            block_name: Block type (e.g., "minecraft:stone")
            block_state: Optional block state properties (e.g., {"facing": "north", "half": "bottom"})
            nbt: Optional NBT data for block entities (e.g., {"Items": [...]})

        Returns:
            1 if successful

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If command execution fails

        Example:
            >>> mc.setblock(100, 64, 200, "minecraft:stone")
            1
            >>> mc.setblock(100, 64, 200, "minecraft:oak_stairs", block_state={"facing": "north", "half": "bottom"})
            1
            >>> mc.setblock(100, 64, 200, "minecraft:chest", nbt={"CustomName": '{"text":"My Chest"}'})
            1
        """
        params = {
            "x": x,
            "y": y,
            "z": z,
            "block": block_name
        }

        if block_state is not None:
            params["block_state"] = block_state

        if nbt is not None:
            params["nbt"] = nbt

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

    def teleport(self, username: str, x: float, y: float, z: float,
                 yaw: float = None, pitch: float = None) -> bool:
        """Teleport player to coordinates

        Args:
            username: Player username
            x: X coordinate
            y: Y coordinate
            z: Z coordinate
            yaw: Optional rotation yaw angle
            pitch: Optional rotation pitch angle

        Returns:
            True if successful

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If player not found or teleport fails

        Example:
            >>> mc.teleport("Steve", 100, 64, 200)
            True
            >>> mc.teleport("Steve", 100, 64, 200, yaw=90.0, pitch=0.0)
            True
        """
        params = {
            "username": username,
            "x": x,
            "y": y,
            "z": z
        }

        if yaw is not None and pitch is not None:
            params["yaw"] = yaw
            params["pitch"] = pitch

        return self._send_command("teleport", params)

    def gamemode(self, username: str, mode: str) -> bool:
        """Change player gamemode

        Args:
            username: Player username
            mode: Gamemode (survival, creative, adventure, spectator)

        Returns:
            True if successful

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If player not found or invalid gamemode

        Example:
            >>> mc.gamemode("Steve", "creative")
            True
            >>> mc.gamemode("Steve", "survival")
            True
        """
        params = {
            "username": username,
            "mode": mode
        }
        return self._send_command("gamemode", params)

    def time(self, action: str, value: int = None) -> int:
        """Control world time

        Args:
            action: Action to perform (set, add, query)
            value: Time value (0-24000) for set/add actions

        Returns:
            Current world time

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If invalid action or value out of range

        Example:
            >>> mc.time("set", 1000)
            1000
            >>> mc.time("add", 6000)
            7000
            >>> mc.time("query")
            7000
        """
        params = {
            "action": action
        }

        if value is not None:
            params["value"] = value

        return self._send_command("time", params)

    def weather(self, condition: str, duration: int = None) -> bool:
        """Control world weather

        Args:
            condition: Weather condition (clear, rain, thunder)
            duration: Optional duration in seconds

        Returns:
            True if successful

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If invalid condition

        Example:
            >>> mc.weather("clear")
            True
            >>> mc.weather("rain", duration=600)
            True
            >>> mc.weather("thunder", duration=300)
            True
        """
        params = {
            "condition": condition
        }

        if duration is not None:
            params["duration"] = duration

        return self._send_command("weather", params)

    def give(self, username: str, item: str, amount: int = 1) -> bool:
        """Give items to player

        Args:
            username: Player username
            item: Item type (e.g., "minecraft:diamond")
            amount: Quantity (1-64, default: 1)

        Returns:
            True if successful

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If player not found, invalid item, or amount out of range

        Example:
            >>> mc.give("Steve", "minecraft:diamond", 10)
            True
            >>> mc.give("Steve", "diamond_sword")
            True
        """
        params = {
            "username": username,
            "item": item,
            "amount": amount
        }
        return self._send_command("give", params)

    def summon(self, entity_type: str, x: float, y: float, z: float) -> str:
        """Summon an entity at coordinates

        Args:
            entity_type: Entity type (e.g., "minecraft:zombie", "pig")
            x: X coordinate
            y: Y coordinate
            z: Z coordinate

        Returns:
            Entity UUID as string

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If invalid entity type or coordinates

        Example:
            >>> uuid = mc.summon("minecraft:zombie", 100, 64, 200)
            >>> uuid = mc.summon("pig", 150, 70, 250)
        """
        params = {
            "entity_type": entity_type,
            "x": x,
            "y": y,
            "z": z
        }
        return self._send_command("summon", params)

    def kill(self, selector: str) -> int:
        """Remove entities from the world

        Args:
            selector: Entity selector ("all", entity type, or "player:username")

        Returns:
            Number of entities killed

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If invalid selector or player not found

        Example:
            >>> mc.kill("all")
            42
            >>> mc.kill("zombie")
            5
            >>> mc.kill("player:Steve")
            1
        """
        params = {
            "selector": selector
        }
        return self._send_command("kill", params)

    def getEntityPos(self, entity_uuid: str) -> dict:
        """Get the position of an entity by UUID

        Args:
            entity_uuid: Entity UUID string

        Returns:
            Position dictionary with keys: x, y, z, yaw, pitch

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If entity not found or invalid UUID

        Example:
            >>> uuid = mc.summon("zombie", 100, 64, 200)
            >>> pos = mc.getEntityPos(uuid)
            >>> print(f"Position: ({pos['x']}, {pos['y']}, {pos['z']})")
        """
        params = {
            "uuid": entity_uuid
        }
        return self._send_command("getEntityPos", params)

    def getEntityStatus(self, entity_uuid: str) -> dict:
        """Get complete status information of an entity

        Args:
            entity_uuid: Entity UUID string

        Returns:
            Status dictionary containing:
                - uuid: Entity UUID
                - type: Entity type (e.g., "minecraft:zombie")
                - custom_name: Custom name or None
                - is_valid: Whether entity is valid
                - is_dead: Whether entity is dead
                - position: {x, y, z} coordinates
                - velocity: {x, y, z} velocity vector
                - world: World name
                - health: Current health (LivingEntity only, None otherwise)
                - max_health: Maximum health (LivingEntity only, None otherwise)
                - has_ai: Whether AI is enabled (LivingEntity only, None otherwise)

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If entity not found or invalid UUID

        Example:
            >>> uuid = mc.summon("zombie", 100, 64, 200)
            >>> status = mc.getEntityStatus(uuid)
            >>> print(f"Health: {status['health']}/{status['max_health']}")
            >>> print(f"AI enabled: {status['has_ai']}")
        """
        params = {
            "uuid": entity_uuid
        }
        return self._send_command("getEntityStatus", params)

    def teleportEntity(
        self,
        entity_uuid: str,
        x: float,
        y: float,
        z: float,
        yaw: float = None,
        pitch: float = None
    ) -> bool:
        """Teleport an entity to specified coordinates

        Args:
            entity_uuid: Entity UUID string
            x: Target X coordinate
            y: Target Y coordinate
            z: Target Z coordinate
            yaw: Optional rotation yaw angle
            pitch: Optional rotation pitch angle

        Returns:
            True if successful

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If entity not found or teleport fails

        Example:
            >>> uuid = mc.summon("pig", 100, 64, 200)
            >>> mc.teleportEntity(uuid, 110, 70, 210)
            True
            >>> mc.teleportEntity(uuid, 120, 64, 220, yaw=90.0, pitch=0.0)
            True
        """
        params = {
            "uuid": entity_uuid,
            "x": x,
            "y": y,
            "z": z
        }

        if yaw is not None and pitch is not None:
            params["yaw"] = yaw
            params["pitch"] = pitch

        return self._send_command("teleportEntity", params)

    def setEntityVelocity(
        self,
        entity_uuid: str,
        vx: float,
        vy: float,
        vz: float
    ) -> bool:
        """Set the velocity of an entity

        Args:
            entity_uuid: Entity UUID string
            vx: X velocity component (blocks/tick, clamped to ±10)
            vy: Y velocity component (blocks/tick, clamped to ±10)
            vz: Z velocity component (blocks/tick, clamped to ±10)

        Returns:
            True if successful

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If entity not found

        Example:
            >>> uuid = mc.summon("pig", 100, 64, 200)
            >>> # Make the pig jump
            >>> mc.setEntityVelocity(uuid, 0, 1.0, 0)
            True
            >>> # Push the pig forward
            >>> mc.setEntityVelocity(uuid, 0.5, 0.2, 0.5)
            True
        """
        params = {
            "uuid": entity_uuid,
            "vx": vx,
            "vy": vy,
            "vz": vz
        }
        return self._send_command("setEntityVelocity", params)

    def setEntityRotation(
        self,
        entity_uuid: str,
        yaw: float,
        pitch: float
    ) -> bool:
        """Set the rotation (facing direction) of an entity

        Args:
            entity_uuid: Entity UUID string
            yaw: Horizontal rotation angle (0=south, 90=west, 180=north, 270=east)
            pitch: Vertical rotation angle (-90=up, 0=horizontal, 90=down)

        Returns:
            True if successful

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If entity not found

        Example:
            >>> uuid = mc.summon("zombie", 100, 64, 200)
            >>> # Face north
            >>> mc.setEntityRotation(uuid, 180.0, 0.0)
            True
            >>> # Look up
            >>> mc.setEntityRotation(uuid, 180.0, -45.0)
            True
        """
        params = {
            "uuid": entity_uuid,
            "yaw": yaw,
            "pitch": pitch
        }
        return self._send_command("setEntityRotation", params)

    def setEntityAI(self, entity_uuid: str, enabled: bool) -> bool:
        """Enable or disable AI for a living entity

        When AI is disabled, the entity will not move, attack, or perform
        any autonomous behavior (like a statue).

        Args:
            entity_uuid: Entity UUID string
            enabled: True to enable AI, False to disable

        Returns:
            True if successful

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If entity not found or not a LivingEntity

        Example:
            >>> uuid = mc.summon("zombie", 100, 64, 200)
            >>> # Freeze the zombie (disable AI)
            >>> mc.setEntityAI(uuid, False)
            True
            >>> # Re-enable AI
            >>> mc.setEntityAI(uuid, True)
            True
        """
        params = {
            "uuid": entity_uuid,
            "enabled": enabled
        }
        return self._send_command("setEntityAI", params)

    def setEntityTarget(
        self,
        entity_uuid: str,
        target_uuid: str = None
    ) -> bool:
        """Set or clear the attack target of a mob entity

        Args:
            entity_uuid: UUID of the mob entity
            target_uuid: UUID of the target entity (must be a LivingEntity),
                        or None to clear the target

        Returns:
            True if successful

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If entity not found, not a Mob, or target is invalid

        Example:
            >>> zombie = mc.summon("zombie", 100, 64, 200)
            >>> villager = mc.summon("villager", 105, 64, 200)
            >>> # Make zombie target the villager
            >>> mc.setEntityTarget(zombie, villager)
            True
            >>> # Clear the target
            >>> mc.setEntityTarget(zombie, None)
            True
        """
        params = {
            "uuid": entity_uuid
        }
        if target_uuid is not None:
            params["target_uuid"] = target_uuid
        return self._send_command("setEntityTarget", params)

    def removeEntity(self, entity_uuid: str) -> bool:
        """Remove an entity from the world

        Args:
            entity_uuid: Entity UUID string

        Returns:
            True if successful

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If entity not found or is a player

        Example:
            >>> uuid = mc.summon("pig", 100, 64, 200)
            >>> mc.removeEntity(uuid)
            True
        """
        params = {
            "uuid": entity_uuid
        }
        return self._send_command("removeEntity", params)

    def getEntityEquipment(self, entity_uuid: str) -> dict:
        """Get the equipment of a living entity

        Args:
            entity_uuid: Entity UUID string

        Returns:
            Equipment dictionary with keys:
                - helmet: Item string or None
                - chestplate: Item string or None
                - leggings: Item string or None
                - boots: Item string or None
                - main_hand: Item string or None
                - off_hand: Item string or None

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If entity not found or doesn't support equipment

        Example:
            >>> uuid = mc.summon("zombie", 100, 64, 200)
            >>> equipment = mc.getEntityEquipment(uuid)
            >>> print(f"Helmet: {equipment['helmet']}")
        """
        params = {
            "uuid": entity_uuid
        }
        return self._send_command("getEntityEquipment", params)

    def setEntityEquipment(
        self,
        entity_uuid: str,
        equipment: dict,
        drop_chances: dict = None
    ) -> bool:
        """Set the equipment of a living entity

        Args:
            entity_uuid: Entity UUID string
            equipment: Dictionary of equipment slots to set. Keys can be:
                - helmet: Item name (e.g., "diamond_helmet")
                - chestplate: Item name
                - leggings: Item name
                - boots: Item name
                - main_hand: Item name
                - off_hand: Item name
            drop_chances: Optional dictionary of drop chances (0.0-1.0) for each slot

        Returns:
            True if successful

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If entity not found or doesn't support equipment

        Example:
            >>> uuid = mc.summon("zombie", 100, 64, 200)
            >>> mc.setEntityEquipment(uuid, {
            ...     "helmet": "diamond_helmet",
            ...     "chestplate": "diamond_chestplate",
            ...     "main_hand": "diamond_sword"
            ... }, drop_chances={"helmet": 0.1, "main_hand": 0.5})
            True
        """
        params = {
            "uuid": entity_uuid,
            "equipment": equipment
        }
        if drop_chances is not None:
            params["drop_chances"] = drop_chances
        return self._send_command("setEntityEquipment", params)

    def getVillagerData(self, villager_uuid: str) -> dict:
        """Get villager profession, level, and trade information

        Args:
            villager_uuid: Villager entity UUID string

        Returns:
            Dictionary containing:
                - profession: Villager profession name (e.g., "LIBRARIAN")
                - level: Villager level (1-5)
                - trades: List of trade dictionaries, each containing:
                    - buy1: {"item": str, "amount": int}
                    - buy2: {"item": str, "amount": int} or None
                    - sell: {"item": str, "amount": int}
                    - uses: Current number of uses
                    - max_uses: Maximum number of uses

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If entity not found or not a villager

        Example:
            >>> uuid = mc.summon("villager", 100, 64, 200)
            >>> data = mc.getVillagerData(uuid)
            >>> print(f"Profession: {data['profession']}")
            >>> print(f"Number of trades: {len(data['trades'])}")
        """
        params = {
            "uuid": villager_uuid
        }
        return self._send_command("getVillagerData", params)

    def setVillagerProfession(self, villager_uuid: str, profession: str) -> bool:
        """Set a villager's profession

        Args:
            villager_uuid: Villager entity UUID string
            profession: Profession name. Valid values are:
                ARMORER, BUTCHER, CARTOGRAPHER, CLERIC, FARMER,
                FISHERMAN, FLETCHER, LEATHERWORKER, LIBRARIAN,
                MASON, NITWIT, NONE, SHEPHERD, TOOLSMITH, WEAPONSMITH

        Returns:
            True if successful

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If entity not found, not a villager, or invalid profession

        Example:
            >>> uuid = mc.summon("villager", 100, 64, 200)
            >>> mc.setVillagerProfession(uuid, "LIBRARIAN")
            True
        """
        params = {
            "uuid": villager_uuid,
            "profession": profession
        }
        return self._send_command("setVillagerProfession", params)

    def setVillagerTrades(self, villager_uuid: str, trades: list) -> bool:
        """Set custom trades for a villager (replaces all existing trades)

        Args:
            villager_uuid: Villager entity UUID string
            trades: List of trade definitions. Each trade is a dictionary with:
                - buy1: {"item": str, "amount": int} - First ingredient (required)
                - buy2: {"item": str, "amount": int} - Second ingredient (optional)
                - sell: {"item": str, "amount": int} - Result item (required)
                - max_uses: Maximum number of uses (default: 10)
                - experience_reward: Whether to give XP (default: True)

        Returns:
            True if successful

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If entity not found, not a villager, or invalid trade data

        Example:
            >>> uuid = mc.summon("villager", 100, 64, 200)
            >>> mc.setVillagerProfession(uuid, "LIBRARIAN")
            >>> mc.setVillagerTrades(uuid, [
            ...     {
            ...         "buy1": {"item": "emerald", "amount": 10},
            ...         "sell": {"item": "diamond_sword", "amount": 1},
            ...         "max_uses": 5
            ...     },
            ...     {
            ...         "buy1": {"item": "emerald", "amount": 5},
            ...         "buy2": {"item": "book", "amount": 1},
            ...         "sell": {"item": "enchanted_book", "amount": 1},
            ...         "max_uses": 10
            ...     }
            ... ])
            True
        """
        params = {
            "uuid": villager_uuid,
            "trades": trades
        }
        return self._send_command("setVillagerTrades", params)

    def clone(self, x1: int, y1: int, z1: int,
              x2: int, y2: int, z2: int,
              dest_x: int, dest_y: int, dest_z: int) -> int:
        """Clone a region of blocks to a new location

        Args:
            x1: Source region start X coordinate
            y1: Source region start Y coordinate
            z1: Source region start Z coordinate
            x2: Source region end X coordinate
            y2: Source region end Y coordinate
            z2: Source region end Z coordinate
            dest_x: Destination X coordinate
            dest_y: Destination Y coordinate
            dest_z: Destination Z coordinate

        Returns:
            Number of blocks cloned

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If region too large (max 32768 blocks)

        Example:
            >>> mc.clone(0, 64, 0, 10, 74, 10, 20, 64, 0)
            1100
        """
        params = {
            "x1": x1,
            "y1": y1,
            "z1": z1,
            "x2": x2,
            "y2": y2,
            "z2": z2,
            "dest_x": dest_x,
            "dest_y": dest_y,
            "dest_z": dest_z
        }
        return self._send_command("clone", params)

    def edit(self, x: int, y: int, z: int, blocks: List[List[List]]) -> int:
        """Bulk edit a 3D region of blocks with high performance (like WorldEdit)

        This method allows you to quickly place large numbers of different blocks
        in a single operation, making it ideal for constructing large buildings
        and structures. It's much faster than using setblock repeatedly.

        Args:
            x: Starting X coordinate
            y: Starting Y coordinate
            z: Starting Z coordinate
            blocks: 3D array of blocks [x][y][z], where each element can be:
                - str: Block name (e.g., "minecraft:stone")
                - None: Skip this position (leave unchanged)
                - dict: Complex block with state and NBT:
                    {
                        "block": "minecraft:chest",
                        "block_state": {"facing": "north"},  # Optional
                        "nbt": {"CustomName": '{"text":"Storage"}'}  # Optional
                    }

        Returns:
            Number of blocks placed

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If invalid block data or coordinates

        Examples:
            >>> # Simple 2x2x2 cube of stone and glass
            >>> blocks = [
            ...     [["stone", "glass"], ["stone", "glass"]],
            ...     [["stone", "glass"], ["stone", "glass"]]
            ... ]
            >>> mc.edit(100, 64, 200, blocks)
            8

            >>> # Mixed mode with simple and complex blocks
            >>> blocks = [
            ...     [[None, "stone"], ["glass", None]],
            ...     [[{
            ...         "block": "minecraft:chest",
            ...         "block_state": {"facing": "north"},
            ...         "nbt": {"CustomName": '{"text":"Loot"}'}
            ...     }, "stone"], ["diamond_block", "gold_block"]]
            ... ]
            >>> mc.edit(100, 64, 200, blocks)
            6

            >>> # Build a 10x5x10 house with different materials
            >>> import numpy as np
            >>> blocks = np.full((10, 5, 10), None, dtype=object)
            >>> # Floor
            >>> blocks[:, 0, :] = "stone"
            >>> # Walls
            >>> blocks[0, :, :] = "cobblestone"
            >>> blocks[9, :, :] = "cobblestone"
            >>> blocks[:, :, 0] = "cobblestone"
            >>> blocks[:, :, 9] = "cobblestone"
            >>> # Roof
            >>> blocks[:, 4, :] = "oak_planks"
            >>> mc.edit(100, 64, 200, blocks.tolist())
            250
        """
        params = {
            "x": x,
            "y": y,
            "z": z,
            "blocks": blocks
        }
        return self._send_command("bulkEdit", params)
