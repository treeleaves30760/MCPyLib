"""End-to-end smoke test against a live Paper/Spigot server running the plugin.

Exercises ~60 commands (blocks, bulk NMS edit, game rules incl. legacy names,
entities, villagers, attributes, scoreboards, bossbars, biomes, world border).
Works with no players online: it force-loads chunk (0, 0) first.

Usage: uv run python ServerPlugin/scripts/live_smoke_test.py <token>
WARNING: modifies the world around (0, 100, 0) - use a throwaway test server.
"""
import sys
import time
import traceback

from mcpylib import MCPyLib

token = sys.argv[1]
mc = MCPyLib(ip="127.0.0.1", port=65535, token=token)
results = []


def check(name, fn, expect=None):
    try:
        t = time.perf_counter()
        v = fn()
        dt = (time.perf_counter() - t) * 1000
        ok = True if expect is None else expect(v)
        results.append((name, ok, f"{dt:.0f}ms", repr(v)[:110]))
    except Exception as e:  # noqa: BLE001
        results.append((name, False, "-", f"{type(e).__name__}: {e}"[:160]))


Y = 100
check("forceload", lambda: mc.forceload("add", 0, 0))
check("setblock", lambda: mc.setblock(0, Y, 0, "minecraft:diamond_block"))
check("getblock", lambda: mc.getblock(0, Y, 0), lambda v: "diamond_block" in str(v))
check("setblock+state", lambda: mc.setblock(1, Y, 0, "minecraft:oak_stairs", block_state={"facing": "east", "half": "top"}))
check("getblock stairs", lambda: mc.getblock(1, Y, 0))
check("fill", lambda: mc.fill(0, Y + 1, 0, 9, Y + 1, 9, "minecraft:stone"), lambda v: v == 100)

# Bulk edit through NMS fast path: 32x16x32 = 16384 blocks spanning 4+ chunks
SX, SY, SZ = 32, 16, 32
blocks = [[["minecraft:glass" if (x + y + z) % 2 else "minecraft:gold_block" for z in range(SZ)] for y in range(SY)] for x in range(SX)]
check("edit 16k (NMS)", lambda: mc.edit(-8, Y + 5, -8, blocks), lambda v: v == SX * SY * SZ)
check("verify edit A", lambda: mc.getblock(-8, Y + 5, -8), lambda v: "gold_block" in str(v))
check("verify edit B", lambda: mc.getblock(-7, Y + 5, -8), lambda v: "glass" in str(v))
# mixed: plain + block_state entries + None skips
mixed = [[[None, "minecraft:stone", {"block": "minecraft:oak_log", "block_state": {"axis": "x"}}]]]
check("edit mixed", lambda: mc.edit(20, Y, 20, mixed))
check("verify log axis", lambda: mc.getblock(20, Y, 22), lambda v: "oak_log" in str(v))
check("getblocks", lambda: mc.getblocks(-8, Y + 5, -8, -6, Y + 6, -6), lambda v: v is not None)
check("clone", lambda: mc.clone(0, Y, 0, 2, Y + 1, 2, 50, Y, 50))

check("time set", lambda: mc.time("set", 6000))
check("weather", lambda: mc.weather("clear"))
check("difficulty", lambda: mc.difficulty("normal"))
check("gamerule new name", lambda: mc.gamerule("keep_inventory", "true"))
check("gamerule get new", lambda: mc.gamerule("keep_inventory"), lambda v: str(v).lower() == "true")
check("gamerule legacy name", lambda: mc.gamerule("mobGriefing"))
check("gamerule renamed legacy", lambda: mc.gamerule("doDaylightCycle", "false"), lambda v: v == "false")
check("gamerule new reads renamed", lambda: mc.gamerule("advance_time"), lambda v: v == "false")
check("gamerule inverted set", lambda: mc.gamerule("disableRaids", "true"), lambda v: v == "true")
check("gamerule inverted underlying", lambda: mc.gamerule("raids"), lambda v: v == "false")
check("gamerule int legacy", lambda: mc.gamerule("randomTickSpeed", "5"), lambda v: v == "5")
check("gamerule namespaced", lambda: mc.gamerule("minecraft:mob_griefing"))

vill = None
try:
    vill = mc.summon("villager", 5, -60, 5)
except Exception as e:  # noqa: BLE001
    results.append(("summon villager", False, "-", str(e)))
uuid = vill if isinstance(vill, str) else (vill or {}).get("uuid") if isinstance(vill, dict) else None
results.append(("summon villager", uuid is not None, "-", repr(vill)[:100]))
if uuid:
    check("entity status", lambda: mc.getEntityStatus(uuid))
    check("profession LIBRARIAN", lambda: mc.setVillagerProfession(uuid, "LIBRARIAN"))
    check("profession namespaced", lambda: mc.setVillagerProfession(uuid, "minecraft:cleric"))
    check("villager data", lambda: mc.getVillagerData(uuid), lambda v: "CLERIC" in str(v))
    check("equipment", lambda: mc.getEntityEquipment(uuid))
    check("attr MAX_HEALTH", lambda: mc.attribute(uuid, "MAX_HEALTH"))
    check("attr legacy GENERIC_MAX_HEALTH", lambda: mc.attribute(uuid, "GENERIC_MAX_HEALTH"))
    check("attr set speed", lambda: mc.attribute(uuid, "movement_speed", "set", 0.3))
    check("tags", lambda: mc.addTag(uuid, "mcpy"))
    check("getTags", lambda: mc.getTags(uuid), lambda v: "mcpy" in str(v))
    check("ai off", lambda: mc.setEntityAI(uuid, False))
    check("tp entity", lambda: mc.teleportEntity(uuid, 6, Y + 2, 6))
    check("damage", lambda: mc.damage(uuid, 1.0))
    check("remove", lambda: mc.removeEntity(uuid))

check("particle", lambda: mc.particle("flame", 0, Y + 3, 0))
check("addObjective", lambda: mc.addObjective("mcpytest", "dummy", "Test"))
check("setScore", lambda: mc.setScore("mcpytest", "Bob", 7))
check("getScore", lambda: mc.getScore("mcpytest", "Bob"), lambda v: str(v) == "7" or v == 7 or "7" in str(v))
check("removeObjective", lambda: mc.removeObjective("mcpytest"))
check("bossbar add", lambda: mc.bossbar("add", "mcpy:test", title="Hi"))
check("bossbar remove", lambda: mc.bossbar("remove", "mcpy:test"))
check("team add", lambda: mc.team("add", f"t{int(time.time())}"))
check("locate village", lambda: mc.locate("village_plains"))
check("fillbiome", lambda: mc.fillbiome(0, Y, 0, 15, Y + 15, 15, "minecraft:desert"))
check("worldborder get", lambda: mc.worldborder("get"))
check("say", lambda: mc.say("MCPyLib smoke test"))
check("exec", lambda: mc.exec("say exec ok"))
check("list", lambda: mc.list())
check("summon zombie", lambda: mc.summon("zombie", 3, -60, 3))
check("kill zombie", lambda: mc.kill("zombie"), lambda v: v is not None)
check("worldborder set timed", lambda: mc.worldborder("set", 1000, 1))
check("time query", lambda: mc.time("query"))

fails = 0
for name, ok, dt, info in results:
    fails += not ok
    print(f"{'PASS' if ok else 'FAIL'} {name:32s} {dt:>7s}  {info}")
print(f"\n{len(results) - fails}/{len(results)} passed")
mc.close()
sys.exit(1 if fails else 0)
