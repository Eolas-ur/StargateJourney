# Stargate Journey — Energy Systems

Technical documentation of all energy sources, storage, and distribution in sgjourney v0.6.44.

---

## Architecture Overview

All energy-handling block entities extend `EnergyBlockEntity`, which wraps `SGJourneyEnergy` — a custom implementation of NeoForge's `IEnergyStorage`. Internally, the mod uses `long` values for energy (supporting values far beyond the 32-bit `int` limit of vanilla FE). The standard `IEnergyStorage` methods (`getEnergyStored`, `extractEnergy`, `receiveEnergy`) cap at `Integer.MAX_VALUE` via `SGJourneyEnergy.regularEnergy()`.

All block entities register `Capabilities.EnergyStorage.BLOCK` in `StargateJourney.onRegisterCapabilities()` (line ~207–234). The capability delegates to `blockEntity.getEnergyHandler(direction)`, which returns the `IEnergyStorage` wrapper if `isCorrectEnergySide(direction)` is true, otherwise `null`.

**Key base classes:**
- `SGJourneyEnergy` — `IEnergyStorage` + `INBTSerializable<Tag>`, `long`-based internal storage
- `EnergyBlockEntity` — abstract BE with `capacity()`, `maxReceive()`, `maxExtract()`, `outputsEnergy()`, `receivesEnergy()`, `isCorrectEnergySide()`
- `CableBlockEntity` — energy passthrough via `ConduitNetwork.transferEnergy()`

**Citations:**
- `src/main/java/net/povstalec/sgjourney/common/capabilities/SGJourneyEnergy.java`
- `src/main/java/net/povstalec/sgjourney/common/block_entities/tech/EnergyBlockEntity.java`
- `src/main/java/net/povstalec/sgjourney/StargateJourney.java` (lines 207–234)

---

## Energy Sources

### Naquadah Generator Mark I

| Property | Value | Source |
|----------|-------|--------|
| Registry | `sgjourney:naquadah_generator_mark_i` | `BlockEntityInit` |
| Block Entity | `NaquadahGeneratorMarkIEntity` extends `NaquadahGeneratorEntity` | |
| Capacity | 100,000 FE (config) | `CommonNaquadahGeneratorConfig.naquadah_generator_mark_i_capacity` |
| Generation | 1,000 FE/tick (config) | `CommonNaquadahGeneratorConfig.naquadah_generator_mark_i_energy_per_tick` |
| Max Extract | 100,000 FE/tick (config) | `CommonNaquadahGeneratorConfig.naquadah_generator_mark_i_max_energy` |
| Fuel | Naquadah Fuel Rod, 1 unit per reaction (100 ticks) | `NaquadahFuelRodItem.depleteFuel()` |
| Output Sides | Bottom + Clockwise + Counter-Clockwise (relative to facing) | `NaquadahGeneratorEntity.isCorrectEnergySide()` |

**Generation logic:** `NaquadahGeneratorEntity.tick()` → `doReaction()`: if active and has fuel, consumes 1 fuel unit per `reactionTime` ticks, calling `generateEnergy(energyPerTick)` each tick of the reaction. Then pushes energy via `outputEnergy()` on 3 sides.

**FE extraction:** `canExtract()` → `outputsEnergy()` → `getMaxExtract() > 0` → **true**. `receivesEnergy()` → **false** (overridden). External mods can pull energy from valid sides.

**Citations:** `NaquadahGeneratorEntity.java` (lines 230–286), `NaquadahGeneratorMarkIEntity.java`

### Naquadah Generator Mark II

| Property | Value | Source |
|----------|-------|--------|
| Registry | `sgjourney:naquadah_generator_mark_ii` | `BlockEntityInit` |
| Capacity | 1,200,000 FE (config) | `CommonNaquadahGeneratorConfig.naquadah_generator_mark_ii_capacity` |
| Generation | 1,200 FE/tick (config) | `CommonNaquadahGeneratorConfig.naquadah_generator_mark_ii_energy_per_tick` |
| Max Extract | 1,000,000 FE/tick (config) | `CommonNaquadahGeneratorConfig.naquadah_generator_mark_ii_max_energy` |
| Fuel | Same as Mark I | |

Same logic and capability exposure as Mark I. Higher tier.

**Citations:** `NaquadahGeneratorMarkIIEntity.java`

---

