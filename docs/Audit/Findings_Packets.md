# Packets Audit Findings

Global audit of all serverbound packets against the rubric.

---

## Serverbound Packet Summary

| Packet | Distance Check | BE Type Check | Dimension Check | Safe Drop | Spam Potential |
|--------|:-:|:-:|:-:|:-:|:-:|
| `ServerboundRingPanelUpdatePacket` | **Yes** (fixed in F001) | Yes (`RingPanelEntity`) | Implicit (same level) | Yes | Low |
| `ServerboundDHDUpdatePacket` | **Yes** (fixed in G001) | Yes (`AbstractDHDEntity`) | Implicit | Yes | Medium |
| `ServerboundInterfaceUpdatePacket` | **Yes** (fixed in G002) | Yes (`AbstractInterfaceEntity` + `AbstractInterfaceBlock`) | Implicit | Yes | Low |
| `ServerboundGDOUpdatePacket` | N/A (item-based) | Yes (`GDOItem` in hand) | N/A | Yes | Low |
| `ServerboundTransceiverUpdatePacket` | **Yes** (fixed in G003) | Yes (`TransceiverEntity`) | Implicit | Yes | Medium |

---

## Detailed Analysis

### ServerboundRingPanelUpdatePacket — ALREADY FIXED (F001)

- **Distance:** ≤ 64.0 squared (8 blocks). Check added before `getBlockEntity()`.
- **BE Type:** `instanceof RingPanelEntity`.
- **Safe Drop:** Returns silently if check fails.
- **Notes:** Reference finding F001.

---

### ServerboundDHDUpdatePacket — FIXED (G001)

- **Distance:** ≤ 64.0 squared (8 blocks). Check added before `getBlockEntity()`.
- **BE Type:** `instanceof AbstractDHDEntity` — prevents crash and validates target type.
- **Safe Drop:** Returns silently if distance or type check fails.
- **Spam Potential:** Medium. Each press engages one chevron with energy cost. Server-side energy validation in `engageChevron()` provides natural rate limiting.
- **Cross-reference:** See G001 in Findings_Gameplay.md.

---

### ServerboundInterfaceUpdatePacket — FIXED (G002)

- **Distance:** ≤ 64.0 squared (8 blocks). Check added before any level access.
- **BE Type:** Dual check — `instanceof AbstractInterfaceEntity` AND `instanceof AbstractInterfaceBlock`.
- **Safe Drop:** Returns silently if distance check fails.
- **Spam Potential:** Low. Setting energy target and mode are idempotent operations.
- **Cross-reference:** See G002 in Findings_Gameplay.md.

---

### ServerboundGDOUpdatePacket — ITEM-BASED (No Distance Issue)

- **Distance:** N/A. The GDO is an item held by the player, not a block interaction. The handler validates the item is in the correct hand.
- **Item Check:** Verifies `ctx.player().getItemInHand()` is a `GDOItem`.
- **Transmission:** `sendTransmission()` scans nearby chunks for receivers. The scan is bounded by config radius and only runs when `transmit=true`.
- **Safe Drop:** Yes — returns if item check fails.
- **Spam Potential:** Low. Transmission scan runs per press, not per tick. Natural cooldown via UI interaction speed.
- **Notes:** This packet is correctly item-based and does not need a distance check. The GDO item's own `use()` method handles finding nearby stargates.

---

### ServerboundTransceiverUpdatePacket — FIXED (G003)

- **Distance:** ≤ 64.0 squared (8 blocks). Check added before `getBlockEntity()`.
- **BE Type:** `instanceof TransceiverEntity`.
- **Safe Drop:** Returns silently if distance check fails.
- **Spam Potential:** Medium. `sendTransmission()` scans loaded chunks within transmission radius. Distance check prevents remote spam.
- **Cross-reference:** See G003 in Findings_Gameplay.md.

---

## Clientbound Packets (No Security Concerns)

Clientbound packets are server-to-client and inherently trusted. They include:
- `ClientboundDialerOpenScreenPacket` — Opens UI
- `ClientboundGDOOpenScreenPacket` — Opens UI
- `ClientboundArcheologistNotebookOpenScreenPacket` — Opens UI
- `ClientboundRingPanelUpdatePacket` — Updates ring panel destination list
- `ClientboundStargateParticleSpawnPacket` — Visual only
- `ClientBoundSoundPackets.*` — Audio only

No security issues identified in clientbound packets.

---

## Recommendations

1. ~~**Add distance checks to G001, G002, G003**~~ — **Done.** All three now have the same distance check pattern as the F001 ring panel fix.
2. **Consider rate limiting for Transceiver transmissions** — Each transmission scans loaded chunks. A server-side cooldown (e.g., 20 ticks between transmissions) would prevent spam. Distance check now prevents remote triggering but local spam is still possible.
3. **Consider extracting a shared utility method** for the distance check pattern used across all 4 serverbound packet handlers:
   ```java
   public static boolean isPlayerInRange(IPayloadContext ctx, BlockPos pos) {
       return ctx.player().distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
   }
   ```
