package top.theillusivec4.champions.common.datagen.lang;

import top.theillusivec4.champions.common.datagen.ChampionLanguageProvider;

/**
 * English (en_us) translations — the baseline. Any key missing from another
 * locale falls back to en_us at runtime.
 */
public final class EnglishTranslations {

    private EnglishTranslations() {}

    public static void add(ChampionLanguageProvider p) {
        // ── Affixes & ranks ─────────────────────────────────────────────────
        p.addAffix("adaptable",   "Adaptable");
        p.addAffix("arctic",      "Arctic");
        p.addAffix("dampening",   "Dampening");
        p.addAffix("desecrating", "Desecrating");
        p.addAffix("enkindling",  "Enkindling");
        p.addAffix("hasty",       "Hasty");
        p.addAffix("infested",    "Infested");
        p.addAffix("knocking",    "Knocking");
        p.addAffix("lively",      "Lively");
        p.addAffix("magnetic",    "Magnetic");
        p.addAffix("molten",      "Molten");
        p.addAffix("paralyzing",  "Paralyzing");
        p.addAffix("plagued",     "Plagued");
        p.addAffix("reflective",  "Reflective");
        p.addAffix("shielding",   "Shielding");
        p.addAffix("wounding",    "Wounding");
        p.addRank(1, "Common");
        p.addRank(2, "Skilled");
        p.addRank(3, "Elite");
        p.addRank(4, "Legendary");
        p.addRank(5, "Ultimate");

        // ── Commands ────────────────────────────────────────────────────────
        p.add("argument.champions.affix.unknown",     "Unknown affix %s");
        p.add("commands.champions.summon.success",    "Summoned new %s");
        p.add("commands.champions.egg.success",       "Created new %s");
        p.add("command.champions.unknown_entity",     "Unknown entity: %s");
        p.add("command.champions.unknown_tier",       "No tier found with level %s. Available: %s");
        p.add("command.champions.egg.unknown_entity", "Unknown entity");
        p.add("command.champions.not_living_entity",  "%s is not a living entity");
        p.add("command.champions.not_champion",       "%s is not a champion");
        p.add("command.champions.spawn_cancelled",    "Spawn was cancelled by a listener");
        p.add("command.champions.remove.success",     "Removed champion status from %s");
        p.add("command.champions.remove.success_deleted", "Removed champion and deleted entity");
        p.add("command.champions.info.title",         "Champion Info:");
        p.add("command.champions.info.tier",          "Tier: %s (Level %s)");
        p.add("command.champions.info.affixes",       "Affixes:");
        p.add("command.champions.info.affixes.none",  "No affixes");
        p.add("command.champions.info.affix_entry",   "  - %s (Strength: %s)");
        p.add("command.champions.info.archetype",     "Archetype: %s");
        p.add("command.champions.help.title",         "§6Champions Commands:§r");
        p.add("command.champions.help.summon",        "  §e/champions summon <entity|@category:X|@archetype:X> <tier> [affixes...] [at <x> <y> <z>]§r - Spawn champion");
        p.add("command.champions.help.apply",         "  §e/champions apply <target> <tier> [affixes...]§r - Apply champion to existing entity");
        p.add("command.champions.help.remove",        "  §e/champions remove <target> [deleteEntity]§r - Remove champion status");
        p.add("command.champions.help.info",          "  §e/champions info <target>§r - Show champion info (available to all players)");
        p.add("command.champions.help.egg",           "  §e/champions egg <entity|@category:X|@archetype:X> <tier|random> [affixes...]§r - Create champion egg");
        p.add("command.champions.help.editor",        "  §e/champions editor§r - Open datapack editor");

        // ── Item / advancements / stats ─────────────────────────────────────
        p.add("item.champions.egg",                 "Champion Egg");
        p.add("item.champions.egg.tooltip",         "Random Affixes");
        p.add("item.champions.egg.random",          "Random");
        p.add("item.champions.egg.no_affixes",      "No affixes");
        p.add("advancements.champions.kill_a_champion.title",       "Champion Hunter");
        p.add("advancements.champions.kill_a_champion.description", "Kill a powerful hostile monster");
        p.add("stat.champions.champion_mobs_killed", "Champion Mobs Killed");
        p.add("config.jade.plugin_champions.enable_affix_compact", "Enable Jade affix compact");

        // ── In-game editor screen (gui.champions.*) ─────────────────────────
        p.add("gui.champions.editor.title",         "Champions Editor");
        p.add("gui.champions.editor.tab.archetypes", "Archetypes");
        p.add("gui.champions.editor.tab.tiers",      "Tiers");
        p.add("gui.champions.editor.tab.modifiers",  "Modifiers");
        p.add("gui.champions.editor.tab.config",     "Config");
        p.add("gui.champions.editor.tab.packs",      "Packs");
        p.add("gui.champions.editor.view.form",     "Form");
        p.add("gui.champions.editor.view.json",     "JSON");
        p.add("gui.champions.editor.new",           "§a+ New");
        p.add("gui.champions.editor.delete",        "§cDelete");
        p.add("gui.champions.editor.save_reload",   "Save & Reload");
        p.add("gui.champions.editor.close",         "Close");
        p.add("gui.champions.editor.entries",       "§8ENTRIES §7%s");
        p.add("gui.champions.editor.unsaved",       "§e● %s unsaved");
        p.add("gui.champions.editor.error.not_object", "JSON must be an object");
        p.add("gui.champions.editor.error.invalid_json", "Invalid JSON: %s");
        p.add("gui.champions.editor.toggle.true",   "§aTrue");
        p.add("gui.champions.editor.toggle.false",  "§cFalse");
        p.add("gui.champions.editor.pick_affix",    "Pick affix…  §8%s");

        p.add("gui.champions.picker.done",          "Done");
        p.add("gui.champions.picker.cancel",        "Cancel");
        p.add("gui.champions.picker.search_hint",   "§7Search…");
        p.add("gui.champions.picker.selected",      "Selected: %s ·");
        p.add("gui.champions.picker.count",         "%s / %s");
        p.add("gui.champions.picker.title.affixes",       "Affixes");
        p.add("gui.champions.picker.title.affix_values",  "Affix values");
        p.add("gui.champions.picker.title.entity_types",  "Entity types");
        p.add("gui.champions.picker.title.mod_namespaces", "Mod namespaces");
        p.add("gui.champions.picker.title.mob_categories", "Mob categories");
        p.add("gui.champions.picker.title.filter_type",   "Filter type");

        p.add("gui.champions.editor.label.id",             "id");
        p.add("gui.champions.editor.label.weight",         "weight");
        p.add("gui.champions.editor.label.type",           "type");
        p.add("gui.champions.editor.label.min",            "min");
        p.add("gui.champions.editor.label.max",            "max");
        p.add("gui.champions.editor.label.tier_min",       "tier min");
        p.add("gui.champions.editor.label.tier_max",       "tier max");
        p.add("gui.champions.editor.label.min_count",      "min count");
        p.add("gui.champions.editor.label.max_count",      "max count");
        p.add("gui.champions.editor.label.affix",          "affix");
        p.add("gui.champions.editor.label.min_strength",   "min strength");
        p.add("gui.champions.editor.label.max_strength",   "max strength");
        p.add("gui.champions.editor.label.strength",       "strength");
        p.add("gui.champions.editor.label.below",          "below");
        p.add("gui.champions.editor.label.seconds",        "seconds");
        p.add("gui.champions.editor.label.count",          "count");
        p.add("gui.champions.editor.label.amount",         "amount");
        p.add("gui.champions.editor.label.operation",      "operation");
        p.add("gui.champions.editor.label.attribute",      "attribute");
        p.add("gui.champions.editor.label.effect",         "effect");
        p.add("gui.champions.editor.label.amplifier",      "amplifier");
        p.add("gui.champions.editor.label.infinite",       "infinite");
        p.add("gui.champions.editor.label.duration_ticks", "duration (ticks)");
        p.add("gui.champions.editor.label.enable",         "enable");
        p.add("gui.champions.editor.label.value",          "value");
        p.add("gui.champions.editor.label.level",          "level");
        p.add("gui.champions.editor.label.color",          "color");
        p.add("gui.champions.editor.label.icon",           "icon");
        p.add("gui.champions.editor.label.repeatable",     "repeatable");
        p.add("gui.champions.editor.label.tag",            "tag");
        p.add("gui.champions.editor.label.whitelist",      "whitelist");
        p.add("gui.champions.editor.label.state",          "state");

        p.add("gui.champions.editor.header.archetype",       "Archetype");
        p.add("gui.champions.editor.header.tier_range",      "Tier Range");
        p.add("gui.champions.editor.header.entity_filter",   "Entity Filter");
        p.add("gui.champions.editor.header.affix_pools",     "Affix Pools");
        p.add("gui.champions.editor.header.pool",            "Pool %s");
        p.add("gui.champions.editor.header.candidates",      "Candidates");
        p.add("gui.champions.editor.header.candidate",       "Candidate %s");
        p.add("gui.champions.editor.header.phases",          "Phases");
        p.add("gui.champions.editor.header.phase",           "Phase: %s");
        p.add("gui.champions.editor.header.condition",       "Condition");
        p.add("gui.champions.editor.header.effects",         "Effects");
        p.add("gui.champions.editor.header.effect",          "Effect %s");
        p.add("gui.champions.editor.header.modifier_setting", "Modifier Setting");
        p.add("gui.champions.editor.header.modifier",        "Modifier");
        p.add("gui.champions.editor.header.conditions",      "Conditions");
        p.add("gui.champions.editor.header.tier",            "Tier");
        p.add("gui.champions.editor.header.display",         "Display");
        p.add("gui.champions.editor.header.import_export",   "Import / Export");
        p.add("gui.champions.editor.header.world_datapacks", "World Datapacks");
        p.add("gui.champions.editor.header.pack",            "Pack: %s");
        p.add("gui.champions.editor.header.server_config",   "Server Config");
        p.add("gui.champions.editor.header.filter",          "Filter %s");

        p.add("gui.champions.editor.action.add_candidate",     "+ Add candidate");
        p.add("gui.champions.editor.action.add_pool",          "+ Add pool");
        p.add("gui.champions.editor.action.add_effect",        "+ Add effect");
        p.add("gui.champions.editor.action.add_phase",         "+ Add phase");
        p.add("gui.champions.editor.action.add_filter",        "+ Add filter");
        p.add("gui.champions.editor.action.add_child_filter",  "+ Add child filter");
        p.add("gui.champions.editor.action.pick_affix_values",  "Affix values… (%s selected)");
        p.add("gui.champions.editor.action.pick_entity_types",  "Entity types… (%s selected)");
        p.add("gui.champions.editor.action.pick_mod_namespaces", "Mod namespaces… (%s selected)");
        p.add("gui.champions.editor.action.pick_categories",    "Categories… (%s selected)");
        p.add("gui.champions.editor.action.export",            "§bExport editor content → zip");
        p.add("gui.champions.editor.action.import",            "§bImport zips from champions_imports/");

        p.add("gui.champions.editor.hint.unknown_condition", "§cunknown condition type");
        p.add("gui.champions.editor.hint.unknown_effect",    "§cunknown effect type");
        p.add("gui.champions.editor.hint.unknown_filter",    "§cunknown filter type: %s");
        p.add("gui.champions.editor.hint.matches_every",     "matches every entity");
        p.add("gui.champions.editor.hint.filters_anded",     "child filters are ANDed");
        p.add("gui.champions.editor.hint.filters_ored",      "child filters are ORed");
        p.add("gui.champions.editor.hint.tier_display",      "color: hex string · icon: texture path");
        p.add("gui.champions.editor.hint.json_view_only",    "affixes.matches / affixes.count: use JSON view");
        p.add("gui.champions.editor.hint.export_target",     "writes champions_<time>.zip into");
        p.add("gui.champions.editor.hint.exports_dir",       "<world>/champions_exports/");
        p.add("gui.champions.editor.hint.import_dir",        "drop datapack zips into <world>/champions_imports/");
        p.add("gui.champions.editor.hint.import_copied",     "they are copied into datapacks/ and enabled");
        p.add("gui.champions.editor.hint.no_packs",          "no packs loaded");
        p.add("gui.champions.editor.hint.packs_enabled",     "%s / %s enabled");
        p.add("gui.champions.editor.hint.reload_on_toggle",  "toggling reloads server resources");
        p.add("gui.champions.editor.hint.select_pack",       "select a pack in the list to enable/disable it");
        p.add("gui.champions.editor.hint.config_reload",     "config values apply on Save & Reload");
        p.add("gui.champions.editor.pack.enabled",           "§a● enabled");
        p.add("gui.champions.editor.pack.disabled",          "§c○ disabled");

        p.add("gui.champions.editor.filter.type.any",          "any");
        p.add("gui.champions.editor.filter.type.all_of",       "all_of (AND)");
        p.add("gui.champions.editor.filter.type.any_of",       "any_of (OR)");
        p.add("gui.champions.editor.filter.type.entity_type",  "entity_type");
        p.add("gui.champions.editor.filter.type.entity_tag",   "entity_tag");
        p.add("gui.champions.editor.filter.type.mod_id",       "mod_id");
        p.add("gui.champions.editor.filter.type.mob_category", "mob_category");
        p.add("gui.champions.editor.filter.type.attribute",    "attribute");
    }
}
