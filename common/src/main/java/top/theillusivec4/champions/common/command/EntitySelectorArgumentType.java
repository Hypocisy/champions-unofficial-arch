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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import top.theillusivec4.champions.common.api.ChampionsRegistries;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Argument type for entity selectors supporting:
 * <ul>
 *   <li>Direct entity ID: {@code minecraft:zombie}</li>
 *   <li>Category selector: {@code @category:monster}</li>
 *   <li>Archetype selector: {@code @archetype:champions:undead}</li>
 * </ul>
 */
public final class EntitySelectorArgumentType implements ArgumentType<EntitySelectorArgumentType.Selector> {

    private static final Collection<String> EXAMPLES = Arrays.asList(
            "minecraft:zombie", "@category:monster", "@archetype:champions:undead");

    private static final DynamicCommandExceptionType INVALID_SELECTOR =
            new DynamicCommandExceptionType(sel ->
                    Component.literal("Invalid entity selector: " + sel));

    public static EntitySelectorArgumentType entitySelector() {
        return new EntitySelectorArgumentType();
    }

    public static Selector getSelector(CommandContext<CommandSourceStack> ctx, String name) {
        return ctx.getArgument(name, Selector.class);
    }

    @Override
    public Selector parse(StringReader reader) throws CommandSyntaxException {
        int start = reader.getCursor();
        String input = reader.readUnquotedString();

        // @category:monster
        if (input.startsWith("@category:")) {
            String categoryName = input.substring("@category:".length());
            return new Selector(SelectorType.CATEGORY, null, categoryName);
        }

        // @archetype:champions:undead
        if (input.startsWith("@archetype:")) {
            String idStr = input.substring("@archetype:".length());
            try {
                ResourceLocation id = ResourceLocation.parse(idStr);
                return new Selector(SelectorType.ARCHETYPE, id, null);
            } catch (Exception e) {
                reader.setCursor(start);
                throw INVALID_SELECTOR.create(input);
            }
        }

        // Direct entity ID
        try {
            ResourceLocation id = ResourceLocation.parse(input);
            return new Selector(SelectorType.DIRECT, id, null);
        } catch (Exception e) {
            reader.setCursor(start);
            throw INVALID_SELECTOR.create(input);
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(
            CommandContext<S> context, SuggestionsBuilder builder) {
        String input = builder.getRemaining().toLowerCase();

        // Suggest @category:X
        if (input.startsWith("@") || input.isEmpty()) {
            for (MobCategory category : MobCategory.values()) {
                String selector = "@category:" + category.getName();
                if (selector.toLowerCase().startsWith(input)) {
                    builder.suggest(selector);
                }
            }
        }

        // Suggest @archetype:X
        if (input.startsWith("@") || input.isEmpty()) {
            ChampionsRegistries.archetypes().getAllKeys().forEach(id -> {
                String selector = "@archetype:" + id.toString();
                if (selector.toLowerCase().startsWith(input)) {
                    builder.suggest(selector);
                }
            });
        }

        // Suggest normal entity IDs (prioritize non-misc mobs)
        return SharedSuggestionProvider.suggestResource(
                BuiltInRegistries.ENTITY_TYPE.stream()
                        .filter(t -> t.getCategory() != MobCategory.MISC)
                        .map(EntityType::getKey),
                builder);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    // ── Result types ──────────────────────────────────────────────────────────

    public enum SelectorType {
        DIRECT,      // minecraft:zombie
        CATEGORY,    // @category:monster
        ARCHETYPE    // @archetype:champions:undead
    }

    public record Selector(
            SelectorType type,
            ResourceLocation id,        // used for DIRECT and ARCHETYPE
            String categoryName         // used for CATEGORY
    ) {
    }
}
