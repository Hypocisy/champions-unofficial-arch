# Champions Unofficial — Datapack 制作指南

> 适用版本：v3.0.0 | 游戏版本：Minecraft 1.21.1

本指南面向整合包作者，帮助你通过 datapack 自定义精英怪的行为，无需修改 mod 本身。

---

## 目录

- [能做什么](#能做什么)
- [快速开始](#快速开始)
- [原型 Archetype](#原型-archetype)
  - [基础结构](#基础结构)
  - [限定实体范围](#限定实体范围)
  - [词缀池](#词缀池)
  - [阶段行为](#阶段行为)
- [等级外观 Tier](#等级外观-tier)
- [词缀一览](#词缀一览)
- [实用示例](#实用示例)

---

## 能做什么

通过 datapack，你可以：

- ✅ 自定义哪些生物能成为精英怪，以及可以拥有哪些词缀
- ✅ 为不同等级的精英怪分配不同的词缀组合
- ✅ 设定「阶段」——当精英怪血量不足时自动触发狂暴或技能变化
- ✅ 修改等级的显示颜色和图标

无法通过 datapack 调整的内容（需要修改 mod 配置文件）：

- ❌ 精英怪的生成概率
- ❌ 词缀的具体数值（如伤害减免百分比、冷却时间等）

---

## 快速开始

在你的 datapack 中按如下路径新建 JSON 文件：

```
data/
└── 你的命名空间/
    └── champions/
        └── archetype/
            └── my_archetype.json
```

一个最简单的原型文件：

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

这会让所有精英怪随机拥有 1~2 个词缀，从"活力"、"迅捷"、"击退"中随机抽取。

---

## 原型 Archetype

### 基础结构

```json
{
  "id": "mypack:示例",
  "tier_range": { "min": 1, "max": 5 },
  "weight": 10,
  "entity_filter": { ... },
  "affix_pools": [ ... ],
  "phases": [ ... ]
}
```

| 字段 | 说明 | 是否必填 | 默认值 |
|---|---|---|---|
| `id` | 唯一标识符，建议与文件名一致 | ✅ 必填 | — |
| `tier_range` | 此原型适用的等级范围 | 可选 | 全部等级 |
| `weight` | 当多个原型都符合条件时，此原型被选中的概率权重（越大越容易被选） | 可选 | 10 |
| `entity_filter` | 限定哪些实体可以使用此原型 | 可选 | 所有实体 |
| `affix_pools` | 词缀池列表 | ✅ 必填 | — |
| `phases` | 阶段行为列表 | 可选 | 无 |

---

### 限定实体范围

`entity_filter` 用来指定哪些生物可以使用这个原型，支持多种过滤方式并可以组合使用。

#### 按生物类别

```json
"entity_filter": {
  "type": "mob_category",
  "categories": ["monster"]
}
```

可用类别：`monster`（敌对）、`creature`（动物）、`ambient`（环境生物如蝙蝠）、`water_creature`、`water_ambient`、`axolotls`、`underground_water_creature`

#### 按特定实体

```json
"entity_filter": {
  "type": "entity_type",
  "types": ["minecraft:zombie", "minecraft:skeleton"],
  "whitelist": true
}
```

- `whitelist: true` → 只有列出的实体才能使用此原型
- `whitelist: false` → 除了列出的实体，其他都可以使用

#### 按实体标签

```json
"entity_filter": {
  "type": "entity_tag",
  "tag": "minecraft:undead",
  "whitelist": true
}
```

#### 按血量范围（适合筛选「大型 Boss 级生物」）

```json
"entity_filter": {
  "type": "attribute",
  "attribute": "minecraft:generic.max_health",
  "min": 40.0
}
```

#### 组合多个条件

用 `all_of`（全部满足）或 `any_of`（满足其一）来组合：

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

### 词缀池

词缀池决定了精英怪会拥有哪些词缀。你可以设置多个词缀池，它们可以针对不同等级分别生效。

```json
"affix_pools": [
  {
    "tier_range": { "min": 1, "max": 3 },
    "candidates": [
      { "affix": "champions:lively",  "weight": 10, "min_strength": 1, "max_strength": 2 },
      { "affix": "champions:knocking","weight": 8 },
      { "affix": "champions:hasty",   "weight": 6 }
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

**词缀池字段说明：**

| 字段 | 说明 | 默认值 |
|---|---|---|
| `tier_range` | 此词缀池在哪些等级生效 | 全部等级 |
| `candidates` | 候选词缀列表 | — |
| `min_count` | 至少从这个池中选几个词缀 | 1 |
| `max_count` | 最多从这个池中选几个词缀 | 1 |

**候选词缀字段说明：**

| 字段 | 说明 | 默认值 |
|---|---|---|
| `affix` | 词缀 ID | — |
| `weight` | 权重，越大越容易被选中 | 10 |
| `min_strength` | 词缀强度最小值 | 1 |
| `max_strength` | 词缀强度最大值（与最小值相同则为固定值）| 1 |

> **关于强度（strength）**：强度影响词缀的效果等级，具体数值由 mod 配置文件决定。通常 1 是基础效果，数字越大效果越强。

---

### 阶段行为

阶段行为让你可以在特定条件触发时改变精英怪的状态，例如「血量低于 30% 时进入狂暴」。

游戏每 **0.5 秒**检测一次阶段条件。

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

| 字段 | 说明 | 默认值 |
|---|---|---|
| `id` | 唯一标识符，用于记录此阶段是否已触发过 | — |
| `condition` | 触发条件 | — |
| `effects` | 触发后执行的效果列表 | — |
| `repeatable` | 是否可重复触发（`false` 则一生只触发一次）| `false` |

#### 触发条件类型

**血量百分比：**
```json
{ "type": "health_percent", "below": 0.5 }
```
当前血量低于最大血量的 50% 时触发。

**存活时间：**
```json
{ "type": "time_elapsed", "seconds": 60 }
```
精英怪存活超过 60 秒后触发。

#### 触发效果类型

**添加词缀：**
```json
{ "type": "add_affix", "affix": "champions:enkindling", "strength": 1 }
```

**添加属性：**
```json
{
  "type": "add_attribute",
  "attribute": "minecraft:generic.movement_speed",
  "amount": 0.3,
  "operation": "add_value"
}
```
`operation` 可选：`add_value`（直接加）、`add_multiplied_base`（乘基础值）、`add_multiplied_total`（乘总值）

**添加药水效果：**
```json
{
  "type": "add_mob_effect",
  "effect": "minecraft:speed",
  "amplifier": 1,
  "infinite": true
}
```
`infinite: false` 时可用 `duration_ticks` 指定持续时间（默认 200 tick = 10 秒）。

---

## 等级外观 Tier

**路径：** `data/<命名空间>/champions/tier/<名称>.json`

```json
{
  "level": 3,
  "display": {
    "color": 16736256,
    "icon": "mypack:textures/gui/tier3.png"
  }
}
```

| 字段 | 说明 |
|---|---|
| `level` | 等级数值 |
| `display.color` | 显示颜色（十进制整数，可用颜色选择器转换 HEX）|
| `display.icon` | 图标纹理路径（可选）|

> HEX 转十进制示例：`#FF6600` → `16736256`

**默认等级配色：**

| 等级 | 颜色 |
|---|---|
| 1 | ⬜ 白色 |
| 2 | 🟦 青色 |
| 3 | 🟨 黄色 |
| 4 | 🟥 红色 |
| 5 | 🟪 紫色 |

---

## 词缀一览

### 战斗类

| 词缀 ID | 名称 | 效果简介 |
|---|---|---|
| `champions:dampening` | 减震 | 减少受到的直接伤害 |
| `champions:knocking` | 击退 | 攻击时击退并附加缓慢，受击时反弹攻击者 |
| `champions:paralyzing` | 麻痹 | 攻击时有概率使目标完全停止移动 |
| `champions:reflective` | 反射 | 将受到的一部分伤害反弹给攻击者 |
| `champions:wounding` | 创伤 | 使目标治疗效果减半、受到伤害增加 50% |

### 持续效果类

| 词缀 ID | 名称 | 效果简介 |
|---|---|---|
| `champions:adaptable` | 适应 | 连续同类型攻击时逐渐降低受到的伤害 |
| `champions:lively` | 活力 | 未近期受伤时自动回血 |
| `champions:shielding` | 护盾 | 周期性激活一个可格挡伤害的护盾 |

### 被动 / 生成类

| 词缀 ID | 名称 | 效果简介 |
|---|---|---|
| `champions:hasty` | 迅捷 | 生成时获得移动速度加成 |
| `champions:molten` | 熔融 | 免疫火焰，行走于熔岩，攻击和受击均会引火 |
| `champions:plagued` | 瘟疫 | 周期性毒化周围的敌对生物 |

### 主动技能类

| 词缀 ID | 名称 | 效果简介 |
|---|---|---|
| `champions:arctic` | 极寒 | 发射追踪冰霜弹，命中附加缓慢III与挖掘疲劳III |
| `champions:desecrating` | 亵渎 | 在目标脚下投放滞留型伤害药水云 |
| `champions:enkindling` | 燃烧 | 发射追踪火焰弹，命中使目标着火 8 秒 |
| `champions:infested` | 寄生 | 周期性生成蜘蛛，死亡时爆发式生成 |
| `champions:magnetic` | 磁吸 | 周期性将目标拉向自己 |

---

## 实用示例

### 示例一：亡灵强化原型

让血量≥40 的亡灵生物（如凋零骷髅、骷髅马）在高等级时变得更凶猛，并在低血量时狂暴。

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

### 示例二：所有怪物的通用词缀池（低强度）

适合作为整合包的「基础层」原型，给所有怪物一个轻量级的词缀池。

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
