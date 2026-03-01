# Audit Rubric

Consistent checks applied to every registered object during the gameplay surface audit.

## Blocks and Items

| Check | Description |
|-------|-------------|
| Server authority | Client cannot cause server action without validation |
| Distance validation | Interactions require proximity (squared distance ≤ 64.0 for containers/GUIs) |
| Dimension validation | Target dimension must match expected |
| Permission validation | Admin/creative-only actions gated properly |
| Null safety | Target BE/entity can be missing without crash |
| NBT safety | Corrupt/missing tags handled with defaults |
| Performance | No heavy scans/allocations in hot paths (tick methods) |
| Multiplayer safety | Cannot be exploited by spoofed packets |
| Cleanup | State resets properly on break/remove |

## Block Entities

| Check | Description |
|-------|-------------|
| Tick cost | Any per-tick scans, sorts, allocations, capability lookups |
| Registry correctness | Add/remove symmetry, no leaks |
| Chunk forcing | Always released even on interruption/destruction |
| Save/load | Keys stable, legacy handling safe, version migration |
| Thread safety | No cross-thread access to mutable state |

## Packets

| Check | Description |
|-------|-------------|
| Distance validation | Player distance to target BlockPos verified |
| Block type validation | Block at pos is expected type |
| BE type validation | BlockEntity at pos is expected type |
| Dimension consistency | Target in same dimension as player |
| Rate limiting | Spam-resistant if action is expensive |
| Client ID trust | Must not trust client-provided IDs/addresses blindly |
| Safe drop | Invalid packets dropped without crash or side effects |

## Severity Definitions

| Level | Criteria |
|-------|----------|
| Critical | Remote exploit or server crash trivial to trigger |
| High | Exploit requires some conditions, or chunk leak, or unbounded registry growth |
| Medium | Performance inefficiency, minor dupe risk, rare crash |
| Low | Cosmetic, minor UX, non-impactful |
