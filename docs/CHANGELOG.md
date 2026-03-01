# Documentation Changelog

## 2026-03-01 (Update 6)

### Performance & Structural Hygiene Sprint (Phase 2)

All Phase 2 findings documented and marked as fixed/improved in audit docs.

**Code changes (from prior commits, now documented):**
- **G010 (FIXED):** Dirty-flag-driven `updateClient()` across all tech BEs. `EnergyBlockEntity.clientDirty` covers generators, batteries, interfaces, crystallizers. Separate `clientDirty` in `AbstractNaquadahLiquidizerEntity` and `TransceiverEntity`. Throttled sync (every 10 ticks) for energy BEs; immediate for Transceiver.
- **G008 (FIXED):** `max_transmission_scan_chunks` config (default 81, range 9–289) in `CommonTransmissionConfig`. All 4 scan loops in `GDOItem` and `TransceiverEntity` now break when exceeding limit.
- **F004 (FIXED):** Early `isEmpty()` return in `StargateNetwork.handleConnections()` and `TransporterNetwork.handleConnections()`. Copy constructor instead of `putAll()`.
- **P001 (IMPROVED):** `StargateNetwork.updateNetwork()` log upgraded to WARN with descriptive message.
- **P002 (IMPROVED):** `TransporterNetwork.updateNetwork()` log upgraded to WARN with descriptive message.
- **P003 (FIXED):** Aggregate malformed-UUID counting + WARN logging in `BlockEntityList.deserializeTransporters()`, `StargateNetwork.deserializeConnections()`, `TransporterNetwork.deserializeConnections()`. Try-catch added to `TransporterNetwork.Dimension.deserialize()` for previously unguarded `UUID.fromString()`.

**Documentation updates:**
- `Findings_Gameplay.md` — G008 and G010 marked FIXED with fix details
- `Findings.md` — F004 marked FIXED with fix details
- `Findings_Persistence.md` — P001/P002 marked IMPROVED, P003 marked FIXED
- `Audit/README.md` — Top 10 table updated (9 of 10 now fixed/improved; only G012 remains open)

**Build:** Successful. Only pre-existing deprecation warnings (10× `@EventBusSubscriber`).

---

## 2026-03-01 (Update 5)

### Security Hardening: All High-Severity Findings Fixed

All 4 High-severity gameplay findings (G001–G004) are now fixed in code and documented.

**Code changes:**
- `ServerboundDHDUpdatePacket.handle()` — Added distance check (64.0 sq / 8 blocks) before any world access (G001)
- `ServerboundInterfaceUpdatePacket.handle()` — Added distance check before level/BE access (G002)
- `ServerboundTransceiverUpdatePacket.handle()` — Added distance check before `getBlockEntity()` (G003)
- `AbstractStargateEntity.resetStargate()` — Added defensive `setChunkForced(false)` guarded by `FORCE_LOAD_CHUNK && connectionID != null` (G004)

**Documentation updates:**
- `Findings_Gameplay.md` — G001–G004 marked FIXED with fix details and code snippets
- `Findings_Packets.md` — Summary table updated (all packets now have distance checks), detailed sections updated to FIXED status, recommendations updated
- `Test_Plan.md` — Added test entries for G001–G004
- `README.md` — Top 10 table updated with FIXED status; statistics updated
- `replit.md` — Added security hardening summary

**Build:** Successful. Only pre-existing deprecation warnings (10× `@EventBusSubscriber` deprecation).

---

## 2026-03-01 (Update 4)

### Full Gameplay Surface Audit

Comprehensive audit of all registered blocks, items, block entities, menus, packets, and SavedData classes.

**New documentation:**
- `docs/Audit/Inventory.md` — Complete registry of ~60 blocks, ~50 items, ~25 BEs, 13 menus, 5 serverbound packets, 7 SavedData classes
- `docs/Audit/Rubric.md` — Consistent audit checks for blocks, items, BEs, and packets
- `docs/Audit/Findings_Gameplay.md` — 15 gameplay findings (G001–G015)
- `docs/Audit/Findings_Packets.md` — Global packet validation matrix with cross-references
- `docs/Audit/Findings_Persistence.md` — 7 persistence/NBT findings (P001–P007)
- `docs/Audit/README.md` — Updated scope, methodology, top 10 recommendations, statistics

**Key High-severity findings:**
- G001: DHD packet has no distance check (same pattern as fixed F001)
- G002: Interface packet has no distance check
- G003: Transceiver packet has no distance check
- G004: Stargate chunk force-loading has same vulnerability as fixed F002

