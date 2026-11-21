# Changelog

## 1.2.0 (2026-11-22)

Targets **Minecraft 26.3** (Paper / Spigot 26.1+). Requires **Java 25**.

### Breaking
- The server plugin now requires Paper or Spigot **26.1 or newer** running on **Java 25**
  (`api-version: 26.1`). For 1.21.x servers, keep using plugin 1.1.x.

### Added
- `getblocks()`: read a 3D region of blocks in one call.
- Persistent connection support in the Python client.
- NMS fast path for bulk `edit()`: writes straight into chunk sections. It is
  reflection-based, so no server jar is needed to build, and it falls back to the
  Bukkit API automatically. On Paper 26.3 a 16,384-block edit takes about 60 ms.
- Game rules accept the new 1.21.11+ registry names (`keep_inventory`,
  `minecraft:mob_griefing`) as well as legacy camelCase names. That includes the
  renamed rules (`doDaylightCycle` → `advance_time`) and the inverted `disableX`
  rules (`disableRaids` → `raids`, with the value flipped).
- Registry lookups for attributes, effects, enchantments and villager professions
  accept namespaced keys (`minecraft:speed`) and legacy Bukkit names
  (`GENERIC_MAX_HEALTH`, `INCREASE_DAMAGE`, `DAMAGE_ALL`).
- Maven Wrapper (`./mvnw`), so building no longer needs a separate Maven install.
- CI: builds against both the Spigot and Paper APIs and runs a Paper
  binary-compatibility check (`ServerPlugin/scripts/check_paper_abi.py`).
- `ServerPlugin/scripts/live_smoke_test.py`: an end-to-end test of about 60 commands against a live server.

### Fixed
- `server.host` was ignored: the socket always listened on every interface. It now
  binds to the configured address, so `127.0.0.1` restricts it to local clients.
- Connections beyond `max-connections` used to queue forever, and idle persistent
  connections held their slot indefinitely. Extra connections now get a
  `Server busy` error, and the new `server.idle-timeout-seconds` (default 300)
  frees abandoned ones.
- Python client: reconnecting after the server closed the connection raised
  `AttributeError`, for example after a server restart.
- Python client: a timed-out command was re-sent automatically and could run
  twice (e.g. `give`). Timeouts now raise `ConnectionError`. Only a stale reused
  connection is retried, and only before any reply byte arrives.
- **Server freeze / watchdog crash** in `exec`, `tellraw`, `fillbiome`,
  `placeFeature`, `placeStructure`, `placeJigsaw`, `placeTemplate` and
  `spreadplayers`. These handlers waited on the main thread from the main thread.
- `gamerule` threw `IncompatibleClassChangeError` on Paper 26.x, because
  `GameRule` is a class in Paper but an interface in Spigot.
- Entity lookup by UUID uses `Bukkit.getEntity`, a direct lookup instead of scanning
  every entity in every world. Entities in unloaded chunks still can't be found:
  use `forceload()` when no player is nearby.
- `setVillagerProfession` no longer gets reset to `NONE` by vanilla AI on
  0-XP villagers.
- Replaced deprecated or removed API calls: `Attribute.valueOf`,
  `Villager.Profession.valueOf`, `PotionEffectType.getByName`,
  `Enchantment.getByName`, `setBedSpawnLocation`, and `WorldBorder#setSize(double, long)`.
- `plugin.yml` version now comes from the Maven project version.

### Dependencies
- Spigot API 26.3-R0.1-SNAPSHOT (Paper API 26.3 for compatibility checks)
- Gson 2.15.0, maven-compiler-plugin 3.16.0, maven-shade-plugin 3.6.2
- python-dotenv ≥ 1.2.4, uv_build 0.11.x
