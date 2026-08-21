# Champions Unofficial — Datapack Authoring Guide

> Version: v3.0.0 | Minecraft 1.21.1

This guide is aimed at modpack authors who want to customize champion behavior through datapacks, without touching mod source code.

---

## Table of Contents

- [What You Can Do](#what-you-can-do)
- [Quick Start](#quick-start)
- [Archetypes](#archetypes)
  - [Basic Structure](#basic-structure)
  - [Filtering Entities](#filtering-entities)
  - [Affix Pools](#affix-pools)
  - [Phase Behaviors](#phase-behaviors)
- [Tier Appearance](#tier-appearance)
- [Affix Reference](#affix-reference)
- [Examples](#examples)

---

## What You Can Do

With a datapack, you can:

- ✅ Define which mobs can become champions and what affixes they can have
- ✅ Assign different affix combinations to different tier levels
- ✅ Set up "phases" — automatic state changes when a champion's health drops
- ✅ Change the display color and icon of each tier

What requires editing mod config files (not possible via datapack):

- ❌ Champion spawn chance
- ❌ Specific numeric values of affixes (e.g. damage reduction percentage, cooldown durations)

---

## Quick Start

Create a JSON file in your datapack at the following path:

```
data/
└── your_namespace/
    └── champions/
        └── archetype/
            └── my_archetype.json
```

The simplest possible archetype file:

```json
{
  "id": "mypack:basic",
  "affix_pools": [
    {
      "candidates": [
        { "affix": "champions:lively" },
        { "affix": "champions:hasty" },
        { "affix": "champions:knocking" }
      ],
      "min_count": 1,
      "max_count": 2
    }
  ]
}
```

This gives all champions 1–2 affixes randomly drawn from Lively, Hasty, and Knocking.

---

## Archetypes

### Basic Structure

```json
{
  "id": "mypack:example",
  "tier_range": { "min": 1, "max": 5 },
  "weight": 10,
  "entity_filter": { ... },
  "affix_pools": [ ... ],
  "phases": [ ... ]
}
```

| Field | Description | Required | Default |
|---|---|---|---|
| `id` | Unique identifier; should match the file name | ✅ Yes | — |
| `tier_range` | Which tier levels this archetype applies to | Optional | All tiers |
| `weight` | When multiple archetypes match, this controls how likely this one is to be selected (higher = more likely) | Optional | `10` |
| `entity_filter` | Restricts which entities can use this archetype | Optional | All entities |
| `affix_pools` | List of affix pools | ✅ Yes | — |
| `phases` | List of phase behaviors | Optional | None |

---

### Filtering Entities

`entity_filter` specifies which mobs are eligible for this archetype. Multiple filter types can be combined.

#### By mob category

```json
"entity_filter": {
  "type": "mob_category",
  "categories": ["monster"]
}
```

Valid categories: `monster`, `creature`, `ambient`, `water_creature`, `water_ambient`, `axolotls`, `underground_water_creature`

#### By specific entity type

```json
"entity_filter": {
  "type": "entity_type",
  "types": ["minecraft:zombie", "minecraft:skeleton"],
  "whitelist": true
}
```

- `whitelist: true` → only the listed entities can use this archetype
- `whitelist: false` → all entities except the listed ones can use this archetype

#### By entity tag

```json
"entity_filter": {
  "type": "entity_tag",
  "tag": "minecraft:undead",
  "whitelist": true
}
```

#### By health range (useful for targeting large/boss-like mobs)

```json
"entity_filter": {
  "type": "attribute",
  "attribute": "minecraft:generic.max_health",
  "min": 40.0
}
```

#### Combining multiple conditions

Use `all_of` (all must match — AND) or `any_of` (at least one must match — OR):

```json
"entity_filter": {
  "type": "all_of",
  "filters": [
    { "type": "mob_category", "categories": ["monster"] },
    { "type": "entity_tag", "tag": "minecraft:undead", "whitelist": true }
  ]
}
```

---

### Affix Pools

Affix pools control which affixes a champion can receive. You can define multiple pools, each targeting a different tier range.

```json
"affix_pools": [
  {
    "tier_range": { "min": 1, "max": 3 },
    "candidates": [
      { "affix": "champions:lively",   "weight": 10, "min_strength": 1, "max_strength": 2 },
      { "affix": "champions:knocking", "weight": 8 },
      { "affix": "champions:hasty",    "weight": 6 }
    ],
    "min_count": 1,
    "max_count": 2
  },
  {
    "tier_range": { "min": 4 },
    "candidates": [
      { "affix": "champions:reflective", "weight": 10, "max_strength": 3 },
      { "affix": "champions:arctic",     "weight": 8 }
    ],
    "min_count": 1,
    "max_count": 2
  }
]
```

**Pool fields:**

| Field | Description | Default |
|---|---|---|
| `tier_range` | Which tier levels activate this pool | All tiers |
| `candidates` | List of candidate affixes | — |
| `min_count` | Minimum number of affixes to pick from this pool | `1` |
| `max_count` | Maximum number of affixes to pick from this pool | `1` |

**Candidate fields:**

| Field | Description | Default |
|---|---|---|
| `affix` | Affix registry ID | — |
| `weight` | Selection weight — higher means more likely to be picked | `10` |
| `min_strength` | Minimum affix strength | `1` |
| `max_strength` | Maximum affix strength (set equal to min for a fixed value) | `1` |

> **About strength**: Strength controls the power level of the affix. The actual numeric effect (e.g. damage reduction %) is determined by the mod config. Generally, higher strength = stronger effect.

---

### Phase Behaviors

Phases let you change a champion's state when a condition is met — for example, triggering an enrage when health drops below 30%.

Phases are checked every **0.5 seconds** (10 ticks).

```json
"phases": [
  {
    "id": "mypack:enrage",
    "condition": { "type": "health_percent", "below": 0.3 },
    "effects": [
      { "type": "add_affix", "affix": "champions:hasty", "strength": 2 },
      { "type": "add_mob_effect", "effect": "minecraft:strength", "amplifier": 1, "infinite": true }
    ],
    "repeatable": false
  }
]
```

| Field | Description | Default |
|---|---|---|
| `id` | Unique identifier; tracks whether this phase has already fired for this champion | — |
| `condition` | When to trigger | — |
| `effects` | List of effects to apply when triggered | — |
| `repeatable` | If `false`, this phase fires at most once per champion lifetime | `false` |

#### Condition types

**Health percent:**
```json
{ "type": "health_percent", "below": 0.5 }
```
Triggers when `currentHP / maxHP` drops below the given value (0.5 = 50%).

**Time elapsed:**
```json
{ "type": "time_elapsed", "seconds": 60 }
```
Triggers after the champion has been alive for the specified number of seconds.

#### Effect types

**Add an affix:**
```json
{ "type": "add_affix", "affix": "champions:enkindling", "strength": 1 }
```

**Add an attribute modifier:**
```json
{
  "type": "add_attribute",
  "attribute": "minecraft:generic.movement_speed",
  "amount": 0.3,
  "operation": "add_value"
}
```
`operation` options: `add_value` (flat add), `add_multiplied_base` (multiply base value), `add_multiplied_total` (multiply total value)

**Add a mob effect:**
```json
{
  "type": "add_mob_effect",
  "effect": "minecraft:speed",
  "amplifier": 1,
  "infinite": true
}
```
When `infinite` is `false`, use `duration_ticks` to set the duration (default `200` ticks = 10 seconds).

---

## Tier Appearance

**Path:** `data/<namespace>/champions/tier/<name>.json`

```json
{
  "level": 3,
  "display": {
    "color": 16736256,
    "icon": "mypack:textures/gui/tier3.png"
  }
}
```

| Field | Description |
|---|---|
| `level` | Tier level number |
| `display.color` | Display color as a decimal integer (convert from HEX using a color picker or calculator) |
| `display.icon` | Optional icon texture path |

> HEX to decimal example: `#FF6600` → `16736256`

**Default tier colors:**

| Level | Color |
|---|---|
| 1 | ⬜ White |
| 2 | 🟦 Aqua |
| 3 | 🟨 Yellow |
| 4 | 🟥 Red |
| 5 | 🟪 Purple |

---

## Affix Reference

### Combat Affixes

| Affix ID | Effect |
|---|---|
| `champions:dampening` | Reduces incoming direct damage; scales with strength |
| `champions:knocking` | Knockback + slowness on attack; knocks back attacker when hurt |
| `champions:paralyzing` | Chance to fully paralyze the target on attack |
| `champions:reflective` | Reflects a portion of incoming damage back to the attacker |
| `champions:wounding` | Applies a wound debuff: halves healing received and increases damage taken by 50% |

### Stateful Affixes

| Affix ID | Effect |
|---|---|
| `champions:adaptable` | Reduces damage from consecutive same-type hits |
| `champions:lively` | Regenerates health when not recently damaged; cooldown decreases with strength |
| `champions:shielding` | Periodically activates a damage-blocking shield |

### Passive / Spawn Affixes

| Affix ID | Effect |
|---|---|
| `champions:hasty` | Gains a movement speed bonus on spawn |
| `champions:molten` | Fire immune, pathfinds over lava, fire aura; ignites both attackers and targets |
| `champions:plagued` | Periodically poisons nearby hostile mobs |

### Active Skill Affixes

| Affix ID | Effect |
|---|---|
| `champions:arctic` | Shoots homing ice projectiles that apply Slowness III and Mining Fatigue III |
| `champions:desecrating` | Drops lingering Harming potion clouds at the target's feet |
| `champions:enkindling` | Shoots homing fire projectiles that set the target on fire for 8 seconds |
| `champions:infested` | Periodically spawns spider parasites; burst-spawns on death |
| `champions:magnetic` | Periodically pulls the target toward the champion |

---

## Examples

### Example 1: Undead Elite Archetype

Targets high-health undead mobs (tier 3+) with a two-stage enrage below 30% health.

```json
{
  "id": "mypack:undead_elite",
  "tier_range": { "min": 3 },
  "weight": 5,
  "entity_filter": {
    "type": "all_of",
    "filters": [
      { "type": "entity_tag", "tag": "minecraft:undead", "whitelist": true },
      { "type": "attribute", "attribute": "minecraft:generic.max_health", "min": 40.0 }
    ]
  },
  "affix_pools": [
    {
      "tier_range": { "min": 3, "max": 4 },
      "candidates": [
        { "affix": "champions:wounding",  "weight": 15, "max_strength": 2 },
        { "affix": "champions:shielding", "weight": 8,  "max_strength": 2 },
        { "affix": "champions:lively",    "weight": 10, "max_strength": 2 }
      ],
      "min_count": 1,
      "max_count": 2
    },
    {
      "tier_range": { "min": 5 },
      "candidates": [
        { "affix": "champions:desecrating", "weight": 10, "min_strength": 2, "max_strength": 3 },
        { "affix": "champions:reflective",  "weight": 8,  "max_strength": 3 }
      ],
      "min_count": 2,
      "max_count": 3
    }
  ],
  "phases": [
    {
      "id": "mypack:undead_enrage",
      "condition": { "type": "health_percent", "below": 0.3 },
      "effects": [
        { "type": "add_affix", "affix": "champions:hasty", "strength": 2 },
        { "type": "add_attribute", "attribute": "minecraft:generic.attack_damage", "amount": 4.0, "operation": "add_value" },
        { "type": "add_mob_effect", "effect": "minecraft:strength", "amplifier": 1, "infinite": true }
      ],
      "repeatable": false
    }
  ]
}
```

### Example 2: Lightweight Baseline Pool

A low-weight fallback archetype for all monsters — good as a "catch-all" layer in a modpack.

```json
{
  "id": "mypack:baseline",
  "weight": 3,
  "entity_filter": {
    "type": "mob_category",
    "categories": ["monster"]
  },
  "affix_pools": [
    {
      "candidates": [
        { "affix": "champions:hasty",    "weight": 10 },
        { "affix": "champions:lively",   "weight": 10 },
        { "affix": "champions:knocking", "weight": 8  },
        { "affix": "champions:molten",   "weight": 6  }
      ],
      "min_count": 1,
      "max_count": 1
    }
  ]
}
```
