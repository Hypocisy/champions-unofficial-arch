# 发布到 Modrinth / CurseForge

## 一次性配置

1. **项目 ID**
   - Modrinth：已在 `gradle.properties` 中配置（`modrinth_project_id`）。
   - CurseForge：在 `gradle.properties` 中填 `curseforge_id=`（**必须为数字项目 ID**，右键 CurseForge 项目页面的 About Project 区块可看到）。
   - 首次上传建议先 `CF_DEBUG=1 ./gradlew publishModPlatforms` —— debug 模式只打印上传元数据不真传。

2. **API Token**（不要提交到仓库）
   - Modrinth：https://modrinth.com/settings/pat → 创建 token（需 `Write projects` 权限）。
   - CurseForge：https://authors.curseforge.com/account/api-tokens → 生成 token。
   - 二选一存放方式：
     - 环境变量：`MODRINTH_TOKEN`、`CURSEFORGE_TOKEN`
     - 或用户级 `~/.gradle/gradle.properties` 中加：
       ```
       modrinth_token=xxx
       curseforge_token=xxx
       ```

## 发布流程

```bash
./gradlew publishModPlatforms
```

该任务会：
- 构建 `fabric` / `neoforge` 两端的发布 jar（`remapJar`）
- 上传到 Modrinth（版本号 `21.1.1.x-fabric` / `-neoforge`，MC 1.21.1）
- 上传到 CurseForge（同名，markdown 更新日志）
- 更新日志**自动**取自 `CHANGELOG.md` 中 `## [当前版本]` 段落（按 `gradle.properties` 的 `mod_version` 匹配）

只发单个平台：

```bash
./gradlew :fabric:modrinth :fabric:curseforge    # 仅 Fabric
./gradlew :neoforge:modrinth :neoforge:curseforge  # 仅 NeoForge
```

## 发布新版本检查清单

1. `gradle.properties` 里提升 `mod_version`
2. 在 `CHANGELOG.md` 顶部新增 `## [<新版本>]` 段落（中文或英文均可，markdown 语法）
3. `./gradlew publishModPlatforms`

> 缺 token 或缺对应项目 ID 时，上传任务会自动跳过（onlyIf 守卫），不会误传半套。

## 依赖关系（已预配置）

| 平台 | 必需 | 可选 |
|---|---|---|
| Fabric | Fabric API、Forge Config API Port | Jade、Mod Menu |
| NeoForge | Architectury API | KubeJS、Jade、The One Probe |
