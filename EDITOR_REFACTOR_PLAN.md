# Champions 游戏内编辑器重构计划

> 目标:把简陋的编辑器升级为可持续承担"游戏内自定义"的核心工具。

## 现状问题

1. `ChampionEditorScreen` 是 ~650 行的单体类:布局、JSON 读写、表单构建、渲染全部耦合。
2. 表单只覆盖极少数顶层字段(`id`/`weight`/`tier_range`),`entity_filter`、`affix_pools`、`phases`、modifier `conditions` 只能切 JSON 手写。
3. 新建条目只写入 `{}`,再靠 JSON 手补 —— 等于没有 UI。
4. JSON 模式无任何校验/提示,写错只能在 reload 后看 log。
5. 编辑产物只能写入内置的 `champions_editor` 数据包,无法导入/导出/管理数据包。

## 重构后架构

```
client/screen/
  ChampionEditorScreen.java          ← 壳:tabs / 列表 / raw JSON / 底部按钮
  editor/
    EditorSession.java               ← 全部编辑状态;弹窗往返可恢复
    json/JsonPathOps.java            ← 稳健的 JSON 路径读写(数字段=数组索引)
    widget/Row.java                  ← 行模型(header / field / action,支持缩进)
    widget/FormBuilder.java          ← 行式表单 DSL + 变更回调 + 弹窗宿主
    picker/PickerEntry.java          ← (id, display) 条目
    picker/RegistryPickerScreen.java ← 通用可搜索选择器(单选/多选)
    picker/PickerSources.java        ← 实体/属性/效果/类别/命名空间/affix 来源
    filter/FilterEditor.java         ← EntityFilter 递归树编辑器
    validate/JsonValidator.java      ← JSON 语法 + Codec 语义校验,键提示
    pane/EditorPane.java             ← Tab 面板接口
    pane/{Archetype,Tier,Modifier,Config,Packs}Pane.java
```

## 功能清单

### A. 选择器(EntityFilter)可视化编辑 —— 核心

- 递归树:`all_of` / `any_of` 可任意嵌套,子过滤器增删、类型原地切换。
- 8 种类型全部有专属配置行:
  - `entity_type` → **实体多选器**(搜索 + 翻译名 + id),whitelist 开关
  - `entity_tag` → tag id 输入 + whitelist
  - `mod_id` → 命名空间多选器(从注册表派生)
  - `mob_category` → 类别多选器
  - `attribute` → 属性选择器 + min/max
  - `any` → 说明行
- 同一套 FilterEditor 复用于 archetype `entity_filter` 与 modifier `conditions.entity_filter`。

### B. UI 模式细则全覆盖

- **Archetype**:`id`/`weight`/`tier_range` + 过滤器树 + **affix_pools**(每个池:tier_range、min/max_count、candidates:affix 选择器、weight、strength 范围,增删)+ **phases**(id、repeatable、condition 三类全参数、effects 三类全参数,增删)。
- **Modifier**:`attributeType`(选择器)、`enable`、`modifier.value`、`modifier.operation`(循环切换);`conditions`:entity_filter 树、`tier` min/max、`affixes.values` 多选(matches/count 留 JSON)。
- **Tier**:`level`、`display.color`、`display.icon`。
- **Config**:全部键。
- 结构性变更(增删节点/切类型)即时重建表单;文本编辑不重建、不丢焦点。

### C. JSON 模式内联提示

- 实时校验:语法错误(提取行号列号)、非对象错误、Codec 语义错误(缺字段/类型不匹配)。
- 键提示面板:当前类型必填/可选键速查。
- 状态显示在编辑器面板底部(绿 ✓ / 红 ✗)。

### D. 数据包管理(Packs tab)

- `EditorPayload` 增加 `packs: List<PackInfo>`(id/title/source/enabled),S2C 下发。
- Packs 页列出全部可用数据包,点击 **Enable/Disable** → 服务端 `PackRepository` 选集变更 + `reloadResources`(客户端乐观更新)。
- **导出**:当前编辑器内容打包为规范数据包 zip → `<world>/champions_exports/champions_<时间戳>.zip`,聊天回报路径。
- **导入**:扫描 `<world>/champions_imports/*.zip` → 复制进 `<world>/datapacks/` 并启用 + reload。
- 新 C2S 包 `EditorPackActionPacket`(`toggle` / `export` / `import`),双平台注册;服务端动作完成后回推最新 payload,编辑器**原地刷新** packs 列表、不打断编辑。

### E. 网络与平台

- `EditorPayload.STREAM_CODEC` 扩展 packs 字段(Fabric/NeoForge 自动跟随)。
- Fabric `registerServerPayloads` / NeoForge `reg.playToServer` 各加一条注册。
- 服务端逻辑集中在 `DatapackEditorHandler`(权限 level 2 校验保持不变)。

## 实施顺序

1. 框架:`EditorSession` / `Row` / `FormBuilder` / `JsonPathOps`
2. 选择器与选择器来源:`PickerSources` / `RegistryPickerScreen` / `FilterEditor`
3. 五个 Pane + 屏幕壳重写
4. JSON 校验面板
5. 数据包管理:payload 扩展、动作包、服务端 handler、平台注册
6. 编译三模块验证 → 更新 CHANGELOG → 提交推送
