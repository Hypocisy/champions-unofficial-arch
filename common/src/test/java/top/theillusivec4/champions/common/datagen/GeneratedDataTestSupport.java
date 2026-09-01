package top.theillusivec4.champions.common.datagen;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import top.theillusivec4.champions.common.archetype.ChampionArchetype;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;

/**
 * Shared helpers for the datagen-output tests.
 *
 * <p>Locates the {@code src/generated/resources} tree of a platform module and
 * parses the generated archetype JSON back through the same codecs the game
 * uses ({@link ChampionArchetype#CODEC}). Gradle test JVMs run with the module
 * directory as cwd ({@code common/}), so {@code ../neoforge} from there lands
 * on the platform module.</p>
 */
final class GeneratedDataTestSupport {

    private GeneratedDataTestSupport() {
    }

    static Path generatedRoot(String platform) {
        Path cwd = Path.of("").toAbsolutePath();
        List<Path> candidates = List.of(
                cwd.resolve("..").resolve(platform).resolve("src/generated/resources"),
                cwd.resolve(platform).resolve("src/generated/resources"),
                cwd.resolve("..").resolve("..").resolve("champions-unofficial-arch")
                        .resolve(platform).resolve("src/generated/resources"));
        for (Path c : candidates) {
            if (Files.isDirectory(c.resolve("data"))) {
                return c.normalize();
            }
        }
        throw new IllegalStateException("Cannot locate generated resources for " + platform +
                " from " + cwd + " (tried " + candidates + ")");
    }

    static List<ChampionArchetype> loadArchetypes(String platform) throws IOException {
        Path dir = generatedRoot(platform).resolve("data/champions/champions/archetype");
        try (var stream = Files.list(dir)) {
            return stream.filter(p -> p.getFileName().toString().endsWith(".json"))
                    .map(GeneratedDataTestSupport::parseArchetype)
                    .sorted(Comparator.comparing(a -> a.id().getPath()))
                    .toList();
        }
    }

    private static ChampionArchetype parseArchetype(Path file) {
        JsonElement json;
        try {
            json = JsonParser.parseReader(Files.newBufferedReader(file, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        ResourceLocation fileId = ResourceLocation.fromNamespaceAndPath(
                "champions", file.getFileName().toString().replace(".json", ""));
        StringBuilder errors = new StringBuilder();
        DataResult<ChampionArchetype> result = ChampionArchetype.CODEC.parse(JsonOps.INSTANCE, json);
        var opt = result.resultOrPartial(errors::append);
        if (opt.isEmpty()) {
            throw new AssertionError("Failed to parse archetype " + file + ": " + errors);
        }
        ChampionArchetype a = opt.get();
        if (!a.id().equals(fileId)) {
            // mirror ArchetypeDataLoader: the file-path id wins
            a = new ChampionArchetype(fileId, a.tierRange(), a.weight(),
                    a.entityFilter(), a.affixPools(), a.phases());
        }
        return a;
    }
}
