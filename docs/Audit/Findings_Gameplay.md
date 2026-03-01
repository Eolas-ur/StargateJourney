# Gameplay Audit Findings

Findings from auditing all blocks, items, tools, weapons, interfaces, and integrations against the rubric in `Rubric.md`.

---

## G001: No Distance Check on DHD Packet — FIXED

- **Severity:** High
- **Status:** Fixed
- **Affected:** `sgjourney:milky_way_dhd`, `sgjourney:pegasus_dhd`, `sgjourney:classic_dhd` — `ServerboundDHDUpdatePacket`
- **Impact:** A malicious client can send a `ServerboundDHDUpdatePacket` with any `BlockPos` in the world. If a DHD exists at that position in a loaded chunk, it will engage a chevron. This allows remote stargate dialing from unlimited distance.
- **Evidence:** `ServerboundDHDUpdatePacket.handle()` — looks up `ctx.player().level().getBlockEntity(packet.blockPos)` with no distance check. Only validates `instanceof AbstractDHDEntity`.
- **Fix Applied:** Added squared-distance check at the top of `ServerboundDHDUpdatePacket.handle()`:
  ```java
  if(ctx.player().distanceToSqr(packet.blockPos.getX() + 0.5, packet.blockPos.getY() + 0.5, packet.blockPos.getZ() + 0.5) > 64.0)
      return;
  ```
  Threshold: **64.0** (8 blocks squared). Check runs before any `getBlockEntity()` call.
- **Verify:** Send crafted packet from 100+ blocks away; silently dropped. Normal DHD use within 8 blocks works.

---

## G002: No Distance Check on Interface Packet — FIXED

- **Severity:** High
- **Status:** Fixed
- **Affected:** `sgjourney:basic_interface`, `sgjourney:crystal_interface`, `sgjourney:advanced_crystal_interface` — `ServerboundInterfaceUpdatePacket`
- **Impact:** A malicious client can modify any Interface block's energy target and mode from unlimited distance, potentially disrupting stargate operations or energy flow.
- **Evidence:** `ServerboundInterfaceUpdatePacket.handle()` — looks up block entity and block with no distance check.
- **Fix Applied:** Added squared-distance check at the top of `ServerboundInterfaceUpdatePacket.handle()`:
  ```java
  if(ctx.player().distanceToSqr(packet.pos.getX() + 0.5, packet.pos.getY() + 0.5, packet.pos.getZ() + 0.5) > 64.0)
      return;
  ```
  Threshold: **64.0** (8 blocks squared). Check runs before any level/BE access.
- **Verify:** Send crafted packet from 100+ blocks away; silently dropped. Normal Interface GUI within 8 blocks works.

---

## G003: No Distance Check on Transceiver Packet — FIXED

- **Severity:** High
- **Status:** Fixed
- **Affected:** `sgjourney:transceiver` — `ServerboundTransceiverUpdatePacket`
- **Impact:** A malicious client can manipulate any Transceiver's frequency, IDC, and trigger transmissions from unlimited distance.
- **Evidence:** `ServerboundTransceiverUpdatePacket.handle()` — no distance check before `getBlockEntity()`.
- **Fix Applied:** Added squared-distance check at the top of `ServerboundTransceiverUpdatePacket.handle()`:
  ```java
  if(ctx.player().distanceToSqr(packet.blockPos.getX() + 0.5, packet.blockPos.getY() + 0.5, packet.blockPos.getZ() + 0.5) > 64.0)
      return;
  ```
  Threshold: **64.0** (8 blocks squared). Check runs before any `getBlockEntity()` call.
- **Verify:** Send crafted packet from 100+ blocks away; silently dropped. Normal Transceiver GUI within 8 blocks works.

---

## G004: Stargate Chunk Force-Loading Not Released On Block Destruction — FIXED

- **Severity:** High
- **Status:** Fixed
- **Affected:** All stargate types — `AbstractStargateEntity`
- **Impact:** If a stargate block is destroyed while a connection is active and `stargate_loads_chunk_when_connected` is true, the chunk may remain force-loaded permanently. The `onRemove` path calls `bypassDisconnectStargate()` → `StargateNetwork.terminateConnection()` → `StargateConnection.terminate()` → `stargate.resetStargate(server, ...)`. But `resetStargate` on the `Stargate` data object calls `getStargateEntity(server)` which may return null if the block entity is already removed, meaning `setConnected(State.IDLE)` (which calls `setChunkForced(false)`) is never reached.
- **Evidence:**
  - `AbstractStargateEntity.setConnected()` (line 1140–1152) — only unforces chunk when transitioning to IDLE.
  - `AbstractStargateBaseBlock.onRemove()` (line 200–216) — calls `bypassDisconnectStargate()` but the terminate path goes through data objects that null-guard `getStargateEntity()`.
  - `StargateConnection.terminate()` (line 245–262) — calls `resetStargate(server, ...)` on `Stargate` data objects.
  - Same pattern as F002 (Transport Rings), already fixed for transporters.