### ZPM Hub (+ ZPM Item)

| Property | Value | Source |
|----------|-------|--------|
| Registry | `sgjourney:zpm_hub` | `BlockEntityInit.ZPM_HUB` |
| Block Entity | `ZPMHubEntity` extends `EnergyBlockEntity` | |
| ZPM Item | `sgjourney:zpm` — `ZeroPointModule` | `ItemInit.ZPM` |
| ZPM Capacity | 1000 entropy levels × 100B FE/level = **100 Trillion FE** (config) | `ZeroPointEnergy`, `CommonZPMConfig` |
| Hub Max Transfer | 100B FE/tick (config) | `CommonZPMConfig.zpm_hub_max_transfer` |
| Hub Capacity | 100B FE (display only, one entropy level) | `CommonZPMConfig.zpm_energy_per_level_of_entropy` |
| Output Side | **DOWN only** | `ZPMHubEntity.isCorrectEnergySide()` |

**How it works:** The ZPM Hub holds one ZPM item in its inventory. On tick, it calls `outputEnergy(Direction.DOWN)`. The overridden `outputEnergy()` extracts energy from the ZPM item's `ZeroPointEnergy` capability and pushes it to the block below.

**Cross-mod output:** The ZPM Hub has **explicit cross-mod support** via config:
- If the block below has `SGJourneyEnergy`, it uses `receiveZeroPointEnergy()` with `long` values.
- If the block below is a foreign `IEnergyStorage` (e.g., Mekanism cable), it checks `CommonZPMConfig.other_mods_use_zero_point_energy` (default: **false**). If enabled, converts to `int` FE and pushes.
- The ZPM Hub's `canExtract()` returns **true** (`maxExtract > 0`), so external mods can also **pull** energy from the DOWN face.

**ZPM Item:** Implements `IEnergyStorage` via `ZeroPointModule.Energy` (registered as `Capabilities.EnergyStorage.ITEM`). Non-rechargeable (`receiveEnergy()` returns 0). `extractEnergy()` works and depletes entropy levels.

**Citations:** `ZPMHubEntity.java` (lines 187–226), `ZeroPointEnergy.java`, `ZeroPointModule.java`, `CommonZPMConfig.java`

---

### Fusion Core (Item Only)

| Property | Value | Source |
|----------|-------|--------|
| Registry | `sgjourney:fusion_core` | `ItemInit.FUSION_CORE` |
| Type | **Item** (not a block) | `FusionCoreItem` |
| Interface | `IEnergyCore` | |
| Fuel Field | `DataComponentInit.FUSION_FUEL` | |
| Gen Rate | `CommonTechConfig.fusion_core_energy_from_fuel` (config) | |
| Capacity | `CommonTechConfig.fusion_core_fuel_capacity` (config) | |
| Infinite Mode | `CommonTechConfig.fusion_core_infinite_energy` (config) | |

**How it works:** The Fusion Core is a consumable item that slots into DHDs (and potentially other machines). It does **not** expose `IEnergyStorage` — it uses the mod-internal `IEnergyCore` interface. The DHD's `tryStoreEnergy()` calls `energyCore.generateEnergy()` to pull power from the item into the DHD's internal buffer.

**Cross-mod:** The Fusion Core is **internal only**. It cannot be used as an energy source by external mods. It has no NeoForge energy capability registration.

**Citations:** `FusionCoreItem.java`, `IEnergyCore.java`, `AbstractDHDEntity.java`

---

### Naquadah Generator Core (Item Only)

| Property | Value | Source |
|----------|-------|--------|
| Registry | `sgjourney:naquadah_generator_core` | `ItemInit.NAQUADAH_GENERATOR_CORE` |
| Type | **Item** (not a block) | `NaquadahGeneratorCoreItem` |
| Interface | `IEnergyCore` | |
| Gen Rate | Uses Mark I config values | `CommonNaquadahGeneratorConfig` |

**How it works:** Similar to Fusion Core. Slots into DHDs. Uses `IEnergyCore` interface. No FE capability.

**Cross-mod:** Internal only.

**Citations:** `NaquadahGeneratorCoreItem.java`

---

## Energy Storage

### Large Naquadah Battery

