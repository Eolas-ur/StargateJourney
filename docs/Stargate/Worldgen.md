# Stargate Worldgen

## Data Namespaces

The mod uses two data namespaces for worldgen:

- **`sgjourney`** — Primary mod data. Always active.
- **`common_stargates`** — Optional cross-mod compatibility layer. Disabled by default (`common_stargate_generation = false` in `CommonGenerationConfig`).

This document covers the `sgjourney` namespace only.

---

## Structure Sets

All files under `src/main/resources/data/sgjourney/worldgen/structure_set/`.

### 1. `stargate_pedestal.json`

The primary stargate structure set for the mod. Contains all surface pedestal gates, including Terra.

- **Placement type:** `sgjourney:stargate_placement`
- **Salt:** 1646217471
- **Bounds:** Configurable via `stargate_generation_x_bound` / `z_bound` (default: 64 chunks = 1024 blocks from center)
- **Center offset:** Configurable via `stargate_generation_center_x_chunk_offset` / `z_chunk_offset` (default: 0)
- **Java class:** `UniqueStructurePlacement.Stargate` in `src/main/java/net/povstalec/sgjourney/common/world/UniqueStructurePlacement.java`
- **Behaviour:** Places exactly **one structure** from the set per dimension within the configured bounds. The position is chosen randomly, and the structure is selected based on biome match at that position.

**Members (all weight 1):**

Each structure references a biome tag under `data/sgjourney/tags/worldgen/biome/has_structure/`. Many vanilla-biome variants ship with **empty biome tags** by default, meaning they are disabled unless a datapack populates the tag. This is intentional — it allows server operators and modpack makers to selectively enable biome-specific pedestals.

| Structure ID | Biome Tag | Default Biomes | Notes |
|---|---|---|---|
| `sgjourney:stargate/milky_way/terra_stargate` | `terra_stargate_biomes` | `#minecraft:is_overworld` | All Overworld biomes |
| `sgjourney:stargate/milky_way/pedestal/stargate_pedestal` | `stargate_pedestal/stargate_pedestal_biomes` | `sgjourney:milky_way_plains`, `sgjourney:milky_way_forest` | Custom mod biomes only |
| `sgjourney:stargate/milky_way/pedestal/stargate_pedestal_deepslate` | `stargate_pedestal/stargate_pedestal_deepslate_biomes` | *(empty)* | Disabled by default |
| `sgjourney:stargate/milky_way/pedestal/stargate_pedestal_badlands` | `stargate_pedestal/stargate_pedestal_badlands_biomes` | *(empty)* | Disabled by default |
| `sgjourney:stargate/milky_way/pedestal/stargate_pedestal_desert` | `stargate_pedestal/stargate_pedestal_desert_biomes` | *(empty)* | Disabled by default |
| `sgjourney:stargate/milky_way/pedestal/stargate_pedestal_jungle` | `stargate_pedestal/stargate_pedestal_jungle_biomes` | *(empty)* | Disabled by default |
| `sgjourney:stargate/milky_way/pedestal/stargate_pedestal_mushroom` | `stargate_pedestal/stargate_pedestal_mushroom_biomes` | *(empty)* | Disabled by default |
| `sgjourney:stargate/milky_way/pedestal/stargate_pedestal_snow` | `stargate_pedestal/stargate_pedestal_snow_biomes` | *(empty)* | Disabled by default |
| `sgjourney:stargate/milky_way/pedestal/stargate_pedestal_deep_dark` | `stargate_pedestal/stargate_pedestal_deep_dark_biomes` | *(empty)* | Disabled by default |
| `sgjourney:stargate/milky_way/pedestal/stargate_pedestal_chulak` | `stargate_pedestal/stargate_pedestal_chulak_biomes` | `#sgjourney:is_chulak` | Chulak dimension |
| `sgjourney:stargate/milky_way/pedestal/stargate_pedestal_unitas` | `stargate_pedestal/stargate_pedestal_unitas_biomes` | `#sgjourney:is_unitas` | Unitas dimension |
| `sgjourney:stargate/milky_way/pedestal/stargate_pedestal_rima` | `stargate_pedestal/stargate_pedestal_rima_biomes` | `#sgjourney:is_rima` | Rima dimension |
| `sgjourney:stargate/milky_way/pedestal/stargate_pedestal_cavum_tenebrae` | `stargate_pedestal/stargate_pedestal_cavum_tenebrae_biomes` | `#sgjourney:is_cavum_tenebrae` | Cavum Tenebrae dimension |
| `sgjourney:stargate/milky_way/pedestal/stargate_pedestal_glacio` | `stargate_pedestal/stargate_pedestal_glacio_biomes` | Ad Astra glacio biomes | Optional; `required: false` |
| `sgjourney:stargate/classic/pedestal/stargate_pedestal` | *(unknown)* | *(likely empty)* | Classic variant, disabled |
| `sgjourney:stargate/pegasus/pegasus_stargate` | `pegasus_stargate_biomes` | *(check tag)* | Athos dimension |