- **Fix Applied:** Added defensive `setChunkForced(false)` at the top of `AbstractStargateEntity.resetStargate()`, guarded by `FORCE_LOAD_CHUNK && connectionID != null && level != null && !level.isClientSide()`. Runs before `connectionID` is cleared and before `setConnected(IDLE)`, guaranteeing chunk release regardless of how `resetStargate` was reached. Redundant unforce from `setConnected(IDLE)` is idempotent.
- **Verify:** Enable chunk loading config. Establish connection. Break a stargate during connection. Run `/forceload query` — chunk is released.

---

## G005: AutoDialer Uses Hardcoded Address

- **Severity:** Low
- **Affected:** `sgjourney:auto_dialer` — `AutoDialerItem`
- **Impact:** The `AutoDialerItem.use()` method dials a hardcoded 6-symbol address `[26, 6, 14, 31, 11, 29]` when activating a stargate. This is likely a development placeholder. In production, this would always dial the same gate (or fail if the address is invalid in the current universe).
- **Evidence:** `AutoDialerItem.use()` — hardcoded `int[]` array.
- **Repro:** Use an Auto Dialer near a stargate. Observe it always dials the same address.
- **Suggested Fix:** Store the target address on the item's data components (NBT), allowing players to configure the destination. Or document as intentional placeholder.
- **Verify:** N/A — informational/design.

---

## G006: StaffWeapon Explosion Can Grief Protected Areas

