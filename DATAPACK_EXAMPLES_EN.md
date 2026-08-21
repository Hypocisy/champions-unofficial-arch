# Champions Unofficial — Datapack Examples

Use alongside [DATAPACK_GUIDE_EN.md](./DATAPACK_GUIDE_EN.md). Every example below is a complete, ready-to-use JSON file.

---

## Example 1: Skeleton Archer Archetype

**Goal**: Give skeletons and strays ranged-skill affixes, making ranged champions far more threatening.

`data/mypack/champions/archetype/skeleton_archer.json`

```json
{
  "id": "mypack:skeleton_archer",
  "weight": 12,
  "entity_filter": {
    "type": "entity_type",
    "types": [
      "minecraft:skeleton",
      "minecraft:stray",
      "minecraft:bogged"
    ],
    "whitelist": true
  },
  "affix_pools": [
    {
      "candidates": [
        { "affix": "champions:arctic",     "weight": 15, "max_strength": 2 },
        { "affix": "champions:enkindling", "weight": 15, "max_strength": 2 },
        { "affix": "champions:hasty",      "weight": 8  }
      ],
      "min_count": 1,
      "max_count": 2
    },
    {
      "tier_range": { "min": 4 },
      "candidates": [
        { "affix": "champions:dampening", "weight": 10, "max_strength": 2 },
        { "affix": "champions:lively",    "weight": 8,  "max_strength": 2 }
      ],
      "min_count": 1,
      "max_count": 1
    }
  ]
}
```

---

## Example 2: Nether Theme Archetype

**Goal**: Bias all Nether mobs (Piglins, Blazes, Ghasts, etc.) toward fire-themed affixes.

`data/mypack/champions/archetype/nether_creatures.json`

```json
{
  "id": "mypack:nether_creatures",
  "weight": 10,
  "entity_filter": {
    "type": "mob_category",
    "categories": ["monster"]
  },
  "affix_pools": [
    {
      "candidates": [
        { "affix": "champions:molten",     "weight": 20 },
        { "affix": "champions:enkindling", "weight": 15, "max_strength": 2 },
        { "affix": "champions:reflective", "weight": 8,  "max_strength": 2 },
        { "affix": "champions:hasty",      "weight": 6  }
      ],
      "min_count": 1,
      "max_count": 2
    }
  ]
}
```

> **Tip**: To target only Nether-specific mobs from another mod, swap the `mob_category` filter for a `mod_id` filter with the appropriate mod ID.

---

## Example 3: Time-Based Escalation — The Longer It Lives, The Stronger It Gets

**Goal**: Punish players who drag out the fight. The champion grows stronger over time, encouraging aggressive play.

`data/mypack/champions/archetype/berserker.json`

```json
{
  "id": "mypack:berserker",
  "weight": 8,
  "entity_filter": {
    "type": "mob_category",
    "categories": ["monster"]
  },
  "affix_pools": [
    {
      "candidates": [
        { "affix": "champions:knocking",   "weight": 10 },
        { "affix": "champions:wounding",   "weight": 10 },
        { "affix": "champions:paralyzing", "weight": 8  }
      ],
      "min_count": 1,
      "max_count": 2
    }
  ],
  "phases": [
    {
      "id": "mypack:berserker_phase1",
      "condition": { "type": "time_elapsed", "seconds": 20 },
      "effects": [
        {
          "type": "add_attribute",
          "attribute": "minecraft:generic.movement_speed",
          "amount": 0.15,
          "operation": "add_multiplied_base"
        },
        {
          "type": "add_mob_effect",
          "effect": "minecraft:strength",
          "amplifier": 0,
          "infinite": true
        }
      ],
      "repeatable": false
    },
    {
      "id": "mypack:berserker_phase2",
      "condition": { "type": "time_elapsed", "seconds": 60 },
      "effects": [
        {
          "type": "add_attribute",
          "attribute": "minecraft:generic.attack_damage",
          "amount": 6.0,
          "operation": "add_value"
        },
        {
          "type": "add_mob_effect",
          "effect": "minecraft:resistance",
          "amplifier": 1,
          "infinite": true
        }
      ],
      "repeatable": false
    }
  ]
}
```

---

## Example 4: Two-Stage Enrage Boss

**Goal**: A high-health champion enters a first stage at 50% health, then a final burst at 20%. Best used on naturally large mobs.

`data/mypack/champions/archetype/two_phase_boss.json`

```json
{
  "id": "mypack:two_phase_boss",
  "tier_range": { "min": 4 },
  "weight": 4,
  "entity_filter": {
    "type": "attribute",
    "attribute": "minecraft:generic.max_health",
    "min": 80.0
  },
  "affix_pools": [
    {
      "candidates": [
        { "affix": "champions:shielding", "weight": 15, "max_strength": 2 },
        { "affix": "champions:adaptable", "weight": 12, "max_strength": 2 },
        { "affix": "champions:dampening", "weight": 10, "max_strength": 2 }
      ],
      "min_count": 1,
      "max_count": 2
    }
  ],
  "phases": [
    {
      "id": "mypack:boss_phase1",
      "condition": { "type": "health_percent", "below": 0.5 },
      "effects": [
        { "type": "add_affix", "affix": "champions:hasty", "strength": 1 },
        {
          "type": "add_mob_effect",
          "effect": "minecraft:strength",
          "amplifier": 0,
          "infinite": true
        }
      ],
      "repeatable": false
    },
    {
      "id": "mypack:boss_phase2",
      "condition": { "type": "health_percent", "below": 0.2 },
      "effects": [
        { "type": "add_affix", "affix": "champions:infested", "strength": 2 },
        { "type": "add_affix", "affix": "champions:molten",   "strength": 1 },
        {
          "type": "add_attribute",
          "attribute": "minecraft:generic.attack_damage",
          "amount": 0.5,
          "operation": "add_multiplied_total"
        },
        {
          "type": "add_mob_effect",
          "effect": "minecraft:regeneration",
          "amplifier": 1,
          "infinite": true
        }
      ],
      "repeatable": false
    }
  ]
}
```

