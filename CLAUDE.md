# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Champions Unofficial (v3.0.0) — a Minecraft 1.21.1 mod that turns eligible mobs into elite "champion" entities with tiers, affixes, and phase-based behavior. Built with Architectury Loom targeting both NeoForge and Fabric.

## Build Commands

```bash
# Build both platform jars
./gradlew build

# Build a single platform
./gradlew :neoforge:build
./gradlew :fabric:build

# Compile only (no jar packaging)
./gradlew :common:compileJava
./gradlew :neoforge:compileJava

# Run the game client (NeoForge)
./gradlew :neoforge:runClient

# Run the game client (Fabric)
./gradlew :fabric:runClient
```

There are no automated tests; verification is done by running the game client.

## Module Structure

Three Gradle subprojects:

- **`common/`** — all platform-agnostic code: public API (`api/`), core game logic, affix implementations, archetype/tier data loading. Compiled against Fabric Loader solely for `@Environment` annotations.
- **`neoforge/`** — NeoForge event bridges, attachment provider, packet handler, KubeJS integration. Merges `common` via Shadow at `remapJar` time.
- **`fabric/`** — Fabric equivalents; uses Cardinal Components API (bundled via `include`) for the attachment system.

Platform code in `neoforge/` and `fabric/` should only contain what cannot live in `common/`. The `ChampionAttachmentProvider` interface in `common/platform/` is the seam — each platform provides its own implementation.

## Core Architecture

### Champion lifecycle

1. **Spawn hook** — Platform event bridge calls `ChampionSpawnHandler.trySpawn(entity, level)` when an entity joins the world. A 10% chance rolls a tier by weighted random.
2. **`ChampionBuilder.trySpawn()`** — Selects a build strategy, assembles the affix list, fires `ChampionEvents.SPAWN` for third-party interception, writes the attachment, sets up AI goals, triggers `SpawnEvent` dispatch, then syncs to tracking players.
3. **Build strategies** — `ArchetypeStrategy` is the default when any archetype datapacks are loaded; `LegacyStrategy` is a fallback and logs a migration warning.

### Affix system

- `AffixType<D>` is a **stateless singleton** in the registry. It defines behavior via `registerHandlers(HandlerRegistry<D>)`.
- `AffixInstance` holds the **per-champion state** (`D extends IAffixData`, serialized as `CompoundTag`).
- `HandlerRegistry` stores typed handlers for internal event classes (`HurtEvent`, `AttackEvent`, `DeathEvent`, `TickEvent`, `SpawnEvent`, `HealEvent`) and `GoalHandler` pairs for AI goal management.
- `GlobalDispatcher.dispatch(eventType, champion, event)` is the single dispatch entry point — platform bridges create an internal event wrapper and call this.

Dispatch path: `Platform event → ChampionEventsHandler → internal event wrapper → GlobalDispatcher → HandlerRegistry.dispatch → AffixHandler.handle`

After every dispatch, `GlobalDispatcher` diffs affix runtime data snapshots and calls `persistRuntimeState()` only when something changed.

### Archetype system (preferred data format)

Archetypes live at `data/<namespace>/champions/archetype/*.json`. Each defines:
- `tier_range` — which tier levels this archetype applies to
- `weight` — weighted random selection among matching archetypes
- `entity_filter` — mob category or entity type filter
- `affix_pools` — list of pools, each with `candidates` (weighted affix refs with strength ranges) and `min_count`/`max_count`
- `phases` — list of `ChampionPhase` entries (condition → effects, evaluated every 10 ticks by `PhaseProcessor`)

### Public API

`ChampionsApi.get()` provides:
- `getChampion(LivingEntity)` / `isChampion(LivingEntity)`
- Affix type registry lookups (`getAffixType(ResourceLocation)`, `getAffixTypes()`)
- Tier registry lookups (`getTier`, `getTierByLevel`, `getTiers()`)

The implementation is injected at startup. Calling `ChampionsApi.get()` before mod load throws `IllegalStateException`.

### Network sync

`PacketHandler` (interface in `common/`, implemented per-platform) syncs `ChampionSyncData` to all tracking players on champion spawn and on affix state changes. Tier data is synced separately via `TierSyncPacket`.

## Key Conventions

- New affixes belong in `common/.../affix/builtin/` and must be registered via `AffixTypeRegistry`.
- Affixes with no per-champion state use `AffixType<EmptyAffixData>` and skip `createData()`.
- All champion state mutations (goal setup/teardown, event dispatch) must happen server-side only.
- `ChampionBuilder` must not be called client-side — it throws `IllegalStateException` if `level().isClientSide()`.
- Spawn chance and tier weights are hardcoded constants in `ChampionSpawnHandler` with `// TODO: replace with config values` notes.
