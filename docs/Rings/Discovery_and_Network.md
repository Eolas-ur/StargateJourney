# Transport Rings — Discovery and Network

## Discovery Mechanism

Ring discovery is **registry-based**, not scan-based.

### Registration

**File:** `AbstractTransporterEntity.java`

- `addTransporterToNetwork()` (line 125) is called during `onLoad()` (line 241).
- This registers the transporter in `TransporterNetwork` and `BlockEntityList`.
- Each transporter is assigned a unique `UUID` (stored as `"transporter_id"` in NBT).

### Network Lookup

**File:** `TransporterNetwork.java`

- `getTransportersFromDimension(ResourceKey<Level>)` (line 184) returns all registered transporters in a given dimension.
- Data is read from the `TransporterNetwork` SavedData, not from scanning loaded chunks.

### Panel Discovery

**File:** `RingPanelEntity.java`

1. **Local ring association:** `setTransportRings()` (line 190) calls `findNearestTransportRings(16)` — scans a **16-block radius** for `TransportRingsEntity` block entities to find the panel's own ring platform.

2. **Destination discovery:** `getNearest6Rings()` (line 146) calls `LocatorHelper.findNearestTransporters(level, pos)`.

### LocatorHelper

**File:** `src/main/java/net/povstalec/sgjourney/common/misc/LocatorHelper.java` (line 70)

```java
public static List<Transporter> findNearestTransporters(ServerLevel level, BlockPos centerPos) {
    List<Transporter> transporters = TransporterNetwork.get(level).getTransportersFromDimension(level.dimension());
    transporters.sort(Comparator.comparing(transporter -> CoordinateHelper.Relative.distance(centerPos, transporter.getBlockPos())));
    // removes invalid entries
    return transporters;
}
```

- Queries the global registry — **not a chunk scan**.
- Returns all transporters in the same dimension, sorted by distance.
- No hardcoded range limit on the search itself; the UI caps display at 6.

---

## Range Constants

| Scope | Range | Source |
|---|---|---|
| Panel → Local Ring | 16 blocks | `RingPanelEntity.setTransportRings()` — line 195 |
| Panel → Target Rings | **Dimension-wide** (nearest 6 shown) | `LocatorHelper.findNearestTransporters()` — no range cap |

---

## Chunk Loading

**File:** `AbstractTransporterEntity.java` (line 209)

```java
protected void loadChunk(boolean load) {
    level.getServer().getLevel(level.dimension()).setChunkForced(
        SectionPos.blockToSectionCoord(this.getBlockPos().getX()),
        SectionPos.blockToSectionCoord(this.getBlockPos().getZ()),
        load
    );
}
```

- When a connection is established, both endpoints call `loadChunk(true)`, force-loading their chunks via `ServerLevel.setChunkForced`.
- When the connection terminates, `loadChunk(false)` releases the chunk.
- This ensures the destination rings are loaded during the transport animation and entity teleportation.

**Discovery does not require chunk loading.** The `TransporterNetwork` registry stores BlockPos and dimension data persistently. Rings can be discovered even if their chunk is unloaded. However, the actual transport requires the destination chunk to be loaded (handled automatically).

---

## Persistence

### Endpoint Data

- **Stored in:** `BlockEntityList` (`sgjourney-block_entities.dat`)
- **Contents per transporter:** UUID, BlockPos, Dimension, custom name
- **Persistence:** Fully persisted in NBT. Not recalculated by world scan.
- **Updated when:** A transporter is placed, loaded, or removed.

### Active Connections

- **Stored in:** `TransporterNetwork` (`sgjourney-transporter_network.dat`)
- **Persistence:** Active `TransporterConnection` objects are serialized to NBT, allowing in-progress transports to survive server restarts.

### Ring Panel State

- `ringsPos` and `ringsName` arrays on `RingPanelEntity` are **transient** — refreshed from the registry on demand.
- Memory Crystal contents in slots are persisted via the standard inventory NBT of the block entity.
