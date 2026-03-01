# Stability and Performance Audit

## Scope

Targeted audit of the Stargate Journey mod (v0.6.44, MC 1.21.1) focusing on:

- **A) Persistent registries** — `StargateNetwork`, `TransporterNetwork`, `BlockEntityList`
- **B) Chunk forcing safety** — `ServerLevel.setChunkForced` usage
- **C) Tick cost / allocations** — hot-path sorting, allocation patterns
- **D) Packet / server validation** — distance checks, null safety, permissions

## Method

1. Read all relevant source files in full.
2. Trace data flows from block placement through network registration, discovery, transport, and removal.
3. Identify patterns that could lead to crashes, data leaks, unbounded growth, or exploits.
4. Each finding cites file paths and method names.

## Output

- **Findings.md** — Prioritised findings with severity, impact, evidence, and suggested fixes.
- **Test_Plan.md** — Manual reproduction steps for each finding.
