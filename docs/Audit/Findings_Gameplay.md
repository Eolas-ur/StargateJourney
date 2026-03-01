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

## G008: GDO/Transceiver Transmission Scans All Loaded Chunks — FIXED

- **Severity:** Medium
- **Status:** Fixed
- **Affected:** `sgjourney:gdo` — `GDOItem`, `sgjourney:transceiver` — `TransceiverEntity`
- **Impact:** `sendTransmission()` iterates through all loaded chunks within the configured transmission radius and calls `getBlockEntities()` on each. With large transmission distances and many loaded chunks, this can cause lag spikes. The scan runs once per button press (not per tick), so impact is moderate.
- **Evidence:**
  - `GDOItem.sendTransmission()` — iterates loaded chunks within radius.
  - `TransceiverEntity.sendTransmission()` — same pattern.
- **Fix Applied:** Added configurable chunk scan cap (`max_transmission_scan_chunks` in `CommonTransmissionConfig`, default 81, range 9–289). All four scan loops (`GDOItem.sendTransmission()`, `GDOItem.checkShieldingState()`, `TransceiverEntity.sendTransmission()`, `TransceiverEntity.checkShieldingState()`) now track `chunksScanned` and break when exceeding the configured limit. Default 81 corresponds to a 9×9 chunk grid (~64 block radius). Default transmission radius (20 blocks) scans only 25 chunks, unaffected by the cap.
- **Verify:** Set transmission distance to 128. Scan completes after 81 chunks instead of 289. Default radius (20) behavior unchanged.

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

## G010: Tech Block Entities Call updateClient() Every Tick — FIXED

- **Severity:** Medium
- **Status:** Fixed
- **Affected:** `NaquadahGeneratorEntity`, `BatteryBlockEntity`, `TransceiverEntity`, all `AbstractInterfaceEntity` subclasses, `AbstractCrystallizerEntity`, `AbstractNaquadahLiquidizerEntity`
- **Impact:** Every tick, these block entities call `updateClient()` which triggers `blockChanged()`. This sends a block entity data packet to all players tracking the chunk, even when no data has changed. With many generators/batteries in a single chunk, this adds unnecessary network traffic and garbage collection pressure.
- **Fix Applied:** Dirty-flag-driven client sync across all affected BEs:
  - **`EnergyBlockEntity`** (base class): Added `protected boolean clientDirty`. Set `true` in `changeEnergy()` when `!simulate`, covering all energy changes for generators, batteries, interfaces, and crystallizers.
  - **`NaquadahGeneratorEntity`**: Also sets dirty on `reactionProgress` changes. Throttled sync: `if(clientDirty && level.getGameTime() % 10 == 0)`.
  - **`BatteryBlockEntity`**: Energy changes already covered by base class. Throttled sync (every 10 ticks when dirty).
  - **`AbstractInterfaceEntity`**: Sets dirty on symbol changes. Throttled sync.
  - **`AbstractCrystallizerEntity`**: Sets dirty on progress changes. Throttled sync.
  - **`AbstractNaquadahLiquidizerEntity`** (extends `BlockEntity`): Added `clientDirty` field directly. Sets dirty on progress changes. Throttled sync.
  - **`TransceiverEntity`** (extends `BlockEntity`): Added `clientDirty` field directly. Sets dirty on `setFrequency()`, `setCurrentCode()`, `toggleFrequency()`, `addToCode()`, `removeFromCode()`. Immediate sync (no throttle) since state changes are rare/user-driven.
  - **Throttle**: Energy-intensive BEs sync at most once per 10 ticks (0.5 seconds) when dirty. Zero syncs when idle. Client-visible state change delay is imperceptible for numerical displays.
- **Verify:** Place 20 idle generators. Profile network traffic — zero block update packets. Place an active generator — syncs every 10 ticks instead of every tick.

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

## G012: Cable Network BFS On Every Block Update — FIXED

- **Severity:** Medium
- **Status:** Fixed
- **Affected:** All cable types — `CableBlock`, `ConduitNetworks`
- **Impact:** When a cable was placed or broken, `ConduitNetworks.update()` immediately ran a BFS traversal of all connected cables (up to `max_cables_in_network`, default 4096). Breaking a cable in a large network triggered BFS separately for each neighbor's state change, resulting in redundant traversals of the same network segments.
- **Evidence:** `ConduitNetworks.findConnectedCables()` — BFS up to 4096 nodes, previously called immediately from `CableBlock.updateCable()`.
- **Fix Applied:** One-tick deferred batching with visited-set coalescing:
  - **`ConduitNetworks.update(Level, BlockPos)`**: Now just adds the position to a per-dimension `pendingUpdates` set (O(1)). No BFS runs at call time.
  - **`ConduitNetworks.processPendingUpdates(MinecraftServer)`**: Runs once per server tick via `ForgeEvents.onTick()`. Iterates all dirty positions per dimension. Uses a `visited` set: when BFS discovers cables, all their positions are marked visited so subsequent dirty positions in the same connected component skip BFS entirely.
  - **`ForgeEvents.onTick()`**: Added `ConduitNetworks.get(server).processPendingUpdates(server)` after existing network handlers.
  - **`removeCable()`**: Unchanged — still immediate O(1) removal of old network from `cableMap`.
  - **Timing**: Network updates are deferred by at most 1 tick. Energy transfer to a newly-placed cable is available starting the tick after placement. This matches normal Minecraft block update timing.
  - **Coalescing**: If a cable is broken in a 1000-cable network with 4 neighbors, instead of 4 separate BFS traversals in the same tick, only 1–2 BFS runs occur (one per resulting connected component, skipping duplicates via visited set).
  - **Safety**: Pending positions are cleared each tick. If a chunk unloads before processing, BFS from that position finds no cables and is a no-op. No memory leak — `pendingUpdates` is transient (not serialized to NBT).
- **Verify:** Place and break 200 cables rapidly. Confirm no lag spike. Profile with Spark: BFS runs at most once per connected component per tick.

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
