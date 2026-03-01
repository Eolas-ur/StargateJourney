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

---

## Privacy Mode (discoverable flag)

### Overview

Each Transport Ring endpoint has a `discoverable` boolean flag that controls whether it appears in Ring Panel auto-discovery lists. When set to `false` (private), the endpoint is hidden from all nearby panels but remains accessible via Memory Crystal (UUID binding).

### Where the Flag Lives

- **Block Entity field:** `AbstractTransporterEntity.discoverable` (default: `true`)
  - File: `src/main/java/net/povstalec/sgjourney/common/block_entities/transporter/AbstractTransporterEntity.java`
  - NBT key: `"discoverable"` — persisted in `saveAdditional()` / `loadAdditional()`
- **Data object field:** `SGJourneyTransporter.discoverable` (default: `true`)
  - File: `src/main/java/net/povstalec/sgjourney/common/sgjourney/transporter/SGJourneyTransporter.java`
  - NBT key: `"Discoverable"` — persisted in `serializeNBT()` / `deserializeNBT()`
  - Propagated from block entity via constructor: `SGJourneyTransporter(AbstractTransporterEntity)`
- **Interface method:** `Transporter.isDiscoverable()` (returns `boolean`)
  - File: `src/main/java/net/povstalec/sgjourney/common/sgjourney/transporter/Transporter.java`

### Default Behaviour

All existing and new Transport Rings default to `discoverable = true`. This is fully backwards compatible — no existing world data is affected. The NBT load uses a conditional check: if the `"discoverable"` key is absent (old data), the field remains `true`.

### How It Affects Panel Listing

The filtering occurs **server-side** in `LocatorHelper.findNearestTransporters()`:

**File:** `src/main/java/net/povstalec/sgjourney/common/misc/LocatorHelper.java` (line 70)

The `removeIf` predicate now includes:
```java
if(!transporter.isDiscoverable())
    return true;
```

This removes non-discoverable endpoints from the list **before** it is sorted, truncated to 6, and sent to the client. The client never sees private endpoints in the Ring Panel GUI.

### How Memory Crystals Bypass Listing

In `RingPanelEntity.activateRings()` (line 198), when a Memory Crystal is present in the selected slot, the code takes a separate path:

```java
if(stack.getItem() instanceof MemoryCrystalItem) {
    UUID uuid = MemoryCrystalItem.getFirstUUID(stack);
    Transporting.startTransport(level.getServer(), transportRings.getTransporter(), uuid);
}
```

This path uses the UUID directly from the crystal. It does **not** consult `LocatorHelper` or the `ringsPos` list. The `discoverable` flag is never checked in this path. Therefore, Memory Crystal transport works regardless of the destination's privacy setting.

### How to Toggle in Survival

**Interaction:** Shift + right-click the Transport Rings block with an empty main hand.

**File:** `src/main/java/net/povstalec/sgjourney/common/blocks/transporter/TransportRingsBlock.java` — `useWithoutItem()` override.

Requirements:
- Player must be **sneaking** (Shift key held)
- Player's **main hand must be empty** (no item held)
- Both conditions prevent accidental toggling

Feedback:
- `"Transport Rings set to: Discoverable"` (green text) — when toggled ON
- `"Transport Rings set to: Private"` (red text) — when toggled OFF

The toggle is **server-authoritative** — the `useWithoutItem` override checks `!level.isClientSide()` before modifying state.

When toggled, `setDiscoverable()` re-registers the transporter with the network (remove + add) to ensure the cached `SGJourneyTransporter` data object in `TransporterNetwork` and `BlockEntityList` reflects the updated state immediately. This avoids stale visibility in the discovery filter.

### Limitations

- Privacy mode only hides from Ring Panel auto-discovery. It does **not** prevent transport via Memory Crystal or programmatic UUID targeting.
- The flag is per-endpoint, not per-player. All players see the same visibility.
- There is no visual indicator on the ring block itself showing whether it is private (no blockstate change, no particle effect). The status can be checked via PDA (`getStatus()` now reports "Discoverable: true/false").
