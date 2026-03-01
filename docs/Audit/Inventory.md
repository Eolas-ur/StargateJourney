# Gameplay Surface Inventory

Derived from `DeferredRegister` usage in `BlockInit`, `ItemInit`, `BlockEntityInit`, `MenuInit`, and `PacketHandlerInit`.

---

## 1. Blocks

### Stargates and Rings

| Registry Name | Class | Special Behaviour |
|---|---|---|
| `universe_stargate` | `UniverseStargateBlock` | Multiblock base, ticking, chunk forcing, networking |
| `universe_ring` | `UniverseStargateRingBlock` | Multiblock part |
| `milky_way_stargate` | `MilkyWayStargateBlock` | Multiblock base, ticking, chunk forcing, networking |
| `milky_way_ring` | `MilkyWayStargateRingBlock` | Multiblock part |
| `pegasus_stargate` | `PegasusStargateBlock` | Multiblock base, ticking, chunk forcing, networking |
| `pegasus_ring` | `PegasusStargateRingBlock` | Multiblock part |
| `classic_stargate` | `ClassicStargateBlock` | Multiblock base, ticking, chunk forcing, networking |
| `classic_ring` | `ClassicStargateRingBlock` | Multiblock part |
| `tollan_stargate` | `TollanStargateBlock` | Multiblock base, ticking, chunk forcing, networking |
| `tollan_ring` | `TollanStargateRingBlock` | Multiblock part |

### Shielding (Iris/Shield)

| Registry Name | Class | Special Behaviour |
|---|---|---|
| Various shielding blocks | `AbstractShieldingBlock` subclasses | Dynamic collision shape, onRemove cleanup |

### DHDs (Dial-Home Devices)

| Registry Name | Class | Special Behaviour |
|---|---|---|
| `milky_way_dhd` | `MilkyWayDHDBlock` | use(), inventory, networking |
| `pegasus_dhd` | `PegasusDHDBlock` | use(), inventory, networking |
| `classic_dhd` | `ClassicDHDBlock` | use(), inventory, networking |

### Transport

| Registry Name | Class | Special Behaviour |
|---|---|---|
| `transport_rings` | `TransportRingsBlock` | useWithoutItem() (privacy toggle), ticking, chunk forcing, networking |
| `ring_panel` | `RingPanelBlock` | use(), inventory, container/GUI |

### Tech / Machines

| Registry Name | Class | Special Behaviour |
|---|---|---|
| `naquadah_generator_mark_i` | `NaquadahGeneratorBlock` | Ticking, inventory, energy production |
| `naquadah_generator_mark_ii` | `NaquadahGeneratorBlock` | Ticking, inventory, energy production |
| `basic_interface` | `BasicInterfaceBlock` | Ticking, energy transfer, CC:Tweaked peripheral, container/GUI |
| `crystal_interface` | `CrystalInterfaceBlock` | Ticking, energy transfer, CC:Tweaked peripheral, container/GUI |
| `advanced_crystal_interface` | `AdvancedCrystalInterfaceBlock` | Ticking, energy transfer, CC:Tweaked peripheral, container/GUI |
| `crystallizer` | `CrystallizerBlock` | Ticking, inventory, fluid handling |
| `advanced_crystallizer` | `AdvancedCrystallizerBlock` | Ticking, inventory, fluid handling |
| `naquadah_liquidizer` | `NaquadahLiquidizerBlock` | Ticking, inventory, fluid handling |
| `heavy_naquadah_liquidizer` | `HeavyNaquadahLiquidizerBlock` | Ticking, inventory, fluid handling |
| `zpm_hub` | `ZPMHubBlock` | Ticking, inventory, energy output |
| `ancient_gene_detector` | `ATAGeneDetectorBlock` | use(), scheduled tick, redstone output |

### Cables / Conduits

| Registry Name | Class | Special Behaviour |
|---|---|---|
| `naquadah_wire` | `CableBlock` | Conduit network, BFS on place/break |
| Various cable types | `CableBlock` subclasses | Conduit network, BFS on place/break |

