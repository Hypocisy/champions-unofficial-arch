# Changelog

## [Unreleased] — 编辑器重构 — 2025

### 新增 (Added)

- **游戏内编辑器全面重构**（详见 `EDITOR_REFACTOR_PLAN.md`）：
  - **选择器可视化编辑**：`EntityFilter` 递归树编辑器，支持 `all_of`/`any_of` 嵌套、八种过滤器类型切换、子过滤器增删；`entity_type`（实体多选器）、`mod_id`（命名空间多选）、`mob_category`、`attribute`（属性选择器 + min/max）等均有专属配置行。同一编辑器复用于 archetype `entity_filter` 与 modifier `conditions.entity_filter`。
  - **UI 模式细则全覆盖**：archetype 的 affix pools（嵌套 tier_range/min-max count/candidates：affix 选择器、weight、strength 范围）与 phases（id、repeatable、三类 condition 全参数、三类 effect 全参数）全部可视化增删改；modifier 的 `modifier.value/operation` 与 `conditions`（entity_filter、tier min/max、affixes.values 多选）。
  - **JSON 模式内联提示**：实时语法校验（带行列号）、Codec 语义校验（缺字段/类型错）、必填/可选键速查面板。
  - **数据包管理（Packs 页）**：列出全部数据包并一键启用/禁用（服务端重载）；**导出**当前编辑内容为规范数据包 zip 到 `<世界>/champions_exports/`；**导入** `<世界>/champions_imports/` 内的 zip 自动复制进 `datapacks/` 并启用。
  - 通用可搜索注册表选择器（实体/属性/效果/类别/命名空间/affix，单选与多选）。
  - 架构：`EditorSession`（弹窗往返不丢状态）、`FormBuilder` 行式表单 DSL、`JsonPathOps` 路径读写、五个 Tab 面板拆分，`ChampionEditorScreen` 瘦身为壳。
- 新网络包 `EditorPackActionPacket`（toggle/export/import），`EditorPayload` 增加 packs 列表，双平台注册。

### 新增 (Added)

- **客户端配置** (`ChampionConfigSpecClient` / `ChampionsClientConfig`):支持 HUD 偏移(`hudXOffset`/`hudYOffset`)、HUD 检测范围(`hudRange`)、Jade 星星间距与底部内边距、Waila/Jade 集成开关等客户端选项,Fabric 端通过 Forge Config API Port 注册并在加载/重载时自动烘焙。
- **伤害类型标签** (`ModDamageTypeTagsProvider`):为 `enkindling_bullet` 补上 `IS_FIRE` 等伤害类型标签,修复火焰免疫/抗性判定;Fabric 端新增 `FabricDamageTypeProvider` 通过动态注册表生成 damage_type JSON。
- **实体类型标签** (`ModEntityTypeTagsProvider` / `ChampionEntityTypes.Tags`):新增 `champions:is_ender`、`champions:allow_champions` 实体类型标签,取代硬编码实体判断(如 Infested 词条对末影系怪物生成末影螨,`infestedEnderParasite` 可配置)。
- **Fabric 攻击/心跳事件桥接** (`MixinLivingEntityTick`):通过 mixin 注入 `LivingEntity#tick`,在 Fabric 端补齐 TickEvent 派发与 PhaseProcessor 每 10 tick 的阶段评估。
- Fabric 端注册 Champion Egg 发射器 (dispenser) 行为。

### 变更 (Changed)

- 重构 `MinecraftMixin`(中键拾取)与 `MouseHelper`,精简为通过 `ChampionView`/`ChampionData` 的轻量实现。
- 内置词条 (Adaptable / Lively / Reflective / Shielding / Dampening / Knocking / Paralyzing / Wounding / Arctic / Desecrating / Enkindling / Infested / Magnetic / Hasty / Molten / Plagued) 统一迁移到标签/注册表驱动的实体与伤害类型判断。
- 配置系统重构:服务端配置加载/重载改为事件驱动(`NeoForgeModConfigEvents`),客户端配置独立成 spec。
- Datagen 整理:NeoForge 端新增 `ModDatapackProvider` 统一产出内置注册表数据;移除旧的 `DamageTypeProvider` 与 `AffixDefaults`。
- 更新各语言文件与 tier/damage_type 生成资源。

### 修复 (Fixed)

- Fabric 端配置在服务器重载 (reload) 后不重新烘焙的问题。
- `enkindling_bullet` 伤害未被识别为火焰伤害的问题。
- Infested 词条在末影系怪物上生成不正确寄生虫的问题。

### 移除 (Removed)

- `AffixDefaults`、旧版 `DamageTypeProvider` 等被注册表/标签方案替代的代码。
