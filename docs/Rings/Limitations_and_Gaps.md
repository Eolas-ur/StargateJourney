# Transport Rings — Limitations and Gaps

## Confirmed Limitations

These are verified by reading the source code.

### 1. Six-Destination Cap

**Source:** `RingPanelEntity.getNearest6Rings()` (line 161)

```java
for(int i = 0; i < 6 && j < ringsFound; i++, j++)
```

The panel displays at most 6 destinations. Hardcoded in both the entity logic and the `RingPanelScreen` UI (6 buttons, 6 slots).

### 2. No Paging

**Source:** `RingPanelScreen.java` — no scroll widget, page button, or page state variable exists.

If more than 6 ring platforms exist in the dimension, destinations beyond the 6th nearest are inaccessible via the panel's auto-discovery UI.

**Workaround:** Memory Crystals. Players can store a specific transporter's UUID on a Memory Crystal and place it in a slot to target rings beyond the nearest 6.

### 3. No In-Panel Naming

**Source:** `RingPanelScreen.java` and `RingPanelMenu.java` — no text input field or rename widget exists.

Ring platforms can be named via **anvil** (standard Minecraft `setCustomName` mechanic on `AbstractTransporterEntity`, line 151). The Ring Panel GUI has no built-in rename feature.

Named rings display their custom name in Aqua text in the panel. Unnamed rings show coordinates only.

### 4. Same-Dimension Discovery Only (UI)

**Source:** `LocatorHelper.findNearestTransporters()` — filters by `level.dimension()`.

The Ring Panel's auto-discovery only shows transporters in the **same dimension**. Cross-dimensional transport is only possible via Memory Crystals (which store a UUID and bypass dimension filtering via `Transporting.startTransport()`).

### 5. Panel-to-Ring Range: 16 Blocks

**Source:** `RingPanelEntity.setTransportRings()` (line 195)

The Ring Panel must be placed within 16 blocks of its associated Transport Ring platform. If moved further, it loses its association and becomes non-functional.

---

## Potential Issues (Inferred)

These are observations based on code reading, not confirmed bugs.

### Stale Registry Entries

If a Transport Ring block is destroyed without proper cleanup (e.g., explosion, chunk corruption), its entry may persist in `BlockEntityList` and `TransporterNetwork`. The Ring Panel would show a destination that cannot be reached.

**Inference:** `AbstractTransporterEntity.removeStargateFromNetwork()` pattern suggests cleanup exists, but edge cases (non-player destruction) may not trigger it consistently.
- TODO: Verify `setRemoved()` cleanup path in `AbstractTransporterEntity`.

### Memory Crystal Portability

Memory Crystals store a `UUID` reference. If the target transporter is destroyed and a new one placed at the same location, the UUID changes and the crystal becomes invalid.

**Inference:** Based on `MemoryCrystalItem.getFirstUUID()` returning a fixed UUID, and new transporters generating fresh UUIDs in `addTransporterToNetwork()`.

### No Visual Indicator of Range

There is no particle effect, sound, or UI indicator showing whether a Ring Panel is within 16 blocks of a Transport Ring platform. A misplaced panel silently fails.

**Inference:** `setTransportRings()` returns `null` if no ring is found within range, and `activateRings()` silently returns if `transportRings == null` (line 200: `//TODO Tell the player there are no rings connected`). The TODO comment confirms this is a known gap.
