package top.theillusivec4.champions.common.datagen.lang;

import top.theillusivec4.champions.common.datagen.ChampionLanguageProvider;

/** Ukrainian (uk_ua) translations. */
public final class UkrainianTranslations {

    private UkrainianTranslations() {}

    public static void add(ChampionLanguageProvider p) {
        // ── Affixes & ranks ─────────────────────────────────────────────────
        p.addAffix("adaptable",   "Адаптований");
        p.addAffix("arctic",      "Арктика");
        p.addAffix("dampening",   "Зволоження");
        p.addAffix("desecrating", "Осквернення");
        p.addAffix("enkindling",  "Осквернення");
        p.addAffix("hasty",       "Поспішно");
        p.addAffix("infested",    "Заражений");
        p.addAffix("knocking",    "Стукіт");
        p.addAffix("lively",      "Жвавий");
        p.addAffix("magnetic",    "Магнітний");
        p.addAffix("molten",      "Розплавлений");
        p.addAffix("paralyzing",  "Паралізуючий");
        p.addAffix("plagued",     "Заражений");
        p.addAffix("reflective",  "Світловідбиваючі");
        p.addAffix("shielding",   "Екранізація");
        p.addAffix("wounding",    "Пораненний");
        p.addRank(1, "Звичайний");
        p.addRank(2, "Кваліфікований");
        p.addRank(3, "Еліта");
        p.addRank(4, "Легендарний");
        p.addRank(5, "Кінцевий");

        // ── Commands ────────────────────────────────────────────────────────
        p.add("argument.champions.affix.unknown",     "Невідомий афікс %s");
        p.add("commands.champions.summon.success",    "Викликається новий %s");
        p.add("commands.champions.egg.success",       "Створено новий %s");
        p.add("command.champions.unknown_entity",     "Невідома сутність: %s");
        p.add("command.champions.unknown_tier",       "Рівень %s не знайдено. Доступно: %s");
        p.add("command.champions.egg.unknown_entity", "Невідома сутність");
        p.add("command.champions.not_living_entity",  "%s не є живою істотою.");
        p.add("command.champions.not_champion",       "%s не є чемпіоном.");
        p.add("command.champions.spawn_cancelled",    "Створення скасовано слухачем SpawnChampion.");
        p.add("command.champions.remove.success",     "Видалено статус чемпіона з %s");
        p.add("command.champions.remove.success_deleted", "Статус чемпіона видалено і сутність видалено.");
        p.add("command.champions.info.title",         "§6Інформація про чемпіона:§r");
        p.add("command.champions.info.tier",          "  §eРівень:§r %s (Рівень %s)");
        p.add("command.champions.info.affixes",       "  §eАфікси:§r");
        p.add("command.champions.info.affixes.none",  "  §eАфікси:§r Немає");
        p.add("command.champions.info.affix_entry",   "    - %s (Сила: %s)");
        p.add("command.champions.info.archetype",     "  §eАрхетип:§r %s");
        p.add("command.champions.help.title",         "§6Команди Champions:§r");
        p.add("command.champions.help.summon",        "  §e/champions summon <сутність|@category:X|@archetype:X> <рівень> [афікси...] [at <x> <y> <z>]§r - Створити чемпіона");
        p.add("command.champions.help.apply",         "  §e/champions apply <ціль> <рівень> [афікси...]§r - Застосувати чемпіона до наявної сутності");
        p.add("command.champions.help.remove",        "  §e/champions remove <ціль> [видалитиСутність]§r - Видалити статус чемпіона");
        p.add("command.champions.help.info",          "  §e/champions info <ціль>§r - Показати інформацію про чемпіона (доступно всім гравцям)");
        p.add("command.champions.help.egg",           "  §e/champions egg <сутність|@category:X|@archetype:X> <рівень|random> [афікси...]§r - Створити яйце чемпіона");
        p.add("command.champions.help.editor",        "  §e/champions editor§r - Відкрити редактор датапаків");

        // ── Item / advancements / stats ─────────────────────────────────────
        p.add("item.champions.egg",                 "Яйце чемпіона");
        p.add("item.champions.egg.tooltip",         "Випадкові афікси");
        p.add("item.champions.egg.random",          "Випадково");
        p.add("item.champions.egg.no_affixes",      "Немає афіксів");
        p.add("advancements.champions.kill_a_champion.title",       "Чемпіонський мисливець");
        p.add("advancements.champions.kill_a_champion.description", "Убийте потужного ворожого монстра");
        p.add("stat.champions.champion_mobs_killed", "Чемпіонські натовпи вбито");
        p.add("config.jade.plugin_champions.enable_affix_compact", "Увімкнути компактний показ афіксів у Jade");

        // ── In-game editor screen (gui.champions.*) ─────────────────────────
        p.add("gui.champions.editor.title",         "Редактор чемпіонів");
        p.add("gui.champions.editor.tab.archetypes", "Архетипи");
        p.add("gui.champions.editor.tab.tiers",      "Рівні");
        p.add("gui.champions.editor.tab.modifiers",  "Модифікатори");
        p.add("gui.champions.editor.tab.config",     "Конфігурація");
        p.add("gui.champions.editor.tab.packs",      "Пакети даних");
        p.add("gui.champions.editor.view.form",     "Форма");
        p.add("gui.champions.editor.view.json",     "JSON");
        p.add("gui.champions.editor.new",           "§a+ Створити");
        p.add("gui.champions.editor.delete",        "§cВидалити");
        p.add("gui.champions.editor.save_reload",   "Зберегти й перезавантажити");
        p.add("gui.champions.editor.close",         "Закрити");
        p.add("gui.champions.editor.entries",       "§8ЗАПИСИ §7%s");
        p.add("gui.champions.editor.unsaved",       "§e● не збережено: %s");
        p.add("gui.champions.editor.error.not_object", "JSON має бути об'єктом");
        p.add("gui.champions.editor.error.invalid_json", "Некоректний JSON: %s");
        p.add("gui.champions.editor.toggle.true",   "§aТак");
        p.add("gui.champions.editor.toggle.false",  "§cНі");
        p.add("gui.champions.editor.pick_affix",    "Обрати афікс… §8%s");

        p.add("gui.champions.picker.done",          "Готово");
        p.add("gui.champions.picker.cancel",        "Скасувати");
        p.add("gui.champions.picker.search_hint",   "§7Пошук…");
        p.add("gui.champions.picker.selected",      "Вибрано: %s ·");
        p.add("gui.champions.picker.count",         "%s / %s");
        p.add("gui.champions.picker.title.affixes",       "Афікси");
        p.add("gui.champions.picker.title.affix_values",  "Значення афіксів");
        p.add("gui.champions.picker.title.entity_types",  "Типи сутностей");
        p.add("gui.champions.picker.title.mod_namespaces", "Простори імен модів");
        p.add("gui.champions.picker.title.mob_categories", "Категорії мобів");
        p.add("gui.champions.picker.title.filter_type",   "Тип фільтра");

        p.add("gui.champions.editor.label.id",             "ID");
        p.add("gui.champions.editor.label.weight",         "вага");
        p.add("gui.champions.editor.label.type",           "тип");
        p.add("gui.champions.editor.label.min",            "мін.");
        p.add("gui.champions.editor.label.max",            "макс.");
        p.add("gui.champions.editor.label.tier_min",       "мін. рівень");
        p.add("gui.champions.editor.label.tier_max",       "макс. рівень");
        p.add("gui.champions.editor.label.min_count",      "мін. кількість");
        p.add("gui.champions.editor.label.max_count",      "макс. кількість");
        p.add("gui.champions.editor.label.affix",          "афікс");
        p.add("gui.champions.editor.label.min_strength",   "мін. сила");
        p.add("gui.champions.editor.label.max_strength",   "макс. сила");
        p.add("gui.champions.editor.label.strength",       "сила");
        p.add("gui.champions.editor.label.below",          "нижче");
        p.add("gui.champions.editor.label.seconds",        "секунди");
        p.add("gui.champions.editor.label.count",          "кількість");
        p.add("gui.champions.editor.label.amount",         "значення");
        p.add("gui.champions.editor.label.operation",      "операція");
        p.add("gui.champions.editor.label.attribute",      "атрибут");
        p.add("gui.champions.editor.label.effect",         "ефект");
        p.add("gui.champions.editor.label.amplifier",      "рівень ефекту");
        p.add("gui.champions.editor.label.infinite",       "нескінченно");
        p.add("gui.champions.editor.label.duration_ticks", "тривалість (тіки)");
        p.add("gui.champions.editor.label.enable",         "увімкнено");
        p.add("gui.champions.editor.label.value",          "значення");
        p.add("gui.champions.editor.label.level",          "рівень");
        p.add("gui.champions.editor.label.color",          "колір");
        p.add("gui.champions.editor.label.icon",           "іконка");
        p.add("gui.champions.editor.label.repeatable",     "повторюваний");
        p.add("gui.champions.editor.label.tag",            "тег");
        p.add("gui.champions.editor.label.whitelist",      "білий список");
        p.add("gui.champions.editor.label.state",          "стан");

        p.add("gui.champions.editor.header.archetype",       "Архетип");
        p.add("gui.champions.editor.header.tier_range",      "Діапазон рівнів");
        p.add("gui.champions.editor.header.entity_filter",   "Фільтр сутностей");
        p.add("gui.champions.editor.header.affix_pools",     "Пули афіксів");
        p.add("gui.champions.editor.header.pool",            "Пул %s");
        p.add("gui.champions.editor.header.candidates",      "Кандидати");
        p.add("gui.champions.editor.header.candidate",       "Кандидат %s");
        p.add("gui.champions.editor.header.phases",          "Фази");
        p.add("gui.champions.editor.header.phase",           "Фаза: %s");
        p.add("gui.champions.editor.header.condition",       "Умова");
        p.add("gui.champions.editor.header.effects",         "Ефекти");
        p.add("gui.champions.editor.header.effect",          "Ефект %s");
        p.add("gui.champions.editor.header.modifier_setting", "Налаштування модифікатора");
        p.add("gui.champions.editor.header.modifier",        "Модифікатор");
        p.add("gui.champions.editor.header.conditions",      "Умови");
        p.add("gui.champions.editor.header.tier",            "Рівень");
        p.add("gui.champions.editor.header.display",         "Відображення");
        p.add("gui.champions.editor.header.import_export",   "Імпорт / Експорт");
        p.add("gui.champions.editor.header.world_datapacks", "Пакети даних світу");
        p.add("gui.champions.editor.header.pack",            "Пакет: %s");
        p.add("gui.champions.editor.header.server_config",   "Конфігурація сервера");
        p.add("gui.champions.editor.header.filter",          "Фільтр %s");

        p.add("gui.champions.editor.action.add_candidate",     "+ Додати кандидата");
        p.add("gui.champions.editor.action.add_pool",          "+ Додати пул");
        p.add("gui.champions.editor.action.add_effect",        "+ Додати ефект");
        p.add("gui.champions.editor.action.add_phase",         "+ Додати фазу");
        p.add("gui.champions.editor.action.add_filter",        "+ Додати фільтр");
        p.add("gui.champions.editor.action.add_child_filter",  "+ Додати дочірній фільтр");
        p.add("gui.champions.editor.action.pick_affix_values",  "Значення афіксів… (вибрано: %s)");
        p.add("gui.champions.editor.action.pick_entity_types",  "Типи сутностей… (вибрано: %s)");
        p.add("gui.champions.editor.action.pick_mod_namespaces", "Простори імен модів… (вибрано: %s)");
        p.add("gui.champions.editor.action.pick_categories",    "Категорії… (вибрано: %s)");
        p.add("gui.champions.editor.action.export",            "§bЕкспорт вмісту редактора → zip");
        p.add("gui.champions.editor.action.import",            "§bІмпорт zip із champions_imports/");

        p.add("gui.champions.editor.hint.unknown_condition", "§cневідомий тип умови");
        p.add("gui.champions.editor.hint.unknown_effect",    "§cневідомий тип ефекту");
        p.add("gui.champions.editor.hint.unknown_filter",    "§cневідомий тип фільтра: %s");
        p.add("gui.champions.editor.hint.matches_every",     "відповідає всім сутностям");
        p.add("gui.champions.editor.hint.filters_anded",     "дочірні фільтри поєднуються по І");
        p.add("gui.champions.editor.hint.filters_ored",      "дочірні фільтри поєднуються по АБО");
        p.add("gui.champions.editor.hint.tier_display",      "колір: hex-рядок · іконка: шлях до текстури");
        p.add("gui.champions.editor.hint.json_view_only",    "affixes.matches / affixes.count: редагуйте в режимі JSON");
        p.add("gui.champions.editor.hint.export_target",     "створює champions_<час>.zip у");
        p.add("gui.champions.editor.hint.exports_dir",       "<world>/champions_exports/");
        p.add("gui.champions.editor.hint.import_dir",        "покладіть zip-пакети в <world>/champions_imports/");
        p.add("gui.champions.editor.hint.import_copied",     "вони копіюються в datapacks/ і вмикаються");
        p.add("gui.champions.editor.hint.no_packs",          "пакети не завантажено");
        p.add("gui.champions.editor.hint.packs_enabled",     "увімкнено: %s з %s");
        p.add("gui.champions.editor.hint.reload_on_toggle",  "перемикання перезавантажує ресурси сервера");
        p.add("gui.champions.editor.hint.select_pack",       "виберіть пакет у списку, щоб увімкнути або вимкнути його");
        p.add("gui.champions.editor.hint.config_reload",     "значення застосовуються після «Зберегти й перезавантажити»");
        p.add("gui.champions.editor.pack.enabled",           "§a● увімкнено");
        p.add("gui.champions.editor.pack.disabled",          "§c○ вимкнено");

        p.add("gui.champions.editor.filter.type.any",          "будь-який");
        p.add("gui.champions.editor.filter.type.all_of",       "усі (І)");
        p.add("gui.champions.editor.filter.type.any_of",       "будь-який (АБО)");
        p.add("gui.champions.editor.filter.type.entity_type",  "тип сутності");
        p.add("gui.champions.editor.filter.type.entity_tag",   "тег сутності");
        p.add("gui.champions.editor.filter.type.mod_id",       "ID мода");
        p.add("gui.champions.editor.filter.type.mob_category", "категорія моба");
        p.add("gui.champions.editor.filter.type.attribute",    "атрибут");
    }
}