- **Severity:** Medium
- **Affected:** `sgjourney:matok` — `StaffWeaponItem`, `PlasmaProjectile`
- **Impact:** The `PlasmaProjectile` calls `level().explode()` on impact. With Heavy Liquid Naquadah fuel, explosion power is 1 (comparable to a ghast fireball). This can destroy blocks in areas without explosion protection (e.g., spawn protection doesn't cover mod explosions in all configurations).
- **Evidence:** `PlasmaProjectile.onHitEntity()` and `onHitBlock()` — calls `level().explode()`.
- **Repro:**
  1. Load a staff weapon with Heavy Liquid Naquadah vials.
  2. Fire at a structure.
  3. Observe: blocks are destroyed.
- **Suggested Fix:** Consider checking `ForgeEventFactory.onExplosionStart()` result or making explosion griefing configurable. Current behaviour is likely intentional for gameplay.
- **Verify:** N/A — design decision.

---

## G007: PersonalShield Fuel Drain Has No Floor

- **Severity:** Low
- **Affected:** `sgjourney:personal_shield_emitter` — `PersonalShieldItem`, `ForgeEvents`
- **Impact:** The shield drains fuel equal to incoming damage amount. For extremely high damage values (e.g., `Float.MAX_VALUE` from iris), the drain could underflow or behave unexpectedly. In practice, `Float.MAX_VALUE` damage would instantly empty the fuel tank, which is correct behaviour. No actual bug, but edge case worth noting.
- **Evidence:** `ForgeEvents.onLivingDamage()` — drains fuel proportional to damage.
- **Repro:** N/A — informational.
- **Suggested Fix:** None needed.
- **Verify:** N/A.

---

## G008: GDO/Transceiver Transmission Scans All Loaded Chunks

- **Severity:** Medium
- **Affected:** `sgjourney:gdo` — `GDOItem`, `sgjourney:transceiver` — `TransceiverEntity`
- **Impact:** `sendTransmission()` iterates through all loaded chunks within the configured transmission radius and calls `getBlockEntities()` on each. With large transmission distances and many loaded chunks, this can cause lag spikes. The scan runs once per button press (not per tick), so impact is moderate.
- **Evidence:**
  - `GDOItem.sendTransmission()` — iterates loaded chunks within radius.
  - `TransceiverEntity.sendTransmission()` — same pattern.
- **Repro:**
  1. Set `max_transceiver_transmission_distance` to a very large value.
  2. Load many chunks (multiple players spread out).
  3. Press Transmit on a Transceiver.
  4. Observe: momentary lag spike.
- **Suggested Fix:** Add a cap on the scan radius or use a spatial index. Current default is likely reasonable for normal gameplay.
- **Verify:** Profile with Spark during transmission.

---

## G009: DHD Stargate Search Uses LocatorHelper Scan

- **Severity:** Low
- **Affected:** All DHD types — `AbstractDHDEntity`
- **Impact:** On load, `AbstractDHDEntity.setStargate()` calls `LocatorHelper.getNearbyStargates()` to find the nearest stargate within 16 blocks. This scans nearby block entities. The 16-block radius keeps this cheap. Once found, the stargate position is cached as a relative offset, so the scan only runs on load/placement.
- **Evidence:** `AbstractDHDEntity.findNearestStargate()` — sorts by distance, picks first unlinked.
- **Repro:** N/A — informational.
- **Suggested Fix:** None needed — scan is bounded and infrequent.
- **Verify:** N/A.

---

## G010: Tech Block Entities Call updateClient() Every Tick

- **Severity:** Medium
- **Affected:** `NaquadahGeneratorEntity`, `BatteryBlockEntity`, `ZPMHubEntity`, `TransceiverEntity`, all `AbstractInterfaceEntity` subclasses
- **Impact:** Every tick, these block entities call `updateClient()` which triggers `level.sendBlockUpdated()`. This sends a block entity data packet to all players tracking the chunk, even when no data has changed. With many generators/batteries in a single chunk, this adds unnecessary network traffic and garbage collection pressure.
- **Evidence:**
  - `NaquadahGeneratorEntity.tick()` — calls `updateClient()` unconditionally.
  - `BatteryBlockEntity.tick()` — same pattern.
  - `ZPMHubEntity.tick()` — same pattern.
- **Repro:**
  1. Place 20 Naquadah Generators in a single chunk.
  2. Profile network traffic with Spark or a packet sniffer.
  3. Observe: 20 block update packets per tick even when idle.
- **Suggested Fix:** Track a `dirty` flag and only call `updateClient()` when state actually changes. For generators, set dirty when `reactionProgress` changes; for batteries, when energy level changes.
- **Verify:** Profile network traffic; no block updates when generators are idle.

---

## G011: Crystallizer/Liquidizer Recipe Lookup Per Tick

- **Severity:** Low
- **Affected:** `CrystallizerEntity`, `NaquadahLiquidizerEntity`
- **Impact:** During processing, the crystallizer checks recipe validity each tick via `getRecipeFor()`. Recipe lookups use the vanilla `RecipeManager` which caches results, but the input assembly and comparison still runs. Impact is low because these machines are typically few in number.
- **Evidence:** `AbstractCrystallizerEntity.tick()` — calls recipe check in processing loop.
- **Repro:** N/A — informational.
- **Suggested Fix:** Cache the current recipe and only re-lookup when inventory changes.
- **Verify:** N/A.

---

## G012: Cable Network BFS On Every Block Update

- **Severity:** Medium
- **Affected:** All cable types — `CableBlock`, `ConduitNetworks`
- **Impact:** When a cable is placed or broken, `ConduitNetworks.update()` runs a BFS traversal of all connected cables (up to `max_cables_in_network`, default 4096). This is correct for maintaining network integrity but can cause lag spikes for very large networks during rapid placement/breaking.
- **Evidence:** `ConduitNetworks.findConnectedCables()` — BFS up to 4096 nodes.
- **Repro:**
  1. Build a cable network with 4000 cables.
  2. Break a cable in the middle.
  3. Observe: BFS runs on each resulting network segment.
- **Suggested Fix:** The 4096 cap is a reasonable limit. For further optimization, defer BFS to end-of-tick to batch multiple changes. Current behaviour is acceptable.
- **Verify:** N/A.

---

## G013: KaraKesh Has No Multiplayer Abuse Prevention

- **Severity:** Low
- **Affected:** `sgjourney:kara_kesh` — `KaraKeshItem`
- **Impact:** Terror Mode inflicts Blindness (10s), Wither (10s), and Slowness 255 (10s) on right-clicked entities. The 200-tick cooldown prevents rapid reuse, but in PvP, this is an extremely powerful effect with no counterplay (cannot be blocked by shields). This is a design choice, not a bug.
- **Evidence:** `KaraKeshItem.interactLivingEntity()` — applies effects with long duration.
- **Repro:** Right-click another player with KaraKesh in Terror Mode.
- **Suggested Fix:** None — design decision. Consider making effect duration configurable.
- **Verify:** N/A.

---

## G014: Wormhole Entity Scan Uses AABB Every Tick

- **Severity:** Low
- **Affected:** All stargate types — `SGJourneyStargate.doWormhole()`
- **Impact:** While a wormhole is active, `getEntitiesOfClass()` scans a 5x5x5 AABB around the gate center every tick on both endpoints. This is standard Minecraft entity scanning and is not a significant performance concern for typical stargate counts (1-2 active wormholes).
- **Evidence:** `SGJourneyStargate.doWormhole()` — calls `level.getEntitiesOfClass()` per tick.
- **Repro:** N/A — informational.
- **Suggested Fix:** None needed for typical usage.
- **Verify:** N/A.

---

## G015: Stargate isObstructed() Scans 5x5 Area

- **Severity:** Low
- **Affected:** All stargate types — `AbstractStargateEntity.isObstructed()`
- **Impact:** During dialing, `isObstructed()` scans a 5x5 area in front of the gate for blocking blocks. This runs once per dial attempt, not per tick. Impact is negligible.
- **Evidence:** `AbstractStargateEntity.isObstructed()`.
- **Repro:** N/A — informational.
- **Suggested Fix:** None needed.
- **Verify:** N/A.

---

## Summary

| Severity | Count | IDs |
|----------|-------|-----|
| Critical | 0 | — |
| High | 4 | G001, G002, G003, G004 |
| Medium | 4 | G006, G008, G010, G012 |
| Low | 7 | G005, G007, G009, G011, G013, G014, G015 |
| **Total** | **15** | |
