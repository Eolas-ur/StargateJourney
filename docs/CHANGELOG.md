# Documentation Changelog

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
