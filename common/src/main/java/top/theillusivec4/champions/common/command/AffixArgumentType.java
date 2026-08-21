package top.theillusivec4.champions.common.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import top.theillusivec4.champions.api.ChampionsApi;
import top.theillusivec4.champions.api.affix.AffixInstance;
import top.theillusivec4.champions.api.affix.AffixType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;

/**
 * Parses a space-separated list of affix specs.
 *
 * <p>Syntax per token: {@code <id>[:<strength>]}</p>
 * <ul>
 *   <li>{@code champions:molten} — type only, strength defaults to tier level at execution time</li>
 *   <li>{@code champions:molten:3} — explicit strength 1–5</li>
 * </ul>
 *
 * <p>Returns a {@link List} of {@link AffixSpec}. The command executor resolves default
 * strength from the tier when {@link AffixSpec#strength()} is empty.</p>
 */
public final class AffixArgumentType implements ArgumentType<List<AffixArgumentType.AffixSpec>> {

    private static final Collection<String> EXAMPLES = Arrays.asList(
            "champions:molten", "champions:lively:3 champions:adaptable:5");

    private static final DynamicCommandExceptionType UNKNOWN_AFFIX =
            new DynamicCommandExceptionType(id -> Component.literal("Unknown affix: " + id));

    private static final DynamicCommandExceptionType BAD_STRENGTH =
            new DynamicCommandExceptionType(s ->
                    Component.literal("Affix strength must be 1–5, got: " + s));

    /**
     * One parsed affix token.
     *
     * @param type     the resolved affix type
     * @param strength explicit strength (1–5), or empty to use the tier level default
     */
    public record AffixSpec(AffixType<?> type, OptionalInt strength) {

        public AffixInstance toInstance(int defaultStrength) {
            int s = strength.isPresent()
                    ? Math.clamp(strength.getAsInt(), 1, 5)
                    : Math.clamp(defaultStrength, 1, 5);
            return new AffixInstance(type, s);
        }
    }

    // ── Factory / getter ──────────────────────────────────────────────────────

    public static AffixArgumentType affixes() {
        return new AffixArgumentType();
    }

    @SuppressWarnings("unchecked")
    public static List<AffixSpec> getAffixes(
            CommandContext<CommandSourceStack> ctx, String name) {
        return (List<AffixSpec>) ctx.getArgument(name, List.class);
    }

    // ── ArgumentType ──────────────────────────────────────────────────────────

    @Override
    public List<AffixSpec> parse(StringReader reader) throws CommandSyntaxException {
        List<AffixSpec> result = new ArrayList<>();

        while (reader.canRead()) {
            reader.skipWhitespace();
            if (!reader.canRead()) break;

            int start = reader.getCursor();

            // Read the full token (up to next whitespace)
            StringBuilder token = new StringBuilder();
            while (reader.canRead() && reader.peek() != ' ') {
                token.append(reader.read());
            }

            // Split on last ':' only — ResourceLocation already contains one ':'
            String raw = token.toString();
            int lastColon = raw.lastIndexOf(':');
            String idPart;
            OptionalInt strength = OptionalInt.empty();

            if (lastColon > 0 && lastColon < raw.length() - 1) {
                String maybeStrength = raw.substring(lastColon + 1);
                if (maybeStrength.matches("[1-5]")) {
                    idPart = raw.substring(0, lastColon);
                    strength = OptionalInt.of(Integer.parseInt(maybeStrength));
                } else if (maybeStrength.matches("\\d+")) {
                    reader.setCursor(start);
                    throw BAD_STRENGTH.create(maybeStrength);
                } else {
                    idPart = raw;
                }
            } else {
                idPart = raw;
            }

            ResourceLocation id;
            try {
                id = ResourceLocation.parse(idPart);
            } catch (Exception e) {
                reader.setCursor(start);
                throw UNKNOWN_AFFIX.create(idPart);
            }

            AffixType<?> type = ChampionsApi.get().getAffixType(id)
                    .orElseThrow(() -> {
                        reader.setCursor(start);
                        return UNKNOWN_AFFIX.create(id);
                    });

            result.add(new AffixSpec(type, strength));
        }

        return result;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(
            CommandContext<S> context, SuggestionsBuilder builder) {
        // Build a sub-builder anchored to the start of the current token so
        // suggestions replace only the token the user is typing, not the whole input.
        String remaining = builder.getRemaining();
        int lastSpace = remaining.lastIndexOf(' ');
        SuggestionsBuilder tokenBuilder = builder.createOffset(builder.getStart() + lastSpace + 1);

        return SharedSuggestionProvider.suggestResource(
                ChampionsApi.get().getAffixTypes().stream()
                        .flatMap(type -> ChampionsApi.get().getAffixTypeId(type).stream()),
                tokenBuilder
        );
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
