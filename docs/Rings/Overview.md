# Transport Rings — Overview

## Block Classes

### Transport Rings (the ring platform)

- **Block:** `src/main/java/net/povstalec/sgjourney/common/blocks/transporter/TransportRingsBlock.java`
- **Block Entity:** `src/main/java/net/povstalec/sgjourney/common/block_entities/transporter/TransportRingsEntity.java`
- The physical ring platform. Handles entity detection in a 3×3×3 area (above or below, depending on facing) and ring animation.

### Ring Panel (wall-mounted controller)

- **Block:** `src/main/java/net/povstalec/sgjourney/common/blocks/transporter/RingPanelBlock.java`
- **Block Entity:** `src/main/java/net/povstalec/sgjourney/common/block_entities/transporter/RingPanelEntity.java`
- Wall-mounted UI controller. Discovers nearby ring platforms and provides a GUI to select a destination and initiate transport.

### Abstract Base Classes

- **Block:** `src/main/java/net/povstalec/sgjourney/common/blocks/transporter/AbstractTransporterBlock.java`
- **Entity:** `src/main/java/net/povstalec/sgjourney/common/block_entities/transporter/AbstractTransporterEntity.java`
- Base classes for all transporter-type blocks. `AbstractTransporterEntity` handles registration with the `TransporterNetwork` and UUID assignment.

---

## Data Classes

### Transporter Interface

- **File:** `src/main/java/net/povstalec/sgjourney/common/sgjourney/transporter/Transporter.java`
- Interface defining `getName()`, `getBlockPos()`, `getDimension()`, etc.

### SGJourneyTransporter

- **File:** `src/main/java/net/povstalec/sgjourney/common/sgjourney/transporter/SGJourneyTransporter.java`
- Concrete implementation of `Transporter`. Holds `UUID`, `ResourceKey<Level>`, `BlockPos`, and `Component name`. Serializable to/from NBT.

### TransporterConnection

- **File:** `src/main/java/net/povstalec/sgjourney/common/sgjourney/TransporterConnection.java`
- Manages an active transport connection between two `Transporter` endpoints. Handles timing (30–46 ticks) and entity transfer.

### Transporting

- **File:** `src/main/java/net/povstalec/sgjourney/common/sgjourney/Transporting.java`
- Static utility class. `transportTravelers()` moves entities between ring sets. `startTransport()` initiates a connection by UUID.

---

## Network Registry

### TransporterNetwork

- **File:** `src/main/java/net/povstalec/sgjourney/common/data/TransporterNetwork.java`
- Extends `SavedData`. Global registry for all transporters, organized by dimension.
- Saves to `sgjourney-transporter_network.dat`.
- Manages dimension → transporter mappings and active `TransporterConnection` objects.

**Key methods:**
| Method | Line | Description |
|---|---|---|
| `addTransporter(Transporter)` | 126 | Adds a transporter to the dimension map |
| `addTransporter(AbstractTransporterEntity)` | 134 | Creates a `Transporter` via `BlockEntityList`, then adds |
| `getTransportersFromDimension(ResourceKey<Level>)` | 184 | Returns all transporters in a given dimension |
| `createConnection(MinecraftServer, Transporter, Transporter)` | 220 | Creates a new `TransporterConnection` |

### BlockEntityList

- **File:** `src/main/java/net/povstalec/sgjourney/common/data/BlockEntityList.java`
- Shared registry for both stargates and transporters.
- Maps transporter `UUID` to `Transporter` data objects.
- Saves to `sgjourney-block_entities.dat`.

---

## Identification

Each transporter has a unique `UUID` stored in NBT with key `"transporter_id"`.

**File:** `AbstractTransporterEntity.java` (line 35)

```java
public static final String ID = "transporter_id";
```

Generated when the transporter is first added to the network and persisted in the block entity's NBT data.

---

## Comparison with Stargate Network

| Feature | StargateNetwork | TransporterNetwork |
|---|---|---|
| **ID type** | 9-Chevron Address (int[]) | UUID |
| **Registry class** | `StargateNetwork` + `BlockEntityList` | `TransporterNetwork` + `BlockEntityList` |
| **Scope** | Cross-dimensional | Same-dimension (UI); cross-dimensional (by UUID) |
| **Primary gate** | Yes (per Solar System) | No |
| **Connection handling** | `StargateConnection` | `TransporterConnection` |

Both use `BlockEntityList` as the master data store and have their own `SavedData` classes for connection management.
