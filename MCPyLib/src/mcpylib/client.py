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

    def exec(self, command: str) -> bool:
        """Execute any Minecraft command on the server

        A universal command executor that can run any Minecraft command.
        Useful for commands not yet wrapped as dedicated methods.

        Args:
            command: The command to execute (with or without leading /)

        Returns:
            True if the command executed successfully

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If command execution fails

        Example:
            >>> mc.exec("seed")
            True
            >>> mc.exec("/say Hello World")
            True
        """
        params = {"command": command}
        return self._send_command("exec", params)

    def effect(self, username: str, effect_type: str, duration: int = 30,
               amplifier: int = 0, hide_particles: bool = False) -> bool:
        """Apply a potion effect to a player

        Args:
            username: Player username
            effect_type: Effect type (e.g., "speed", "strength", "minecraft:invisibility")
            duration: Duration in seconds (default: 30)
            amplifier: Effect amplifier/level (0 = level I, 1 = level II, etc.)
            hide_particles: Whether to hide effect particles

        Returns:
            True if the effect was applied successfully

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If player not found or invalid effect type

        Example:
            >>> mc.effect("Steve", "speed", duration=60, amplifier=1)
            True
            >>> mc.effect("Steve", "minecraft:invisibility", hide_particles=True)
            True
        """
        params = {
            "username": username,
            "effect": effect_type,
            "duration": duration,
            "amplifier": amplifier,
            "hide_particles": hide_particles
        }
        return self._send_command("effect", params)

    def clearEffect(self, username: str, effect_type: str = None) -> bool:
        """Remove potion effects from a player

        Args:
            username: Player username
            effect_type: Specific effect to remove, or None to remove all effects

        Returns:
            True if successful

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If player not found or invalid effect type

        Example:
            >>> mc.clearEffect("Steve", "speed")
            True
            >>> mc.clearEffect("Steve")  # Remove all effects
            True
        """
        params = {"username": username}
        if effect_type is not None:
            params["effect"] = effect_type
        return self._send_command("clearEffect", params)

    def clear(self, username: str, item: str = None, max_count: int = -1) -> int:
        """Clear items from a player's inventory

        Args:
            username: Player username
            item: Item type to clear (e.g., "diamond"), or None to clear all
            max_count: Maximum number of items to remove (-1 for all)

        Returns:
            Number of items removed

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If player not found or invalid item type

        Example:
            >>> mc.clear("Steve")  # Clear entire inventory
            64
            >>> mc.clear("Steve", "diamond", 10)  # Remove up to 10 diamonds
            10
        """
        params = {"username": username, "max_count": max_count}
        if item is not None:
            params["item"] = item
        return self._send_command("clear", params)

    def experience(self, username: str, action: str, amount: int = 0,
                   target: str = "points") -> dict:
        """Manage player experience points and levels

        Args:
            username: Player username
            action: Action to perform ("add", "set", or "query")
            amount: Amount of XP points or levels
            target: Target type ("points" or "levels")

        Returns:
            Dictionary with keys: level, points, progress

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If player not found or invalid action

        Example:
            >>> mc.experience("Steve", "add", 100)
            {'level': 3, 'points': 100, 'progress': 0.5}
            >>> mc.experience("Steve", "set", 10, target="levels")
            {'level': 10, 'points': 0, 'progress': 0.0}
            >>> mc.experience("Steve", "query")
            {'level': 10, 'points': 0, 'progress': 0.0}
        """
        params = {
            "username": username,
            "action": action,
            "amount": amount,
            "target": target
        }
        return self._send_command("experience", params)

    def difficulty(self, level: str) -> bool:
        """Set the world difficulty

        Args:
            level: Difficulty level ("peaceful", "easy", "normal", "hard")

        Returns:
            True if successful

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If invalid difficulty level

        Example:
            >>> mc.difficulty("hard")
            True
        """
        params = {"level": level}
        return self._send_command("difficulty", params)

    def gamerule(self, rule: str, value: str = None) -> str:
        """Get or set a game rule

        Args:
            rule: Game rule name (e.g., "doDaylightCycle", "keepInventory")
            value: Value to set, or None to query current value

        Returns:
            Current value of the game rule as string

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If invalid game rule

        Example:
            >>> mc.gamerule("doDaylightCycle", "false")
            'false'
            >>> mc.gamerule("keepInventory")
            'false'
        """
        params = {"rule": rule}
        if value is not None:
            params["value"] = value
        return self._send_command("gamerule", params)

    def say(self, message: str) -> bool:
        """Broadcast a message to all players

        Args:
            message: Message to broadcast

        Returns:
            True if successful

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If broadcast fails

        Example:
            >>> mc.say("Hello everyone!")
            True
        """
        params = {"message": message}
        return self._send_command("say", params)

    def tell(self, username: str, message: str) -> bool:
        """Send a private message to a player

        Args:
            username: Player username
            message: Message to send

        Returns:
            True if successful

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If player not found

        Example:
            >>> mc.tell("Steve", "Hello Steve!")
            True
        """
        params = {"username": username, "message": message}
        return self._send_command("tell", params)

    def tellraw(self, username: str, json_text: str) -> bool:
        """Send a raw JSON text message to a player

        Args:
            username: Player username
            json_text: JSON text component string

        Returns:
            True if successful

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If player not found or invalid JSON

        Example:
            >>> mc.tellraw("Steve", '{"text":"Hello","color":"gold","bold":true}')
            True
        """
        params = {"username": username, "json_text": json_text}
        return self._send_command("tellraw", params)

    def title(self, username: str, title: str = "", subtitle: str = "",
              fade_in: int = 10, stay: int = 70, fade_out: int = 20) -> bool:
        """Display a title on a player's screen

        Args:
            username: Player username
            title: Main title text
            subtitle: Subtitle text
            fade_in: Fade in time in ticks (default: 10)
            stay: Stay time in ticks (default: 70)
            fade_out: Fade out time in ticks (default: 20)

        Returns:
            True if successful

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If player not found

        Example:
            >>> mc.title("Steve", "Welcome!", "Enjoy your stay")
            True
        """
        params = {
            "username": username,
            "title": title,
            "subtitle": subtitle,
            "fade_in": fade_in,
            "stay": stay,
            "fade_out": fade_out
        }
        return self._send_command("title", params)

    def playsound(self, username: str, sound: str, source: str = "master",
                  x: float = None, y: float = None, z: float = None,
                  volume: float = 1.0, pitch: float = 1.0) -> bool:
        """Play a sound for a player

        Args:
            username: Player username
            sound: Sound name (e.g., "entity.experience_orb.pickup")
            source: Sound source category (master, music, record, weather, block, hostile, neutral, player, ambient, voice)
            x: X coordinate (default: player position)
            y: Y coordinate (default: player position)
            z: Z coordinate (default: player position)
            volume: Volume (default: 1.0)
            pitch: Pitch (default: 1.0)

        Returns:
            True if successful

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If player not found or invalid sound

        Example:
            >>> mc.playsound("Steve", "entity.experience_orb.pickup")
            True
            >>> mc.playsound("Steve", "block.note_block.harp", x=100, y=64, z=200, pitch=1.5)
            True
        """
        params = {
            "username": username,
            "sound": sound,
            "source": source,
            "volume": volume,
            "pitch": pitch
        }
        if x is not None and y is not None and z is not None:
            params["x"] = x
            params["y"] = y
            params["z"] = z
        return self._send_command("playsound", params)

    def stopsound(self, username: str, sound: str = None, source: str = None) -> bool:
        """Stop sounds for a player

        Args:
            username: Player username
            sound: Specific sound to stop, or None to stop all
            source: Sound source category to stop, or None for all sources

        Returns:
            True if successful

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If player not found

        Example:
            >>> mc.stopsound("Steve")  # Stop all sounds
            True
            >>> mc.stopsound("Steve", "minecraft:music.creative")
            True
        """
        params = {"username": username}
        if sound is not None:
            params["sound"] = sound
        if source is not None:
            params["source"] = source
        return self._send_command("stopsound", params)

    def particle(self, particle_type: str, x: float, y: float, z: float,
                 count: int = 1, dx: float = 0, dy: float = 0, dz: float = 0,
                 speed: float = 0) -> bool:
        """Spawn particles at a location

        Args:
            particle_type: Particle type (e.g., "flame", "heart", "minecraft:smoke")
            x: X coordinate
            y: Y coordinate
            z: Z coordinate
            count: Number of particles (default: 1)
            dx: X spread (default: 0)
            dy: Y spread (default: 0)
            dz: Z spread (default: 0)
            speed: Particle speed (default: 0)

        Returns:
            True if successful

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If invalid particle type

        Example:
            >>> mc.particle("flame", 100, 65, 200, count=50, dx=1, dy=1, dz=1, speed=0.1)
            True
        """
        params = {
            "particle": particle_type,
            "x": x, "y": y, "z": z,
            "count": count,
            "dx": dx, "dy": dy, "dz": dz,
            "speed": speed
        }
        return self._send_command("particle", params)

    def spawnpoint(self, username: str, x: float = None, y: float = None,
                   z: float = None) -> bool:
        """Set a player's spawn point

        Args:
            username: Player username
            x: X coordinate (default: player's current position)
            y: Y coordinate (default: player's current position)
            z: Z coordinate (default: player's current position)

        Returns:
            True if successful

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If player not found

        Example:
            >>> mc.spawnpoint("Steve", 100, 64, 200)
            True
            >>> mc.spawnpoint("Steve")  # Use current position
            True
        """
        params = {"username": username}
        if x is not None and y is not None and z is not None:
            params["x"] = x
            params["y"] = y
            params["z"] = z
        return self._send_command("spawnpoint", params)

    def setworldspawn(self, x: int, y: int, z: int) -> bool:
        """Set the world spawn location

        Args:
            x: X coordinate
            y: Y coordinate
            z: Z coordinate

        Returns:
            True if successful

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If setting spawn fails

        Example:
            >>> mc.setworldspawn(0, 64, 0)
            True
        """
        params = {"x": x, "y": y, "z": z}
        return self._send_command("setworldspawn", params)

    def worldborder(self, action: str, value: float = None, time: int = None,
                    x: float = None, z: float = None) -> dict:
        """Manage the world border

        Args:
            action: Action ("get", "set", "center", "add", "damage", "warning")
            value: Size value for set/add, damage amount for damage, warning distance for warning
            time: Transition time in seconds for set/add, warning time for warning
            x: X coordinate for center action
            z: Z coordinate for center action

        Returns:
            Dictionary with border information

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If invalid action or parameters

        Example:
            >>> mc.worldborder("get")
            {'size': 60000000, 'center_x': 0, 'center_z': 0, ...}
            >>> mc.worldborder("set", value=1000, time=10)
            {'size': 1000}
            >>> mc.worldborder("center", x=100, z=200)
            {'center_x': 100, 'center_z': 200}
        """
        params = {"action": action}
        if value is not None:
            params["value"] = value
        if time is not None:
            params["time"] = time
        if x is not None:
            params["x"] = x
        if z is not None:
            params["z"] = z
        return self._send_command("worldborder", params)

    def forceload(self, action: str, x: int, z: int) -> bool:
        """Force-load or unload chunks

        Args:
            action: Action ("add", "remove", "query")
            x: Block X coordinate (will be converted to chunk coordinate)
            z: Block Z coordinate (will be converted to chunk coordinate)

        Returns:
            True/False for add/remove/query operations

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If invalid action

        Example:
            >>> mc.forceload("add", 100, 200)
            True
            >>> mc.forceload("query", 100, 200)
            True
            >>> mc.forceload("remove", 100, 200)
            True
        """
        params = {"action": action, "x": x, "z": z}
        return self._send_command("forceload", params)

    def damage(self, entity_uuid: str, amount: float,
               source_uuid: str = None) -> bool:
        """Deal damage to an entity

        Args:
            entity_uuid: Target entity UUID
            amount: Damage amount
            source_uuid: Optional UUID of the entity dealing damage

        Returns:
            True if successful

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If entity not found or not a LivingEntity

        Example:
            >>> uuid = mc.summon("zombie", 100, 64, 200)
            >>> mc.damage(uuid, 5.0)
            True
        """
        params = {"uuid": entity_uuid, "amount": amount}
        if source_uuid is not None:
            params["source_uuid"] = source_uuid
        return self._send_command("damage", params)

    def addObjective(self, name: str, criteria: str,
                     display_name: str = None) -> bool:
        """Add a scoreboard objective

        Args:
            name: Objective name
            criteria: Criteria (e.g., "dummy", "playerKillCount")
            display_name: Display name (default: same as name)

        Returns:
            True if successful

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If objective already exists

        Example:
            >>> mc.addObjective("kills", "playerKillCount", "Player Kills")
            True
        """
        params = {"name": name, "criteria": criteria}
        if display_name is not None:
            params["display_name"] = display_name
        return self._send_command("addObjective", params)

    def removeObjective(self, name: str) -> bool:
        """Remove a scoreboard objective

        Args:
            name: Objective name

        Returns:
            True if successful

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If objective not found

        Example:
            >>> mc.removeObjective("kills")
            True
        """
        params = {"name": name}
        return self._send_command("removeObjective", params)

    def setScore(self, objective: str, player: str, score: int) -> bool:
        """Set a player's score for an objective

        Args:
            objective: Objective name
            player: Player name or entity name
            score: Score value

        Returns:
            True if successful

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If objective not found

        Example:
            >>> mc.setScore("kills", "Steve", 10)
            True
        """
        params = {"objective": objective, "player": player, "score": score}
        return self._send_command("setScore", params)

    def getScore(self, objective: str, player: str) -> int:
        """Get a player's score for an objective

        Args:
            objective: Objective name
            player: Player name or entity name

        Returns:
            Score value

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If objective not found

        Example:
            >>> mc.getScore("kills", "Steve")
            10
        """
        params = {"objective": objective, "player": player}
        return self._send_command("getScore", params)

    def setDisplaySlot(self, slot: str, objective: str = None) -> bool:
        """Set which objective is displayed in a display slot

        Args:
            slot: Display slot ("below_name", "sidebar", "player_list")
            objective: Objective name, or None to clear the slot

        Returns:
            True if successful

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If invalid slot or objective not found

        Example:
            >>> mc.setDisplaySlot("sidebar", "kills")
            True
            >>> mc.setDisplaySlot("sidebar")  # Clear sidebar
            True
        """
        params = {"slot": slot}
        if objective is not None:
            params["objective"] = objective
        return self._send_command("setDisplaySlot", params)

    def addTag(self, entity_uuid: str, tag: str) -> bool:
        """Add a scoreboard tag to an entity

        Args:
            entity_uuid: Entity UUID
            tag: Tag string

        Returns:
            True if the tag was added (False if already had it)

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If entity not found

        Example:
            >>> uuid = mc.summon("zombie", 100, 64, 200)
            >>> mc.addTag(uuid, "boss")
            True
        """
        params = {"uuid": entity_uuid, "tag": tag}
        return self._send_command("addTag", params)

    def removeTag(self, entity_uuid: str, tag: str) -> bool:
        """Remove a scoreboard tag from an entity

        Args:
            entity_uuid: Entity UUID
            tag: Tag string

        Returns:
            True if the tag was removed

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If entity not found

        Example:
            >>> mc.removeTag(uuid, "boss")
            True
        """
        params = {"uuid": entity_uuid, "tag": tag}
        return self._send_command("removeTag", params)

    def getTags(self, entity_uuid: str) -> List[str]:
        """Get all scoreboard tags of an entity

        Args:
            entity_uuid: Entity UUID

        Returns:
            List of tag strings

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If entity not found

        Example:
            >>> mc.getTags(uuid)
            ['boss', 'enemy']
        """
        params = {"uuid": entity_uuid}
        return self._send_command("getTags", params)

    def team(self, action: str, name: str = None, members: List[str] = None,
             option: str = None, value: str = None,
             display_name: str = None) -> dict:
        """Manage scoreboard teams

        Args:
            action: Action ("add", "remove", "join", "leave", "modify", "list")
            name: Team name (required for most actions)
            members: List of player names (for join/leave)
            option: Option name for modify (displayname, color, friendlyfire, seefriendlyinvisibles, prefix, suffix)
            value: Value for modify action
            display_name: Display name for add action

        Returns:
            Dictionary with team information

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If team not found or invalid action

        Example:
            >>> mc.team("add", "red_team", display_name="Red Team")
            {'name': 'red_team'}
            >>> mc.team("join", "red_team", members=["Steve", "Alex"])
            {'added': 2}
            >>> mc.team("modify", "red_team", option="color", value="RED")
            {'modified': True}
            >>> mc.team("list")
            {'teams': [...]}
        """
        params = {"action": action}
        if name is not None:
            params["name"] = name
        if members is not None:
            params["members"] = members
        if option is not None:
            params["option"] = option
        if value is not None:
            params["value"] = value
        if display_name is not None:
            params["display_name"] = display_name
        return self._send_command("team", params)

    def bossbar(self, action: str, bar_id: str, title: str = None,
                color: str = None, style: str = None, progress: float = None,
                visible: bool = None, username: str = None) -> dict:
        """Manage boss bars

        Args:
            action: Action ("add", "remove", "set", "addplayer", "removeplayer", "get", "list")
            bar_id: Boss bar ID
            title: Bar title (for add/set)
            color: Bar color (WHITE, PINK, BLUE, RED, GREEN, YELLOW, PURPLE) (for add/set)
            style: Bar style (SOLID, SEGMENTED_6, SEGMENTED_10, SEGMENTED_12, SEGMENTED_20) (for add/set)
            progress: Progress value 0.0-1.0 (for add/set)
            visible: Visibility (for add/set)
            username: Player name (for addplayer/removeplayer)

        Returns:
            Dictionary with boss bar information

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If boss bar not found or invalid action

        Example:
            >>> mc.bossbar("add", "my_bar", title="Boss Fight", color="RED")
            {'id': 'my_bar'}
            >>> mc.bossbar("addplayer", "my_bar", username="Steve")
            {'added': True}
            >>> mc.bossbar("set", "my_bar", progress=0.5)
            {'updated': True}
        """
        params = {"action": action, "id": bar_id}
        if title is not None:
            params["title"] = title
        if color is not None:
            params["color"] = color
        if style is not None:
            params["style"] = style
        if progress is not None:
            params["progress"] = progress
        if visible is not None:
            params["visible"] = visible
        if username is not None:
            params["username"] = username
        return self._send_command("bossbar", params)

    def attribute(self, entity_uuid: str, attribute_name: str,
                  action: str = "get", value: float = None) -> dict:
        """Get or set entity attributes

        Args:
            entity_uuid: Entity UUID
            attribute_name: Attribute name (e.g., "MAX_HEALTH", "MOVEMENT_SPEED", "ATTACK_DAMAGE")
            action: Action ("get" or "set")
            value: Value to set (required for "set" action)

        Returns:
            Dictionary with base_value and value

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If entity not found or invalid attribute

        Example:
            >>> mc.attribute(uuid, "MAX_HEALTH")
            {'base_value': 20.0, 'value': 20.0}
            >>> mc.attribute(uuid, "MAX_HEALTH", action="set", value=40.0)
            {'base_value': 40.0, 'value': 40.0}
        """
        params = {
            "uuid": entity_uuid,
            "attribute": attribute_name,
            "action": action
        }
        if value is not None:
            params["value"] = value
        return self._send_command("attribute", params)

    def enchant(self, username: str, enchantment: str, level: int = 1) -> bool:
        """Enchant the item in a player's main hand

        Args:
            username: Player username
            enchantment: Enchantment name (e.g., "sharpness", "minecraft:unbreaking")
            level: Enchantment level (default: 1)

        Returns:
            True if successful

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If player not found, not holding item, or invalid enchantment

        Example:
            >>> mc.enchant("Steve", "sharpness", 5)
            True
        """
        params = {
            "username": username,
            "enchantment": enchantment,
            "level": level
        }
        return self._send_command("enchant", params)

    def getItem(self, username: str, slot: int) -> dict:
        """Get item information from a player's inventory slot

        Args:
            username: Player username
            slot: Inventory slot number (0-35 for main inventory)

        Returns:
            Dictionary with item, amount, and optionally enchantments

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If player not found or invalid slot

        Example:
            >>> mc.getItem("Steve", 0)
            {'item': 'minecraft:diamond_sword', 'amount': 1, 'enchantments': {'sharpness': 5}}
        """
        params = {"username": username, "slot": slot}
        return self._send_command("getItem", params)

    def setItem(self, username: str, slot: int, item: str,
                amount: int = 1) -> bool:
        """Set an item in a player's inventory slot

        Args:
            username: Player username
            slot: Inventory slot number (0-35 for main inventory)
            item: Item type (e.g., "diamond_sword", "minecraft:golden_apple")
            amount: Item count (default: 1)

        Returns:
            True if successful

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If player not found, invalid slot, or invalid item

        Example:
            >>> mc.setItem("Steve", 0, "diamond_sword")
            True
        """
        params = {
            "username": username,
            "slot": slot,
            "item": item,
            "amount": amount
        }
        return self._send_command("setItem", params)

    def locate(self, structure: str, x: float = None, z: float = None) -> dict:
        """Locate the nearest structure

        Args:
            structure: Structure type (e.g., "village", "minecraft:stronghold")
            x: Search from X coordinate (default: world spawn)
            z: Search from Z coordinate (default: world spawn)

        Returns:
            Dictionary with found (bool) and x, y, z if found

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If invalid structure type

        Example:
            >>> mc.locate("village")
            {'found': True, 'x': 256, 'y': 64, 'z': -128}
        """
        params = {"structure": structure}
        if x is not None and z is not None:
            params["x"] = x
            params["z"] = z
        return self._send_command("locate", params)

    def advancement(self, username: str, action: str,
                    advancement_key: str) -> bool:
        """Grant, revoke, or query player advancements

        Args:
            username: Player username
            action: Action ("grant", "revoke", or "query")
            advancement_key: Advancement key (e.g., "story/mine_stone", "minecraft:story/iron_tools")

        Returns:
            True if successful (for grant/revoke), or bool for query (whether completed)

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If player not found or invalid advancement

        Example:
            >>> mc.advancement("Steve", "grant", "story/mine_stone")
            True
            >>> mc.advancement("Steve", "query", "story/mine_stone")
            True
        """
        params = {
            "username": username,
            "action": action,
            "advancement": advancement_key
        }
        return self._send_command("advancement", params)

    def loot(self, loot_table: str, x: float, y: float, z: float) -> int:
        """Generate loot from a loot table and drop it at a location

        Args:
            loot_table: Loot table name (e.g., "chests/simple_dungeon")
            x: X coordinate
            y: Y coordinate
            z: Z coordinate

        Returns:
            Number of items generated

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If invalid loot table

        Example:
            >>> mc.loot("chests/simple_dungeon", 100, 64, 200)
            5
        """
        params = {
            "loot_table": loot_table,
            "x": x, "y": y, "z": z
        }
        return self._send_command("loot", params)

    def fillbiome(self, x1: int, y1: int, z1: int, x2: int, y2: int, z2: int,
                  biome: str, filter_biome: str = None) -> bool:
        """Fill a region with a specific biome

        Args:
            x1: Start X coordinate
            y1: Start Y coordinate
            z1: Start Z coordinate
            x2: End X coordinate
            y2: End Y coordinate
            z2: End Z coordinate
            biome: Target biome (e.g., "minecraft:plains")
            filter_biome: Only replace this biome (optional)

        Returns:
            True if successful

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If invalid biome or region too large

        Example:
            >>> mc.fillbiome(0, -64, 0, 100, 320, 100, "minecraft:plains")
            True
        """
        params = {
            "x1": x1, "y1": y1, "z1": z1,
            "x2": x2, "y2": y2, "z2": z2,
            "biome": biome
        }
        if filter_biome is not None:
            params["filter_biome"] = filter_biome
        return self._send_command("fillbiome", params)

    def placeFeature(self, feature: str, x: int = None, y: int = None,
                     z: int = None) -> bool:
        """Place a configured feature at a location

        Args:
            feature: Feature name (e.g., "minecraft:oak")
            x: X coordinate (optional)
            y: Y coordinate (optional)
            z: Z coordinate (optional)

        Returns:
            True if successful

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If invalid feature

        Example:
            >>> mc.placeFeature("minecraft:oak", 100, 64, 200)
            True
        """
        params = {"feature": feature}
        if x is not None and y is not None and z is not None:
            params["x"] = x
            params["y"] = y
            params["z"] = z
        return self._send_command("placeFeature", params)

    def placeStructure(self, structure: str, x: int = None, y: int = None,
                       z: int = None) -> bool:
        """Place a structure at a location

        Args:
            structure: Structure name (e.g., "minecraft:village_plains")
            x: X coordinate (optional)
            y: Y coordinate (optional)
            z: Z coordinate (optional)

        Returns:
            True if successful

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If invalid structure

        Example:
            >>> mc.placeStructure("minecraft:village_plains", 100, 64, 200)
            True
        """
        params = {"structure": structure}
        if x is not None and y is not None and z is not None:
            params["x"] = x
            params["y"] = y
            params["z"] = z
        return self._send_command("placeStructure", params)

    def placeJigsaw(self, pool: str, target: str, max_depth: int,
                    x: int = None, y: int = None, z: int = None) -> bool:
        """Place a jigsaw structure pool at a location

        Args:
            pool: Jigsaw pool name
            target: Target name
            max_depth: Maximum depth (1-20)
            x: X coordinate (optional)
            y: Y coordinate (optional)
            z: Z coordinate (optional)

        Returns:
            True if successful

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If invalid pool or parameters

        Example:
            >>> mc.placeJigsaw("minecraft:village/plains/houses", "minecraft:empty", 7, 100, 64, 200)
            True
        """
        params = {
            "pool": pool,
            "target": target,
            "max_depth": max_depth
        }
        if x is not None and y is not None and z is not None:
            params["x"] = x
            params["y"] = y
            params["z"] = z
        return self._send_command("placeJigsaw", params)

    def placeTemplate(self, template: str, x: int = None, y: int = None,
                      z: int = None, rotation: str = "none",
                      mirror: str = "none", integrity: float = 1.0,
                      seed: int = 0) -> bool:
        """Place a structure template at a location

        Args:
            template: Template name
            x: X coordinate (optional)
            y: Y coordinate (optional)
            z: Z coordinate (optional)
            rotation: Rotation (none, clockwise_90, 180, counterclockwise_90)
            mirror: Mirror mode (none, front_back, left_right)
            integrity: Structure integrity 0.0-1.0 (default: 1.0)
            seed: Random seed for integrity (default: 0)

        Returns:
            True if successful

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If invalid template

        Example:
            >>> mc.placeTemplate("my_house", 100, 64, 200, rotation="clockwise_90")
            True
        """
        params = {
            "template": template,
            "rotation": rotation,
            "mirror": mirror,
            "integrity": integrity,
            "seed": seed
        }
        if x is not None and y is not None and z is not None:
            params["x"] = x
            params["y"] = y
            params["z"] = z
        return self._send_command("placeTemplate", params)

    def ride(self, passenger_uuid: str, vehicle_uuid: str = None) -> bool:
        """Make an entity ride another entity, or dismount

        Args:
            passenger_uuid: UUID of the passenger entity
            vehicle_uuid: UUID of the vehicle entity, or None to dismount

        Returns:
            True if successful

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If entity not found

        Example:
            >>> pig = mc.summon("pig", 100, 64, 200)
            >>> zombie = mc.summon("zombie", 100, 64, 200)
            >>> mc.ride(zombie, pig)  # Zombie rides the pig
            True
            >>> mc.ride(zombie)  # Zombie dismounts
            True
        """
        params = {"passenger_uuid": passenger_uuid}
        if vehicle_uuid is not None:
            params["vehicle_uuid"] = vehicle_uuid
        return self._send_command("ride", params)

    def spreadplayers(self, center_x: float, center_z: float,
                      spread_distance: float, max_range: float,
                      usernames: List[str]) -> bool:
        """Spread players randomly around a center point

        Args:
            center_x: Center X coordinate
            center_z: Center Z coordinate
            spread_distance: Minimum distance between players
            max_range: Maximum distance from center
            usernames: List of player usernames to spread

        Returns:
            True if successful

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If players not found or invalid parameters

        Example:
            >>> mc.spreadplayers(0, 0, 10, 100, ["Steve", "Alex"])
            True
        """
        params = {
            "center_x": center_x,
            "center_z": center_z,
            "spread_distance": spread_distance,
            "max_range": max_range,
            "usernames": usernames
        }
        return self._send_command("spreadplayers", params)

    def defaultgamemode(self, mode: str) -> bool:
        """Set the default game mode for new players

        Args:
            mode: Game mode (survival, creative, adventure, spectator)

        Returns:
            True if successful

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails
            CommandError: If invalid game mode

        Example:
            >>> mc.defaultgamemode("creative")
            True
        """
        params = {"mode": mode}
        return self._send_command("defaultgamemode", params)

    def list(self) -> dict:
        """Get list of online players

        Returns:
            Dictionary with online_count, max_players, and players list

        Raises:
            ConnectionError: If connection fails
            AuthenticationError: If authentication fails

        Example:
            >>> mc.list()
            {'online_count': 2, 'max_players': 20, 'players': [{'name': 'Steve', ...}]}
        """
        return self._send_command("list", {})
