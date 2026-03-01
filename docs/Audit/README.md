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
| `Findings.md` | Original targeted findings (F001–F009), F001/F002/F004 fixed |
| `Findings_Gameplay.md` | Full gameplay audit (G001–G015) |
| `Findings_Packets.md` | Global packet audit with validation matrix |
| `Findings_Persistence.md` | NBT/SavedData audit (P001–P007) |
| `Test_Plan.md` | Manual reproduction steps |

## Top 10 Recommended Fixes (by Severity / ROI)

| Priority | ID | Title | Severity | Status |
|---|---|---|---|---|
| 1 | G001 | Distance check on DHD packet | High | **FIXED** |
| 2 | G002 | Distance check on Interface packet | High | **FIXED** |
| 3 | G003 | Distance check on Transceiver packet | High | **FIXED** |
| 4 | G004 | Defensive chunk unforce on stargate destruction | High | **FIXED** |
| 5 | G010 | Dirty-flag `updateClient()` in tech BEs | Medium | **FIXED** |
| 6 | P001 | Log warning on StargateNetwork version migration | Medium | **IMPROVED** |
| 7 | P003 | Clean orphaned UUID entries in BlockEntityList | Medium | **FIXED** |
| 8 | G008 | Cap GDO/Transceiver transmission scan radius | Medium | **FIXED** |
| 9 | G012 | Defer cable BFS to end-of-tick | Medium | Open |
| 10 | F004 | Guard `handleConnections()` with `isEmpty()` | Medium | **FIXED** |

## Statistics

- **Objects audited:** ~60 blocks, ~50 items, ~25 block entities, 13 menus, 5 serverbound packets, 7 SavedData classes
- **Findings:** 31 total (across F/G/P series)
  - Critical: 0
  - High: 4 — **all fixed** (G001–G004; plus F001 and F002 from earlier sprint)
  - Medium: 10
  - Low: 11
  - Positive/Informational: 6