### Communication

| Registry Name | Class | Special Behaviour |
|---|---|---|
| `transceiver` | `TransceiverBlock` | Ticking, transmission, redstone, container/GUI |

### Decorative / Lore

| Registry Name | Class | Special Behaviour |
|---|---|---|
| `sandstone_cartouche` | `CartoucheBlock` | Entity interaction |
| `stone_cartouche` | `CartoucheBlock` | Entity interaction |
| `sandstone_symbol` | `SymbolBlock` | Decorative |
| `stone_symbol` | `SymbolBlock` | Decorative |
| `archeology_table` | `ArcheologyTableBlock` | use() |

### Resources / Ores

| Registry Name | Class | Special Behaviour |
|---|---|---|
| `naquadah_ore` | Standard ore block | None |
| `nether_naquadah_ore` | Standard ore block | None |
| `deepslate_naquadah_ore` | Standard ore block | None |
| `raw_naquadah_block` | Standard block | None |
| `pure_naquadah_block` | Standard block | None |
| `naquadah_block` | Standard block | None |
| `liquid_naquadah` | `LiquidBlock` | Fluid |
| `heavy_liquid_naquadah` | `LiquidBlock` | Fluid |

---

## 2. Items

### Weapons

| Registry Name | Class | Special Behaviour |
|---|---|---|
| `matok` | `StaffWeaponItem` | use() (shoot/toggle/reload), projectile spawning, melee damage modifier, cooldown |
| `kara_kesh` | `KaraKeshItem` | use() (mode toggle), interactLivingEntity() (knockback/terror), GoauldTech requirement |

### Tools / Devices

| Registry Name | Class | Special Behaviour |
|---|---|---|
| `gdo` | `GDOItem` | use() (check shielding / open GUI), packet-based transmission |
| `pda` | `PDAItem` | useOn() (scan block), use() (scan self), interactLivingEntity() (scan entity) |
| `auto_dialer` | `AutoDialerItem` | use() (connect/disconnect nearest stargate) |
| `ring_remote` | `RingRemoteItem` | use() (trigger transport / manage inventory) |

### Armor

| Registry Name | Class | Special Behaviour |
|---|---|---|
| `personal_shield_emitter` | `PersonalShieldItem` | Passive damage cancel, projectile reflection, fuel drain (ForgeEvents) |
| `jaffa_helmet/chestplate/leggings/boots` | `ArmorItem` | Standard armor |
| `system_lord_helmet/chestplate/leggings/boots` | `ArmorItem` | Standard armor |
| `jackal_helmet` | `ArmorItem` | Cosmetic variant |
| `falcon_helmet` | `ArmorItem` | Cosmetic variant |
| `naquadah_helmet/chestplate/leggings/boots` | `ArmorItem` | Standard armor |

### Tools (Naquadah)

| Registry Name | Class | Special Behaviour |
|---|---|---|
| `naquadah_sword/pickaxe/axe/shovel/hoe` | Standard tool items | Attribute modifiers |

### Irises and Shields

| Registry Name | Class | Special Behaviour |
|---|---|---|
| `copper_iris` through `trinium_iris` | `StargateIrisItem` | Durability-based, placed into stargate |
| Shield variants | `StargateShieldItem` | Energy-based shielding |

### Crystals

| Registry Name | Class | Special Behaviour |
|---|---|---|
| `control_crystal` | `ControlCrystalItem` | DHD component |
| `memory_crystal` | `MemoryCrystalItem` | Stores transporter UUID, used in Ring Panel / Ring Remote |
| `energy_crystal` | `EnergyCrystalItem` | DHD energy component |
| `transfer_crystal` | `TransferCrystalItem` | DHD energy transfer |
| `communication_crystal` | `CommunicationCrystalItem` | DHD communication |
| Advanced variants | Respective `*Item` classes | Higher-tier versions |

### Materials

