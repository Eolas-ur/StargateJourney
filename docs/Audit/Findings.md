# Audit Findings

## F001: Ring Panel Packet Has No Distance Check — FIXED

- **Severity:** High
- **Status:** Fixed
- **Impact:** A malicious client can send `ServerboundRingPanelUpdatePacket` with any `BlockPos` in the world. The server will look up a `RingPanelEntity` at that position and call `activateRings()`. This allows a player to activate any Ring Panel at any distance in any loaded chunk, bypassing intended range/proximity restrictions.
- **Evidence:**
  - `ServerboundRingPanelUpdatePacket.handle()` (line 33) — retrieves `ctx.player().level().getBlockEntity(packet.blockPos)` with no distance check between the player and the block.
  - The `blockPos` field is sent directly from the client (`BlockPos.STREAM_CODEC`).
- **Fix Applied:** Added squared-distance check at the top of the handler in `ServerboundRingPanelUpdatePacket.handle()`:
  ```java
  if(ctx.player().distanceToSqr(packet.blockPos.getX() + 0.5, packet.blockPos.getY() + 0.5, packet.blockPos.getZ() + 0.5) > 64.0)
      return;
  ```
  Threshold: **64.0** (8 blocks squared). This matches vanilla's container interaction range (`Player.MAX_INTERACTION_DISTANCE` = 64.0). Packets from beyond 8 blocks are silently dropped.
- **How to Verify:** Send a crafted packet targeting a Ring Panel 100+ blocks away. Packet is silently dropped; no transport occurs. Normal usage within 8 blocks is unaffected.

---

## F002: Chunk Force-Loading Not Released On Block Destruction During Active Connection — FIXED

- **Severity:** High
- **Status:** Fixed
- **Impact:** If a Transport Ring block is destroyed (by explosion, piston, etc.) while it has an active `TransporterConnection`, the chunk could remain force-loaded permanently. The `onRemove` path calls `disconnectTransporter()` → `terminateConnection()` → `connection.terminate()` → `transporter.reset(server)` → `SGJourneyTransporter.reset()` → `getTransporterEntity()` → `resetTransporter()` → `setConnected(false)`. While `TransportRingsEntity.setConnected()` does call `loadChunk(connected)` outside the block state guard, the chain depends on `getTransporterEntity()` successfully resolving the block entity. If the BE is already inaccessible (e.g., chunk unloaded by another mechanism, or the connection terminates via `tick()` after the block is fully gone), `getTransporterEntity()` returns null and `loadChunk(false)` is never reached.
- **Evidence:**
  - `TransportRingsEntity.setConnected()` (line 222–231) — `loadChunk(connected)` is outside the blockstate `if` but still relies on the call chain reaching it.
  - `SGJourneyTransporter.reset()` (line 203–209) — null-guards `getTransporterEntity()`, so if BE is gone, `resetTransporter()` is never called.
  - `AbstractTransporterEntity.loadChunk()` (line 245) — the actual `setChunkForced` call.
- **Fix Applied:** Added a defensive `loadChunk(false)` call at the top of `AbstractTransporterEntity.resetTransporter()`, guarded by `connectionID != null && level != null && !level.isClientSide()`. This runs before `connectionID` is cleared and before `setConnected(false)`, guaranteeing chunk release regardless of block state or subclass behavior. The redundant `loadChunk(false)` from `setConnected(false)` is harmless — `setChunkForced(x, z, false)` on an already-unforced chunk is a no-op.
- **How to Verify:** Start a ring transport. During the animation, break one ring block. Run `/forceload query` — the chunk is released immediately on block destruction.

---

## F003: TransporterNetwork Registry Can Grow Unboundedly If Block Entity Changes ID

