"""Live regression test for the 1.2.1 and 1.2.2 fixes: two-block blocks in edit(),
setEntityRotation error reporting, experience points after "set", alias
properties on blocks without them, and the item hint in edit() errors.

Usage: uv run python ServerPlugin/scripts/live_fix_test.py <token> <server.log> [player]
A player must be online for the experience and player-rotation checks
(an offline-mode test server and any client named <player>, default TestBot).
WARNING: modifies the world around (200, 100, 200) - use a throwaway test server.
"""
import re
import sys
import time
import uuid

from mcpylib import MCPyLib
from mcpylib import CommandError

token, log_path = sys.argv[1], sys.argv[2]
player = sys.argv[3] if len(sys.argv) > 3 else "TestBot"
mc = MCPyLib(ip="127.0.0.1", port=65535, token=token)
results = []


def check(name, ok, detail=""):
    results.append((name, bool(ok), str(detail)[:120]))


def state_is(x, y, z, predicate):
    mc.setblock(200, 150, 200, "minecraft:air")
    mc.exec(f"execute if block {x} {y} {z} {predicate} run setblock 200 150 200 minecraft:gold_block")
    return "gold_block" in str(mc.getblock(200, 150, 200))


def console_reply(command, pattern):
    """Run a console command and return the first log line after it matching pattern."""
    with open(log_path, encoding="utf-8", errors="replace") as f:
        f.seek(0, 2)
        start = f.tell()
    mc.exec(command)
    time.sleep(0.3)
    with open(log_path, encoding="utf-8", errors="replace") as f:
        f.seek(start)
        for line in f:
            line = re.sub(r"\[[0-9;]*m", "", line)
            if re.search(pattern, line):
                return line.strip()
    return ""


# ---------------------------------------------------------------- edit()
X, Y, Z = 200, 100, 200
mc.forceload("add", X, Z)
mc.fill(X - 2, Y - 1, Z - 2, X + 12, Y - 1, Z + 12, "minecraft:stone")
mc.fill(X - 2, Y, Z - 2, X + 12, Y + 3, Z + 12, "minecraft:air")

# Compressed path (the client's default): only the lower half / bed foot given.
mc.edit(X, Y, Z, [[[{"block": "oak_door", "block_state": {"facing": "south"}}], [None]]])
check("edit: lone door lower -> upper added",
      state_is(X, Y, Z, "minecraft:oak_door[half=lower,facing=south]")
      and state_is(X, Y + 1, Z, "minecraft:oak_door[half=upper,facing=south]"))

mc.edit(X + 2, Y, Z + 2, [[["red_bed"]]])  # default facing=north, part=foot
check("edit: plain bed -> head added",
      state_is(X + 2, Y, Z + 2, "minecraft:red_bed[part=foot,facing=north]")
      and state_is(X + 2, Y, Z + 1, "minecraft:red_bed[part=head,facing=north]"))
# Both halves given, lower first in x/y/z order (this used to break the door).
mc.edit(X + 4, Y, Z, [[[{"block": "spruce_door", "block_state": {"facing": "east", "half": "lower", "hinge": "right"}}],
                       [{"block": "spruce_door", "block_state": {"facing": "east", "half": "upper", "hinge": "right"}}]]])
check("edit: both door halves kept",
      state_is(X + 4, Y, Z, "minecraft:spruce_door[half=lower,facing=east,hinge=right]")
      and state_is(X + 4, Y + 1, Z, "minecraft:spruce_door[half=upper,facing=east,hinge=right]"))

mc.edit(X + 6, Y, Z, [[["sunflower"]]])
check("edit: tall flower both halves",
      state_is(X + 6, Y, Z, "minecraft:sunflower[half=lower]") and state_is(X + 6, Y + 1, Z, "minecraft:sunflower[half=upper]"))

# Bed with both parts given explicitly, head first along -z.
mc.edit(X + 8, Y, Z, [[[{"block": "blue_bed", "block_state": {"facing": "north", "part": "head"}},
                        {"block": "blue_bed", "block_state": {"facing": "north", "part": "foot"}}]]])
check("edit: both bed parts kept",
      state_is(X + 8, Y, Z, "minecraft:blue_bed[part=head]") and state_is(X + 8, Y, Z + 1, "minecraft:blue_bed[part=foot]"))

# Plain chest in a large edit (NMS path) still gets its block entity.
big = [[["minecraft:chest" if (x, y, z) == (0, 0, 0) else "minecraft:glass" for z in range(4)] for y in range(2)] for x in range(4)]
mc.edit(X, Y, Z + 6, big)
check("edit: chest has block entity (NMS path)",
      "has the following block data" in console_reply(f"data get block {X} {Y} {Z + 6}", r"block data|not a block entity"))
check("edit: plain glass still placed", "glass" in str(mc.getblock(X + 3, Y + 1, Z + 9)))

# Legacy (uncompressed) protocol path.
mc.fill(X, Y, Z + 11, X + 3, Y + 2, Z + 11, "minecraft:air")
mc._send_command("bulkEdit", {"x": X, "y": Y, "z": Z + 11,
                          "blocks": [[[{"block": "iron_door", "block_state": {"facing": "west"}}]], [["white_bed"]]]})
check("edit legacy: door + bed completed",
      state_is(X, Y + 1, Z + 11, "minecraft:iron_door[half=upper,facing=west]")
      and state_is(X + 1, Y, Z + 10, "minecraft:white_bed[part=head]"))

# 1.2.2: legacy alias keys on a block without that property are rejected.
def error_of(fn):
    try:
        fn()
    except CommandError as e:
        return str(e)
    return ""