| Registry Name | Class | Special Behaviour |
|---|---|---|
| `raw_naquadah` | Standard item | None |
| `naquadah_alloy` | Standard item | None |
| `refined_naquadah` | Standard item | None |
| `pure_naquadah` | Standard item | None |
| `naquadah_rod` | Standard item | Fuel rod for generators |
| `zero_point_module` | `ZPMItem` | Energy storage item (ZPE) |
| `vial` | `VialItem` | Fluid container (Liquid Naquadah) |
| `syringe` | `SyringeItem` | Gene injection |
| `call_forwarding_device` | `CallForwardingDevice` | DHD component (tooltip only) |

---

## 3. Block Entities

| Registry Name | Class | Systems |
|---|---|---|
| `universe_stargate` | `UniverseStargateEntity` | BlockEntityList, StargateNetwork, tick, chunk forcing |
| `milky_way_stargate` | `MilkyWayStargateEntity` | BlockEntityList, StargateNetwork, tick, chunk forcing |
| `pegasus_stargate` | `PegasusStargateEntity` | BlockEntityList, StargateNetwork, tick, chunk forcing |
| `classic_stargate` | `ClassicStargateEntity` | BlockEntityList, StargateNetwork, tick, chunk forcing |
| `tollan_stargate` | `TollanStargateEntity` | BlockEntityList, StargateNetwork, tick, chunk forcing |
| `milky_way_dhd` | `MilkyWayDHDEntity` | tick (stargate search), inventory |
| `pegasus_dhd` | `PegasusDHDEntity` | tick (stargate search), inventory |
| `classic_dhd` | `ClassicDHDEntity` | tick (stargate search), inventory |
| `transport_rings` | `TransportRingsEntity` | BlockEntityList, TransporterNetwork, tick, chunk forcing |
| `ring_panel` | `RingPanelEntity` | inventory, networking |
| `naquadah_generator_mark_i` | `NaquadahGeneratorMarkIEntity` | tick (energy production), inventory |
| `naquadah_generator_mark_ii` | `NaquadahGeneratorMarkIIEntity` | tick (energy production), inventory |
| `basic_interface` | `BasicInterfaceEntity` | tick (energy transfer, iris/rotation), CC:Tweaked peripheral |
| `crystal_interface` | `CrystalInterfaceEntity` | tick (energy transfer, iris/rotation), CC:Tweaked peripheral |
| `advanced_crystal_interface` | `AdvancedCrystalInterfaceEntity` | tick (energy transfer, iris/rotation), CC:Tweaked peripheral |
| `crystallizer` | `CrystallizerEntity` | tick (recipe processing), inventory, fluid |
| `advanced_crystallizer` | `AdvancedCrystallizerEntity` | tick (recipe processing), inventory, fluid |
| `naquadah_liquidizer` | `NaquadahLiquidizerEntity` | tick (fluid processing), inventory, fluid |
| `heavy_naquadah_liquidizer` | `HeavyNaquadahLiquidizerEntity` | tick (fluid processing), inventory, fluid |
| `zpm_hub` | `ZPMHubEntity` | tick (energy output), inventory |
| `transceiver` | `TransceiverEntity` | tick (transmission timing), transmission |
| `naquadah_battery` | `BatteryBlockEntity` | tick (energy transfer), inventory |
| `cartouche` variants | `CartoucheEntity` | NBT storage |
| `symbol` variants | `SymbolEntity` | NBT storage |
| Cable types | `CableBlockEntity` | ConduitNetworks (energy routing) |

---

## 4. Menus and Screens

