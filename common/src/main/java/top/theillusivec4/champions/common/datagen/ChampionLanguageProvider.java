package top.theillusivec4.champions.common.datagen;
import top.theillusivec4.champions.common.utils.Utils;

import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.champions.common.datagen.lang.BrazilianPortugueseTranslations;
import top.theillusivec4.champions.common.datagen.lang.ChineseTranslations;
import top.theillusivec4.champions.common.datagen.lang.EnglishTranslations;
import top.theillusivec4.champions.common.datagen.lang.KoreanTranslations;
import top.theillusivec4.champions.common.datagen.lang.RussianTranslations;
import top.theillusivec4.champions.common.datagen.lang.TurkishTranslations;
import top.theillusivec4.champions.common.datagen.lang.UkrainianTranslations;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Platform-agnostic, multi-locale language provider for the Champions mod.
 *
 * <p>Only ONE provider instance per locale is registered (by each platform's
 * datagen); the actual translation content lives in dedicated classes under
 * {@link top.theillusivec4.champions.common.datagen.lang} — one per locale —
 * which this provider delegates to via {@link #addTranslations()}.</p>
 *
 * <p>Platform-specific entries that depend on registry objects are added by
 * platform subclasses via {@link #addPlatformEntries()}.</p>
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

    /** Delegates to the per-locale translation class in {@code datagen.lang}. */
    protected void addTranslations() {
        switch (locale) {
            case "zh_cn"  -> ChineseTranslations.add(this);
            case "ko_kr"  -> KoreanTranslations.add(this);
            case "ru_ru"  -> RussianTranslations.add(this);
            case "tr_tr"  -> TurkishTranslations.add(this);
            case "uk_ua"  -> UkrainianTranslations.add(this);
            case "pt_br"  -> BrazilianPortugueseTranslations.add(this);
            default        -> EnglishTranslations.add(this);
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
        addAffix(Utils.key(path), name);
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
        addBuiltinDeathMessages();
        addPlatformEntries();

        JsonObject json = new JsonObject();
        data.forEach(json::addProperty);

        Path path = output.getOutputFolder()
                .resolve("assets/champions/lang/" + locale + ".json");
        return DataProvider.saveStable(cache, json, path);
    }

    /**
     * Death messages for damage types owned by common code, so BOTH platforms
     * (and every locale) get them. Previously these were only generated for
     * en_us by the NeoForge subclass, leaving Fabric builds and other locales
     * without translations.
     */
    private void addBuiltinDeathMessages() {
        ResourceLocation reflection = Utils.key("reflection");
        switch (locale) {
            case "zh_cn" -> addDamageType(reflection,
                    "%1$s自食其果",
                    "%1$s在与%2$s战斗时自食其果");
            case "ko_kr" -> addDamageType(reflection,
                    "%1$s이(가) 자업자득의 대가를 치렀습니다",
                    "%1$s이(가) %2$s과(와)의 전투에서 자업자득의 대가를 치렀습니다");
            case "ru_ru" -> addDamageType(reflection,
                    "%1$s получил по заслугам",
                    "%1$s получил по заслугам в битве с %2$s");
            case "tr_tr" -> addDamageType(reflection,
                    "%1$s kendi ilacını tatmış oldu",
                    "%1$s %2$s ile savaşırken kendi ilacını tatmış oldu");
            case "uk_ua" -> addDamageType(reflection,
                    "%1$s отримав по заслугах",
                    "%1$s отримав по заслугах у битві з %2$s");
            case "pt_br" -> addDamageType(reflection,
                    "%1$s recebeu uma dose do próprio remédio",
                    "%1$s recebeu uma dose do próprio remédio enquanto lutava contra %2$s");
            default -> addDamageType(reflection,
                    "%1$s got a taste of their own medicine",
                    "%1$s got a taste of their own medicine whilst fighting %2$s");
        }
    }

    @Override
    public @NotNull String getName() {
        return "Champions Language (" + locale + ")";
    }
}