for key, value in (("facing", "north"), ("waterlogged", "true"), ("axis", "y")):
    err = error_of(lambda: mc.setblock(X, Y + 3, Z, "minecraft:stone", block_state={key: value}))
    check(f"setblock: stone + {key} rejected", "Unsupported property" in err, err)
err = error_of(lambda: mc.setblock(X, Y + 3, Z, "minecraft:oak_stairs", block_state={"facing": "sideways"}))
check("setblock: bad alias value rejected", "Unsupported property" in err, err)
mc.setblock(X, Y + 3, Z, "minecraft:oak_log", block_state={"axis": "x"})
check("setblock: valid alias still works", state_is(X, Y + 3, Z, "minecraft:oak_log[axis=x]"))
mc.setblock(X + 1, Y + 3, Z, "minecraft:air")
err = error_of(lambda: mc.edit(X + 1, Y + 3, Z, [[[{"block": "stone", "block_state": {"facing": "east"}}]]]))
check("edit: stone + facing rejected", "Unsupported property" in err, err)
check("edit: nothing placed on bad state", "air" in str(mc.getblock(X + 1, Y + 3, Z)))

# 1.2.2: edit() names item ids that aren't blocks.
err = error_of(lambda: mc.edit(X, Y + 3, Z, [[["minecraft:brick"]]]))
check("edit: item hint (palette string)", "item, not a placeable block" in err, err)
err = error_of(lambda: mc.edit(X, Y + 3, Z, [[[{"block": "diamond"}]]]))
check("edit: item hint (palette object)", "item, not a placeable block" in err, err)
err = error_of(lambda: mc._send_command("bulkEdit", {"x": X, "y": Y + 3, "z": Z, "blocks": [[["stick"]]]}))
check("edit legacy: item hint", "item, not a placeable block" in err, err)

# ---------------------------------------------------------------- setEntityRotation
zombie = mc.summon("zombie", X + 10, Y, Z + 10)
zuuid = zombie["uuid"] if isinstance(zombie, dict) else zombie
mc.setEntityAI(zuuid, False)
mc.exec(f"execute positioned {X + 10} {Y} {Z + 10} run summon chicken ~ ~ ~ {{Tags:[\"fixtest\"],NoAI:1b}}")
mc.exec(f"ride @e[tag=fixtest,limit=1] mount {zuuid}")
check("rotation: entity with passenger", mc.setEntityRotation(zuuid, 90.0, 30.0) is True)
line = console_reply(f"data get entity {zuuid} Rotation", r"entity data")
check("rotation: applied in-game", "90.0f, 30.0f" in line, line)

puuid = None
reply = console_reply(f"data get entity {player} UUID", r"entity data")
m = re.search(r"\[I; (-?\d+), (-?\d+), (-?\d+), (-?\d+)\]", reply)
if m:
    n = 0
    for part in m.groups():
        n = (n << 32) | (int(part) & 0xFFFFFFFF)
    puuid = str(uuid.UUID(int=n))
check("player online", puuid is not None, puuid)
if puuid:
    check("rotation: player", mc.setEntityRotation(puuid, 45.0, 10.0) is True)
    line = console_reply(f"data get entity {player} Rotation", r"entity data")
    check("rotation: player applied in-game", "45.0f, 10.0f" in line, line)
    # A riding player is rotated in place, not teleported off the vehicle.
    mc.exec(f"ride @e[tag=fixtest,limit=1] dismount")
    mc.exec(f"ride {player} mount {zuuid}")
    check("rotation: riding player", mc.setEntityRotation(puuid, 120.0, -20.0) is True)
    line = console_reply(f"data get entity {player} Rotation", r"entity data")
    check("rotation: riding player applied", "120.0f, -20.0f" in line, line)
    line = console_reply(f"ride {player} dismount", r"riding|dismount|stopped")
    check("rotation: riding player still mounted", "not riding" not in line, line)

    # ------------------------------------------------------------ experience
    mc.experience(player, "set", 0, "levels")
    mc.experience(player, "set", 0, "points")
    r = mc.experience(player, "set", 10, "levels")
    check("xp: set 10 levels -> 160 points", r["level"] == 10 and r["points"] == 160, r)
    r = mc.experience(player, "query")
    check("xp: query after set agrees", r["level"] == 10 and r["points"] == 160, r)
    r = mc.experience(player, "set", 9, "points")  # 9 of 27 for level 10
    check("xp: set points in level", r["points"] == 169 and abs(r["progress"] - 9 / 27) < 1e-3, r)
    r = mc.experience(player, "add", 5, "levels")
    check("xp: add levels counts", r["level"] == 15 and r["points"] == 315 + round(9 / 27 * 37), r)
    mc.experience(player, "set", 0, "levels")
    mc.experience(player, "set", 0, "points")
    r = mc.experience(player, "add", 100, "points")
    check("xp: add 100 points from 0", r["points"] == 100 and r["level"] == 7, r)
    r = mc.experience(player, "set", 31, "levels")
    check("xp: level 31 total 1507", r["points"] == 1507 + round(r["progress"] * 121), r)

mc.forceload("remove", X, Z)
width = max(len(n) for n, _, _ in results)
for name, ok, detail in results:
    print(f"{'PASS' if ok else 'FAIL'}  {name:<{width}}  {detail}")
failed = sum(not ok for _, ok, _ in results)
print(f"\n{len(results) - failed}/{len(results)} passed")
sys.exit(1 if failed else 0)