#### Default Overworld Behaviour

In a vanilla install with no additional datapacks:
- **Terra** (`#minecraft:is_overworld`) is the **only** structure in this set that matches vanilla Overworld biomes.
- The biome-specific vanilla pedestals (badlands, desert, jungle, mushroom, snow, deepslate, deep_dark) all ship with **empty** biome tags and will never match.
- The generic `stargate_pedestal` only matches `sgjourney:milky_way_plains` / `milky_way_forest` (custom mod dimension biomes, not the vanilla Overworld).
- Therefore, in a vanilla Overworld with default config, **Terra is guaranteed** to be the selected Overworld gate from this set.

#### With Datapacks

If a datapack or modpack populates the empty biome tags (e.g., adds `minecraft:desert` to `stargate_pedestal_desert_biomes`), those variants become active and compete with Terra. All structures have equal weight (1), so at a given spawn position, every structure whose biome tag matches enters a random draw. In that scenario:
- In a desert biome: both Terra and `stargate_pedestal_desert` would match → 50% each.
- In a jungle biome: Terra, `stargate_pedestal`, and `stargate_pedestal_jungle` could match → 33% each.

### 2. `buried_stargate.json`

- **Placement type:** `sgjourney:buried_stargate_placement`
- **Salt:** 14324351
- **Bounds:** Configurable via `buried_stargate_generation_x_bound` / `z_bound` (default: 64 chunks)
- **Java class:** `UniqueStructurePlacement.BuriedStargate` (extends `Stargate`)

**Members:**

| Structure ID | Biome Tag | Default Biomes |
|---|---|---|
| `sgjourney:stargate/milky_way/buried/buried_stargate` | `buried_stargate/buried_stargate_biomes` | Extensive vanilla Overworld list (beaches, forests, hills, jungles, mountains, oceans, rivers, savannas, taigas, mushroom fields, plains, caves, deep dark, etc.) |
| `sgjourney:stargate/milky_way/buried/buried_stargate_desert` | `buried_stargate/buried_stargate_desert_biomes` | `minecraft:desert` |
| `sgjourney:stargate/milky_way/buried/buried_stargate_badlands` | `buried_stargate/buried_stargate_badlands_biomes` | `#minecraft:is_badlands` |

All placed at `start_height: -5` projected to `OCEAN_FLOOR_WG`. Always below surface. Unlike pedestals, buried stargates have **populated** biome tags by default and will generate in most Overworld biomes.

### 3. `stargate_temple.json`

- **Placement type:** `sgjourney:stargate_placement`
- **Salt:** 1543892866

**Members:**

| Structure ID | Biomes | Height |
|---|---|---|
| `sgjourney:stargate/milky_way/temple/abydos_pyramid` | `#sgjourney:is_abydos` | Y=0 → `WORLD_SURFACE_WG` |
| `sgjourney:stargate/milky_way/temple/stargate_temple_nether` | `#minecraft:is_nether` | Uniform Y=33–70 |

**Unused structure:** `sgjourney:stargate/milky_way/temple/great_pyramid` exists in the `worldgen/structure/` directory with biome tag `great_pyramid_biomes`, but is **not referenced** by any structure set or tag. It appears to be an unused/reserved structure.

### 4. `end_stargate_pedestal.json`

