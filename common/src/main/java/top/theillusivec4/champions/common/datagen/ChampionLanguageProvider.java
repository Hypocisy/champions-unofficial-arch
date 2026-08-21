package top.theillusivec4.champions.common.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;

import java.nio.file.Path;

/**
 * Platform-agnostic, multi-locale language provider for the Champions mod.
 *
 * <p>Contains built-in translations for all supported locales (en_us, zh_cn, ko_kr,
 * ru_ru, tr_tr, uk_ua, pt_br). Platform-specific entries that depend on registry
 * objects not yet present in the new project (items, entity types, mob effects,
 * damage types) are left to platform subclasses via {@link #addPlatformEntries()}.</p>
 */
public class ChampionLanguageProvider implements DataProvider {

    private final PackOutput output;
    private final String locale;
    private final Map<String, String> data = new LinkedHashMap<>();

    public ChampionLanguageProvider(PackOutput output, String locale) {
        this.output = output;
        this.locale = locale;
    }

    /** Returns the locale code this provider is generating for. */
    public String getLocale() {
        return locale;
    }

    // ── Override hooks ────────────────────────────────────────────────────────

    /** Called first. Override to add locale-specific translations. */
    protected void addTranslations() {
        switch (locale) {
            case "zh_cn"  -> addChineseTranslations();
            case "ko_kr"  -> addKoreanTranslations();
            case "ru_ru"  -> addRussianTranslations();
            case "tr_tr"  -> addTurkishTranslations();
            case "uk_ua"  -> addUkrainianTranslations();
            case "pt_br"  -> addBrazilianPortugueseTranslations();
            default        -> addEnglishTranslations();
        }
    }

    /**
     * Called after {@link #addTranslations()}.
     * Override in platform subclasses to add entries that depend on
     * registry objects (items, entity types, effects, damage types).
     */
    protected void addPlatformEntries() {}

    // ── Public helpers ────────────────────────────────────────────────────────

    public void add(String key, String value) {
        if (data.put(key, value) != null) {
            throw new IllegalStateException("Duplicate translation key: " + key);
        }
    }

    /** Affix display name: {@code affix.<ns>.<path>.name} */
    public void addAffix(ResourceLocation id, String name) {
        add("affix." + id.getNamespace() + "." + id.getPath() + ".name", name);
    }

    /** Convenience — namespace defaults to {@code "champions"}. */
    public void addAffix(String path, String name) {
        addAffix(ResourceLocation.fromNamespaceAndPath("champions", path), name);
    }

    /** Rank title: {@code rank.champions.title.<level>} */
    public void addRank(int level, String title) {
        add("rank.champions.title." + level, title);
    }

    /** Damage type death messages. */
    public void addDamageType(ResourceLocation damageTypeId, String death, String deathByPlayer) {
        add("death.attack." + damageTypeId.getPath(), death);
        if (!deathByPlayer.isEmpty()) {
            add("death.attack." + damageTypeId.getPath() + ".player", deathByPlayer);
        }
    }