| Property | Value | Source |
|----------|-------|--------|
| Registry | `sgjourney:large_naquadah_battery` | `BlockEntityInit.LARGE_NAQUADAH_BATTERY` |
| Block Entity | `BatteryBlockEntity.Naquadah` extends `BatteryBlockEntity` | |
| Capacity | `CommonTechConfig.large_naquadah_battery_capacity` (config) | |
| Max Transfer | `CommonTechConfig.large_naquadah_battery_max_transfer` (config, both in and out) | |
| Output Sides | **All sides** (no `isCorrectEnergySide` override) | |

**How it works:** A passive energy buffer. On tick, drains energy from item in slot 0 (`extractItemEnergy`) and fills item in slot 1 (`fillItemEnergy`). Does not generate energy — only stores and transfers.

**FE extraction:** `canExtract()` → **true** (maxExtract > 0). `canReceive()` → **true** (maxReceive > 0). External mods can push to and pull from all sides.

**Citations:** `BatteryBlockEntity.java` (lines 146–169)

---

### Energy Items

| Item | Registry | FE Capability | Can Extract | Can Receive |
|------|----------|:---:|:---:|:---:|
| Energy Crystal | `sgjourney:energy_crystal` | Yes | Yes | Yes |
| Advanced Energy Crystal | `sgjourney:advanced_energy_crystal` | Yes | Yes | Yes |
| Naquadah Power Cell | `sgjourney:naquadah_power_cell` | Yes | Yes | Yes |
| ZPM | `sgjourney:zpm` | Yes | Yes | **No** (non-rechargeable) |

All registered via `Capabilities.EnergyStorage.ITEM` in `StargateJourney.onRegisterCapabilities()` (lines 186–189).

---

## Energy Consumers

### Stargates

All stargate types (`universe`, `milky_way`, `pegasus`, `tollan`, `classic`) extend `AbstractStargateEntity` which extends `EnergyBlockEntity`. They register `Capabilities.EnergyStorage.BLOCK` and can receive energy but do **not** output it. `canExtract()` returns **false** by default since `maxExtract()` returns 0.

### DHDs

All DHD types (`milky_way_dhd`, `pegasus_dhd`, `classic_dhd`) extend `AbstractDHDEntity` which extends `EnergyBlockEntity`. They use `IEnergyCore` items (Fusion Core / Naquadah Generator Core) to generate power internally. They expose `Capabilities.EnergyStorage.BLOCK` and can receive external FE.

### Transport Rings

Registry `sgjourney:transport_rings`. Extends `EnergyBlockEntity`. Receives energy. Registered for `Capabilities.EnergyStorage.BLOCK`.

### Tech Interfaces

Basic, Crystal, and Advanced Crystal Interfaces extend `AbstractInterfaceEntity`. They both **input and output** energy. `outputsEnergy()` returns **true**, `receivesEnergy()` returns **true**. They act as energy bridges between external power and connected Stargates (with a configurable energy target). All sides except the front face expose energy.

---

## FE Capability Matrix

| Block/Item | `IEnergyStorage` Registered | `canExtract()` | `canReceive()` | Output Sides | Cross-Mod Compatible |
|------------|:---:|:---:|:---:|---|:---:|
| Naquadah Generator Mk I | Yes (Block) | **Yes** | No | Bottom, CW, CCW | **Yes — works now** |
| Naquadah Generator Mk II | Yes (Block) | **Yes** | No | Bottom, CW, CCW | **Yes — works now** |
| ZPM Hub | Yes (Block) | **Yes** | No | DOWN only | **Yes** (push: config-gated; pull: always) |
| Large Naquadah Battery | Yes (Block) | **Yes** | **Yes** | All sides | **Yes — works now** |
| Tech Interfaces | Yes (Block) | **Yes** | **Yes** | All except front | **Yes — works now** |
| Cables | Yes (Block) | No | **Yes** (receive only) | All connected | **Yes — input bridge** |
| Stargates | Yes (Block) | No | **Yes** | All sides | **Yes — receive only** |
| DHDs | Yes (Block) | Depends | **Yes** | Varies | Receive only |
| Transport Rings | Yes (Block) | Depends | **Yes** | Varies | Receive only |
| Fusion Core | **No** | N/A | N/A | N/A | **No — internal only** |
| Naq. Gen. Core | **No** | N/A | N/A | N/A | **No — internal only** |
| ZPM (item) | Yes (Item) | **Yes** | No | N/A | **Yes** |
| Energy Crystal | Yes (Item) | **Yes** | **Yes** | N/A | **Yes** |
| Adv. Energy Crystal | Yes (Item) | **Yes** | **Yes** | N/A | **Yes** |
| Naq. Power Cell | Yes (Item) | **Yes** | **Yes** | N/A | **Yes** |

