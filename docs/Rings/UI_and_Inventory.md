# Transport Rings — UI and Inventory

## Screen Class

- **File:** `src/main/java/net/povstalec/sgjourney/client/screens/RingPanelScreen.java`
- Extends `AbstractContainerScreen<RingPanelMenu>`
- Texture: `sgjourney:textures/gui/ring_panel_gui.png`
- Renders 6 `RingPanelButton` widgets in a 2×3 grid (starting at x+51, y+48)

### Button Widget

- **File:** `src/main/java/net/povstalec/sgjourney/client/widgets/RingPanelButton.java`
- Custom button for each destination slot. Label is set from `menu.getRingsPos(index)`.

---

## Menu Class

- **File:** `src/main/java/net/povstalec/sgjourney/common/menu/RingPanelMenu.java`
- Extends `InventoryMenu`
- Manages 6 inventory slots and transport activation via `ServerboundRingPanelUpdatePacket`

### Destination Labels

`RingPanelMenu.getRingsPos(index)` returns a `Component` for each button:
- If the transporter has a custom name: displays name in **Aqua** + coordinates in **Dark Green**
- If no name: displays coordinates only in **Dark Green** (formatted as `[x y z]`)

---

## Inventory Slots

### Configuration

**File:** `RingPanelEntity.java` (lines 106–141, via `createHandler()`)

6 slots are created in the `RingPanelMenu` (lines 43–48).

| Slot Index | Valid Items | Stack Size | Purpose |
|---|---|---|---|
| 0–5 | `ItemInit.MEMORY_CRYSTAL` only | 1 | Override destination for corresponding button |

### Item Validation

```java
// RingPanelEntity.java, line 119
return stack.getItem() == ItemInit.MEMORY_CRYSTAL.get();
```

Only `MemoryCrystalItem` instances are accepted. Stack limit is 1 per slot (line 125).

### Memory Crystal Mechanics

**File:** `src/main/java/net/povstalec/sgjourney/common/items/crystals/MemoryCrystalItem.java`

- Stores a `UUID` reference to a specific transporter.
- When a Memory Crystal is present in a slot, clicking the corresponding button transports to the crystal's stored UUID target — overriding the nearest-ring discovery.
- `MemoryCrystalItem.getFirstUUID(stack)` (line 246) extracts the stored UUID from the item's NBT.

**File:** `RingPanelEntity.java` (lines 203–209):
```java
if(stack.getItem() instanceof MemoryCrystalItem) {
    UUID uuid = MemoryCrystalItem.getFirstUUID(stack);
    Transporting.startTransport(level.getServer(), transportRings.getTransporter(), uuid);
}
```

---

## Destination Tiles

### Source

**File:** `RingPanelEntity.getNearest6Rings()` (line 146)

1. Calls `LocatorHelper.findNearestTransporters(level, pos)` — retrieves all transporters in the current dimension from `TransporterNetwork`, sorted by distance.
2. Skips the first result if it matches the panel's own rings (self-avoidance, line 158).
3. Takes the next **6 nearest** transporters.
4. Stores their positions in `ringsPos` and names in `ringsName`.
5. Syncs to client via `ClientboundRingPanelUpdatePacket`.

### Paging

**No paging support.** The implementation is hardcoded to 6 buttons and 6 slots. The `for` loop at line 161 iterates `i < 6`. There is no UI element for scrolling or page navigation in `RingPanelScreen`.

### Live vs. Stored

- Destinations are **live-discovered** from the `TransporterNetwork` registry each time `getNearest6Rings()` is called.
- The `ringsPos` and `ringsName` arrays are transient — they are refreshed on demand, not persisted in NBT.
- Memory Crystal slots provide **stored** endpoints that bypass discovery.

---

## Naming

### Can endpoints be named?

**Yes**, via the standard Minecraft anvil rename mechanic on the `AbstractTransporterEntity`.

**File:** `AbstractTransporterEntity.java` (lines 151, 167)
- `setCustomName(Component name)` — stores name under NBT key `"custom_name"` (line 36)
- `getCustomName()` — retrieves it

The name propagates through `SGJourneyTransporter(AbstractTransporterEntity)` constructor (line 53), which reads `transporterEntity.getCustomName()`.

There is no in-game naming UI within the Ring Panel itself. Players must use an anvil to rename the ring block or use commands.

### Display

Named endpoints appear in the Ring Panel GUI with their custom name in Aqua text, followed by coordinates in Dark Green.
