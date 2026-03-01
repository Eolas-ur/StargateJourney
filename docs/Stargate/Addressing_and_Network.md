# Stargate Addressing and Network

## Address Types

The mod uses a hierarchical addressing system:

| Type | Symbols | Scope |
|---|---|---|
| **7-Chevron** | 6 symbols + Point of Origin | Galactic — addresses a Solar System within a Galaxy |
| **8-Chevron** | 7 symbols + Point of Origin | Extragalactic — addresses a Solar System across Galaxies |
| **9-Chevron** | 8 symbols + Point of Origin | Unique gate ID — addresses a specific physical Stargate |

7 and 8-chevron addresses are assigned to **Solar Systems** (not directly to individual gates). When dialed, the connection routes to the **Primary Stargate** of that Solar System.

9-chevron addresses are unique per physical gate.

---

## Address Generation

### Method: `generate9ChevronAddress()`

**File:** `src/main/java/net/povstalec/sgjourney/common/block_entities/stargate/AbstractStargateEntity.java` (line 426)

```java
protected Address.Immutable generate9ChevronAddress() {
    Random random = new Random();
    Address.Immutable address;
    while(true) {
        address = Address.Immutable.randomAddress(8, 36, random.nextLong());
        if(!BlockEntityList.get(level).containsStargate(address))
            break;
    }
    return address;
}
```

**Algorithm:**
1. Creates an **unseeded** `java.util.Random` instance (uses system entropy).
2. Generates an 8-symbol array. Each symbol is a random integer in range 1–35, with no duplicates enforced by `ArrayHelper.differentNumbers`.
3. Checks `BlockEntityList` for uniqueness. Loops until no collision.
4. After return, `Address.Immutable.extendWithPointOfOrigin()` appends symbol `0` (Point of Origin) as the 9th symbol.

**Key properties:**
- **Non-deterministic.** Not based on BlockPos, dimension, seed, or any external input.
- **Unique.** Guaranteed by the collision check against the global `BlockEntityList`.
- **Applies to all gates** — both worldgen-placed and player-placed.

### Method: `addStargateToNetwork()`

**File:** `AbstractStargateEntity.java` (line 400)

```java
public void addStargateToNetwork() {
    if(id9ChevronAddress.getType() != Address.Type.ADDRESS_9_CHEVRON
       || BlockEntityList.get(level).containsStargate(id9ChevronAddress))
        set9ChevronAddress(Address.Immutable.extendWithPointOfOrigin(generate9ChevronAddress()));
    StargateNetwork.get(level).addStargate(this);
    this.setChanged();
}
```

Called from `onLoad()` (line 1588) via the `StructureGenEntity` interface pattern. If the existing address is invalid or collides, a new one is generated.

---

## Network Registration Chain

1. **`AbstractStargateEntity.addStargateToNetwork()`** — validates address, generates if needed
2. **`StargateNetwork.get(level).addStargate(this)`** — entry point into network
   - File: `src/main/java/net/povstalec/sgjourney/common/data/StargateNetwork.java`
3. **`BlockEntityList.get(server).addStargate(stargateEntity)`** — stores gate in master map keyed by 9-chevron address
   - File: `src/main/java/net/povstalec/sgjourney/common/data/BlockEntityList.java`
4. **`Universe.get(server).addStargateToDimension(...)`** — links gate to its dimension's `SolarSystem`
   - File: `src/main/java/net/povstalec/sgjourney/common/data/Universe.java`
   - File: `src/main/java/net/povstalec/sgjourney/common/sgjourney/SolarSystem.java`

### Primary Gate Selection

In `SolarSystem.Serializable.addStargate`, gates are prioritized. A gate with a DHD, newer generation, or higher usage is preferred as the "primary" gate for 7/8-chevron connections.

---

## NBT Storage

Addresses are persisted per block entity in chunk NBT.

| Field | NBT Key | Type | Description |
|---|---|---|---|
| `id9ChevronAddress` | `"9_chevron_address"` | `IntArray` | Unique 9-symbol gate ID |
| `address` | `"address"` | `IntArray` | Currently dialed sequence |

**Save:** `serializeStargateInfo()` (line 305) via `saveAdditional()`
**Load:** `deserializeStargateInfo()` (line 241) via `loadAdditional()` — includes legacy support for typo key `"9_hevron_address"`

Addresses are **not recalculated on load.** They persist in NBT. A new address is only generated if the loaded one is invalid or collides.

---

## Survival Methods for Reading Addresses

### 1. PDA Item

**File:** `src/main/java/net/povstalec/sgjourney/common/items/PDAItem.java` (line 51)
**Trigger:** `AbstractStargateEntity.getStatus(Player player)` (line 1336)

Right-clicking a Stargate with a PDA sends chat messages to the player:

- **Point of Origin** (line 1341)
- **Symbols** (line 1342)
- **Encoded Address** — currently dialed sequence (line 1347)
- **9-Chevron Address** — unique gate ID (line 1350)
- **Primary status** (line 1352)

Addresses use `Address.toComponent(true)` which adds a **click-to-copy** hover event, allowing clipboard copy.

### 2. CC: Tweaked Integration

**File:** `src/main/java/net/povstalec/sgjourney/common/compatibility/cctweaked/methods/StargateMethods.java`

Via Interface blocks + ComputerCraft computers:
- `getDialedAddress()` — currently dialed address
- `getLocalAddress()` — gate's own 9-chevron address (Advanced Crystal Interface)
- `getConnectedAddress()` — connected gate's address (Advanced Crystal Interface)

### 3. DHD Screens

DHD screens (`MilkyWayDHDScreen`, `PegasusDHDScreen`) allow symbol input for dialing but do **not** textually display the local gate's own address.
