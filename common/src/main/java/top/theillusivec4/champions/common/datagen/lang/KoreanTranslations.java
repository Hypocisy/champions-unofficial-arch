package top.theillusivec4.champions.common.datagen.lang;

import top.theillusivec4.champions.common.datagen.ChampionLanguageProvider;

/** Korean (ko_kr) translations. */
public final class KoreanTranslations {

    private KoreanTranslations() {}

    public static void add(ChampionLanguageProvider p) {
        // ── Affixes & ranks ─────────────────────────────────────────────────
        p.addAffix("adaptable",   "적응");
        p.addAffix("arctic",      "극점");
        p.addAffix("dampening",   "감쇠");
        p.addAffix("desecrating", "모독");
        p.addAffix("enkindling",  "맹화");
        p.addAffix("hasty",       "신속");
        p.addAffix("infested",    "감염");
        p.addAffix("knocking",    "넉백");
        p.addAffix("lively",      "활력");
        p.addAffix("magnetic",    "자력");
        p.addAffix("molten",      "융해");
        p.addAffix("paralyzing",  "마비");
        p.addAffix("plagued",     "질병");
        p.addAffix("reflective",  "반사");
        p.addAffix("shielding",   "방패");
        p.addAffix("wounding",    "상처");
        p.addRank(1, "평범한");
        p.addRank(2, "숙련된");
        p.addRank(3, "엘리트");
        p.addRank(4, "전설적인");
        p.addRank(5, "궁극의");

        // ── Commands ────────────────────────────────────────────────────────
        p.add("argument.champions.affix.unknown",     "알 수 없는 수식어 %s");
        p.add("commands.champions.summon.success",    "새로운 %s을(를) 소환");
        p.add("commands.champions.egg.success",       "새로운 %s을(를) 생성");
        p.add("command.champions.unknown_entity",     "알 수 없는 엔티티: %s");
        p.add("command.champions.unknown_tier",       "레벨 %s인 등급을 찾을 수 없습니다. 사용 가능: %s");
        p.add("command.champions.egg.unknown_entity", "알 수 없는 엔티티");
        p.add("command.champions.not_living_entity",  "%s은(는) 생물 엔티티가 아닙니다.");
        p.add("command.champions.not_champion",       "%s은(는) 챔피언이 아닙니다.");
        p.add("command.champions.spawn_cancelled",    "생성이 SpawnChampion 리스너에 의해 취소되었습니다.");
        p.add("command.champions.remove.success",     "%s에서 챔피언 상태 제거됨");
        p.add("command.champions.remove.success_deleted", "챔피언 상태를 제거하고 엔티티를 삭제했습니다.");
        p.add("command.champions.info.title",         "§6챔피언 정보:§r");
        p.add("command.champions.info.tier",          "  §e등급:§r %s (레벨 %s)");
        p.add("command.champions.info.affixes",       "  §e수식어:§r");
        p.add("command.champions.info.affixes.none",  "  §e수식어:§r 없음");
        p.add("command.champions.info.affix_entry",   "    - %s (강도: %s)");
        p.add("command.champions.info.archetype",     "  §e원형:§r %s");
        p.add("command.champions.help.title",         "§6챔피언 명령어:§r");
        p.add("command.champions.help.summon",        "  §e/champions summon <개체|@category:X|@archetype:X> <등급> [수식어...] [at <x> <y> <z>]§r - 챔피언 소환");
        p.add("command.champions.help.apply",         "  §e/champions apply <대상> <등급> [수식어...]§r - 기존 개체에게 챔피언 적용");
        p.add("command.champions.help.remove",        "  §e/champions remove <대상> [개체 삭제]§r - 챔피언 상태 제거");
        p.add("command.champions.help.info",          "  §e/champions info <대상>§r - 챔피언 정보 표시 (모든 플레이어 사용 가능)");
        p.add("command.champions.help.egg",           "  §e/champions egg <개체|@category:X|@archetype:X> <등급|random> [수식어...]§r - 챔피언 알 생성");
        p.add("command.champions.help.editor",        "  §e/champions editor§r - 데이터 팩 편집기 열기");

        // ── Item / advancements / stats ─────────────────────────────────────
        p.add("item.champions.egg",                 "챔피언 알");
        p.add("item.champions.egg.tooltip",         "무작위 수식어");
        p.add("item.champions.egg.random",          "무작위");
        p.add("item.champions.egg.no_affixes",      "수식어 없음");
        p.add("advancements.champions.kill_a_champion.title",       "챔피언 사냥꾼");
        p.add("advancements.champions.kill_a_champion.description", "강력한 적대적 몬스터를 죽이세요");
        p.add("stat.champions.champion_mobs_killed", "챔피언 처치");
        p.add("config.jade.plugin_champions.enable_affix_compact", "Jade 수식어 간략 표시 활성화");

        // ── In-game editor screen (gui.champions.*) ─────────────────────────
        p.add("gui.champions.editor.title",         "챔피언 편집기");
        p.add("gui.champions.editor.tab.archetypes", "원형");
        p.add("gui.champions.editor.tab.tiers",      "등급");
        p.add("gui.champions.editor.tab.modifiers",  "수정자");
        p.add("gui.champions.editor.tab.config",     "설정");
        p.add("gui.champions.editor.tab.packs",      "데이터 팩");
        p.add("gui.champions.editor.view.form",     "양식");
        p.add("gui.champions.editor.view.json",     "JSON");
        p.add("gui.champions.editor.new",           "§a+ 새로 만들기");
        p.add("gui.champions.editor.delete",        "§c삭제");
        p.add("gui.champions.editor.save_reload",   "저장 후 다시 불러오기");
        p.add("gui.champions.editor.close",         "닫기");
        p.add("gui.champions.editor.entries",       "§8항목 §7%s");
        p.add("gui.champions.editor.unsaved",       "§e● %s개 저장 안 됨");
        p.add("gui.champions.editor.error.not_object", "JSON은 객체여야 합니다");
        p.add("gui.champions.editor.error.invalid_json", "잘못된 JSON: %s");
        p.add("gui.champions.editor.toggle.true",   "§a참");
        p.add("gui.champions.editor.toggle.false",  "§c거짓");
        p.add("gui.champions.editor.pick_affix",    "수식어 선택… §8%s");

        p.add("gui.champions.picker.done",          "완료");
        p.add("gui.champions.picker.cancel",        "취소");
        p.add("gui.champions.picker.search_hint",   "§7검색…");
        p.add("gui.champions.picker.selected",      "선택됨: %s ·");
        p.add("gui.champions.picker.count",         "%s / %s");
        p.add("gui.champions.picker.title.affixes",       "수식어");
        p.add("gui.champions.picker.title.affix_values",  "수식어 값");
        p.add("gui.champions.picker.title.entity_types",  "개체 유형");
        p.add("gui.champions.picker.title.mod_namespaces", "모드 네임스페이스");
        p.add("gui.champions.picker.title.mob_categories", "몹 분류");
        p.add("gui.champions.picker.title.filter_type",   "필터 유형");

        p.add("gui.champions.editor.label.id",             "ID");
        p.add("gui.champions.editor.label.weight",         "가중치");
        p.add("gui.champions.editor.label.type",           "유형");
        p.add("gui.champions.editor.label.min",            "최소");
        p.add("gui.champions.editor.label.max",            "최대");
        p.add("gui.champions.editor.label.tier_min",       "최소 등급");
        p.add("gui.champions.editor.label.tier_max",       "최대 등급");
        p.add("gui.champions.editor.label.min_count",      "최소 개수");
        p.add("gui.champions.editor.label.max_count",      "최대 개수");
        p.add("gui.champions.editor.label.affix",          "수식어");
        p.add("gui.champions.editor.label.min_strength",   "최소 강도");
        p.add("gui.champions.editor.label.max_strength",   "최대 강도");
        p.add("gui.champions.editor.label.strength",       "강도");
        p.add("gui.champions.editor.label.below",          "미만");
        p.add("gui.champions.editor.label.seconds",        "초");
        p.add("gui.champions.editor.label.count",          "개수");
        p.add("gui.champions.editor.label.amount",         "값");
        p.add("gui.champions.editor.label.operation",      "연산");
        p.add("gui.champions.editor.label.attribute",      "속성");
        p.add("gui.champions.editor.label.effect",         "효과");
        p.add("gui.champions.editor.label.amplifier",      "강화 수준");
        p.add("gui.champions.editor.label.infinite",       "무한");
        p.add("gui.champions.editor.label.duration_ticks", "지속 시간 (틱)");
        p.add("gui.champions.editor.label.enable",         "활성화");
        p.add("gui.champions.editor.label.value",          "값");
        p.add("gui.champions.editor.label.level",          "레벨");
        p.add("gui.champions.editor.label.color",          "색상");
        p.add("gui.champions.editor.label.icon",           "아이콘");
        p.add("gui.champions.editor.label.repeatable",     "반복 가능");
        p.add("gui.champions.editor.label.tag",            "태그");
        p.add("gui.champions.editor.label.whitelist",      "화이트리스트");
        p.add("gui.champions.editor.label.state",          "상태");

        p.add("gui.champions.editor.header.archetype",       "원형");
        p.add("gui.champions.editor.header.tier_range",      "등급 범위");
        p.add("gui.champions.editor.header.entity_filter",   "개체 필터");
        p.add("gui.champions.editor.header.affix_pools",     "수식어 풀");
        p.add("gui.champions.editor.header.pool",            "풀 %s");
        p.add("gui.champions.editor.header.candidates",      "후보");
        p.add("gui.champions.editor.header.candidate",       "후보 %s");
        p.add("gui.champions.editor.header.phases",          "단계");
        p.add("gui.champions.editor.header.phase",           "단계: %s");
        p.add("gui.champions.editor.header.condition",       "조건");
        p.add("gui.champions.editor.header.effects",         "효과");
        p.add("gui.champions.editor.header.effect",          "효과 %s");
        p.add("gui.champions.editor.header.modifier_setting", "수정자 설정");
        p.add("gui.champions.editor.header.modifier",        "수정자");
        p.add("gui.champions.editor.header.conditions",      "조건");
        p.add("gui.champions.editor.header.tier",            "등급");
        p.add("gui.champions.editor.header.display",         "표시");
        p.add("gui.champions.editor.header.import_export",   "가져오기 / 내보내기");
        p.add("gui.champions.editor.header.world_datapacks", "월드 데이터 팩");
        p.add("gui.champions.editor.header.pack",            "팩: %s");
        p.add("gui.champions.editor.header.server_config",   "서버 설정");
        p.add("gui.champions.editor.header.filter",          "필터 %s");

        p.add("gui.champions.editor.action.add_candidate",     "+ 후보 추가");
        p.add("gui.champions.editor.action.add_pool",          "+ 풀 추가");
        p.add("gui.champions.editor.action.add_effect",        "+ 효과 추가");
        p.add("gui.champions.editor.action.add_phase",         "+ 단계 추가");
        p.add("gui.champions.editor.action.add_filter",        "+ 필터 추가");
        p.add("gui.champions.editor.action.add_child_filter",  "+ 하위 필터 추가");
        p.add("gui.champions.editor.action.pick_affix_values",  "수식어 값… (%s개 선택됨)");
        p.add("gui.champions.editor.action.pick_entity_types",  "개체 유형… (%s개 선택됨)");
        p.add("gui.champions.editor.action.pick_mod_namespaces", "모드 네임스페이스… (%s개 선택됨)");
        p.add("gui.champions.editor.action.pick_categories",    "분류… (%s개 선택됨)");
        p.add("gui.champions.editor.action.export",            "§b편집기 내용을 zip으로 내보내기");
        p.add("gui.champions.editor.action.import",            "§bchampions_imports/에서 zip 가져오기");

        p.add("gui.champions.editor.hint.unknown_condition", "§c알 수 없는 조건 유형");
        p.add("gui.champions.editor.hint.unknown_effect",    "§c알 수 없는 효과 유형");
        p.add("gui.champions.editor.hint.unknown_filter",    "§c알 수 없는 필터 유형: %s");
        p.add("gui.champions.editor.hint.matches_every",     "모든 개체와 일치");
        p.add("gui.champions.editor.hint.filters_anded",     "하위 필터는 AND 관계");
        p.add("gui.champions.editor.hint.filters_ored",      "하위 필터는 OR 관계");
        p.add("gui.champions.editor.hint.tier_display",      "색상: 16진수 문자열 · 아이콘: 텍스처 경로");
        p.add("gui.champions.editor.hint.json_view_only",    "affixes.matches / affixes.count: JSON 보기에서 편집하세요");
        p.add("gui.champions.editor.hint.export_target",     "champions_<시간>.zip 생성 위치:");
        p.add("gui.champions.editor.hint.exports_dir",       "<world>/champions_exports/");
        p.add("gui.champions.editor.hint.import_dir",        "데이터 팩 zip을 <world>/champions_imports/에 넣으세요");
        p.add("gui.champions.editor.hint.import_copied",     "datapacks/로 복사된 후 활성화됩니다");
        p.add("gui.champions.editor.hint.no_packs",          "불러온 팩 없음");
        p.add("gui.champions.editor.hint.packs_enabled",     "%s / %s개 활성화됨");
        p.add("gui.champions.editor.hint.reload_on_toggle",  "전환 시 서버 리소스를 다시 불러옵니다");
        p.add("gui.champions.editor.hint.select_pack",       "목록에서 팩을 선택하여 활성화/비활성화하세요");
        p.add("gui.champions.editor.hint.config_reload",     "설정은 저장 후 다시 불러오기 시 적용됩니다");
        p.add("gui.champions.editor.pack.enabled",           "§a● 활성화됨");
        p.add("gui.champions.editor.pack.disabled",          "§c○ 비활성화됨");

        p.add("gui.champions.editor.filter.type.any",          "임의");
        p.add("gui.champions.editor.filter.type.all_of",       "모두 일치 (AND)");
        p.add("gui.champions.editor.filter.type.any_of",       "하나 이상 일치 (OR)");
        p.add("gui.champions.editor.filter.type.entity_type",  "개체 유형");
        p.add("gui.champions.editor.filter.type.entity_tag",   "개체 태그");
        p.add("gui.champions.editor.filter.type.mod_id",       "모드 ID");
        p.add("gui.champions.editor.filter.type.mob_category", "몹 분류");
        p.add("gui.champions.editor.filter.type.attribute",    "속성");
    }
}