---

## Example 5: Wither Skeleton Specialist

**Goal**: Give Wither Skeletons a dedicated affix pool that plays into their wither theme, plus a last-stand phase.

`data/mypack/champions/archetype/wither_skeleton.json`

```json
{
  "id": "mypack:wither_skeleton",
  "weight": 20,
  "entity_filter": {
    "type": "entity_type",
    "types": ["minecraft:wither_skeleton"],
    "whitelist": true
  },
  "affix_pools": [
    {
      "candidates": [
        { "affix": "champions:wounding",  "weight": 20, "min_strength": 1, "max_strength": 3 },
        { "affix": "champions:plagued",   "weight": 15, "max_strength": 2 },
        { "affix": "champions:knocking",  "weight": 10, "max_strength": 2 },
        { "affix": "champions:molten",    "weight": 12 }
      ],
      "min_count": 1,
      "max_count": 2
    }
  ],
  "phases": [
    {
      "id": "mypack:wither_skeleton_last_stand",
      "condition": { "type": "health_percent", "below": 0.35 },
      "effects": [
        { "type": "add_affix", "affix": "champions:plagued", "strength": 2 },
        {
          "type": "add_mob_effect",
          "effect": "minecraft:speed",
          "amplifier": 1,
          "infinite": true
        }
      ],
      "repeatable": false
    }
  ]
}
```

---

## Example 6: Aquatic Elites

**Goal**: Turn water mobs (Guardians, Drowned, etc.) into threatening aquatic champions — great for ocean exploration modpacks.

`data/mypack/champions/archetype/aquatic_elite.json`

```json
{
  "id": "mypack:aquatic_elite",
  "weight": 10,
  "entity_filter": {
    "type": "any_of",
    "filters": [
      { "type": "mob_category", "categories": ["water_creature"] },
      {
        "type": "entity_type",
        "types": [
          "minecraft:guardian",
          "minecraft:elder_guardian",
          "minecraft:drowned"
        ],
        "whitelist": true
      }
    ]
  },
  "affix_pools": [
    {
      "candidates": [
        { "affix": "champions:magnetic",   "weight": 15, "max_strength": 2 },
        { "affix": "champions:arctic",     "weight": 12, "max_strength": 2 },
        { "affix": "champions:reflective", "weight": 10, "max_strength": 2 },
        { "affix": "champions:lively",     "weight": 8,  "max_strength": 2 }
      ],
      "min_count": 1,
      "max_count": 2
    }
  ]
}
```

---

## Example 7: Blacklisting Specific Entities

**Goal**: Apply an archetype to all monsters, but exclude spiders and cave spiders.

`data/mypack/champions/archetype/common_monsters.json`

```json
{
  "id": "mypack:common_monsters",
  "weight": 8,
  "entity_filter": {
    "type": "all_of",
    "filters": [
      { "type": "mob_category", "categories": ["monster"] },
      {
        "type": "entity_type",
        "types": ["minecraft:spider", "minecraft:cave_spider"],
        "whitelist": false
      }
    ]
  },
  "affix_pools": [
    {
      "tier_range": { "min": 1, "max": 2 },
      "candidates": [
        { "affix": "champions:hasty",    "weight": 10 },
        { "affix": "champions:knocking", "weight": 10 },
        { "affix": "champions:lively",   "weight": 8  }
      ],
      "min_count": 1,
      "max_count": 1
    },
    {
      "tier_range": { "min": 3 },
      "candidates": [
        { "affix": "champions:reflective", "weight": 10, "max_strength": 2 },
        { "affix": "champions:wounding",   "weight": 10, "max_strength": 2 },
        { "affix": "champions:shielding",  "weight": 8,  "max_strength": 2 },
        { "affix": "champions:dampening",  "weight": 8,  "max_strength": 2 }
      ],
      "min_count": 1,
      "max_count": 2
    }
  ]
}
```

---

## Example 8: Custom Tier Colors

**Goal**: Replace the default 5-tier colors with an RPG-style green → blue → purple → gold → red progression.

`data/mypack/champions/tier/tier_1.json`
```json
{ "level": 1, "display": { "color": 5635925 } }
```

`data/mypack/champions/tier/tier_2.json`
```json
{ "level": 2, "display": { "color": 5592575 } }
```

`data/mypack/champions/tier/tier_3.json`
```json
{ "level": 3, "display": { "color": 10494192 } }
```

`data/mypack/champions/tier/tier_4.json`
```json
{ "level": 4, "display": { "color": 16766720 } }
```

`data/mypack/champions/tier/tier_5.json`
```json
{ "level": 5, "display": { "color": 16729156 } }
```

> Color reference: Green `#55FF55` → Blue `#5555FF` → Purple `#A020F0` → Gold `#FFD700` → Red `#FF2244`
>
> To convert HEX to decimal: open Windows Calculator in Programmer mode, enter the HEX value, then read the decimal output.

---

## How Multiple Archetypes Work Together

When you define multiple archetypes, the game:

1. Filters all archetypes where both `entity_filter` and `tier_range` match the spawning champion
2. Picks one using weighted random selection based on each archetype's `weight`
3. Assigns affixes to the champion using the selected archetype's `affix_pools`

**Recommended pattern**: Define one low-weight general-purpose archetype as a fallback, then define higher-weight specialist archetypes for specific mob types. The specialist archetype will almost always win for its target mobs, while the general archetype covers everything else.
