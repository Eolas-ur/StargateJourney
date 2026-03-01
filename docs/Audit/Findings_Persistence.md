# Persistence and NBT Audit Findings

Global audit of all SavedData classes, NBT stores, and serialization paths.

---

## P001: StargateNetwork Version Migration Is Destructive — IMPROVED

- **Severity:** Medium
- **Status:** Improved (warning log added; migration behavior unchanged)
- **Affected:** `StargateNetwork` — `sgjourney-stargate_network`
- **Impact:** When `version` in the saved data differs from `updateVersion` (currently 17), `updateNetwork()` calls `stellarUpdate()` which terminates all connections, erases universe data, and re-registers stargates. Previously logged at DEBUG level with no description of consequences.
- **Fix Applied:** Changed `StargateNetwork.updateNetwork()` log from `LOGGER.debug` to `LOGGER.warn` with explicit message: `"Stargate Network version migration: {} -> {}. All active connections will be terminated, universe data regenerated, and stargates re-registered."` Includes old and new version numbers.
- **Verify:** Set `version` to a lower value in `sgjourney-stargate_network.dat`. On reload, server log shows WARN-level message with version numbers and description of what will be reset.

---

## P002: TransporterNetwork Version Migration Same Pattern — IMPROVED

- **Severity:** Medium
- **Status:** Improved (warning log added; migration behavior unchanged)
- **Affected:** `TransporterNetwork` — `sgjourney-transporter_network`
- **Impact:** Same destructive migration pattern as P001. When version differs from `UPDATE_VERSION` (currently 2), all transporter connections and dimension data are cleared.
- **Fix Applied:** Changed `TransporterNetwork.updateNetwork()` log from `LOGGER.info` to `LOGGER.warn` with explicit message: `"Transporter Network version migration: {} -> {}. All transporter connections and dimension mappings will be cleared and re-registered."` Includes old and new version numbers.
- **Verify:** Same as P001 but for transporter network data.

---

## P003: BlockEntityList UUID Parse Failure Creates Orphaned Entries — FIXED

- **Severity:** Medium
- **Status:** Fixed
- **Affected:** `BlockEntityList` — `sgjourney-block_entities`, `StargateNetwork`, `TransporterNetwork`
- **Impact:** Malformed UUID keys in saved data could silently accumulate. Additionally, `TransporterNetwork.Dimension.deserialize()` had an unguarded `UUID.fromString()` that could crash the entire TransporterNetwork load.
- **Fix Applied:**
  - **`BlockEntityList.deserializeTransporters()`**: Added aggregate counting of recovered (malformed UUID, new UUID assigned) and skipped (unrecoverable) entries. Logs WARN with counts after deserialization. Orphaned entries do not persist because `serialize()` writes from the in-memory `transporterMap` (not the source CompoundTag).
  - **`StargateNetwork.deserializeConnections()`**: Added aggregate malformed UUID counter and WARN log (previously silently swallowed `IllegalArgumentException`).
  - **`TransporterNetwork.deserializeConnections()`**: Changed per-entry `LOGGER.error` to aggregate counter with single WARN line, reducing log spam.
  - **`TransporterNetwork.Dimension.deserialize()`**: Added `try-catch` around `UUID.fromString(transporterID)` — previously unguarded, could crash entire TransporterNetwork load. Logs aggregate WARN with dimension name.
- **Verify:** Insert malformed UUID key in `sgjourney-block_entities.dat`. Reload — no crash, WARN log shows count. Save and reload — malformed key does not persist.

---

## P004: ConduitNetworks Uses Integer IDs Without Overflow Protection

- **Severity:** Low
- **Affected:** `ConduitNetworks` — `sgjourney-conduits`
- **Impact:** Network IDs are assigned using an incrementing integer. On every cable update, old networks are removed and new ones created with fresh IDs. Over very long server lifetimes, the ID counter could theoretically overflow. In practice, `Integer.MAX_VALUE` (~2.1 billion) updates would need to occur, which is unrealistic.
- **Evidence:** `ConduitNetworks.update()` — generates new network IDs.
- **Repro:** N/A — theoretical.
- **Suggested Fix:** None needed. If concerned, reset IDs periodically during `serialize()` by re-numbering from 0.
- **Verify:** N/A.