- **Placement type:** `sgjourney:unique_placement`
- **Salt:** 148317432
- **Bounds:** x_bound_min=8, z_bound_min=8, x_bound_max=12, z_bound_max=12 (128–192 blocks from origin)

**Members:**

| Structure ID | Biomes | Height |
|---|---|---|
| `sgjourney:stargate/universe/pedestal/universe_stargate_pedestal_island_end` | `#minecraft:is_end` | Uniform Y=5–10 |

### 5. `destiny.json`

- **Placement type:** `sgjourney:unique_placement`
- **Salt:** 8615654
- **Fixed position:** chunk (0, 0), Y=0
- **Biomes:** `sgjourney:destiny`

One structure only: `sgjourney:stargate/universe/destiny`.

### 6. `goauld_temple.json`

- **Placement type:** `minecraft:random_spread`
- **Salt:** 1224862866
- **Spacing:** 48 chunks, **Separation:** 32 chunks

**Members:**

| Structure ID | Biomes |
|---|---|
| `sgjourney:goauld_temple/desert_pyramid/desert_pyramid` | `sgjourney:abydos_desert` |
| `sgjourney:goauld_temple/desert_pyramid/abandoned/desert_pyramid_abandoned` | `minecraft:desert` |
| `sgjourney:goauld_temple/jungle_pyramid/jungle_pyramid` | `#minecraft:is_jungle` |
| `sgjourney:goauld_temple/badlands_ziggurat/badlands_ziggurat` | `#minecraft:is_badlands` |

### 7. `cartouche.json`

- **Placement type:** `minecraft:random_spread`
- **Salt:** 29878650
- **Spacing:** 24 chunks, **Separation:** 16 chunks

Contains cartouche monuments for Chulak, Abydos, and Overworld biomes.

### 8. `city.json`

- **Placement type:** `minecraft:random_spread`
- **Salt:** 23448872
- **Spacing:** 48 chunks, **Separation:** 32 chunks
- Contains: `sgjourney:city/abydos_city` (Abydos desert biome only)

### 9. `stargate_outpost.json`

Contains `sgjourney:stargate/pegasus/outpost/lantean_outpost_ocean` for Lantea dimension.

---

## Tag Inclusion Summary

### `sgjourney:stargate_map` (used by Archeologist Lv.5 trade)

**File:** `src/main/resources/data/sgjourney/tags/worldgen/structure/stargate_map.json`

```
#sgjourney:buried_stargate
#sgjourney:universe_stargate_pedestal
#sgjourney:milky_way_stargate_pedestal
#sgjourney:stargate_temple
```

**Structures included via these sub-tags:**
- All 3 buried stargates
- `universe_stargate_pedestal_island_end`
- 12 Milky Way pedestal variants (biome-specific)
- Stargate temples (Abydos pyramid, Nether temple)

**Structures NOT in `stargate_map`:**
- **`sgjourney:stargate/milky_way/terra_stargate`** — Not in any sub-tag
- `sgjourney:stargate/classic/pedestal/stargate_pedestal` — Not included
- `sgjourney:stargate/pegasus/pegasus_stargate` — Not included
- `sgjourney:stargate/universe/destiny` — Not included
- All Goa'uld temples — Not included (separate tag: `on_archeologist_maps`)
- Cartouche monuments — Not included

### `sgjourney:on_archeologist_maps` (used by Archeologist Lv.1 trade)

**File:** `src/main/resources/data/sgjourney/tags/worldgen/structure/on_archeologist_maps.json`

Contains: `#sgjourney:goauld_temple`

---

## Custom Dimensions

All defined under `src/main/resources/data/sgjourney/dimension/`.

| Dimension | Biomes |
|---|---|
| `sgjourney:abydos` | `abydos_desert`, `abydos_spires`, `abydos_oasis` |
| `sgjourney:chulak` | `chulak_plains`, `chulak_forest` |
| `sgjourney:lantea` | `lantean_deep_ocean` |
| `sgjourney:athos` | `athos_forest` |
| `sgjourney:cavum_tenebrae` | `cavum_tenebrae_shattered_crust` |
| `sgjourney:rima` | `rima_cracks`, `rima_shattered_plains` |
| `sgjourney:unitas` | `unitas_desert` |
