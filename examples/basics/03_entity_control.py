"""
Entity Control Example

This script demonstrates how to use MCPyLib's entity control features
to spawn, manipulate, and control Minecraft entities programmatically.
Similar to the Citizens plugin, you can control entity movement, AI, and behavior.

This example includes:
- Basic entity spawning and positioning
- Entity status and AI control
- Equipment control for living entities
- Villager profession and custom trading
"""

from mcpylib import MCPyLib
from dotenv import load_dotenv
import os
import time

# Load environment variables from .env file
load_dotenv()

# Initialize the client
mc = MCPyLib(
    ip=os.getenv("SERVER_IP"),
    port=65535,
    token=os.getenv("SERVER_TOKEN")
)

try:
    # Get player position as reference point
    username = os.getenv("PLAYER_NAME", "Player")
    position = mc.getPos(username)
    px, py, pz = position[0], position[1], position[2]
    print(f"Player {username} position: ({px}, {py}, {pz})")

    # =========================================
    # Example 1: Spawn an entity and get its position
    # =========================================
    print("\n--- Example 1: Spawn Entity and Get Position ---")

    # Spawn a pig near the player
    pig_uuid = mc.summon("pig", px + 3, py, pz)
    print(f"Spawned pig with UUID: {pig_uuid}")

    # Get the pig's position
    pig_pos = mc.getEntityPos(pig_uuid)
    print(f"Pig position: ({pig_pos['x']:.2f}, {pig_pos['y']:.2f}, {pig_pos['z']:.2f})")
    print(f"Pig rotation: yaw={pig_pos['yaw']:.2f}, pitch={pig_pos['pitch']:.2f}")

    # =========================================
    # Example 2: Get detailed entity status
    # =========================================
    print("\n--- Example 2: Get Entity Status ---")

    # Spawn a zombie
    zombie_uuid = mc.summon("zombie", px + 5, py, pz)
    print(f"Spawned zombie with UUID: {zombie_uuid}")

    # Get full status
    status = mc.getEntityStatus(zombie_uuid)
    print(f"Entity type: {status['type']}")
    print(f"World: {status['world']}")
    print(f"Health: {status['health']}/{status['max_health']}")
    print(f"AI enabled: {status['has_ai']}")
    print(f"Is valid: {status['is_valid']}")
    print(f"Position: ({status['position']['x']:.2f}, {status['position']['y']:.2f}, {status['position']['z']:.2f})")
    print(f"Velocity: ({status['velocity']['x']:.4f}, {status['velocity']['y']:.4f}, {status['velocity']['z']:.4f})")

    # =========================================
    # Example 3: Disable AI (create a statue)
    # =========================================
    print("\n--- Example 3: Disable AI (Statue Mode) ---")

    # Disable the zombie's AI - it will freeze like a statue
    mc.setEntityAI(zombie_uuid, False)
    print("Disabled zombie AI - zombie is now frozen like a statue")

    # Verify AI is disabled
    status = mc.getEntityStatus(zombie_uuid)
    print(f"AI enabled after disable: {status['has_ai']}")

    time.sleep(2)

    # Re-enable AI
    mc.setEntityAI(zombie_uuid, True)
    print("Re-enabled zombie AI - zombie can move again")

    # =========================================
    # Example 4: Teleport entity
    # =========================================
    print("\n--- Example 4: Teleport Entity ---")

    # Teleport the pig to a new location
    new_x, new_y, new_z = px + 8, py + 2, pz + 5
    mc.teleportEntity(pig_uuid, new_x, new_y, new_z)
    print(f"Teleported pig to ({new_x}, {new_y}, {new_z})")

    # Verify new position
    pig_pos = mc.getEntityPos(pig_uuid)
    print(f"Pig new position: ({pig_pos['x']:.2f}, {pig_pos['y']:.2f}, {pig_pos['z']:.2f})")

    # Teleport with rotation (face north)
    mc.teleportEntity(pig_uuid, new_x, new_y, new_z, yaw=180.0, pitch=0.0)
    print("Teleported pig and set rotation to face north")

    # =========================================
    # Example 5: Set entity rotation
    # =========================================
    print("\n--- Example 5: Set Entity Rotation ---")

    # Make the zombie look in different directions
    directions = [
        (0.0, "South"),
        (90.0, "West"),
        (180.0, "North"),
        (270.0, "East"),
    ]

    for yaw, direction in directions:
        mc.setEntityRotation(zombie_uuid, yaw, 0.0)
        print(f"Zombie now facing: {direction} (yaw={yaw})")
        time.sleep(0.5)

    # Make it look up
    mc.setEntityRotation(zombie_uuid, 180.0, -45.0)
    print("Zombie now looking up at the sky")

    # =========================================
    # Example 6: Set entity velocity (movement)
    # =========================================
    print("\n--- Example 6: Set Entity Velocity ---")

    # Make the pig jump
    mc.setEntityVelocity(pig_uuid, 0, 0.8, 0)
    print("Made the pig jump! (velocity Y = 0.8)")

    time.sleep(1)

    # Push the pig forward (positive X direction)
    mc.setEntityVelocity(pig_uuid, 0.5, 0.3, 0)
    print("Pushed the pig forward with a small hop")

    # =========================================
    # Example 7: Set attack target
    # =========================================
    print("\n--- Example 7: Set Attack Target ---")

    # Spawn a villager for the zombie to target
    villager_uuid = mc.summon("villager", px + 10, py, pz)
    print(f"Spawned villager with UUID: {villager_uuid}")

    # Make the zombie target the villager
    mc.setEntityTarget(zombie_uuid, villager_uuid)
    print("Set zombie's target to the villager - zombie will chase it!")

    time.sleep(3)

    # Clear the target
    mc.setEntityTarget(zombie_uuid, None)
    print("Cleared zombie's target")

    # =========================================
    # Example 8: Create a patrol path (advanced)
    # =========================================
    print("\n--- Example 8: Entity Patrol Path ---")

    # Spawn a new zombie for patrol demo
    patrol_zombie = mc.summon("zombie", px, py, pz + 10)
    mc.setEntityAI(patrol_zombie, False)  # Disable AI for manual control
    print(f"Spawned patrol zombie: {patrol_zombie}")

    # Define patrol waypoints
    waypoints = [
        (px, py, pz + 10),
        (px + 5, py, pz + 10),
        (px + 5, py, pz + 15),
        (px, py, pz + 15),
    ]

    print("Starting patrol (2 cycles)...")
    for cycle in range(2):
        for i, (wx, wy, wz) in enumerate(waypoints):
            mc.teleportEntity(patrol_zombie, wx, wy, wz)
            # Calculate yaw to face next waypoint
            next_wp = waypoints[(i + 1) % len(waypoints)]
            import math
            dx = next_wp[0] - wx
            dz = next_wp[2] - wz
            yaw = math.degrees(math.atan2(-dx, dz))
            mc.setEntityRotation(patrol_zombie, yaw, 0.0)
            print(f"  Waypoint {i + 1}: ({wx}, {wy}, {wz})")
            time.sleep(0.8)

    print("Patrol complete!")

    # =========================================
    # Example 9: Entity Equipment Control
    # =========================================
    print("\n--- Example 9: Entity Equipment Control ---")

    # Spawn a skeleton and give it full diamond armor
    skeleton_uuid = mc.summon("skeleton", px + 12, py, pz)
    print(f"Spawned skeleton with UUID: {skeleton_uuid}")

    # Set full diamond armor and weapons
    mc.setEntityEquipment(skeleton_uuid, {
        "helmet": "diamond_helmet",
        "chestplate": "diamond_chestplate",
        "leggings": "diamond_leggings",
        "boots": "diamond_boots",
        "main_hand": "bow",
        "off_hand": "shield"
    }, drop_chances={
        "helmet": 0.1,
        "chestplate": 0.1,
        "main_hand": 0.5
    })
    print("Equipped skeleton with full diamond armor, bow, and shield")

    # Get and display the equipment
    equipment = mc.getEntityEquipment(skeleton_uuid)
    print("Current equipment:")
    for slot, item in equipment.items():
        if item:
            print(f"  {slot}: {item}")

    # =========================================
    # Example 10: Villager Profession and Trading
    # =========================================
    print("\n--- Example 10: Villager Profession and Trading ---")

    # Spawn a villager and set profession
    trader_uuid = mc.summon("villager", px + 15, py, pz)
    print(f"Spawned villager with UUID: {trader_uuid}")

    # Set villager to be a librarian
    mc.setVillagerProfession(trader_uuid, "LIBRARIAN")
    print("Set villager profession to LIBRARIAN")

    # Get villager data before custom trades
    villager_data = mc.getVillagerData(trader_uuid)
    print(f"Profession: {villager_data['profession']}")
    print(f"Level: {villager_data['level']}")
    print(f"Current trades: {len(villager_data['trades'])}")

    # Set custom trades for the villager
    custom_trades = [
        {
            "buy1": {"item": "emerald", "amount": 10},
            "sell": {"item": "diamond_sword", "amount": 1},
            "max_uses": 5
        },
        {
            "buy1": {"item": "emerald", "amount": 20},
            "sell": {"item": "diamond_pickaxe", "amount": 1},
            "max_uses": 5
        },
        {
            "buy1": {"item": "emerald", "amount": 5},
            "buy2": {"item": "book", "amount": 1},
            "sell": {"item": "enchanted_book", "amount": 1},
            "max_uses": 10
        },
        {
            "buy1": {"item": "wheat", "amount": 20},
            "sell": {"item": "emerald", "amount": 1},
            "max_uses": 16
        }
    ]
    mc.setVillagerTrades(trader_uuid, custom_trades)
    print(f"Set {len(custom_trades)} custom trades for the villager")

    # Verify the trades were set
    villager_data = mc.getVillagerData(trader_uuid)
    print(f"Updated trades count: {len(villager_data['trades'])}")
    for i, trade in enumerate(villager_data['trades']):
        buy1 = trade['buy1']
        sell = trade['sell']
        buy2_str = ""
        if trade.get('buy2'):
            buy2 = trade['buy2']
            buy2_str = f" + {buy2['amount']}x {buy2['item']}"
        print(f"  Trade {i + 1}: {buy1['amount']}x {buy1['item']}{buy2_str} -> {sell['amount']}x {sell['item']}")

    # =========================================
    # Example 11: Cleanup - Remove entities
    # =========================================
    print("\n--- Example 11: Remove Entities ---")

    entities_to_remove = [pig_uuid, zombie_uuid, villager_uuid, patrol_zombie, skeleton_uuid, trader_uuid]
    for uuid in entities_to_remove:
        try:
            mc.removeEntity(uuid)
            print(f"Removed entity: {uuid[:8]}...")
        except Exception as e:
            print(f"Could not remove {uuid[:8]}...: {e}")

    print("\nAll demo entities cleaned up!")

except Exception as e:
    print(f"Error: {e}")
    import traceback
    traceback.print_exc()
