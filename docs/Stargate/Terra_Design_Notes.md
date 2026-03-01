# Terra Stargate — Design Notes

> **Status:** Design commentary and proposal. Not implemented.

---

## Current Problem

The Terra Stargate (`sgjourney:stargate/milky_way/terra_stargate`) has two significant issues:

### 1. Not Locatable By Any Survival Tool

- Terra is **not included** in the `sgjourney:stargate_map` tag.
- The Archeologist Level 5 villager trade ("Chappa'ai Map") uses `stargate_map` for its `findNearestMapStructure()` call.
- Therefore, **no map trade can locate Terra**.
- No other survival-accessible scanner, compass, or locator item exists in the mod.
- The only way to find Terra in survival is underground exploration/mining.

**Evidence:**
- `src/main/resources/data/sgjourney/tags/worldgen/structure/stargate_map.json` — Terra absent from all sub-tags.
- `src/main/resources/data/sgjourney/tags/worldgen/structure/milky_way_stargate_pedestal.json` — 12 entries, none are Terra.
- `src/main/java/net/povstalec/sgjourney/common/misc/TreasureMapForEmeraldsTrade.java` — uses `STARGATE_MAP` tag.

### 2. Shares Structure Set With Pedestal Variants

Terra is in the same structure set as all other pedestals (`stargate_pedestal.json`). Since only one structure from the set spawns per dimension, Terra competes with any other structure whose biome tag matches at the chosen position.

**In a default install:** The biome-specific vanilla pedestal variants (badlands, desert, jungle, mushroom, snow, deepslate, deep_dark) all ship with **empty** biome tags and will not compete. The generic `stargate_pedestal` only matches `sgjourney:milky_way_plains` and `sgjourney:milky_way_forest` (custom dimension biomes). Therefore, **Terra is guaranteed** to be the Overworld gate by default.

**With datapacks:** If a datapack populates the empty biome tags, those variants become active and compete with Terra. All structures have weight 1, so the chance of Terra being selected decreases proportionally.

**Evidence:**
- `src/main/resources/data/sgjourney/worldgen/structure_set/stargate_pedestal.json` — Terra is listed at weight 1 alongside 14 other structures.
- `src/main/resources/data/sgjourney/tags/worldgen/biome/has_structure/terra_stargate_biomes.json` — matches `#minecraft:is_overworld`.
- `src/main/resources/data/sgjourney/tags/worldgen/biome/has_structure/stargate_pedestal/` — Most vanilla-biome variants have empty `values` arrays.

### 3. Buried 20 Blocks Underground

- `start_height: {"absolute": -20}` projected to `OCEAN_FLOOR_WG`.
- Always below surface, regardless of terrain.
- With `terrain_adaptation: beard_thin`, the terrain above is smoothed but does not expose the gate.
- A player would need to mine at approximately Y=40–80 (depending on terrain) within 1024 blocks of world center.

**Evidence:**
- `src/main/resources/data/sgjourney/worldgen/structure/stargate/milky_way/terra_stargate.json`

---

## Proposed Solutions

### Option A: Separate Structure Set

Create `stargate_terra.json` in `worldgen/structure_set/` with its own placement, ensuring Terra always generates independently of the pedestal set.

- Remove `terra_stargate` from `stargate_pedestal.json`.
- Create `stargate_terra.json` with `sgjourney:stargate_placement` or `sgjourney:unique_placement`.
- This guarantees one Terra gate per Overworld.

### Option B: Dedicated Terra Map Trade

Add `terra_stargate` to the `stargate_map` tag, or create a dedicated tag and trade:

- Add `sgjourney:stargate/milky_way/terra_stargate` to `milky_way_stargate_pedestal.json`, or
- Create a new tag `sgjourney:terra_stargate` and add a new Archeologist trade tier that uses it.

### Option C: Both

Separate the structure set (Option A) AND make it locatable (Option B). This provides the strongest guarantee that Terra exists and can be found.

---

## Summary

| Issue | Severity | Root Cause |
|---|---|---|
| Not in `stargate_map` tag | High | Omission from tag file |
| Shares structure set (risk with datapacks) | Low | Shared structure set; no competition by default |
| Buried with no indicator | Low | Intentional design (lore-accurate) |
