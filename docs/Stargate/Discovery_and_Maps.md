# Stargate Discovery and Maps

## Archeologist Villager Profession

**File:** `src/main/java/net/povstalec/sgjourney/common/init/VillagerInit.java`

- **Profession:** `sgjourney:archeologist`
- **POI Block:** `sgjourney:archeology_table` (`BlockInit.ARCHEOLOGY_TABLE`)
- **Work Sound:** `SoundEvents.VILLAGER_WORK_CARTOGRAPHER`

---

## Trades

All trades registered in `ForgeEvents.addCustomTrades()`.
**File:** `src/main/java/net/povstalec/sgjourney/common/events/ForgeEvents.java` (lines 391–507)

### Level 1

| Trade | Cost | Result | Max Uses |
|---|---|---|---|
| Paper trade | 20 Paper | 1 Emerald | 4 |
| Golden Idol trade | 1 Golden Idol | 5 Emeralds | 4 |
| **Goa'uld Temple Map** | 8 Emeralds + 1 Compass | Explorer Map | 1 |

The Goa'uld Temple Map uses `TreasureMapForEmeraldsTrade` (base class) with tag `TagInit.Structures.ON_ARCHEOLOGIST_MAPS`.

### Level 2

| Trade | Cost | Result | Max Uses |
|---|---|---|---|
| Compass | 4 Emeralds | 1 Compass | 4 |
| Writable Book | 4 Emeralds | 1 Writable Book | 4 |
| Gold trade | 3 Gold Ingots | 1 Emerald | 4 |

### Level 3

| Trade | Cost | Result | Max Uses |
|---|---|---|---|
| Fire Pit | 3 Emeralds | 4 Fire Pits | 1 |
| Hieroglyphs trade | 3 Sandstone Hieroglyphs | 1 Emerald | 4 |
| Sandstone with Lapis | 4 Emeralds | 3 Sandstone with Lapis | 4 |

### Level 4

Various symbol block trades (Stone Symbol, Sandstone Symbol, Red Sandstone Symbol) and Bone trade.

### Level 5

| Trade | Cost | Result | Max Uses |
|---|---|---|---|
| **Chappa'ai Map** | 8 Emeralds + 1 Compass | Stargate Explorer Map | 1 |

---

## Map Trade Mechanics

### TreasureMapForEmeraldsTrade

**File:** `src/main/java/net/povstalec/sgjourney/common/misc/TreasureMapForEmeraldsTrade.java`

Base class. Calls `level.findNearestMapStructure(this.destination, entity.blockPosition(), 100, true)` to locate the nearest structure matching the given tag within 100 chunks of the villager.

### StargateMapTrade (inner class)

**File:** Same file, line 74

Overrides `getOffer()` to:
1. Calculate center offset from config: `CommonGenerationConfig.stargate_generation_center_x_chunk_offset * 16` (x and z).
2. Search from that center position (not from the villager's position).
3. Search radius: **150 chunks** (2400 blocks).
4. Uses tag: `TagInit.Structures.STARGATE_MAP` → `sgjourney:stargate_map`.
5. Creates a `MapItem` with `RED_X` decoration and translatable name `"filled_map.sgjourney.chappa_ai"`.

---

## Tag Chain Analysis

### `sgjourney:stargate_map`

**File:** `src/main/resources/data/sgjourney/tags/worldgen/structure/stargate_map.json`

```
#sgjourney:buried_stargate
#sgjourney:universe_stargate_pedestal
#sgjourney:milky_way_stargate_pedestal
#sgjourney:stargate_temple
```

### Sub-tag: `milky_way_stargate_pedestal`

**File:** `src/main/resources/data/sgjourney/tags/worldgen/structure/milky_way_stargate_pedestal.json`

Contains 12 pedestal variants. **Does NOT contain `terra_stargate`.**

### Sub-tag: `buried_stargate`

Contains 3 buried stargate variants. No Terra.

### Sub-tag: `stargate_temple`

Contains Milky Way temples. No Terra.

### Sub-tag: `universe_stargate_pedestal`

Contains `universe_stargate_pedestal_island_end`. No Terra.

---

## Conclusion: Terra Is Not Locatable Via Map

`sgjourney:stargate/milky_way/terra_stargate` is not present in any sub-tag of `stargate_map`. The Archeologist Level 5 trade's `findNearestMapStructure()` call will **never** locate a Terra Stargate.

### `sgjourney:on_archeologist_maps`

**File:** `src/main/resources/data/sgjourney/tags/worldgen/structure/on_archeologist_maps.json`

Contains `#sgjourney:goauld_temple` only. Used by the Level 1 Goa'uld Temple Map trade. Also does not include Terra.
