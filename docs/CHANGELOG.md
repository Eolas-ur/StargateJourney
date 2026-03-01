# Documentation Changelog

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
