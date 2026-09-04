package top.theillusivec4.champions.common.datagen;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import top.theillusivec4.champions.api.champion.ChampionTier;
import top.theillusivec4.champions.common.data.TierDataLoader;
import top.theillusivec4.champions.common.utils.Utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates the generated {@code data/champions/champions/tier/*.json} files.
 *
 * <p>Re-parses each file body the way {@link TierDataLoader} does
 * ({@code level} required, {@code display} optional) and checks the intended
 * 1–5 fallback-tier contract on both platforms.</p>
 */
class TierGeneratedDataTest {

    private record TierFile(Path file, int level, JsonElement display) {
    }

    @Test
    void tierFilesParseAndCoverLevels1To5OnBothPlatforms() throws Exception {
        for (String platform : List.of("neoforge", "fabric")) {
            List<TierFile> tiers = loadTiers(platform);
            // exactly 5 tier files, one per level 1..5
            assertEquals(List.of(1, 2, 3, 4, 5),
                    tiers.stream().map(TierFile::level).sorted().toList(),
                    platform + " must generate exactly tiers 1..5");

            for (TierFile t : tiers) {
                assertTrue(t.level() >= 1 && t.level() <= 5,
                        platform + " tier file " + t.file + " has implausible level " + t.level());
                // display is optional and must be an object when present
                if (t.display() != null) {
                    assertTrue(t.display().isJsonObject(),
                            platform + " tier file " + t.file + " display must be an object");
                }
            }
        }
    }

    @Test
    void tierIdsFollowTheChampionsNamespaceConvention() throws Exception {
        for (String platform : List.of("neoforge", "fabric")) {
            for (TierFile t : loadTiers(platform)) {
                ResourceLocation id = Utils.key( t.file().getFileName().toString().replace(".json", ""));
                assertEquals("champions", id.getNamespace(),
                        platform + " tier file " + t.file + " must be in champions namespace");
                assertTrue(id.getPath().matches("tier_\\d+"),
                        platform + " tier file " + t.file + " must match tier_N naming");
            }
        }
    }

    @Test
    void eachTierBuildsIntoAValidChampionTier() throws Exception {
        for (String platform : List.of("neoforge", "fabric")) {
            for (TierFile t : loadTiers(platform)) {
                ResourceLocation id = Utils.key(t.file().getFileName().toString().replace(".json", ""));
                // Rebuild the ChampionTier the loader would produce and sanity-check invariants.
                ChampionTier.TierDisplay display = t.display() != null
                        ? parseDisplay(t.display())
                        : ChampionTier.TierDisplay.defaultFor(t.level());
                ChampionTier tier = new ChampionTier(id, t.level(), display);
                assertEquals(id, tier.id(), platform + " tier id mismatch");
                assertEquals(t.level(), tier.level(), platform + " tier level mismatch");
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static List<TierFile> loadTiers(String platform) throws IOException {
        Path dir = GeneratedDataTestSupport.generatedRoot(platform).resolve("data/champions/champions/tier");
        List<TierFile> out = new ArrayList<>();
        try (var stream = Files.list(dir)) {
            for (Path p : stream.filter(x -> x.getFileName().toString().endsWith(".json"))
                    .sorted(Comparator.comparing(x -> x.getFileName().toString())).toList()) {
                JsonElement json = JsonParser.parseReader(Files.newBufferedReader(p, StandardCharsets.UTF_8));
                assertTrue(json.isJsonObject(), "tier file " + p + " must be a JSON object");
                JsonElement levelEl = json.getAsJsonObject().get("level");
                assertNotNull(levelEl, "tier file " + p + " is missing required 'level' field");
                assertTrue(levelEl.isJsonPrimitive() && levelEl.getAsJsonPrimitive().isNumber(),
                        "tier file " + p + " 'level' must be a number");
                int level = levelEl.getAsInt();
                JsonElement display = json.getAsJsonObject().get("display"); // optional
                out.add(new TierFile(p, level, display));
            }
        }
        return out;
    }

    private static ChampionTier.TierDisplay parseDisplay(JsonElement display) {
        // {"color": <int>, "icon": "rl"} — the loader defaults any missing field.
        int color = display.getAsJsonObject().has("color")
                ? display.getAsJsonObject().get("color").getAsInt()
                : 0xFFFFFFFF;
        return new ChampionTier.TierDisplay(color,
                ResourceLocation.tryParse("textures/gui/icons.png"));
    }
}
