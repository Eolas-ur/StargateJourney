# Audit Test Plan

## F001: Ring Panel Packet Distance Check — FIXED

### Setup
1. Place Transport Rings (A) and Ring Panel near A.
2. Place Transport Rings (B) at least 100 blocks away.

### Steps
1. Open Ring Panel GUI from normal range (within 8 blocks). Verify B appears in list and transport works.
2. Using a mod or packet-crafting tool, send `ServerboundRingPanelUpdatePacket` targeting the Ring Panel's BlockPos from 100+ blocks away.
3. Observe whether transport activates.

### Expected: Packet is silently dropped when player is >8 blocks from the Ring Panel. Normal usage within 8 blocks works.

### Verification Notes
- Threshold: 64.0 squared distance (8 blocks). Matches vanilla `Player.MAX_INTERACTION_DISTANCE`.
- Check applied in `ServerboundRingPanelUpdatePacket.handle()` before `getBlockEntity()` lookup, so no world access occurs for out-of-range packets.

---

## F002: Chunk Force-Loading on Block Destruction — FIXED

### Setup
1. Place Transport Rings (A) and Transport Rings (B) at least 100 blocks apart (different chunks).
2. Place Ring Panel near A.

### Steps
1. Initiate transport from A to B via Ring Panel.
2. During the ring animation (within the ~46-tick window), break Ring B with a pickaxe.
3. Run `/forceload query` in B's chunk immediately after breaking.

### Expected: B's chunk is released when the block is broken. `/forceload query` shows no forced chunks at that position.

### Verification Notes
- Fix location: `AbstractTransporterEntity.resetTransporter()` — calls `loadChunk(false)` when `connectionID != null` before clearing state.
- Null safety: guarded by `level != null && !level.isClientSide()` to prevent NPE during teardown.
- Idempotent: `setChunkForced(x, z, false)` on an already-unforced chunk is a no-op, so the redundant call from `setConnected(false)` is harmless.

---

## F003: Stale Registry Entries

### Setup
1. Place 5 Transport Ring platforms in separate chunks.
2. Note their positions.

### Steps
1. Save and quit.
2. Break 3 of the 5 ring platforms.
3. Immediately unload those chunks (move far away or use `/forceload remove`).
4. Save and quit again.
5. Reload the server.
6. Check server logs for "Deserializing Transporters" — note the count.

### Expected: After reload, only 2 transporters should be in the registry. (Currently, stale entries may persist until their chunks are loaded and verified.)

---

## F004: Idle Connection Map Copy

### Reproduction
1. Start a server with no active Stargate or Transporter connections.
2. Profile with Spark or JVisualVM.
3. Observe allocations in `StargateNetwork.handleConnections()` and `TransporterNetwork.handleConnections()`.

### Expected (Current): `HashMap` allocated every tick.
### Expected (Fixed): No allocation when `connections.isEmpty()`.

---

## F007: TransporterConnection Null Transporter

### Setup
1. Place two Transport Ring platforms and initiate transport.
2. While transport is active, save the world.
3. Edit `sgjourney-block_entities.dat` to remove one transporter's UUID entry.
4. Reload the server.

### Expected (Current): Stale connection created with null endpoint; log error on next tick; connection terminates.
### Expected (Fixed): Stale connection discarded during deserialization; no error logged.

---

## Ring Privacy Mode Test Plan

### Setup
1. Place two Transport Ring platforms, A and B.
2. Rename both via anvil (e.g., "Alpha" and "Bravo") for easy identification.
3. Place a Ring Panel within 16 blocks of A.

### Test 1: Toggle Visibility
1. Open Ring Panel. Verify both Alpha and Bravo appear in the destination list.
2. Walk to platform B. Crouch (Shift) with empty main hand and right-click B.
3. Verify chat message: "Transport Rings set to: Private" (red text).
4. Return to Ring Panel and reopen GUI. Verify Bravo no longer appears in the list.

### Test 2: Memory Crystal Bypass
1. Obtain a Memory Crystal bound to platform B's UUID.
2. Place the Memory Crystal into slot 0 of the Ring Panel.
3. Click the corresponding button. Verify transport initiates to Bravo despite it being private.

### Test 3: Toggle Back
1. Walk to platform B. Crouch with empty main hand and right-click B.
2. Verify chat message: "Transport Rings set to: Discoverable" (green text).
3. Return to Ring Panel. Verify Bravo reappears in the destination list.

### Test 4: Persistence
1. Set platform B to Private.
2. Save and quit world.
3. Reload world.
4. Open Ring Panel. Verify Bravo is still absent (privacy persisted).
5. Check platform B with PDA. Verify "Discoverable: false" in status output.

### Test 5: Block Destruction
1. Set platform B to Private.
2. Break platform B.
3. Verify no crash occurs.
4. Verify B does not appear as a ghost entry in any Ring Panel.

### Test 6: Accidental Toggle Prevention
1. Right-click platform B with a non-empty main hand while crouching. Verify no toggle occurs.
2. Right-click platform B with empty hand while NOT crouching. Verify no toggle occurs.
3. Only Shift + empty hand + right-click should toggle.

---

## G001: DHD Packet Distance Check — FIXED

### Setup
1. Place a Milky Way DHD and a connected Stargate.
2. Note the DHD's BlockPos.

### Steps
1. Open DHD GUI from normal range (within 8 blocks). Verify chevron engagement works.
2. Using a packet-crafting tool, send `ServerboundDHDUpdatePacket` targeting the DHD's BlockPos with symbol 1 from 100+ blocks away.
3. Observe: packet should be silently dropped.

### Expected: Packet dropped at >8 blocks. Normal use within 8 blocks works.

---

## G002: Interface Packet Distance Check — FIXED

### Setup
1. Place a Basic Interface connected to a Stargate.
2. Note its BlockPos.

### Steps
1. Open Interface GUI from normal range. Verify energy target and mode changes work.
2. From 100+ blocks away, send crafted `ServerboundInterfaceUpdatePacket`.
3. Observe: packet should be silently dropped.

### Expected: Packet dropped at >8 blocks. Normal GUI use within 8 blocks works.

---

## G003: Transceiver Packet Distance Check — FIXED

### Setup
1. Place a Transceiver.
2. Note its BlockPos.

### Steps
1. Open Transceiver GUI from normal range. Verify frequency input and transmission work.
2. From 100+ blocks away, send crafted `ServerboundTransceiverUpdatePacket` with `transmit=true`.
3. Observe: packet should be silently dropped.

### Expected: Packet dropped at >8 blocks. Normal GUI use within 8 blocks works.

---

## G004: Stargate Chunk Force-Loading Release — FIXED

### Setup
1. Enable `stargate_loads_chunk_when_connected` in mod config.
2. Place two Stargates (A and B) in different chunks, at least 100 blocks apart.

### Steps
1. Dial from A to B. Verify connection established and both chunks force-loaded.
2. While connection is active, break Stargate B's base block.
3. Run `/forceload query` at Stargate B's former position.
4. Verify: chunk is NOT force-loaded.
5. Rebuild Stargate B, repeat with breaking Stargate A instead.

### Expected: Chunk force-loading is released on block destruction regardless of which gate is broken.
