package top.theillusivec4.champions.common.datagen.lang;

import top.theillusivec4.champions.common.datagen.ChampionLanguageProvider;

/** Turkish (tr_tr) translations. */
public final class TurkishTranslations {

    private TurkishTranslations() {}

    public static void add(ChampionLanguageProvider p) {
        // ── Affixes & ranks ─────────────────────────────────────────────────
        p.addAffix("adaptable",   "Adaptif");
        p.addAffix("arctic",      "Buz-gibi");
        p.addAffix("dampening",   "Soğurucu");
        p.addAffix("desecrating", "Saygısız");
        p.addAffix("enkindling",  "Tutuşturucu");
        p.addAffix("hasty",       "Aceleci");
        p.addAffix("infested",    "Böcekli");
        p.addAffix("knocking",    "Tepici");
        p.addAffix("lively",      "Yaşam-dolu");
        p.addAffix("magnetic",    "Manyetik");
        p.addAffix("molten",      "Erimiş");
        p.addAffix("paralyzing",  "Felç-edici");
        p.addAffix("plagued",     "Hastalıklı");
        p.addAffix("reflective",  "Yansıtıcı");
        p.addAffix("shielding",   "Korumalı");
        p.addAffix("wounding",    "Yaralayıcı");
        p.addRank(1, "Yaygın");
        p.addRank(2, "Yetenekli");
        p.addRank(3, "Elit");
        p.addRank(4, "Efsanevi");
        p.addRank(5, "Nihai");

        // ── Commands ────────────────────────────────────────────────────────
        p.add("argument.champions.affix.unknown",     "%s - Bilinmeyen özellik");
        p.add("commands.champions.summon.success",    "Yeni %s çağrıldı");
        p.add("commands.champions.egg.success",       "Yeni %s oluşturuldu");
        p.add("command.champions.unknown_entity",     "Bilinmeyen varlık: %s");
        p.add("command.champions.unknown_tier",       "%s seviyesinde bir kademe bulunamadı. Kullanılabilir: %s");
        p.add("command.champions.egg.unknown_entity", "Bilinmeyen Yaratık");
        p.add("command.champions.not_living_entity",  "%s canlı bir varlık değil.");
        p.add("command.champions.not_champion",       "%s bir şampiyon değil.");
        p.add("command.champions.spawn_cancelled",    "Oluşturma SpawnChampion dinleyicisi tarafından iptal edildi.");
        p.add("command.champions.remove.success",     "%s'den şampiyon durumu kaldırıldı");
        p.add("command.champions.remove.success_deleted", "Şampiyon durumu kaldırıldı ve varlık silindi.");
        p.add("command.champions.info.title",         "§6Şampiyon Bilgisi:§r");
        p.add("command.champions.info.tier",          "  §eSeviye:§r %s (Seviye %s)");
        p.add("command.champions.info.affixes",       "  §eÖzellikler:§r");
        p.add("command.champions.info.affixes.none",  "  §eÖzellikler:§r Yok");
        p.add("command.champions.info.affix_entry",   "    - %s (Güç: %s)");
        p.add("command.champions.info.archetype",     "  §eArketip:§r %s");
        p.add("command.champions.help.title",         "§6Şampiyon Komutları:§r");
        p.add("command.champions.help.summon",        "  §e/champions summon <varlık|@category:X|@archetype:X> <seviye> [özellikler...] [at <x> <y> <z>]§r - Şampiyon oluştur");
        p.add("command.champions.help.apply",         "  §e/champions apply <hedef> <seviye> [özellikler...]§r - Mevcut varlığa şampiyon uygula");
        p.add("command.champions.help.remove",        "  §e/champions remove <hedef> [varlığıSil]§r - Şampiyon durumunu kaldır");
        p.add("command.champions.help.info",          "  §e/champions info <hedef>§r - Şampiyon bilgilerini göster (tüm oyuncular kullanabilir)");
        p.add("command.champions.help.egg",           "  §e/champions egg <varlık|@category:X|@archetype:X> <seviye|random> [özellikler...]§r - Şampiyon yumurtası oluştur");
        p.add("command.champions.help.editor",        "  §e/champions editor§r - Veri paketi düzenleyicisini aç");

        // ── Item / advancements / stats ─────────────────────────────────────
        p.add("item.champions.egg",                 "Şampiyon Yumurtası");
        p.add("item.champions.egg.tooltip",         "Rasgele Özellikli");
        p.add("item.champions.egg.random",          "Rastgele");
        p.add("item.champions.egg.no_affixes",      "Özellik yok");
        p.add("advancements.champions.kill_a_champion.title",       "Şampiyon Avcısı");
        p.add("advancements.champions.kill_a_champion.description", "Güçlü bir düşman öldür");
        p.add("stat.champions.champion_mobs_killed", "Öldürülen Şampiyon Sayısı");
        p.add("config.jade.plugin_champions.enable_affix_compact", "Jade özellik kompakt görünümünü etkinleştir");

        // ── In-game editor screen (gui.champions.*) ─────────────────────────
        p.add("gui.champions.editor.title",         "Şampiyon Düzenleyici");
        p.add("gui.champions.editor.tab.archetypes", "Arketipler");
        p.add("gui.champions.editor.tab.tiers",      "Kademeler");
        p.add("gui.champions.editor.tab.modifiers",  "Değiştiriciler");
        p.add("gui.champions.editor.tab.config",     "Yapılandırma");
        p.add("gui.champions.editor.tab.packs",      "Veri Paketleri");
        p.add("gui.champions.editor.view.form",     "Form");
        p.add("gui.champions.editor.view.json",     "JSON");
        p.add("gui.champions.editor.new",           "§a+ Yeni");
        p.add("gui.champions.editor.delete",        "§cSil");
        p.add("gui.champions.editor.save_reload",   "Kaydet ve Yeniden Yükle");
        p.add("gui.champions.editor.close",         "Kapat");
        p.add("gui.champions.editor.entries",       "§8GİRİŞLER §7%s");
        p.add("gui.champions.editor.unsaved",       "§e● %s kaydedilmedi");
        p.add("gui.champions.editor.error.not_object", "JSON bir nesne olmalıdır");
        p.add("gui.champions.editor.error.invalid_json", "Geçersiz JSON: %s");
        p.add("gui.champions.editor.toggle.true",   "§aAçık");
        p.add("gui.champions.editor.toggle.false",  "§cKapalı");
        p.add("gui.champions.editor.pick_affix",    "Özellik seç… §8%s");

        p.add("gui.champions.picker.done",          "Tamam");
        p.add("gui.champions.picker.cancel",        "İptal");
        p.add("gui.champions.picker.search_hint",   "§7Ara…");
        p.add("gui.champions.picker.selected",      "Seçili: %s ·");
        p.add("gui.champions.picker.count",         "%s / %s");
        p.add("gui.champions.picker.title.affixes",       "Özellikler");
        p.add("gui.champions.picker.title.affix_values",  "Özellik değerleri");
        p.add("gui.champions.picker.title.entity_types",  "Varlık türleri");
        p.add("gui.champions.picker.title.mod_namespaces", "Mod ad alanları");
        p.add("gui.champions.picker.title.mob_categories", "Yaratık kategorileri");
        p.add("gui.champions.picker.title.filter_type",   "Filtre türü");

        p.add("gui.champions.editor.label.id",             "ID");
        p.add("gui.champions.editor.label.weight",         "ağırlık");
        p.add("gui.champions.editor.label.type",           "tür");
        p.add("gui.champions.editor.label.min",            "min");
        p.add("gui.champions.editor.label.max",            "maks");
        p.add("gui.champions.editor.label.tier_min",       "min. kademe");
        p.add("gui.champions.editor.label.tier_max",       "maks. kademe");
        p.add("gui.champions.editor.label.min_count",      "min. adet");
        p.add("gui.champions.editor.label.max_count",      "maks. adet");
        p.add("gui.champions.editor.label.affix",          "özellik");
        p.add("gui.champions.editor.label.min_strength",   "min. güç");
        p.add("gui.champions.editor.label.max_strength",   "maks. güç");
        p.add("gui.champions.editor.label.strength",       "güç");
        p.add("gui.champions.editor.label.below",          "altında");
        p.add("gui.champions.editor.label.seconds",        "saniye");
        p.add("gui.champions.editor.label.count",          "adet");
        p.add("gui.champions.editor.label.amount",         "miktar");
        p.add("gui.champions.editor.label.operation",      "işlem");
        p.add("gui.champions.editor.label.attribute",      "nitelik");
        p.add("gui.champions.editor.label.effect",         "etki");
        p.add("gui.champions.editor.label.amplifier",      "etki seviyesi");
        p.add("gui.champions.editor.label.infinite",       "sonsuz");
        p.add("gui.champions.editor.label.duration_ticks", "süre (tik)");
        p.add("gui.champions.editor.label.enable",         "etkin");
        p.add("gui.champions.editor.label.value",          "değer");
        p.add("gui.champions.editor.label.level",          "seviye");
        p.add("gui.champions.editor.label.color",          "renk");
        p.add("gui.champions.editor.label.icon",           "simge");
        p.add("gui.champions.editor.label.repeatable",     "tekrarlanabilir");
        p.add("gui.champions.editor.label.tag",            "etiket");
        p.add("gui.champions.editor.label.whitelist",      "beyaz liste");
        p.add("gui.champions.editor.label.state",          "durum");

        p.add("gui.champions.editor.header.archetype",       "Arketip");
        p.add("gui.champions.editor.header.tier_range",      "Kademe Aralığı");
        p.add("gui.champions.editor.header.entity_filter",   "Varlık Filtresi");
        p.add("gui.champions.editor.header.affix_pools",     "Özellik Havuzları");
        p.add("gui.champions.editor.header.pool",            "Havuz %s");
        p.add("gui.champions.editor.header.candidates",      "Adaylar");
        p.add("gui.champions.editor.header.candidate",       "Aday %s");
        p.add("gui.champions.editor.header.phases",          "Aşamalar");
        p.add("gui.champions.editor.header.phase",           "Aşama: %s");
        p.add("gui.champions.editor.header.condition",       "Koşul");
        p.add("gui.champions.editor.header.effects",         "Etkiler");
        p.add("gui.champions.editor.header.effect",          "Etki %s");
        p.add("gui.champions.editor.header.modifier_setting", "Değiştirici Ayarı");
        p.add("gui.champions.editor.header.modifier",        "Değiştirici");
        p.add("gui.champions.editor.header.conditions",      "Koşullar");
        p.add("gui.champions.editor.header.tier",            "Kademe");
        p.add("gui.champions.editor.header.display",         "Görünüm");
        p.add("gui.champions.editor.header.import_export",   "İçe / Dışa Aktarma");
        p.add("gui.champions.editor.header.world_datapacks", "Dünya Veri Paketleri");
        p.add("gui.champions.editor.header.pack",            "Paket: %s");
        p.add("gui.champions.editor.header.server_config",   "Sunucu Yapılandırması");
        p.add("gui.champions.editor.header.filter",          "Filtre %s");

        p.add("gui.champions.editor.action.add_candidate",     "+ Aday ekle");
        p.add("gui.champions.editor.action.add_pool",          "+ Havuz ekle");
        p.add("gui.champions.editor.action.add_effect",        "+ Etki ekle");
        p.add("gui.champions.editor.action.add_phase",         "+ Aşama ekle");
        p.add("gui.champions.editor.action.add_filter",        "+ Filtre ekle");
        p.add("gui.champions.editor.action.add_child_filter",  "+ Alt filtre ekle");
        p.add("gui.champions.editor.action.pick_affix_values",  "Özellik değerleri… (%s seçili)");
        p.add("gui.champions.editor.action.pick_entity_types",  "Varlık türleri… (%s seçili)");
        p.add("gui.champions.editor.action.pick_mod_namespaces", "Mod ad alanları… (%s seçili)");
        p.add("gui.champions.editor.action.pick_categories",    "Kategoriler… (%s seçili)");
        p.add("gui.champions.editor.action.export",            "§bDüzenleyici içeriğini zip olarak dışa aktar");
        p.add("gui.champions.editor.action.import",            "§bchampions_imports/ içinden zip içe aktar");

        p.add("gui.champions.editor.hint.unknown_condition", "§cbilinmeyen koşul türü");
        p.add("gui.champions.editor.hint.unknown_effect",    "§cbilinmeyen etki türü");
        p.add("gui.champions.editor.hint.unknown_filter",    "§cbilinmeyen filtre türü: %s");
        p.add("gui.champions.editor.hint.matches_every",     "her varlıkla eşleşir");
        p.add("gui.champions.editor.hint.filters_anded",     "alt filtreler VE ile birleşir");
        p.add("gui.champions.editor.hint.filters_ored",      "alt filtreler VEYA ile birleşir");
        p.add("gui.champions.editor.hint.tier_display",      "renk: onaltılık dize · simge: doku yolu");
        p.add("gui.champions.editor.hint.json_view_only",    "affixes.matches / affixes.count: JSON görünümünü kullanın");
        p.add("gui.champions.editor.hint.export_target",     "champions_<zaman>.zip oluşturur:");
        p.add("gui.champions.editor.hint.exports_dir",       "<world>/champions_exports/");
        p.add("gui.champions.editor.hint.import_dir",        "veri paketi zip'lerini <world>/champions_imports/ içine koyun");
        p.add("gui.champions.editor.hint.import_copied",     "datapacks/ klasörüne kopyalanır ve etkinleştirilir");
        p.add("gui.champions.editor.hint.no_packs",          "paket yüklenmedi");
        p.add("gui.champions.editor.hint.packs_enabled",     "%s / %s etkin");
        p.add("gui.champions.editor.hint.reload_on_toggle",  "geçiş, sunucu kaynaklarını yeniden yükler");
        p.add("gui.champions.editor.hint.select_pack",       "etkinleştirmek/devre dışı bırakmak için listeden bir paket seçin");
        p.add("gui.champions.editor.hint.config_reload",     "yapılandırma, Kaydet ve Yeniden Yükle ile uygulanır");
        p.add("gui.champions.editor.pack.enabled",           "§a● etkin");
        p.add("gui.champions.editor.pack.disabled",          "§c○ devre dışı");

        p.add("gui.champions.editor.filter.type.any",          "herhangi");
        p.add("gui.champions.editor.filter.type.all_of",       "tümü (VE)");
        p.add("gui.champions.editor.filter.type.any_of",       "herhangi biri (VEYA)");
        p.add("gui.champions.editor.filter.type.entity_type",  "varlık türü");
        p.add("gui.champions.editor.filter.type.entity_tag",   "varlık etiketi");
        p.add("gui.champions.editor.filter.type.mod_id",       "mod kimliği");
        p.add("gui.champions.editor.filter.type.mob_category", "yaratık kategorisi");
        p.add("gui.champions.editor.filter.type.attribute",    "nitelik");
    }
}