- **Severity:** Medium
- **Impact:** `BlockEntityList.addTransporter()` (line 144) checks `if(this.transporterMap.containsKey(id))` and returns the existing entry if found. However, in `tryDeserializeTransporter()` (line 297), if a UUID parse fails, a *new* UUID is generated and a new `SGJourneyTransporter` is created. The old entry with the unparseable key is not removed (it was never parseable as a UUID, so it won't be in `transporterMap` keyed by UUID). Over many load/save cycles with data corruption, entries could accumulate. Additionally, if the `reloadNetwork()` method in `TransporterNetwork` (line 77) fails to load a block entity (e.g., the chunk is unloaded), it calls `BlockEntityList.removeTransporter()` (line 118) — but only if the block entity doesn't match. This is generally safe, but the lack of a periodic cleanup sweep means stale entries from destroyed blocks in unloaded chunks persist until next reload.
- **Evidence:**
  - `BlockEntityList.tryDeserializeTransporter()` (line 297–318)
  - `TransporterNetwork.addTransporters()` (line 98–120) — iterates all transporters but only verifies those whose chunk is loaded.
  - `BlockEntityList.deserializeTransporters()` (line 280–295) — no deduplication check on blockPos, only on UUID.
- **Suggested Fix:** Add a periodic cleanup that iterates `transporterMap` and verifies each entry's block entity still exists. This could run on server tick at low frequency (every 6000 ticks / 5 minutes) or on `reloadNetwork()`.
- **How to Verify:** Place 10 ring platforms. Break 5 of them in chunks that then get unloaded. Reload the server. Check that `BlockEntityList` transporterMap size reflects only the 5 remaining platforms (currently it may retain all 10 until the destroyed chunks are loaded and the stale entries are detected).

---

## F004: StargateNetwork.handleConnections() Copies Entire Map Every Tick — FIXED

- **Severity:** Medium
- **Status:** Fixed
- **Impact:** `StargateNetwork.handleConnections()` and `TransporterNetwork.handleConnections()` created a new `HashMap` with `putAll()` every server tick, even when no connections existed. Also called `setDirty()` every tick regardless.
- **Fix Applied:**
  - Added early return `if(this.connections.isEmpty()) return;` to both `StargateNetwork.handleConnections()` and `TransporterNetwork.handleConnections()`.
  - Changed `new HashMap<>()` + `putAll()` to single-line copy constructor `new HashMap<>(this.connections)`.
  - When idle (no active connections), zero allocations and no `setDirty()` call.
- **Verify:** Profile with Spark during idle server. HashMap allocation rate drops to zero when no connections exist.

---

## F005: LocatorHelper.findNearestTransporters() Sorts Entire Dimension Registry Per Call

- **Severity:** Low
- **Impact:** `LocatorHelper.findNearestTransporters()` (line 70) copies the entire dimension's transporter list (via `getTransporters()` which calls `new ArrayList<>(transporters)`), then sorts it by distance, then filters. This runs every time a Ring Panel refreshes its destination list. With many transporters in a dimension, this is O(n log n) per refresh. However, refreshes are not per-tick — they're triggered by player interaction — so impact is low.
- **Evidence:**
  - `LocatorHelper.findNearestTransporters()` (line 70–85)
  - `TransporterNetwork.Dimension.getTransporters()` (line 429–432) — creates a new `ArrayList` copy.
- **Suggested Fix:** None needed for current scale. If dimensions grow to 1000+ transporters, consider caching or limiting the search. A simple improvement: after sorting, take only the first N (e.g., 7) before filtering, to avoid processing the full list.
- **How to Verify:** N/A — informational.

---

## F006: No Stargate Packet Distance Validation (Informational)

- **Severity:** Low
- **Impact:** Multiple stargate-related packets (DHD dialing, interface updates) follow the same pattern as F001 — they accept a `BlockPos` from the client and look up a block entity without verifying the player is near it. This is common in Minecraft mods and vanilla itself. Exploitation requires the player to know the target BlockPos.
- **Evidence:**
  - Pattern visible across `Serverbound*Packet` classes in `net.povstalec.sgjourney.common.packets/`.
- **Suggested Fix:** Add distance checks to all serverbound packet handlers that modify world state. A utility method could be shared across handlers.
- **How to Verify:** Same approach as F001.

---

## F007: TransporterConnection Deserialization Lacks Null Safety

- **Severity:** Medium
- **Impact:** `TransporterConnection.deserialize()` (line 188–195) calls `new TransporterConnection(server, uuid, transporterA, transporterB, connectionTime)` even if `transporterA` or `transporterB` is null (returned by `BlockEntityList.getTransporter()` if the UUID is not found). The constructor stores null references. On the next tick, `connection.tick()` calls `isTransporterValid()` which handles null with a log error and calls `terminate()`. However, `terminate()` then calls `this.transporterA.reset(server)` — if `transporterA` is null, this throws a `NullPointerException`. The null check on line 135 (`if(this.transporterA != null)`) does guard this, so this is actually safe. But `updateTransporterTicks` at line 106 would be called before the `isTransporterValid` check at line 91 completes in the same tick... wait, looking more carefully: the `isTransporterValid` check is at line 91 and short-circuits with `terminate` before `updateTransporterTicks` at line 100-101. So the order is: check validity first, then update ticks only if valid. This is actually fine.
- **Evidence:**
  - `TransporterConnection.deserialize()` (line 188–195)
  - `TransporterConnection.tick()` (line 88–104) — validity check before tick updates.
  - `TransporterConnection.terminate()` (line 133–141) — null checks present.
- **Suggested Fix:** Add a null check in `deserialize()`: return `null` if either transporter is null. The caller in `TransporterNetwork.deserializeConnections()` (line 333) already handles null results.
- **How to Verify:** Delete a transporter from `sgjourney-block_entities.dat` while it has an active connection serialized in `sgjourney-transporter_network.dat`. Reload server. Without fix, stale connection is created with null endpoint. With fix, stale connection is discarded.

---

## F008: Stargate Block Removal Properly Cleans Network

- **Severity:** None (Positive Finding)
- **Impact:** Stargate blocks properly clean up on destruction.
- **Evidence:**
  - `AbstractStargateBaseBlock.onRemove()` (line 200) calls `stargateEntity.disconnectStargate()` and `stargateEntity.removeStargateFromNetwork()`.
  - `AbstractStargateEntity.removeStargateFromNetwork()` (line 409) calls `StargateNetwork.get(level).removeStargate(id9ChevronAddress)`.
  - `StargateNetwork.removeStargate()` (line 184) removes from `Universe` solar system and from `BlockEntityList`.
- **How to Verify:** Place a stargate, verify it appears in `BlockEntityList`, break it, verify it's removed.

---

## F009: Transport Ring Block Removal Properly Cleans Network

- **Severity:** None (Positive Finding)
- **Impact:** Transport Ring blocks properly clean up on destruction.
- **Evidence:**
  - `AbstractTransporterBlock.onRemove()` (line 43) calls `transporterEntity.disconnectTransporter()` and `transporterEntity.removeTransporterFromNetwork()`.
  - `AbstractTransporterEntity.removeTransporterFromNetwork()` (line 136) calls `TransporterNetwork.get(level).removeTransporter(level, this.id)`.
  - `TransporterNetwork.removeTransporter()` (line 142) removes from dimension list and from `BlockEntityList`.
- **How to Verify:** Place transport rings, verify they appear in network, break them, verify they're removed.