    // ── DataProvider ──────────────────────────────────────────────────────────

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput cache) {
        addTranslations();
        addPlatformEntries();

        JsonObject json = new JsonObject();
        data.forEach(json::addProperty);

        Path path = output.getOutputFolder()
                .resolve("assets/champions/lang/" + locale + ".json");
        return DataProvider.saveStable(cache, json, path);
    }

    @Override
    public @NotNull String getName() {
        return "Champions Language (" + locale + ")";
    }

    // ── Built-in locale methods ───────────────────────────────────────────────

    protected void addEnglishTranslations() {
        addAffix("adaptable",   "Adaptable");
        addAffix("arctic",      "Arctic");
        addAffix("dampening",   "Dampening");
        addAffix("desecrating", "Desecrating");
        addAffix("enkindling",  "Enkindling");
        addAffix("hasty",       "Hasty");
        addAffix("infested",    "Infested");
        addAffix("knocking",    "Knocking");
        addAffix("lively",      "Lively");
        addAffix("magnetic",    "Magnetic");
        addAffix("molten",      "Molten");
        addAffix("paralyzing",  "Paralyzing");
        addAffix("plagued",     "Plagued");
        addAffix("reflective",  "Reflective");
        addAffix("shielding",   "Shielding");
        addAffix("wounding",    "Wounding");
        addRank(1, "Common");
        addRank(2, "Skilled");
        addRank(3, "Elite");
        addRank(4, "Legendary");
        addRank(5, "Ultimate");
        add("argument.champions.affix.unknown",     "Unknown affix %s");
        add("commands.champions.summon.success",    "Summoned new %s");
        add("commands.champions.egg.success",       "Created new %s");
        add("command.champions.unknown_entity",     "Unknown entity: %s");
        add("command.champions.unknown_tier",       "No tier found with level %s. Available: %s");
        add("command.champions.egg.unknown_entity", "Unknown entity");
        add("command.champions.not_living_entity",  "%s is not a living entity");
        add("command.champions.not_champion",       "%s is not a champion");
        add("command.champions.spawn_cancelled",    "Spawn was cancelled by a listener");
        add("command.champions.remove.success",     "Removed champion status from %s");
        add("command.champions.remove.success_deleted", "Removed champion and deleted entity");
        add("command.champions.info.title",         "Champion Info:");
        add("command.champions.info.tier",          "Tier: %s (Level %s)");
        add("command.champions.info.affixes",       "Affixes:");
        add("command.champions.info.affixes.none",  "No affixes");
        add("command.champions.info.affix_entry",   "  - %s (Strength: %s)");
        add("command.champions.info.archetype",     "Archetype: %s");
        add("command.champions.help.title",         "§6Champions Commands:§r");
        add("command.champions.help.summon",        "  §e/champions summon <entity|@category:X|@archetype:X> <tier> [affixes...] [at <x> <y> <z>]§r - Spawn champion");
        add("command.champions.help.apply",         "  §e/champions apply <target> <tier> [affixes...]§r - Apply champion to existing entity");
        add("command.champions.help.remove",        "  §e/champions remove <target> [deleteEntity]§r - Remove champion status");
        add("command.champions.help.info",          "  §e/champions info <target>§r - Show champion info (available to all players)");
        add("command.champions.help.egg",           "  §e/champions egg <entity|@category:X|@archetype:X> <tier|random> [affixes...]§r - Create champion egg");
        add("command.champions.help.editor",        "  §e/champions editor§r - Open datapack editor");
        add("item.champions.egg", "Champion Egg");
        add("item.champions.egg.tooltip",           "Random Affixes");
        add("item.champions.egg.random",            "Random");
        add("item.champions.egg.no_affixes",        "No affixes");
        add("advancements.champions.kill_a_champion.title",       "Champion Hunter");
        add("advancements.champions.kill_a_champion.description", "Kill a powerful hostile monster");
        add("stat.champions.champion_mobs_killed",  "Champion Mobs Killed");
        add("config.jade.plugin_champions.enable_affix_compact", "Enable Jade affix compact");
    }

    protected void addChineseTranslations() {
        addAffix("adaptable",   "适应");
        addAffix("arctic",      "极寒");
        addAffix("dampening",   "抑制");
        addAffix("desecrating", "亵渎");
        addAffix("enkindling",  "点燃");
        addAffix("hasty",       "急速");
        addAffix("infested",    "感染");
        addAffix("knocking",    "爆震");
        addAffix("lively",      "活力");
        addAffix("magnetic",    "磁力");
        addAffix("molten",      "熔融");
        addAffix("paralyzing",  "瘫痪");
        addAffix("plagued",     "瘟疫");
        addAffix("reflective",  "反射");
        addAffix("shielding",   "保护");
        addAffix("wounding",    "创伤");
        addRank(1, "普通");
        addRank(2, "稀有");
        addRank(3, "精英");
        addRank(4, "传奇");
        addRank(5, "终极");
        add("argument.champions.affix.unknown",     "未知词缀 %s");
        add("commands.champions.summon.success",    "召唤了新的 %s");
        add("commands.champions.egg.success",       "创建了 %s");
        add("command.champions.unknown_entity",     "未知实体: %s");
        add("command.champions.unknown_tier",       "未找到等级 %s 的阶级。可用: %s");
        add("command.champions.egg.unknown_entity", "未知生物");
        add("command.champions.not_living_entity",  "%s 不是生物实体");
        add("command.champions.not_champion",       "%s 不是冠军");
        add("command.champions.spawn_cancelled",    "生成被监听器取消");
        add("command.champions.remove.success",     "已从 %s 移除冠军状态");
        add("command.champions.remove.success_deleted", "已移除冠军并删除实体");
        add("command.champions.info.title",         "冠军信息:");
        add("command.champions.info.tier",          "等级: %s (等级 %s)");
        add("command.champions.info.affixes",       "词缀:");
        add("command.champions.info.affixes.none",  "无词缀");
        add("command.champions.info.affix_entry",   "  - %s (强度: %s)");
        add("command.champions.info.archetype",     "原型: %s");
        add("command.champions.help.title",         "§6冠军命令:§r");
        add("command.champions.help.summon",        "  §e/champions summon <实体|@category:X|@archetype:X> <等级> [词缀...] [at <x> <y> <z>]§r - 生成冠军");
        add("command.champions.help.apply",         "  §e/champions apply <目标> <等级> [词缀...]§r - 将冠军应用于现有实体");
        add("command.champions.help.remove",        "  §e/champions remove <目标> [删除实体]§r - 移除冠军状态");
        add("command.champions.help.info",          "  §e/champions info <目标>§r - 显示冠军信息 (所有玩家可用)");
        add("command.champions.help.egg",           "  §e/champions egg <实体|@category:X|@archetype:X> <等级|random> [词缀...]§r - 创建冠军蛋");
        add("command.champions.help.editor",        "  §e/champions editor§r - 打开数据包编辑器");
        add("item.champions.egg", "强敌蛋");
        add("item.champions.egg.tooltip",           "随机词缀");
        add("item.champions.egg.random",            "随机");
        add("item.champions.egg.no_affixes",        "无词缀");
        add("advancements.champions.kill_a_champion.title",       "冠军猎人");
        add("advancements.champions.kill_a_champion.description", "击杀一个强大的敌对怪物");
        add("stat.champions.champion_mobs_killed",  "冠军怪物击杀数");
        add("config.jade.plugin_champions.enable_affix_compact", "启用jade词条兼容");
    }

    protected void addKoreanTranslations() {
        addAffix("adaptable",   "적응");
        addAffix("arctic",      "극점");
        addAffix("dampening",   "감쇠");
        addAffix("desecrating", "모독");
        addAffix("enkindling",  "맹화");
        addAffix("hasty",       "신속");
        addAffix("infested",    "감염");
        addAffix("knocking",    "넉백");
        addAffix("lively",      "활력");
        addAffix("magnetic",    "자력");
        addAffix("molten",      "융해");
        addAffix("paralyzing",  "마비");
        addAffix("plagued",     "질병");
        addAffix("reflective",  "반사");
        addAffix("shielding",   "방패");
        addAffix("wounding",    "상처");
        addRank(1, "평범한");
        addRank(2, "숙련된");
        addRank(3, "엘리트");
        addRank(4, "전설적인");
        addRank(5, "궁극의");
        add("argument.champions.affix.unknown",     "알 수 없는 수식어 %s");
        add("commands.champions.summon.success",    "새로운 %s을(를) 소환");
        add("commands.champions.egg.success",       "새로운 %s을(를) 생성");
        add("command.champions.egg.unknown_entity", "알 수 없는 엔티티");
        add("command.champions.not_living_entity",  "%s은(는) 생물 엔티티가 아닙니다.");
        add("command.champions.not_champion",       "%s은(는) 챔피언이 아닙니다.");
        add("command.champions.spawn_cancelled",    "생성이 SpawnChampion 리스너에 의해 취소되었습니다.");
        add("command.champions.remove.success",     "%s에서 챔피언 상태 제거됨");
        add("command.champions.remove.success_deleted", "챔피언 상태를 제거하고 엔티티를 삭제했습니다.");
        add("command.champions.info.title",         "§6챔피언 정보:§r");
        add("command.champions.info.tier",          "  §e등급:§r %s (레벨 %s)");
        add("command.champions.info.affixes",       "  §e수식어:§r");
        add("command.champions.info.affixes.none",  "  §e수식어:§r 없음");
        add("command.champions.info.affix_entry",   "    - %s (강도: %s)");
        add("command.champions.info.archetype",     "  §e원형:§r %s");
        add("item.champions.egg.tooltip",           "무작위 수식어");
        add("advancements.champions.kill_a_champion.title",       "챔피언 사냥꾼");
        add("advancements.champions.kill_a_champion.description", "강력한 적대적 몬스터를 죽이세요");
        add("stat.champions.champion_mobs_killed",  "챔피언 처치");
        add("config.jade.plugin_champions.enable_affix_compact", "Enable Jade affix compact");
    }

    protected void addRussianTranslations() {
        addAffix("adaptable",   "Адаптируемый");
        addAffix("arctic",      "Снежный");
        addAffix("dampening",   "Водянистый");
        addAffix("desecrating", "Оскверненный");
        addAffix("enkindling",  "Раскалённый");
        addAffix("hasty",       "Ловкий");
        addAffix("infested",    "Зараженный");
        addAffix("knocking",    "Отбивающий");
        addAffix("lively",      "Живучий");
        addAffix("magnetic",    "Магнитный");
        addAffix("molten",      "Расплавленный");
        addAffix("paralyzing",  "Парализующий");
        addAffix("plagued",     "Чумной");
        addAffix("reflective",  "Рефлекторный");
        addAffix("shielding",   "Укрепленный");
        addAffix("wounding",    "Убойный");
        addRank(1, "Обыкновенный");
        addRank(2, "Умелый");
        addRank(3, "Элитный");
        addRank(4, "Легендарный");
        addRank(5, "Ультимативный");
        add("argument.champions.affix.unknown",     "Unknown affix %s");
        add("commands.champions.summon.success",    "Призван новый %s");
        add("commands.champions.egg.success",       "Рожден новый %s");
        add("command.champions.egg.unknown_entity", "Unknown entity");
        add("command.champions.not_living_entity",  "%s не является живым существом.");
        add("command.champions.not_champion",       "%s не является чемпионом.");
        add("command.champions.spawn_cancelled",    "Создание было отменено слушателем SpawnChampion.");
        add("command.champions.remove.success",     "Удален статус чемпиона с %s");
        add("command.champions.remove.success_deleted", "Удален статус чемпиона и удалена сущность.");
        add("command.champions.info.title",         "§6Информация о чемпионе:§r");
        add("command.champions.info.tier",          "  §eУровень:§r %s (Уровень %s)");
        add("command.champions.info.affixes",       "  §eАффиксы:§r");
        add("command.champions.info.affixes.none",  "  §eАффиксы:§r Нет");
        add("command.champions.info.affix_entry",   "    - %s (Сила: %s)");
        add("command.champions.info.archetype",     "  §eАрхетип:§r %s");
        add("item.champions.egg.tooltip",           "Моб с случайными усиливающими особенностями");
        add("advancements.champions.kill_a_champion.title",       "Охотник на Чемпионов");
        add("advancements.champions.kill_a_champion.description", "Убейте чемпионского моба");
        add("stat.champions.champion_mobs_killed",  "Убито чемпионских мобов");
        add("config.jade.plugin_champions.enable_affix_compact", "Enable Jade affix compact");
    }

    protected void addTurkishTranslations() {
        addAffix("adaptable",   "Adaptif");
        addAffix("arctic",      "Buz-gibi");
        addAffix("dampening",   "Soğurucu");
        addAffix("desecrating", "Saygısız");
        addAffix("enkindling",  "Tutuşturucu");
        addAffix("hasty",       "Aceleci");
        addAffix("infested",    "Böcekli");
        addAffix("knocking",    "Tepici");
        addAffix("lively",      "Yaşam-dolu");
        addAffix("magnetic",    "Manyetik");
        addAffix("molten",      "Erimiş");
        addAffix("paralyzing",  "Felç-edici");
        addAffix("plagued",     "Hastalıklı");
        addAffix("reflective",  "Yansıtıcı");
        addAffix("shielding",   "Korumalı");
        addAffix("wounding",    "Yaralayıcı");
        addRank(1, "Yaygın");
        addRank(2, "Yetenekli");
        addRank(3, "Elit");
        addRank(4, "Efsanevi");
        addRank(5, "Nihai");
        add("argument.champions.affix.unknown",     "%s - Bilinmeyen özellik");
        add("commands.champions.summon.success",    "Yeni %s çağrıldı");
        add("commands.champions.egg.success",       "Yeni %s oluşturuldu");
        add("command.champions.egg.unknown_entity", "Bilinmeyen Yaratık");
        add("command.champions.not_living_entity",  "%s canlı bir varlık değil.");
        add("command.champions.not_champion",       "%s bir şampiyon değil.");
        add("command.champions.spawn_cancelled",    "Oluşturma SpawnChampion dinleyicisi tarafından iptal edildi.");
        add("command.champions.remove.success",     "%s'den şampiyon durumu kaldırıldı");
        add("command.champions.remove.success_deleted", "Şampiyon durumu kaldırıldı ve varlık silindi.");
        add("command.champions.info.title",         "§6Şampiyon Bilgisi:§r");
        add("command.champions.info.tier",          "  §eSeviye:§r %s (Seviye %s)");
        add("command.champions.info.affixes",       "  §eÖzellikler:§r");
        add("command.champions.info.affixes.none",  "  §eÖzellikler:§r Yok");
        add("command.champions.info.affix_entry",   "    - %s (Güç: %s)");
        add("command.champions.info.archetype",     "  §eArketip:§r %s");
        add("item.champions.egg.tooltip",           "Rasgele Özellikli");
        add("advancements.champions.kill_a_champion.title",       "Şampiyon Avcısı");
        add("advancements.champions.kill_a_champion.description", "Güçlü bir düşman öldür");
        add("stat.champions.champion_mobs_killed",  "Öldürülen Şampiyon Sayısı");
        add("config.jade.plugin_champions.enable_affix_compact", "Enable Jade affix compact");
    }

    protected void addUkrainianTranslations() {
        addAffix("adaptable",   "Адаптований");
        addAffix("arctic",      "Арктика");
        addAffix("dampening",   "Зволоження");
        addAffix("desecrating", "Осквернення");
        addAffix("enkindling",  "Осквернення");
        addAffix("hasty",       "Поспішно");
        addAffix("infested",    "Заражений");
        addAffix("knocking",    "Стукіт");
        addAffix("lively",      "Жвавий");
        addAffix("magnetic",    "Магнітний");
        addAffix("molten",      "Розплавлений");
        addAffix("paralyzing",  "Паралізуючий");
        addAffix("plagued",     "Заражений");
        addAffix("reflective",  "Світловідбиваючі");
        addAffix("shielding",   "Екранізація");
        addAffix("wounding",    "Пораненний");
        addRank(1, "Звичайний");
        addRank(2, "Кваліфікований");
        addRank(3, "Еліта");
        addRank(4, "Легендарний");
        addRank(5, "Кінцевий");
        add("argument.champions.affix.unknown",     "Невідомий афікс %s");
        add("commands.champions.summon.success",    "Викликається новий %s");
        add("commands.champions.egg.success",       "Створено новий %s");
        add("command.champions.egg.unknown_entity", "Невідомий суб'єкт");
        add("command.champions.not_living_entity",  "%s не є живою істотою.");
        add("command.champions.not_champion",       "%s не є чемпіоном.");
        add("command.champions.spawn_cancelled",    "Створення скасовано слухачем SpawnChampion.");
        add("command.champions.remove.success",     "Видалено статус чемпіона з %s");
        add("command.champions.remove.success_deleted", "Статус чемпіона видалено і сутність видалено.");
        add("command.champions.info.title",         "§6Інформація про чемпіона:§r");
        add("command.champions.info.tier",          "  §eРівень:§r %s (Рівень %s)");
        add("command.champions.info.affixes",       "  §eАфікси:§r");
        add("command.champions.info.affixes.none",  "  §eАфікси:§r Немає");
        add("command.champions.info.affix_entry",   "    - %s (Сила: %s)");
        add("command.champions.info.archetype",     "  §eАрхетип:§r %s");
        add("item.champions.egg.tooltip",           "Випадкові афікси");
        add("advancements.champions.kill_a_champion.title",       "Чемпіонський мисливець");
        add("advancements.champions.kill_a_champion.description", "Убийте потужного ворожого монстра");
        add("stat.champions.champion_mobs_killed",  "Чемпіонські натовпи вбито");
        add("config.jade.plugin_champions.enable_affix_compact", "Enable Jade affix compact");
    }

    protected void addBrazilianPortugueseTranslations() {
        addAffix("adaptable",   "Adaptável");
        addAffix("arctic",      "Ártico");
        addAffix("dampening",   "Amortecedor");
        addAffix("desecrating", "Profanador");
        addAffix("enkindling",  "Incendiário");
        addAffix("hasty",       "Apresado");
        addAffix("infested",    "Infestado");
        addAffix("knocking",    "Empurrador");
        addAffix("lively",      "Vigoroso");
        addAffix("magnetic",    "Magnético");
        addAffix("molten",      "Fundido");
        addAffix("paralyzing",  "Paralisante");
        addAffix("plagued",     "Pestilento");
        addAffix("reflective",  "Reflexivo");
        addAffix("shielding",   "Protetor");
        addAffix("wounding",    "Dilacerante");
        addRank(1, "Comum");
        addRank(2, "Habilidoso");
        addRank(3, "Elite");
        addRank(4, "Lendário");
        addRank(5, "Supremo");
        add("argument.champions.affix.unknown",     "Afixo desconhecido %s");
        add("commands.champions.summon.success",    "Invocado novo %s");
        add("commands.champions.egg.success",       "Criado novo %s");
        add("command.champions.egg.unknown_entity", "Entidade desconhecida");
        add("command.champions.not_living_entity",  "%s não é uma entidade viva.");
        add("command.champions.not_champion",       "%s não é um campeão.");
        add("command.champions.spawn_cancelled",    "A criação foi cancelada por um ouvinte SpawnChampion.");
        add("command.champions.remove.success",     "Status de campeão removido de %s");
        add("command.champions.remove.success_deleted", "Status de campeão removido e entidade excluída.");
        add("command.champions.info.title",         "§6Informações do Campeão:§r");
        add("command.champions.info.tier",          "  §eNível:§r %s (Nível %s)");
        add("command.champions.info.affixes",       "  §eAfixos:§r");
        add("command.champions.info.affixes.none",  "  §eAfixos:§r Nenhum");
        add("command.champions.info.affix_entry",   "    - %s (Força: %s)");
        add("command.champions.info.archetype",     "  §eArquétipo:§r %s");
        add("item.champions.egg.tooltip",           "Afixos Aleatórios");
        add("advancements.champions.kill_a_champion.title",       "Caçador de Campeões");
        add("advancements.champions.kill_a_champion.description", "Mate um monstro hostil poderoso");
        add("stat.champions.champion_mobs_killed",  "Campeões Derrotados");
        add("config.jade.plugin_champions.enable_affix_compact", "Enable Jade affix compact");
    }
}