---

## Cross-Mod Compatibility Summary

### Already Works

The following energy sources **already expose standard NeoForge `IEnergyStorage` with `canExtract() == true`** and can be used by Mekanism cables, Thermal Dynamics ducts, or any mod that queries `Capabilities.EnergyStorage.BLOCK`:

1. **Naquadah Generator Mark I** — external mods can pull up to 100K FE/tick from bottom/side faces
2. **Naquadah Generator Mark II** — external mods can pull up to 1M FE/tick from bottom/side faces
3. **Large Naquadah Battery** — bidirectional, all sides, configurable max transfer
4. **Tech Interfaces** — bidirectional, all sides except front
5. **ZPM Hub** — pull works on DOWN face always; push to foreign blocks requires `other_mods_use_zero_point_energy` config = true

### Internal Only (No Change Needed)

The Fusion Core and Naquadah Generator Core are **items that slot into DHDs**. They use the internal `IEnergyCore` interface, not FE. This is by design — they are fuel cells, not standalone generators. No FE exposure is appropriate.

### ZPM Hub Push Behaviour

The ZPM Hub already supports pushing energy to foreign mods, but it is **config-gated** behind `CommonZPMConfig.other_mods_use_zero_point_energy` (default: **false**). This is the only barrier to full cross-mod ZPM power. The config exists to let server admins control whether the extremely high ZPM output (100B FE/tick) should flow into other mods' networks.

To enable: set `other_mods_use_zero_point_energy = true` in the server config. No code change required.

### No Code Changes Needed

All primary energy generators and storage blocks already implement and register `IEnergyStorage` with working `extractEnergy()`. The mod is **already cross-mod compatible** for energy output. The only restriction is the ZPM Hub push config, which is intentional game balance.

---

## Config Keys Involved

| Config Key | Class | Default | Purpose |
|------------|-------|---------|---------|
| `naquadah_generator_mark_i_capacity` | `CommonNaquadahGeneratorConfig` | 100,000 | Mk I buffer size |
| `naquadah_generator_mark_i_energy_per_tick` | `CommonNaquadahGeneratorConfig` | 1,000 | Mk I gen rate |
| `naquadah_generator_mark_i_max_energy` | `CommonNaquadahGeneratorConfig` | 100,000 | Mk I max extract/tick |
| `naquadah_generator_mark_i_reaction_time` | `CommonNaquadahGeneratorConfig` | 100 | Ticks per fuel unit |
| `naquadah_generator_mark_ii_capacity` | `CommonNaquadahGeneratorConfig` | 1,200,000 | Mk II buffer size |
| `naquadah_generator_mark_ii_energy_per_tick` | `CommonNaquadahGeneratorConfig` | 1,200 | Mk II gen rate |
| `naquadah_generator_mark_ii_max_energy` | `CommonNaquadahGeneratorConfig` | 1,000,000 | Mk II max extract/tick |
| `naquadah_generator_mark_ii_reaction_time` | `CommonNaquadahGeneratorConfig` | 150 | Ticks per fuel unit |
| `zpm_hub_max_transfer` | `CommonZPMConfig` | 100,000,000,000 | ZPM Hub max FE/tick |
| `zpm_energy_per_level_of_entropy` | `CommonZPMConfig` | 100,000,000,000 | FE per entropy level |
| `other_mods_use_zero_point_energy` | `CommonZPMConfig` | false | Allow ZPM push to foreign blocks |
| `tech_uses_zero_point_energy` | `CommonZPMConfig` | (config) | Allow ZPE receive by tech BEs |
| `large_naquadah_battery_capacity` | `CommonTechConfig` | (config) | Battery capacity |
| `large_naquadah_battery_max_transfer` | `CommonTechConfig` | (config) | Battery max in/out per tick |
| `fusion_core_fuel_capacity` | `CommonTechConfig` | (config) | Fusion Core max fuel |
| `fusion_core_energy_from_fuel` | `CommonTechConfig` | (config) | FE per fuel unit |
| `fusion_core_infinite_energy` | `CommonTechConfig` | false | Infinite fuel mode |
