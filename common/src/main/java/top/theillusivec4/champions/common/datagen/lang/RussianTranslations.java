package top.theillusivec4.champions.common.datagen.lang;

import top.theillusivec4.champions.common.datagen.ChampionLanguageProvider;

/** Russian (ru_ru) translations. */
public final class RussianTranslations {

    private RussianTranslations() {}

    public static void add(ChampionLanguageProvider p) {
        // ── Affixes & ranks ─────────────────────────────────────────────────
        p.addAffix("adaptable",   "Адаптируемый");
        p.addAffix("arctic",      "Снежный");
        p.addAffix("dampening",   "Водянистый");
        p.addAffix("desecrating", "Оскверненный");
        p.addAffix("enkindling",  "Раскалённый");
        p.addAffix("hasty",       "Ловкий");
        p.addAffix("infested",    "Зараженный");
        p.addAffix("knocking",    "Отбивающий");
        p.addAffix("lively",      "Живучий");
        p.addAffix("magnetic",    "Магнитный");
        p.addAffix("molten",      "Расплавленный");
        p.addAffix("paralyzing",  "Парализующий");
        p.addAffix("plagued",     "Чумной");
        p.addAffix("reflective",  "Рефлекторный");
        p.addAffix("shielding",   "Укрепленный");
        p.addAffix("wounding",    "Убойный");
        p.addRank(1, "Обыкновенный");
        p.addRank(2, "Умелый");
        p.addRank(3, "Элитный");
        p.addRank(4, "Легендарный");
        p.addRank(5, "Ультимативный");

        // ── Commands ────────────────────────────────────────────────────────
        p.add("argument.champions.affix.unknown",     "Неизвестный аффикс %s");
        p.add("commands.champions.summon.success",    "Призван новый %s");
        p.add("commands.champions.egg.success",       "Рожден новый %s");
        p.add("command.champions.unknown_entity",     "Неизвестная сущность: %s");
        p.add("command.champions.unknown_tier",       "Уровень %s не найден. Доступно: %s");
        p.add("command.champions.egg.unknown_entity", "Неизвестная сущность");
        p.add("command.champions.not_living_entity",  "%s не является живым существом.");
        p.add("command.champions.not_champion",       "%s не является чемпионом.");
        p.add("command.champions.spawn_cancelled",    "Создание было отменено слушателем SpawnChampion.");
        p.add("command.champions.remove.success",     "Удален статус чемпиона с %s");
        p.add("command.champions.remove.success_deleted", "Удален статус чемпиона и удалена сущность.");
        p.add("command.champions.info.title",         "§6Информация о чемпионе:§r");
        p.add("command.champions.info.tier",          "  §eУровень:§r %s (Уровень %s)");
        p.add("command.champions.info.affixes",       "  §eАффиксы:§r");
        p.add("command.champions.info.affixes.none",  "  §eАффиксы:§r Нет");
        p.add("command.champions.info.affix_entry",   "    - %s (Сила: %s)");
        p.add("command.champions.info.archetype",     "  §eАрхетип:§r %s");
        p.add("command.champions.help.title",         "§6Команды Champions:§r");
        p.add("command.champions.help.summon",        "  §e/champions summon <сущность|@category:X|@archetype:X> <уровень> [аффиксы...] [at <x> <y> <z>]§r - Создать чемпиона");
        p.add("command.champions.help.apply",         "  §e/champions apply <цель> <уровень> [аффиксы...]§r - Применить чемпиона к существующей сущности");
        p.add("command.champions.help.remove",        "  §e/champions remove <цель> [удалитьСущность]§r - Убрать статус чемпиона");
        p.add("command.champions.help.info",          "  §e/champions info <цель>§r - Показать информацию о чемпионе (доступно всем игрокам)");
        p.add("command.champions.help.egg",           "  §e/champions egg <сущность|@category:X|@archetype:X> <уровень|random> [аффиксы...]§r - Создать яйцо чемпиона");
        p.add("command.champions.help.editor",        "  §e/champions editor§r - Открыть редактор датапаков");

        // ── Item / advancements / stats ─────────────────────────────────────
        p.add("item.champions.egg",                 "Яйцо чемпиона");
        p.add("item.champions.egg.tooltip",         "Моб со случайными усиливающими особенностями");
        p.add("item.champions.egg.random",          "Случайно");
        p.add("item.champions.egg.no_affixes",      "Нет аффиксов");
        p.add("advancements.champions.kill_a_champion.title",       "Охотник на Чемпионов");
        p.add("advancements.champions.kill_a_champion.description", "Убейте чемпионского моба");
        p.add("stat.champions.champion_mobs_killed", "Убито чемпионских мобов");
        p.add("config.jade.plugin_champions.enable_affix_compact", "Включить компактный показ аффиксов в Jade");

        // ── In-game editor screen (gui.champions.*) ─────────────────────────
        p.add("gui.champions.editor.title",         "Редактор чемпионов");
        p.add("gui.champions.editor.tab.archetypes", "Архетипы");
        p.add("gui.champions.editor.tab.tiers",      "Уровни");
        p.add("gui.champions.editor.tab.modifiers",  "Модификаторы");
        p.add("gui.champions.editor.tab.config",     "Конфигурация");
        p.add("gui.champions.editor.tab.packs",      "Пакеты данных");
        p.add("gui.champions.editor.view.form",     "Форма");
        p.add("gui.champions.editor.view.json",     "JSON");
        p.add("gui.champions.editor.new",           "§a+ Создать");
        p.add("gui.champions.editor.delete",        "§cУдалить");
        p.add("gui.champions.editor.save_reload",   "Сохранить и перезагрузить");
        p.add("gui.champions.editor.close",         "Закрыть");
        p.add("gui.champions.editor.entries",       "§8ЗАПИСИ §7%s");
        p.add("gui.champions.editor.unsaved",       "§e● не сохранено: %s");
        p.add("gui.champions.editor.error.not_object", "JSON должен быть объектом");
        p.add("gui.champions.editor.error.invalid_json", "Неверный JSON: %s");
        p.add("gui.champions.editor.toggle.true",   "§aВкл");
        p.add("gui.champions.editor.toggle.false",  "§cВыкл");
        p.add("gui.champions.editor.pick_affix",    "Выбрать аффикс… §8%s");

        p.add("gui.champions.picker.done",          "Готово");
        p.add("gui.champions.picker.cancel",        "Отмена");
        p.add("gui.champions.picker.search_hint",   "§7Поиск…");
        p.add("gui.champions.picker.selected",      "Выбрано: %s ·");
        p.add("gui.champions.picker.count",         "%s / %s");
        p.add("gui.champions.picker.title.affixes",       "Аффиксы");
        p.add("gui.champions.picker.title.affix_values",  "Значения аффиксов");
        p.add("gui.champions.picker.title.entity_types",  "Типы сущностей");
        p.add("gui.champions.picker.title.mod_namespaces", "Пространства имён модов");
        p.add("gui.champions.picker.title.mob_categories", "Категории мобов");
        p.add("gui.champions.picker.title.filter_type",   "Тип фильтра");

        p.add("gui.champions.editor.label.id",             "ID");
        p.add("gui.champions.editor.label.weight",         "вес");
        p.add("gui.champions.editor.label.type",           "тип");
        p.add("gui.champions.editor.label.min",            "мин.");
        p.add("gui.champions.editor.label.max",            "макс.");
        p.add("gui.champions.editor.label.tier_min",       "мин. уровень");
        p.add("gui.champions.editor.label.tier_max",       "макс. уровень");
        p.add("gui.champions.editor.label.min_count",      "мин. количество");
        p.add("gui.champions.editor.label.max_count",      "макс. количество");
        p.add("gui.champions.editor.label.affix",          "аффикс");
        p.add("gui.champions.editor.label.min_strength",   "мин. сила");
        p.add("gui.champions.editor.label.max_strength",   "макс. сила");
        p.add("gui.champions.editor.label.strength",       "сила");
        p.add("gui.champions.editor.label.below",          "ниже");
        p.add("gui.champions.editor.label.seconds",        "секунды");
        p.add("gui.champions.editor.label.count",          "количество");
        p.add("gui.champions.editor.label.amount",         "значение");
        p.add("gui.champions.editor.label.operation",      "операция");
        p.add("gui.champions.editor.label.attribute",      "атрибут");
        p.add("gui.champions.editor.label.effect",         "эффект");
        p.add("gui.champions.editor.label.amplifier",      "уровень эффекта");
        p.add("gui.champions.editor.label.infinite",       "бесконечно");
        p.add("gui.champions.editor.label.duration_ticks", "длительность (тики)");
        p.add("gui.champions.editor.label.enable",         "включено");
        p.add("gui.champions.editor.label.value",          "значение");
        p.add("gui.champions.editor.label.level",          "уровень");
        p.add("gui.champions.editor.label.color",          "цвет");
        p.add("gui.champions.editor.label.icon",           "иконка");
        p.add("gui.champions.editor.label.repeatable",     "повторяемый");
        p.add("gui.champions.editor.label.tag",            "тег");
        p.add("gui.champions.editor.label.whitelist",      "белый список");
        p.add("gui.champions.editor.label.state",          "состояние");

        p.add("gui.champions.editor.header.archetype",       "Архетип");
        p.add("gui.champions.editor.header.tier_range",      "Диапазон уровней");
        p.add("gui.champions.editor.header.entity_filter",   "Фильтр сущностей");
        p.add("gui.champions.editor.header.affix_pools",     "Пулы аффиксов");
        p.add("gui.champions.editor.header.pool",            "Пул %s");
        p.add("gui.champions.editor.header.candidates",      "Кандидаты");
        p.add("gui.champions.editor.header.candidate",       "Кандидат %s");
        p.add("gui.champions.editor.header.phases",          "Фазы");
        p.add("gui.champions.editor.header.phase",           "Фаза: %s");
        p.add("gui.champions.editor.header.condition",       "Условие");
        p.add("gui.champions.editor.header.effects",         "Эффекты");
        p.add("gui.champions.editor.header.effect",          "Эффект %s");
        p.add("gui.champions.editor.header.modifier_setting", "Настройка модификатора");
        p.add("gui.champions.editor.header.modifier",        "Модификатор");
        p.add("gui.champions.editor.header.conditions",      "Условия");
        p.add("gui.champions.editor.header.tier",            "Уровень");
        p.add("gui.champions.editor.header.display",         "Отображение");
        p.add("gui.champions.editor.header.import_export",   "Импорт / Экспорт");
        p.add("gui.champions.editor.header.world_datapacks", "Пакеты данных мира");
        p.add("gui.champions.editor.header.pack",            "Пакет: %s");
        p.add("gui.champions.editor.header.server_config",   "Конфигурация сервера");
        p.add("gui.champions.editor.header.filter",          "Фильтр %s");

        p.add("gui.champions.editor.action.add_candidate",     "+ Добавить кандидата");
        p.add("gui.champions.editor.action.add_pool",          "+ Добавить пул");
        p.add("gui.champions.editor.action.add_effect",        "+ Добавить эффект");
        p.add("gui.champions.editor.action.add_phase",         "+ Добавить фазу");
        p.add("gui.champions.editor.action.add_filter",        "+ Добавить фильтр");
        p.add("gui.champions.editor.action.add_child_filter",  "+ Добавить дочерний фильтр");
        p.add("gui.champions.editor.action.pick_affix_values",  "Значения аффиксов… (выбрано: %s)");
        p.add("gui.champions.editor.action.pick_entity_types",  "Типы сущностей… (выбрано: %s)");
        p.add("gui.champions.editor.action.pick_mod_namespaces", "Пространства имён модов… (выбрано: %s)");
        p.add("gui.champions.editor.action.pick_categories",    "Категории… (выбрано: %s)");
        p.add("gui.champions.editor.action.export",            "§bЭкспорт содержимого редактора → zip");
        p.add("gui.champions.editor.action.import",            "§bИмпорт zip из champions_imports/");

        p.add("gui.champions.editor.hint.unknown_condition", "§cнеизвестный тип условия");
        p.add("gui.champions.editor.hint.unknown_effect",    "§cнеизвестный тип эффекта");
        p.add("gui.champions.editor.hint.unknown_filter",    "§cнеизвестный тип фильтра: %s");
        p.add("gui.champions.editor.hint.matches_every",     "соответствует всем сущностям");
        p.add("gui.champions.editor.hint.filters_anded",     "дочерние фильтры объединяются по И");
        p.add("gui.champions.editor.hint.filters_ored",      "дочерние фильтры объединяются по ИЛИ");
        p.add("gui.champions.editor.hint.tier_display",      "цвет: hex-строка · иконка: путь к текстуре");
        p.add("gui.champions.editor.hint.json_view_only",    "affixes.matches / affixes.count: редактируйте в режиме JSON");
        p.add("gui.champions.editor.hint.export_target",     "создаёт champions_<время>.zip в");
        p.add("gui.champions.editor.hint.exports_dir",       "<world>/champions_exports/");
        p.add("gui.champions.editor.hint.import_dir",        "положите zip-пакеты в <world>/champions_imports/");
        p.add("gui.champions.editor.hint.import_copied",     "они копируются в datapacks/ и включаются");
        p.add("gui.champions.editor.hint.no_packs",          "пакеты не загружены");
        p.add("gui.champions.editor.hint.packs_enabled",     "включено: %s из %s");
        p.add("gui.champions.editor.hint.reload_on_toggle",  "переключение перезагружает ресурсы сервера");
        p.add("gui.champions.editor.hint.select_pack",       "выберите пакет в списке, чтобы включить или отключить его");
        p.add("gui.champions.editor.hint.config_reload",     "значения применяются после «Сохранить и перезагрузить»");
        p.add("gui.champions.editor.pack.enabled",           "§a● включён");
        p.add("gui.champions.editor.pack.disabled",          "§c○ отключён");

        p.add("gui.champions.editor.filter.type.any",          "любой");
        p.add("gui.champions.editor.filter.type.all_of",       "все (И)");
        p.add("gui.champions.editor.filter.type.any_of",       "любой (ИЛИ)");
        p.add("gui.champions.editor.filter.type.entity_type",  "тип сущности");
        p.add("gui.champions.editor.filter.type.entity_tag",   "тег сущности");
        p.add("gui.champions.editor.filter.type.mod_id",       "ID мода");
        p.add("gui.champions.editor.filter.type.mob_category", "категория моба");
        p.add("gui.champions.editor.filter.type.attribute",    "атрибут");
    }
}