---

## P005: Universe Galaxy/SolarSystem Deserialization Lacks Defensive Null Checks

- **Severity:** Low
- **Affected:** `Universe` — `sgjourney-universe`
- **Impact:** `Universe.deserialize()` iterates `solar_systems` and `galaxies` CompoundTags and creates `SolarSystem.Serializable` and `Galaxy.Serializable` objects. If a solar system's `CompoundTag` is missing expected keys (e.g., `dimensions` or `address`), the constructor may produce an object with null/empty fields rather than failing cleanly. Downstream code that accesses these fields (e.g., `Dialing.getPreferredStargate()`) null-checks stargates but may not null-check the solar system itself.
- **Evidence:** `Universe.deserialize()` — iterates tags without key-presence validation.
- **Repro:**
  1. Edit `sgjourney-universe.dat` to remove the `dimensions` key from a solar system entry.
  2. Reload the server.
  3. Dial a stargate targeting that solar system.
  4. Observe: potential NPE or empty dimension list (graceful failure depends on downstream checks).
- **Suggested Fix:** Add key-presence checks in `SolarSystem.Serializable` constructor. Log and skip entries with missing required keys.
- **Verify:** Reload with malformed data; no crash, warning logged.

---

## P006: StargateNetworkSettings Stores Config in Raw CompoundTag

- **Severity:** Low
- **Affected:** `StargateNetworkSettings` — `sgjourney-stargate_network_settings`
- **Impact:** Settings are stored directly in a `CompoundTag` field and accessed via `getBoolean()` with string keys. Missing keys return `false` by default (NBT `getBoolean` behaviour). This means adding a new setting that should default to `true` would silently default to `false` on existing worlds.
- **Evidence:** `StargateNetworkSettings` — stores settings as raw CompoundTag, no default-value handling.
- **Repro:** N/A — informational, relevant for future settings additions.
- **Suggested Fix:** Use explicit default values: `tag.contains(key) ? tag.getBoolean(key) : DEFAULT_VALUE`.
- **Verify:** N/A.

---

## P007: Factions SavedData Is Empty Stub

- **Severity:** Low (Informational)
- **Affected:** `Factions` — `sgjourney-factions`
- **Impact:** The `Factions` class exists but contains no functional logic. The `goauld` key is defined but never populated. The file is created and saved empty on every server start, consuming minimal disk I/O with no benefit.
- **Evidence:** `Factions.java` — stub class with empty serialization.
- **Repro:** N/A.
- **Suggested Fix:** Remove the class or don't register it until needed.
- **Verify:** N/A.

---

## Key Naming Consistency Check

| SavedData Class | Key Style | Consistent |
|---|---|---|
| `StargateNetwork` | lowercase_snake (`version`, `connections`) | Yes |
| `TransporterNetwork` | PascalCase (`Dimension`, `Transporters`) | Yes (internal) |
| `Universe` | lowercase_snake (`solar_systems`, `dimensions`, `galaxies`) | Yes |
| `BlockEntityList` | lowercase_snake (`stargates`, `transporters`) | Yes |
| `ConduitNetworks` | lowercase_snake (`cables`) | Yes |
| `StargateNetworkSettings` | lowercase_snake (`use_datapack_addresses`, etc.) | Yes |

**Cross-class inconsistency:** `TransporterNetwork` uses PascalCase (`Dimension`, `Transporters`) while all other SavedData classes use lowercase_snake_case. This is functional (NBT keys are just strings) but inconsistent.

---

## Summary

| Severity | Count | IDs |
|----------|-------|-----|
| Critical | 0 | — |
| High | 0 | — |
| Medium | 3 | P001, P002, P003 |
| Low | 4 | P004, P005, P006, P007 |
| **Total** | **7** | |
