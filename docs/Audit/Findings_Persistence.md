# Persistence and NBT Audit Findings

Global audit of all SavedData classes, NBT stores, and serialization paths.

---

## P001: StargateNetwork Version Migration Is Destructive

- **Severity:** Medium
- **Affected:** `StargateNetwork` — `sgjourney-stargate_network`
- **Impact:** When `version` in the saved data is less than `updateVersion` (currently 17), `deserialize()` skips loading connections entirely and increments the version. This means all active stargate connections are silently dropped on version upgrade. While this prevents crashes from format changes, it provides no migration path — any connection active at the time of the upgrade is permanently lost.
- **Evidence:** `StargateNetwork.deserialize()` — checks `version < updateVersion` and skips `deserializeConnections()`.
- **Repro:**
  1. Establish a stargate connection.
  2. Save the world.
  3. Manually set `version` to a lower value in `sgjourney-stargate_network.dat`.
  4. Reload.
  5. Observe: all connections are gone.
- **Suggested Fix:** Log a warning when connections are dropped due to version migration. Consider serializing connections in a format that degrades gracefully across versions.
- **Verify:** Check server log for migration warning.

---

## P002: TransporterNetwork Version Migration Same Pattern

- **Severity:** Medium
- **Affected:** `TransporterNetwork` — `sgjourney-transporter_network`
- **Impact:** Same destructive migration pattern as P001. When `version < UPDATE_VERSION` (currently 2), all transporter connections and dimension data are dropped.
- **Evidence:** `TransporterNetwork.deserialize()` — checks version and skips deserialization.
- **Repro:** Same as P001 but for transporter network.
- **Suggested Fix:** Same as P001.
- **Verify:** Same as P001.

---

## P003: BlockEntityList UUID Parse Failure Creates Orphaned Entries

- **Severity:** Medium
- **Affected:** `BlockEntityList` — `sgjourney-block_entities`
- **Impact:** In `tryDeserializeTransporter()`, if a UUID string fails to parse (`IllegalArgumentException`), a new UUID is generated and the transporter is stored under the new key. The old malformed key remains in the `CompoundTag` source. On save, both the old (unparseable) and new entries exist. Over multiple load/save cycles with data corruption, orphaned entries accumulate.
- **Evidence:** `BlockEntityList.tryDeserializeTransporter()` — catches parse exception, generates new UUID, does not remove old key.
- **Repro:**
  1. Edit `sgjourney-block_entities.dat` to insert a transporter with a malformed UUID key.
  2. Reload the server.
  3. Save and reload again.
  4. Inspect the dat file — both the malformed key and the new valid UUID entry exist.
- **Suggested Fix:** On UUID parse failure, log a warning with the malformed key. After deserialization, remove keys that failed to parse from the source `CompoundTag` before re-serializing.
- **Verify:** After reload, only valid UUID entries exist in the saved data.

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
