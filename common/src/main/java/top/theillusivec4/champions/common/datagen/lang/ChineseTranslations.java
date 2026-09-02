package top.theillusivec4.champions.common.datagen.lang;

import top.theillusivec4.champions.common.datagen.ChampionLanguageProvider;

/** Chinese Simplified (zh_cn) translations. */
public final class ChineseTranslations {

    private ChineseTranslations() {}

    public static void add(ChampionLanguageProvider p) {
        // ── Affixes & ranks ─────────────────────────────────────────────────
        p.addAffix("adaptable",   "适应");
        p.addAffix("arctic",      "极寒");
        p.addAffix("dampening",   "抑制");
        p.addAffix("desecrating", "亵渎");
        p.addAffix("enkindling",  "点燃");
        p.addAffix("hasty",       "急速");
        p.addAffix("infested",    "感染");
        p.addAffix("knocking",    "爆震");
        p.addAffix("lively",      "活力");
        p.addAffix("magnetic",    "磁力");
        p.addAffix("molten",      "熔融");
        p.addAffix("paralyzing",  "瘫痪");
        p.addAffix("plagued",     "瘟疫");
        p.addAffix("reflective",  "反射");
        p.addAffix("shielding",   "保护");
        p.addAffix("wounding",    "创伤");
        p.addRank(1, "普通");
        p.addRank(2, "稀有");
        p.addRank(3, "精英");
        p.addRank(4, "传奇");
        p.addRank(5, "终极");

        // ── Commands ────────────────────────────────────────────────────────
        p.add("argument.champions.affix.unknown",     "未知词缀 %s");
        p.add("commands.champions.summon.success",    "召唤了新的 %s");
        p.add("commands.champions.egg.success",       "创建了 %s");
        p.add("command.champions.unknown_entity",     "未知实体: %s");
        p.add("command.champions.unknown_tier",       "没有找到等级 %s 的阶级。可用: %s");
        p.add("command.champions.egg.unknown_entity", "未知生物");
        p.add("command.champions.not_living_entity",  "%s 不是生物实体");
        p.add("command.champions.not_champion",       "%s 不是冠军");
        p.add("command.champions.spawn_cancelled",    "生成被监听器取消");
        p.add("command.champions.remove.success",     "已从 %s 移除冠军状态");
        p.add("command.champions.remove.success_deleted", "已移除冠军并删除实体");
        p.add("command.champions.info.title",         "冠军信息:");
        p.add("command.champions.info.tier",          "阶级: %s (等级 %s)");
        p.add("command.champions.info.affixes",       "词缀:");
        p.add("command.champions.info.affixes.none",  "无词缀");
        p.add("command.champions.info.affix_entry",   "  - %s (强度: %s)");
        p.add("command.champions.info.archetype",     "原型: %s");
        p.add("command.champions.help.title",         "§6冠军命令:§r");
        p.add("command.champions.help.summon",        "  §e/champions summon <实体|@category:X|@archetype:X> <等级> [词缀...] [at <x> <y> <z>]§r - 生成冠军");
        p.add("command.champions.help.apply",         "  §e/champions apply <目标> <等级> [词缀...]§r - 将冠军应用于现有实体");
        p.add("command.champions.help.remove",        "  §e/champions remove <目标> [删除实体]§r - 移除冠军状态");
        p.add("command.champions.help.info",          "  §e/champions info <目标>§r - 显示冠军信息 (所有玩家可用)");
        p.add("command.champions.help.egg",           "  §e/champions egg <实体|@category:X|@archetype:X> <等级|random> [词缀...]§r - 创建冠军蛋");
        p.add("command.champions.help.editor",        "  §e/champions editor§r - 打开数据包编辑器");

        // ── Item / advancements / stats ─────────────────────────────────────
        p.add("item.champions.egg",                 "强敌蛋");
        p.add("item.champions.egg.tooltip",         "随机词缀");
        p.add("item.champions.egg.random",          "随机");
        p.add("item.champions.egg.no_affixes",      "无词缀");
        p.add("advancements.champions.kill_a_champion.title",       "冠军猎人");
        p.add("advancements.champions.kill_a_champion.description", "击杀一个强大的敌对怪物");
        p.add("stat.champions.champion_mobs_killed", "冠军怪物击杀数");
        p.add("config.jade.plugin_champions.enable_affix_compact", "启用jade词条兼容");

        // ── In-game editor screen (gui.champions.*) ─────────────────────────
        p.add("gui.champions.editor.title",         "冠军编辑器");
        p.add("gui.champions.editor.tab.archetypes", "原型");
        p.add("gui.champions.editor.tab.tiers",      "阶级");
        p.add("gui.champions.editor.tab.modifiers",  "修饰符");
        p.add("gui.champions.editor.tab.config",     "配置");
        p.add("gui.champions.editor.tab.packs",      "数据包");
        p.add("gui.champions.editor.view.form",     "表单");
        p.add("gui.champions.editor.view.json",     "JSON");
        p.add("gui.champions.editor.new",           "§a+ 新建");
        p.add("gui.champions.editor.delete",        "§c删除");
        p.add("gui.champions.editor.save_reload",   "保存并重载");
        p.add("gui.champions.editor.close",         "关闭");
        p.add("gui.champions.editor.entries",       "§8条目 §7%s");
        p.add("gui.champions.editor.unsaved",       "§e● %s 项未保存");
        p.add("gui.champions.editor.error.not_object", "JSON 必须是对象");
        p.add("gui.champions.editor.error.invalid_json", "无效 JSON: %s");
        p.add("gui.champions.editor.toggle.true",   "§a是");
        p.add("gui.champions.editor.toggle.false",  "§c否");
        p.add("gui.champions.editor.pick_affix",    "选择词缀… §8%s");

        p.add("gui.champions.picker.done",          "完成");
        p.add("gui.champions.picker.cancel",        "取消");
        p.add("gui.champions.picker.search_hint",   "§7搜索…");
        p.add("gui.champions.picker.selected",      "已选: %s ·");
        p.add("gui.champions.picker.count",         "%s / %s");
        p.add("gui.champions.picker.title.affixes",       "词缀");
        p.add("gui.champions.picker.title.affix_values",  "词缀值");
        p.add("gui.champions.picker.title.entity_types",  "实体类型");
        p.add("gui.champions.picker.title.mod_namespaces", "模组命名空间");
        p.add("gui.champions.picker.title.mob_categories", "生物类别");
        p.add("gui.champions.picker.title.filter_type",   "过滤类型");

        p.add("gui.champions.editor.label.id",             "ID");
        p.add("gui.champions.editor.label.weight",         "权重");
        p.add("gui.champions.editor.label.type",           "类型");
        p.add("gui.champions.editor.label.min",            "最小值");
        p.add("gui.champions.editor.label.max",            "最大值");
        p.add("gui.champions.editor.label.tier_min",       "阶级下限");
        p.add("gui.champions.editor.label.tier_max",       "阶级上限");
        p.add("gui.champions.editor.label.min_count",      "数量下限");
        p.add("gui.champions.editor.label.max_count",      "数量上限");
        p.add("gui.champions.editor.label.affix",          "词缀");
        p.add("gui.champions.editor.label.min_strength",   "最小强度");
        p.add("gui.champions.editor.label.max_strength",   "最大强度");
        p.add("gui.champions.editor.label.strength",       "强度");
        p.add("gui.champions.editor.label.below",          "低于");
        p.add("gui.champions.editor.label.seconds",        "秒数");
        p.add("gui.champions.editor.label.count",          "数量");
        p.add("gui.champions.editor.label.amount",         "增量");
        p.add("gui.champions.editor.label.operation",      "运算方式");
        p.add("gui.champions.editor.label.attribute",      "属性");
        p.add("gui.champions.editor.label.effect",         "效果");
        p.add("gui.champions.editor.label.amplifier",      "效果等级");
        p.add("gui.champions.editor.label.infinite",       "无限持续");
        p.add("gui.champions.editor.label.duration_ticks", "持续时间 (tick)");
        p.add("gui.champions.editor.label.enable",         "启用");
        p.add("gui.champions.editor.label.value",          "数值");
        p.add("gui.champions.editor.label.level",          "等级");
        p.add("gui.champions.editor.label.color",          "颜色");
        p.add("gui.champions.editor.label.icon",           "图标");
        p.add("gui.champions.editor.label.repeatable",     "可重复");
        p.add("gui.champions.editor.label.tag",            "标签");
        p.add("gui.champions.editor.label.whitelist",      "白名单");
        p.add("gui.champions.editor.label.state",          "状态");

        p.add("gui.champions.editor.header.archetype",       "原型");
        p.add("gui.champions.editor.header.tier_range",      "阶级范围");
        p.add("gui.champions.editor.header.entity_filter",   "实体过滤器");
        p.add("gui.champions.editor.header.affix_pools",     "词缀池");
        p.add("gui.champions.editor.header.pool",            "词缀池 %s");
        p.add("gui.champions.editor.header.candidates",      "候选项");
        p.add("gui.champions.editor.header.candidate",       "候选项 %s");
        p.add("gui.champions.editor.header.phases",          "阶段");
        p.add("gui.champions.editor.header.phase",           "阶段: %s");
        p.add("gui.champions.editor.header.condition",       "条件");
        p.add("gui.champions.editor.header.effects",         "效果");
        p.add("gui.champions.editor.header.effect",          "效果 %s");
        p.add("gui.champions.editor.header.modifier_setting", "修饰符设置");
        p.add("gui.champions.editor.header.modifier",        "修饰符");
        p.add("gui.champions.editor.header.conditions",      "条件");
        p.add("gui.champions.editor.header.tier",            "阶级");
        p.add("gui.champions.editor.header.display",         "显示");
        p.add("gui.champions.editor.header.import_export",   "导入 / 导出");
        p.add("gui.champions.editor.header.world_datapacks", "世界数据包");
        p.add("gui.champions.editor.header.pack",            "数据包: %s");
        p.add("gui.champions.editor.header.server_config",   "服务端配置");
        p.add("gui.champions.editor.header.filter",          "过滤器 %s");

        p.add("gui.champions.editor.action.add_candidate",     "+ 添加候选项");
        p.add("gui.champions.editor.action.add_pool",          "+ 添加词缀池");
        p.add("gui.champions.editor.action.add_effect",        "+ 添加效果");
        p.add("gui.champions.editor.action.add_phase",         "+ 添加阶段");
        p.add("gui.champions.editor.action.add_filter",        "+ 添加过滤器");
        p.add("gui.champions.editor.action.add_child_filter",  "+ 添加子过滤器");
        p.add("gui.champions.editor.action.pick_affix_values",  "词缀值… (已选 %s 个)");
        p.add("gui.champions.editor.action.pick_entity_types",  "实体类型… (已选 %s 个)");
        p.add("gui.champions.editor.action.pick_mod_namespaces", "模组命名空间… (已选 %s 个)");
        p.add("gui.champions.editor.action.pick_categories",    "类别… (已选 %s 个)");
        p.add("gui.champions.editor.action.export",            "§b导出编辑器内容 → zip");
        p.add("gui.champions.editor.action.import",            "§b从 champions_imports/ 导入 zip");

        p.add("gui.champions.editor.hint.unknown_condition", "§c未知条件类型");
        p.add("gui.champions.editor.hint.unknown_effect",    "§c未知效果类型");
        p.add("gui.champions.editor.hint.unknown_filter",    "§c未知过滤器类型: %s");
        p.add("gui.champions.editor.hint.matches_every",     "匹配所有实体");
        p.add("gui.champions.editor.hint.filters_anded",     "子过滤器为“与”关系 (须全部满足)");
        p.add("gui.champions.editor.hint.filters_ored",      "子过滤器为“或”关系 (满足其一即可)");
        p.add("gui.champions.editor.hint.tier_display",      "颜色: 十六进制字符串 · 图标: 贴图路径");
        p.add("gui.champions.editor.hint.json_view_only",    "affixes.matches / affixes.count: 请在 JSON 视图中编辑");
        p.add("gui.champions.editor.hint.export_target",     "将生成 champions_<时间>.zip 到");
        p.add("gui.champions.editor.hint.exports_dir",       "<world>/champions_exports/");
        p.add("gui.champions.editor.hint.import_dir",        "将数据包 zip 放入 <world>/champions_imports/");
        p.add("gui.champions.editor.hint.import_copied",     "它们会被复制到 datapacks/ 并启用");
        p.add("gui.champions.editor.hint.no_packs",          "未加载数据包");
        p.add("gui.champions.editor.hint.packs_enabled",     "%s / %s 已启用");
        p.add("gui.champions.editor.hint.reload_on_toggle",  "切换后会重载服务端资源");
        p.add("gui.champions.editor.hint.select_pack",       "在列表中选择数据包以启用/禁用");
        p.add("gui.champions.editor.hint.config_reload",     "配置在“保存并重载”后生效");
        p.add("gui.champions.editor.pack.enabled",           "§a● 已启用");
        p.add("gui.champions.editor.pack.disabled",          "§c○ 已禁用");

        p.add("gui.champions.editor.filter.type.any",          "任意");
        p.add("gui.champions.editor.filter.type.all_of",       "全部满足 (AND)");
        p.add("gui.champions.editor.filter.type.any_of",       "任一满足 (OR)");
        p.add("gui.champions.editor.filter.type.entity_type",  "实体类型");
        p.add("gui.champions.editor.filter.type.entity_tag",   "实体标签");
        p.add("gui.champions.editor.filter.type.mod_id",       "模组 ID");
        p.add("gui.champions.editor.filter.type.mob_category", "生物类别");
        p.add("gui.champions.editor.filter.type.attribute",    "属性");
    }
}
