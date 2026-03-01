# Stability, Security, and Performance Audit

## Scope

Full gameplay-surface audit of the Stargate Journey mod (v0.6.44, MC 1.21.1) covering:

- All registered Blocks, Items, BlockEntities, Menus/Screens, Packets, and SavedData
- Security: packet validation, distance checks, server authority
- Stability: chunk forcing, registry cleanup, null safety, NBT integrity
- Performance: tick cost, allocations, scans in hot paths

## How Inventory Was Derived

Registry initialization classes were enumerated via code search:
- `BlockInit.java` — all `DeferredRegister<Block>` entries
- `ItemInit.java` — all `DeferredRegister<Item>` entries
- `BlockEntityInit.java` — all `DeferredRegister<BlockEntityType>` entries
- `MenuInit.java` — all `DeferredRegister<MenuType>` entries
- `PacketHandlerInit.java` — all packet registrations

Each registered object was traced to its implementation class and checked against the rubric.

## How Findings Were Prioritised

Severity follows the rubric in `Rubric.md`:
- **Critical:** Remote exploit or server crash trivial to trigger
- **High:** Exploit requires some conditions, or chunk leak, or unbounded growth
- **Medium:** Performance inefficiency, minor dupe risk, rare crash
- **Low:** Cosmetic, minor UX, non-impactful

## Output Files

| File | Contents |
|------|----------|
| `Inventory.md` | Complete registry of all blocks, items, BEs, menus, packets, SavedData |
| `Rubric.md` | Consistent checks applied to every object |
| `Findings.md` | Original targeted findings (F001–F009), F001 and F002 fixed |
| `Findings_Gameplay.md` | Full gameplay audit (G001–G015) |
| `Findings_Packets.md` | Global packet audit with validation matrix |
| `Findings_Persistence.md` | NBT/SavedData audit (P001–P007) |
| `Test_Plan.md` | Manual reproduction steps |

## Top 10 Recommended Fixes (by Severity / ROI)

| Priority | ID | Title | Severity | Effort |
|---|---|---|---|---|
| 1 | G001 | Add distance check to DHD packet | High | 2 lines |
| 2 | G002 | Add distance check to Interface packet | High | 2 lines |
| 3 | G003 | Add distance check to Transceiver packet | High | 2 lines |
| 4 | G004 | Defensive chunk unforce on stargate destruction | High | ~5 lines |
| 5 | G010 | Dirty-flag `updateClient()` in tech BEs | Medium | ~20 lines |
| 6 | P001 | Log warning on StargateNetwork version migration | Medium | 1 line |
| 7 | P003 | Clean orphaned UUID entries in BlockEntityList | Medium | ~5 lines |
| 8 | G008 | Cap GDO/Transceiver transmission scan radius | Medium | ~3 lines |
| 9 | G012 | Defer cable BFS to end-of-tick | Medium | ~15 lines |
| 10 | F004 | Guard `handleConnections()` with `isEmpty()` | Medium | 1 line |

## Statistics

- **Objects audited:** ~60 blocks, ~50 items, ~25 block entities, 13 menus, 5 serverbound packets, 7 SavedData classes
- **Findings:** 31 total (across F/G/P series)
  - Critical: 0
  - High: 4 (G001–G004; F001 and F002 already fixed)
  - Medium: 10
  - Low: 11
  - Positive/Informational: 6
