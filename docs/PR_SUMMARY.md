# PR: Security Hardening, Performance Fixes, and Persistence Safety

**Mod:** Stargate Journey (sgjourney) v0.6.44, MC 1.21.1, NeoForge

## Problem Statement

A comprehensive audit of the mod's gameplay surface identified 31 findings across security, stability, performance, and data persistence. This PR addresses the 13 highest-priority items:

- **Security (6 findings):** Serverbound packets accepted arbitrary `BlockPos` from clients with no distance validation, allowing remote activation of block entities across the map. Chunk force-loading could leak if blocks were destroyed during active connections.
- **Performance (4 findings):** Tech block entities sent network sync packets every tick even when idle. Connection handlers allocated a new HashMap every tick regardless of state. GDO/Transceiver transmission scans had no chunk count cap. Cable network BFS ran redundantly on every neighbor update.
- **Persistence (3 findings):** Version migration in StargateNetwork/TransporterNetwork silently wiped connections with only DEBUG/INFO logging. Malformed UUID keys in saved data could crash TransporterNetwork loading or accumulate silently.

## Fixes Included

### Security — Packet Distance Validation
- **F001:** `ServerboundRingPanelUpdatePacket` — added distance check (64.0 sq / 8 blocks)
- **F002:** `AbstractTransporterEntity.resetTransporter()` — defensive `loadChunk(false)` on block destruction
- **G001:** `ServerboundDHDUpdatePacket` — added distance check (64.0 sq / 8 blocks)
- **G002:** `ServerboundInterfaceUpdatePacket` — added distance check
- **G003:** `ServerboundTransceiverUpdatePacket` — added distance check
- **G004:** `AbstractStargateEntity.resetStargate()` — defensive `setChunkForced(false)`

### Performance — Tick Cost Reduction
- **G010:** Dirty-flag `updateClient()` across all tech BEs; 10-tick throttle for energy BEs, immediate for user-driven state (Transceiver)
- **F004:** `StargateNetwork.handleConnections()` and `TransporterNetwork.handleConnections()` — early `isEmpty()` return; copy-constructor instead of `new HashMap<>()` + `putAll()`
- **G008:** Configurable `max_transmission_scan_chunks` (default 81, range 9–289) caps GDO/Transceiver chunk scanning
- **G012:** Cable network BFS batched per-tick with visited-set coalescing; `ConduitNetworks.processPendingUpdates()` called from `ForgeEvents.onTick()`

### Persistence — Logging and Crash Prevention
- **P001:** `StargateNetwork.updateNetwork()` — log upgraded from DEBUG to WARN with descriptive migration message
- **P002:** `TransporterNetwork.updateNetwork()` — log upgraded from INFO to WARN with descriptive migration message
- **P003:** Aggregate malformed-UUID counting and WARN logging in `BlockEntityList`, `StargateNetwork`, and `TransporterNetwork` deserialization; try-catch added to `TransporterNetwork.Dimension.deserialize()` for previously unguarded `UUID.fromString()`

## Key Behavioural Guarantees

- **No gameplay rebalance.** Energy values, distances, timings, recipes, and item stats are unchanged.
- **Backwards compatible.** No new SavedData formats. No NBT schema changes. Existing worlds load without migration.
- **1-tick cable update delay.** Cable network connectivity is now resolved on the next server tick after placement/removal, not immediately. Energy transfer to a newly-placed cable begins one tick after placement. This matches normal Minecraft block update timing and is imperceptible in gameplay.
- **Client sync delay.** Tech block entity GUI updates may lag by up to 0.5 seconds (10 ticks) for energy displays. User-driven state changes (Transceiver frequency/code) sync immediately.
- **Config addition.** One new config key: `max_transmission_scan_chunks` (default 81). No existing config keys changed.

## How to Verify

1. **Packet distance checks (F001, G001–G003):** From >8 blocks away, send a crafted serverbound packet targeting a Ring Panel / DHD / Interface / Transceiver. Packet is silently dropped. Normal use within 8 blocks works.
2. **Chunk force-load release (F002, G004):** Initiate ring transport or stargate connection. Break the destination block during animation. Run `/forceload query` — chunk is not forced.
3. **Idle sync reduction (G010):** Place 20 idle Naquadah Generators. Profile network traffic — zero block update packets. Activate one — syncs every 10 ticks.
4. **Idle connection handler (F004):** Start server with no active connections. Profile allocations in `handleConnections()` — zero HashMap allocations.
5. **Transmission scan cap (G008):** Set transmission distance to 128 blocks. Scan completes after 81 chunks maximum.
6. **Cable BFS batching (G012):** Build 200-cable network. Rapidly break/place cables. No lag spike; network settles within 1 tick.
7. **Migration logging (P001, P002):** Downgrade `version` in `sgjourney-stargate_network.dat`. Reload — server log shows WARN with version numbers and reset description.
8. **UUID crash prevention (P003):** Insert malformed UUID key in `sgjourney-block_entities.dat`. Reload — no crash, WARN log with count.

## Files Changed (Code)

| File | Changes |
|------|---------|
| `ServerboundRingPanelUpdatePacket.java` | Distance check |
| `ServerboundDHDUpdatePacket.java` | Distance check |
| `ServerboundInterfaceUpdatePacket.java` | Distance check |
| `ServerboundTransceiverUpdatePacket.java` | Distance check |
| `AbstractTransporterEntity.java` | Defensive chunk unforce |
| `AbstractStargateEntity.java` | Defensive chunk unforce |
| `EnergyBlockEntity.java` | `clientDirty` flag, conditional `updateClient()` |
| `NaquadahGeneratorEntity.java` | Dirty flag on reaction progress, throttled sync |
| `BatteryBlockEntity.java` | Throttled sync |
| `AbstractInterfaceEntity.java` | Dirty on symbol change, throttled sync |
| `AbstractCrystallizerEntity.java` | Dirty on progress, throttled sync |
| `AbstractNaquadahLiquidizerEntity.java` | Own `clientDirty`, throttled sync |
| `TransceiverEntity.java` | Own `clientDirty`, immediate sync |
| `StargateNetwork.java` | Early return in `handleConnections()`, WARN migration log, UUID error counting |
| `TransporterNetwork.java` | Early return in `handleConnections()`, WARN migration log, UUID error counting, try-catch in `Dimension.deserialize()` |
| `BlockEntityList.java` | Aggregate UUID error logging |
| `CommonTransmissionConfig.java` | `max_transmission_scan_chunks` config |
| `GDOItem.java` | Chunk scan cap in `sendTransmission()` and `checkShieldingState()` |
| `ConduitNetworks.java` | Batched BFS with `pendingUpdates` and `processPendingUpdates()` |
| `ForgeEvents.java` | Tick hook for `processPendingUpdates()` |
