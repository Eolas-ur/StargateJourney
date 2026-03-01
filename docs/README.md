# Stargate Journey — Technical Documentation

This directory contains reverse-engineered documentation for the [Stargate Journey](https://github.com/Povstalec/StargateJourney) NeoForge Minecraft mod (`sgjourney`).

## Structure

```
docs/
├── README.md                          ← You are here
├── CHANGELOG.md                       ← Dated log of doc changes
├── Stargate/
│   ├── Worldgen.md                    ← Structure sets, placement, biomes
│   ├── Addressing_and_Network.md      ← Address generation, NBT, network registry
│   ├── Discovery_and_Maps.md          ← Archeologist trades, map mechanics
│   └── Terra_Design_Notes.md          ← Terra gate analysis + proposals
└── Rings/
    ├── Overview.md                    ← Block/entity classes, network registry
    ├── UI_and_Inventory.md            ← GUI, slots, memory crystals
    ├── Discovery_and_Network.md       ← How rings find each other
    └── Limitations_and_Gaps.md        ← Known UX issues
```

## Methodology

All documentation is derived from reading the source code directly. Every claim cites a file path and, where applicable, a class or method name. Items marked `TODO` indicate areas where the code was ambiguous or could not be fully resolved.

## Mod Version

- **Mod ID:** `sgjourney`
- **Version:** 0.6.44
- **Minecraft:** 1.21.1
- **Modloader:** NeoForge
