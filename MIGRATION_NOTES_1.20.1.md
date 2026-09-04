# 1.21.1 → 1.20.1 降级迁移总结

> 2026-09-05 · 分支 `1.20.1`

本次迁移将 Champions Unofficial 从 **Minecraft 1.21.1 (Java 21) / NeoForge + Fabric** 完整降级到 **Minecraft 1.20.1 (Java 17) / Forge + NeoForge + Fabric**。所有游戏逻辑保留在 `common/`，平台差异仅存在于各平台模块中。

## 一、依赖与构建

| 项目 | 1.21.1 | 1.20.1 |
|---|---|---|
| Java | 21 | 17 |
| Architectury API | 13.0.8 | 9.2.14 |
| Cardinal Components | 6.1.2 | 5.2.3 |
| Fabric API | 0.116.12+1.21.1 | 0.92.12+1.20.1 |
| Fabric Loader | 0.19.3 | 0.19.5 |
| KubeJS | 2101.7.x | 2001.6.5-build.26 |
| Forge | — | 47.4.23 |
| NeoForge | 21.1.215 | 保留模块（本分支不构建） |

- `settings.gradle` 增加 Forge maven；`enabled_platforms` 保持 `fabric,neoforge` 字段，NeoForge 模块**保留在仓库中**但本分支不参与构建——直接删除会在此后切换分支时丢失该工作区。
- 发布目标由 `:neoforge:*` 改为 `:forge:*`（`publishModPlatforms`），并临时关闭了 changelog 缺失检查。

## 二、核心 API 迁移（common/）

### 网络层统一重构
1.21.1 各平台持有自己的 packet 类（`ChampionSyncPacket`、`TierSyncPacket`、`ClientTierCache` 均有三份）。迁移后：
- **`common/network/`**：`ChampionSyncPacket`、`TierSyncPacket`、`ClientTierCache` 成为平台无关实现，基于 `FriendlyByteBuf` 编解码；`ChampionSyncPacket` 保留 `IAffixClientSync` 的 payload 层（注册为平台自定义 packet）。
- 平台只保留一个 handler：`FabricPacketHandler` / `ForgePacketHandler`（NeoForge 版本随模块保留）。
- tier 数据同步仅携带 `id / level / display.color`，图标纹理路径由客户端按 id 解析。

### 1.21 → 1.20 的 Codec 兼容层
新增 `common/utils/ChampionsCodecs.java`，为 1.20.1 补齐 1.21 才有的 vanilla codec：
- `AttributeModifier.Operation` 编解码（`add_value` / `add_multiplied_base` / `add_multiplied_total`，兼容旧名）；
- `MinMaxBounds.Doubles` codec；
- `Either` / optional 字段的辅助包装。

JSON 文件形状与 `DATAPACK_GUIDE.md` 保持一致，1.21.1 的 datapack 无需改动即可在 1.20.1 加载。

### 其余 API 适配
- `ResourceLocation.fromNamespaceAndPath` → `new ResourceLocation(...)`（全仓 ~150 处，收口在 `Utils.key()`）。
- 注册流程改为 1.20.1 的 `DeferredRegister` / `FabricRegistryBuilder` + `Registry.register`；attribute modifier 设置数据改回 `AttributeModifier` 旧构造器。
- 遍历 tag/damage type 的 1.21 registry lookups 全部改为 1.20.1 等价物；`is_fire` 等伤害类型 tag JSON 改用 `forge:` / `neoforge:` 命名空间。

## 三、平台层变化

### Forge（新模块）
- 从 NeoForge 模块 fork：`neoforge.mods.toml` → `mods.toml`，事件桥、附件系统（capability）、KubeJS、The One Probe 集成同步移植。
- 注册核心统一为 Forge 的 `DeferredRegister`，网络为 `SimpleChannel`。
- `forge/gradle.properties` 固定 `loom.platform=forge`。

### Fabric
- 附件系统继续使用 Cardinal Components；tick/attack 事件桥保留 Mixin 注入。
- 依赖坐标降级到 1.20.1 版本线（见上表）。

### NeoForge
- 模块源码保留在仓库（`gradle.properties` 中 `neoforge_version=21.1.215` 指回 1.21.1），本分支不构建。

## 四、仓库清理（本次提交）

1. **移除误提交的 datagen 生成物**（~180 个文件）：`*/src/generated/resources/` 下的 archetype/tier/lang/modifier_setting JSON 与 `.cache` 全部从 git 移除，并写入 `.gitignore`（`common|forge|fabric|neoforge/src/generated/`）。这些文件可随时用 datagen 重新生成。
2. **移除提交过的真实 token**：`gradle.properties` 中的 Modrinth / CurseForge token 已清空（`PUBLISHING.md` 注明 token 只应来自环境变量 `MODRINTH_TOKEN` / `CURSEFORGE_TOKEN`）。⚠️ 这些 token 早已随历史提交公开，**必须去 Modrinth / CurseForge 后台吊销重发**。
3. 删除杂项：`.architectury-transformer/debug.log`、`build.log` 等运行时产物不再纳入版本管理。

## 五、验证

- `./gradlew build` 通过（Fabric + Forge）。
- `common` 内 datagen codec 单测（`TierGeneratedDataTest` 等）基于生成的 JSON 树运行，生成物重新跑 `runData` 即可恢复。
- 游戏内验证（生成、phase、编辑器、Jade/TOP HUD）在 1.20.1 客户端完成。
