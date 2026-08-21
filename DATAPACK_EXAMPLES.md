# Champions Unofficial — Datapack 示例集

配合 [DATAPACK_GUIDE.md](./DATAPACK_GUIDE.md) 使用。每个示例都是可以直接放入 datapack 的完整文件。

---

## 示例 1：骷髅弓手专属原型

**场景**：让骷髅和流浪者获得远程技能词缀，打造更危险的远程精英怪。

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

## 示例 2：下界主题原型

**场景**：让所有下界生物（猪灵、烈焰人、地狱幻翼等）偏向火焰主题词缀。

`data/mypack/champions/archetype/nether_creatures.json`

```json
{
  "id": "mypack:nether_creatures",
  "weight": 10,
  "entity_filter": {
    "type": "mod_id",
    "mod_ids": ["minecraft"],
    "whitelist": true
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

> **提示**：如果整合包里有下界相关的其他 mod（如 Quark、Alex's Mobs），将 `mod_ids` 改为具体的 mod ID 可以更精确地筛选生物。

---

## 示例 3：时间阶段——越战越勇

**场景**：精英怪存活越久越强，逼迫玩家速战速决。

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

## 示例 4：多阶段 Boss——两段狂暴

**场景**：血量降到 50% 进入第一阶段，降到 20% 进入最终爆发阶段。适合高血量的精英怪。

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
        { "affix": "champions:shielding",  "weight": 15, "max_strength": 2 },
        { "affix": "champions:adaptable",  "weight": 12, "max_strength": 2 },
        { "affix": "champions:dampening",  "weight": 10, "max_strength": 2 }
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

## 示例 5：凋灵骷髅专属词缀

**场景**：为凋灵骷髅量身定制词缀池，强调其凋零主题，并在低血量时感染周围区域。

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

## 示例 6：水生精英

**场景**：让水中生物（守卫者、溺尸等）成为精英怪，适合水下探索整合包。

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

## 示例 7：排除特定实体（黑名单用法）

**场景**：让所有敌对生物都能成为精英怪，但排除蜘蛛和洞穴蜘蛛（因为它们已经足够烦人了）。

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

## 示例 8：自定义等级配色

**场景**：将默认的 5 个等级改为绿色→蓝色→紫色→金色→红色渐进配色，更有 RPG 感。

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

> 颜色对照：绿色 `#55FF55` → 蓝色 `#5555FF` → 紫色 `#A020F0` → 金色 `#FFD700` → 红色 `#FF2244`
> 
> HEX 转十进制方法：在 Windows 计算器（程序员模式）中输入 HEX 值，切换到十进制即可。

---

## 多原型共存说明

当你定义了多个原型时，游戏会：

1. 筛选出所有 `entity_filter` 和 `tier_range` 同时满足的原型
2. 按照 `weight` 加权随机选择其中一个
3. 按照选中原型的 `affix_pools` 为精英怪分配词缀

**建议**：可以同时存在一个「通用低权重」原型（weight 低）作为兜底，再为特定生物设置「专属高权重」原型（weight 高），这样特定生物大概率走专属原型，其他生物走通用原型。