| Menu Class | Screen Class | Serverbound Packets |
|---|---|---|
| `InterfaceMenu` | `InterfaceScreen` | `ServerboundInterfaceUpdatePacket` |
| `RingPanelMenu` | `RingPanelScreen` | `ServerboundRingPanelUpdatePacket` |
| `DHDCrystalMenu` | `DHDCrystalScreen` | None (inventory only) |
| `MilkyWayDHDMenu` | `MilkyWayDHDScreen` | `ServerboundDHDUpdatePacket` |
| `PegasusDHDMenu` | `PegasusDHDScreen` | `ServerboundDHDUpdatePacket` |
| `ClassicDHDMenu` | `ClassicDHDScreen` | `ServerboundDHDUpdatePacket` |
| `NaquadahGeneratorMenu` | `NaquadahGeneratorScreen` | None (inventory only) |
| `ZPMHubMenu` | `ZPMHubScreen` | None (inventory only) |
| `LiquidizerMenu.LiquidNaquadah` | `LiquidizerScreen.LiquidNaquadah` | None (inventory only) |
| `LiquidizerMenu.HeavyLiquidNaquadah` | `LiquidizerScreen.HeavyLiquidNaquadah` | None (inventory only) |
| `CrystallizerMenu` | `CrystallizerScreen` | None (inventory only) |
| `TransceiverMenu` | `TransceiverScreen` | `ServerboundTransceiverUpdatePacket` |
| `BatteryMenu` | `BatteryScreen` | None (inventory only) |

### Independent Screens (no server Menu)

| Screen Class | Opens Via |
|---|---|
| `GDOScreen` | `ClientboundGDOOpenScreenPacket` |
| `DialerScreen` | `ClientboundDialerOpenScreenPacket` |
| `ArcheologistNotebookScreen` | `ClientboundArcheologistNotebookOpenScreenPacket` |

---

## 5. Packets

### Serverbound (Client → Server)

| Packet Class | Action | Parameters |
|---|---|---|
| `ServerboundDHDUpdatePacket` | Engage chevron on DHD | `BlockPos`, `int symbol` |
| `ServerboundRingPanelUpdatePacket` | Activate ring transport | `BlockPos`, `int number` |
| `ServerboundInterfaceUpdatePacket` | Update interface energy/mode | `BlockPos`, `long energyTarget`, `InterfaceMode` |
| `ServerboundGDOUpdatePacket` | Update GDO IDC/frequency, transmit | `boolean mainHand`, `String idc`, `int frequency`, `boolean transmit` |
| `ServerboundTransceiverUpdatePacket` | Manage transceiver codes/transmit | `BlockPos`, `boolean remove`, `boolean toggleFrequency`, `int number`, `boolean transmit` |

### Clientbound (Server → Client)

| Packet Class | Action |
|---|---|
| `ClientboundDialerOpenScreenPacket` | Open Dialer UI |
| `ClientboundGDOOpenScreenPacket` | Open GDO UI |
| `ClientboundArcheologistNotebookOpenScreenPacket` | Open Notebook UI |
| `ClientboundRingPanelUpdatePacket` | Update ring panel destination list |
| `ClientboundStargateParticleSpawnPacket` | Spawn kawoosh/wormhole particles |
| `ClientBoundSoundPackets.*` | Play various sounds (wormhole open/close/idle, chevron, iris thud, rotation) |

---

## 6. SavedData and Persistent Stores

| Class | File Name | Purpose | NBT Keys |
|---|---|---|---|
| `StargateNetwork` | `sgjourney-stargate_network` | Active stargate connections, version | `version` (int), `connections` (CompoundTag) |
| `TransporterNetwork` | `sgjourney-transporter_network` | Transporter connections, dimension map | `version` (int), `dimensions` (CompoundTag), `connections` (CompoundTag) |
| `Universe` | `sgjourney-universe` | Solar systems, galaxies, dimension map | `solar_systems`, `dimensions`, `galaxies` |
| `BlockEntityList` | `sgjourney-block_entities` | Global stargate/transporter registry | `stargates` (by 9-chevron address), `transporters` (by UUID) |
| `StargateNetworkSettings` | `sgjourney-stargate_network_settings` | Network-wide config overrides | `use_datapack_addresses`, `generate_random_solarSystems`, `random_address_from_seed` |
| `ConduitNetworks` | `sgjourney-conduits` | Cable energy networks | `cables` (by network int ID, contains BlockPos arrays) |
| `Factions` | `sgjourney-factions` | Faction data (stub) | `goauld` |
