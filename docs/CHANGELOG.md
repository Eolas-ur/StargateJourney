# Documentation Changelog

## 2026-03-01

### Phase 3: Cable Network BFS Batching (G012)

- `ConduitNetworks.update()` deferred to per-tick `processPendingUpdates()` with visited-set coalescing
- `ForgeEvents.onTick()` drives batched processing
- G012 marked FIXED in audit docs; 4 test cases added to `Test_Plan.md`

### Phase 2: Performance & Persistence Hygiene

- **G010 (FIXED):** Dirty-flag `updateClient()` across all tech BEs; 10-tick throttle for energy BEs
- **G008 (FIXED):** `max_transmission_scan_chunks` config (default 81, range 9–289) caps chunk scanning
- **F004 (FIXED):** Early `isEmpty()` return + copy constructor in `handleConnections()`
- **P001 (IMPROVED):** `StargateNetwork.updateNetwork()` migration log upgraded to WARN
- **P002 (IMPROVED):** `TransporterNetwork.updateNetwork()` migration log upgraded to WARN
- **P003 (FIXED):** Aggregate UUID error logging; try-catch in `TransporterNetwork.Dimension.deserialize()`

### Phase 1: Security Hardening

- **F001 (FIXED):** Distance check on `ServerboundRingPanelUpdatePacket`
- **F002 (FIXED):** Defensive `loadChunk(false)` in `AbstractTransporterEntity.resetTransporter()`
- **G001 (FIXED):** Distance check on `ServerboundDHDUpdatePacket`
- **G002 (FIXED):** Distance check on `ServerboundInterfaceUpdatePacket`
- **G003 (FIXED):** Distance check on `ServerboundTransceiverUpdatePacket`
- **G004 (FIXED):** Defensive `setChunkForced(false)` in `AbstractStargateEntity.resetStargate()`

### Energy Systems Documentation

- `docs/Power/ENERGY_SYSTEMS.md` — All energy sources, storage, consumers, FE capability matrix, cross-mod compatibility analysis

### Audit Documentation

- Full gameplay-surface audit: ~60 blocks, ~50 items, ~25 BEs, 13 menus, 5 packets, 7 SavedData classes
- 31 findings across F/G/P series (0 Critical, 6 High, 10 Medium, 11 Low, 4 Positive)
- All High-severity and all Top 10 recommendations addressed
- New files: `Inventory.md`, `Rubric.md`, `Findings_Gameplay.md`, `Findings_Packets.md`, `Findings_Persistence.md`

### Ring Privacy Mode (Feature)

- Per-endpoint `discoverable` toggle on Transport Rings (Shift+right-click with empty hand)
- Memory Crystal UUID bypass preserved
- NBT-persisted; filtered in `LocatorHelper.findNearestTransporters()`

### Initial Documentation

- Stargate worldgen, addressing, discovery/maps, Terra design notes
- Transport Rings overview, UI/inventory, discovery/network, limitations