**Statistics:** 31 total findings across F/G/P series. 0 Critical, 4 High (now all fixed), 10 Medium, 11 Low.

---

## 2026-03-01 (Update 3)

### Audit Fixes: F001 and F002

**F001 — Ring Panel Packet Distance Check (High → Fixed)**
- Added server-side squared-distance check (threshold 64.0, i.e. 8 blocks) to `ServerboundRingPanelUpdatePacket.handle()`.
- Packets from beyond 8 blocks are silently dropped before any world access occurs.
- File: `ServerboundRingPanelUpdatePacket.java`

**F002 — Chunk Force-Loading Release on Block Destruction (High → Fixed)**
- Added defensive `loadChunk(false)` call in `AbstractTransporterEntity.resetTransporter()`, guarded by `connectionID != null && level != null && !level.isClientSide()`.
- Guarantees forced chunks are released even if the block entity is inaccessible during the `TransporterConnection.terminate()` → `SGJourneyTransporter.reset()` call chain.
- File: `AbstractTransporterEntity.java`

**Documentation updated:**
- `docs/Audit/Findings.md` — F001 and F002 marked as FIXED with applied fix details
- `docs/Audit/Test_Plan.md` — F001 and F002 updated with verification notes

---

## 2026-03-01 (Update 2)

### Ring Privacy Mode

Added per-endpoint `discoverable` toggle to Transport Rings. When set to `false`, the ring endpoint is hidden from Ring Panel auto-discovery lists. Memory Crystal UUID-based targeting continues to work regardless.

**Files modified:**
- `AbstractTransporterEntity.java` — Added `discoverable` field, NBT persistence, getter/setter, PDA status output
- `SGJourneyTransporter.java` — Added `discoverable` field, NBT serialization, interface implementation
- `Transporter.java` — Added `isDiscoverable()` interface method
- `LocatorHelper.java` — Added `!transporter.isDiscoverable()` filter in `findNearestTransporters()`
- `TransportRingsBlock.java` — Added Shift+right-click toggle interaction via `useWithoutItem()`

**Documentation added:**
- `docs/Rings/Discovery_and_Network.md` — New section: "Privacy Mode (discoverable flag)"
- `docs/Rings/Limitations_and_Gaps.md` — New section: "What Privacy Mode Does NOT Solve"

### Stability and Performance Audit

Created targeted audit documentation covering registries, chunk forcing, tick cost, and packet validation.

**Documentation added:**
- `docs/Audit/README.md` — Audit methodology
- `docs/Audit/Findings.md` — 9 findings (F001–F009) with severity ratings
- `docs/Audit/Test_Plan.md` — Manual test procedures for each finding

**Key findings:**
- F001 (High): Ring Panel packet has no server-side distance check
- F002 (High): Chunk force-loading may not be released when ring block is destroyed during active connection
- F003 (Medium): Registry entries can grow unboundedly with data corruption
- F004 (Medium): Connection handler copies entire HashMap every tick, even when idle
- F008/F009 (Positive): Block destruction cleanup for both Stargates and Rings is properly implemented

---

## 2026-03-01

### Initial Documentation

- Created `docs/` directory structure.
- **Stargate/Worldgen.md** — Documented all structure sets, placement types, biome restrictions, dimension applicability, and the pedestal competition problem. Confirmed Terra is not in the `stargate_map` tag.
- **Stargate/Addressing_and_Network.md** — Documented address generation algorithm (`generate9ChevronAddress`), NBT storage, network registration chain, and survival methods for reading addresses (PDA, CC:Tweaked).
- **Stargate/Discovery_and_Maps.md** — Documented Archeologist profession, Level 1 and Level 5 trades, `TreasureMapForEmeraldsTrade` logic, and complete tag chain analysis.
- **Stargate/Terra_Design_Notes.md** — Documented the Terra gate problem (not locatable via map trade) and proposed solutions.
- **Rings/Overview.md** — Documented all ring-related classes, block entities, and the `TransporterNetwork` registry.
- **Rings/UI_and_Inventory.md** — Documented `RingPanelScreen`, `RingPanelMenu`, 6-slot inventory, Memory Crystal mechanics, and destination display.
- **Rings/Discovery_and_Network.md** — Documented registry-based discovery, chunk force-loading during transport, and NBT persistence.
- **Rings/Limitations_and_Gaps.md** — Listed confirmed UX limitations (6-destination cap, no paging, naming via anvil only).
