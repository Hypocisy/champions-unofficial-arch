package top.theillusivec4.champions.common.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import top.theillusivec4.champions.api.ChampionsApi;
import top.theillusivec4.champions.api.affix.AffixInstance;
import top.theillusivec4.champions.api.champion.Champion;
import top.theillusivec4.champions.api.champion.ChampionTier;
import top.theillusivec4.champions.common.api.ChampionsRegistries;
import top.theillusivec4.champions.common.champion.ChampionData;
import top.theillusivec4.champions.common.item.ChampionEggItem;
import top.theillusivec4.champions.common.item.ChampionItems;
import top.theillusivec4.champions.common.network.PacketHandler;
import top.theillusivec4.champions.platform.ChampionAttachmentProvider;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * /champions summon <entity|@category:X|@archetype:X> <tier> [affixes...] [at <x> <y> <z>]
 * /champions apply <target> <tier> [affixes...]
 * /champions remove <target> [deleteEntity]
 * /champions info <target> (available to all players)
 * /champions egg <entity|@category:X|@archetype:X> <tier|random> [affixes...]
 * /champions editor (opens datapack editor)
 * /champions help (shows all commands)
 *
 * <p>Entity argument supports:
 * <ul>
 *   <li>Direct entity ID: {@code minecraft:zombie}</li>
 *   <li>Category selector: {@code @category:monster} - picks random entity from that category</li>
 *   <li>Archetype selector: {@code @archetype:champions:undead} - picks random entity from archetype's filter</li>
 * </ul>
 */
public final class ChampionCommand {

    private ChampionCommand() {}

    private static final DynamicCommandExceptionType UNKNOWN_ENTITY =
            new DynamicCommandExceptionType(id ->
                    Component.translatable("command.champions.unknown_entity", id));

    private static final DynamicCommandExceptionType UNKNOWN_TIER =
            new DynamicCommandExceptionType(level ->
                    Component.translatable("command.champions.unknown_tier", level, availableLevels()));

    private static final DynamicCommandExceptionType NOT_CHAMPION =
            new DynamicCommandExceptionType(entity ->
                    Component.translatable("command.champions.not_champion", entity));

    private static final DynamicCommandExceptionType NOT_LIVING_ENTITY =
            new DynamicCommandExceptionType(entity ->
                    Component.translatable("command.champions.not_living_entity", entity));

    // ── Registration ──────────────────────────────────────────────────────────

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // ── /champions info <target> (available to all players) ───────────────
        dispatcher.register(
            Commands.literal("champions")
                .then(Commands.literal("info")
                    .then(Commands.argument("target", EntityArgument.entity())
                        .executes(ChampionCommand::executeInfo)))
        );

        // ── OP-only subcommands ───────────────────────────────────────────────
        dispatcher.register(
            Commands.literal("champions")
                .requires(src -> src.hasPermission(2))

                // ── /champions help ────────────────────────────────────────────
                .then(Commands.literal("help")
                    .executes(ChampionCommand::executeHelp))

                // ── /champions editor ──────────────────────────────────────────
                .then(Commands.literal("editor")
                    .executes(ChampionCommand::executeOpenEditor))

                // ── /champions summon <entity|@category:X|@archetype:X> <tier> [affixes...] [at <x> <y> <z>]
                .then(Commands.literal("summon")
                    .then(Commands.argument("entity", ResourceLocationArgument.id())
                        .suggests((ctx, b) -> suggestEntitiesAndSpecial(b))
                        .then(Commands.argument("tier", IntegerArgumentType.integer(1))
                            .suggests((ctx, b) -> suggestTiers(b))
                            // No affixes, no position
                            .executes(ctx -> executeSummon(ctx, null,
                                IntegerArgumentType.getInteger(ctx, "tier"), List.of()))
                            // With affixes, no position
                            .then(Commands.argument("affixes", AffixArgumentType.affixes())
                                .executes(ctx -> executeSummon(ctx, null,
                                    IntegerArgumentType.getInteger(ctx, "tier"),
                                    AffixArgumentType.getAffixes(ctx, "affixes")))
                                // With affixes AND position
                                .then(Commands.literal("at")
                                    .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(ctx -> executeSummon(ctx,
                                            BlockPosArgument.getSpawnablePos(ctx, "pos"),
                                            IntegerArgumentType.getInteger(ctx, "tier"),
                                            AffixArgumentType.getAffixes(ctx, "affixes"))))))
                            // No affixes, WITH position
                            .then(Commands.literal("at")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                    .executes(ctx -> executeSummon(ctx,
                                        BlockPosArgument.getSpawnablePos(ctx, "pos"),
                                        IntegerArgumentType.getInteger(ctx, "tier"),
                                        List.of())))))))

                // ── /champions apply <target> <tier> [affixes...] ─────────────
                .then(Commands.literal("apply")
                    .then(Commands.argument("target", EntityArgument.entity())
                        .then(Commands.argument("tier", IntegerArgumentType.integer(1))
                            .suggests((ctx, b) -> suggestTiers(b))
                            .executes(ctx -> executeApply(ctx,
                                IntegerArgumentType.getInteger(ctx, "tier"), List.of()))
                            .then(Commands.argument("affixes", AffixArgumentType.affixes())
                                .executes(ctx -> executeApply(ctx,
                                    IntegerArgumentType.getInteger(ctx, "tier"),
                                    AffixArgumentType.getAffixes(ctx, "affixes")))))))

                // ── /champions remove <target> [deleteEntity] ──────────────────
                .then(Commands.literal("remove")
                    .then(Commands.argument("target", EntityArgument.entity())
                        .executes(ctx -> executeRemove(ctx, false))
                        .then(Commands.argument("deleteEntity", BoolArgumentType.bool())
                            .executes(ctx -> executeRemove(ctx,
                                BoolArgumentType.getBool(ctx, "deleteEntity"))))))

                // ── /champions egg <entity|@category:X|@archetype:X> <tier> [affixes...] ─────
                // ── /champions egg <entity|@category:X|@archetype:X> random   ─────────────────
                .then(Commands.literal("egg")
                    .then(Commands.argument("entity", ResourceLocationArgument.id())
                        .suggests((ctx, b) -> suggestEntitiesAndSpecial(b))
                        // preset mode: tier + optional affixes
                        .then(Commands.argument("tier", IntegerArgumentType.integer(1))
                            .suggests((ctx, b) -> suggestTiers(b))
                            .executes(ctx -> executeEgg(ctx,
                                IntegerArgumentType.getInteger(ctx, "tier"), List.of()))
                            .then(Commands.argument("affixes", AffixArgumentType.affixes())
                                .executes(ctx -> executeEgg(ctx,
                                    IntegerArgumentType.getInteger(ctx, "tier"),
                                    AffixArgumentType.getAffixes(ctx, "affixes")))))
                        // random mode: no preset
                        .then(Commands.literal("random")
                            .executes(ChampionCommand::executeEggRandom))))
        );
    }

    // ── /champions help ──────────────────────────────────────────────────────

    private static int executeHelp(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        source.sendSuccess(() -> Component.translatable("command.champions.help.title"), false);
        source.sendSuccess(() -> Component.translatable("command.champions.help.summon"), false);
        source.sendSuccess(() -> Component.translatable("command.champions.help.apply"), false);
        source.sendSuccess(() -> Component.translatable("command.champions.help.remove"), false);
        source.sendSuccess(() -> Component.translatable("command.champions.help.info"), false);
        source.sendSuccess(() -> Component.translatable("command.champions.help.egg"), false);
        source.sendSuccess(() -> Component.translatable("command.champions.help.editor"), false);
        return Command.SINGLE_SUCCESS;
    }

    // ── /champions editor ─────────────────────────────────────────────────────

    private static int executeOpenEditor(CommandContext<CommandSourceStack> ctx)
            throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        PacketHandler.Holder.get().sendEditorToPlayer(player);
        return Command.SINGLE_SUCCESS;
    }

    // ── /champions remove ─────────────────────────────────────────────────────

    private static int executeRemove(
            CommandContext<CommandSourceStack> ctx,
            boolean deleteEntity
    ) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        Entity target = EntityArgument.getEntity(ctx, "target");

        if (!(target instanceof LivingEntity living)) {
            source.sendFailure(Component.translatable("command.champions.not_living_entity", target.getName()));
            return 0;
        }

        if (!ChampionsApi.get().isChampion(living)) {
            throw NOT_CHAMPION.create(target.getName());
        }

        ChampionAttachmentProvider.Holder.get().remove(living);
        PacketHandler.Holder.get().clearChampionForTrackers(living);

        if (deleteEntity) {
            living.discard();
            source.sendSuccess(() -> Component.translatable("command.champions.remove.success_deleted"), true);
        } else {
            source.sendSuccess(() -> Component.translatable("command.champions.remove.success", target.getName()), true);
        }

        return Command.SINGLE_SUCCESS;
    }

    // ── /champions info ───────────────────────────────────────────────────────

    private static int executeInfo(CommandContext<CommandSourceStack> ctx)
            throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        Entity target = EntityArgument.getEntity(ctx, "target");

        if (!(target instanceof LivingEntity living)) {
            source.sendFailure(Component.translatable("command.champions.not_living_entity", target.getName()));
            return 0;
        }

        var championOpt = ChampionsApi.get().getChampion(living);
        if (championOpt.isEmpty()) {
            throw NOT_CHAMPION.create(target.getName());
        }

        var champion = championOpt.get();
        var tier = champion.tier();

        // Build info message
        source.sendSuccess(() -> Component.translatable("command.champions.info.title"), false);

        String tierName = Component.translatableWithFallback(
                "rank.champions.title." + tier.level(),
                "Tier " + tier.level()
        ).getString();
        source.sendSuccess(() -> Component.translatable(
                "command.champions.info.tier", tierName, tier.level()), false);

        if (champion.affixes().isEmpty()) {
            source.sendSuccess(() -> Component.translatable("command.champions.info.affixes.none"), false);
        } else {
            source.sendSuccess(() -> Component.translatable("command.champions.info.affixes"), false);
            for (AffixInstance affix : champion.affixes()) {
                ChampionsApi.get().getAffixTypeId(affix.type()).ifPresent(id -> {
                    String affixName = Component.translatableWithFallback(
                            "affix." + id.getNamespace() + "." + id.getPath() + ".name",
                            id.getPath()
                    ).getString();
                    source.sendSuccess(() -> Component.translatable(
                            "command.champions.info.affix_entry", affixName, affix.strength()), false);
                });
            }
        }

        if (champion instanceof Champion.Server server) {
            server.archetypeId().ifPresent(id ->
                    source.sendSuccess(() -> Component.translatable("command.champions.info.archetype", id.toString()), false)
            );
        }

        return Command.SINGLE_SUCCESS;
    }

    // ── /champions summon ─────────────────────────────────────────────────────

    private static int executeSummon(
            CommandContext<CommandSourceStack> ctx,
            BlockPos explicitPos,
            int tierLevel,
            List<AffixArgumentType.AffixSpec> affixTypes
    ) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        ServerLevel level = source.getLevel();

        EntityType<?> entityType = resolveEntity(ctx);
        ChampionTier tier = resolveTier(tierLevel, source);

        Vec3 src = source.getPosition();
        BlockPos spawnPos = explicitPos != null
                ? explicitPos
                : BlockPos.containing(src.x, src.y, src.z);

        Entity raw = entityType.create(level, null, spawnPos, MobSpawnType.COMMAND, false, false);
        if (!(raw instanceof LivingEntity living)) {
            source.sendFailure(Component.translatable(
                    "command.champions.not_living_entity",
                    ResourceLocationArgument.getId(ctx, "entity")));
            return 0;
        }

        List<AffixInstance> affixes = buildAffixInstances(affixTypes, tier);
        var result = affixes.isEmpty()
                ? ChampionsRegistries.builder().trySpawn(living, tier, living.getRandom())
                : ChampionsRegistries.builder().trySpawnWithAffixes(
                        living, tier, affixes, living.getRandom(), null);

        level.addFreshEntity(living);

        if (result.isPresent()) {
            Component label = buildLabel(tier, living, result.get().affixes());
            source.sendSuccess(() -> Component.translatable("commands.champions.summon.success", label), true);
            return Command.SINGLE_SUCCESS;
        }
        source.sendFailure(Component.translatable("command.champions.spawn_cancelled"));
        return 0;
    }

    // ── /champions apply ──────────────────────────────────────────────────────

    private static int executeApply(
            CommandContext<CommandSourceStack> ctx,
            int tierLevel,
            List<AffixArgumentType.AffixSpec> affixTypes
    ) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        Entity target = EntityArgument.getEntity(ctx, "target");

        if (!(target instanceof LivingEntity living)) {
            source.sendFailure(Component.translatable("command.champions.not_living_entity", target.getName()));
            return 0;
        }
        if (target.level().isClientSide()) {
            source.sendFailure(Component.literal("Target must be server-side."));
            return 0;
        }

        ChampionTier tier = resolveTier(tierLevel, source);
        List<AffixInstance> affixes = buildAffixInstances(affixTypes, tier);

        var result = affixes.isEmpty()
                ? ChampionsRegistries.builder().trySpawn(living, tier, living.getRandom())
                : ChampionsRegistries.builder().trySpawnWithAffixes(
                        living, tier, affixes, living.getRandom(), null);

        if (result.isPresent()) {
            Component label = buildLabel(tier, living, result.get().affixes());
            source.sendSuccess(() -> Component.translatable("commands.champions.summon.success", label), true);
            return Command.SINGLE_SUCCESS;
        }
        source.sendFailure(Component.translatable("command.champions.spawn_cancelled"));
        return 0;
    }

    // ── /champions egg ────────────────────────────────────────────────────────

    private static int executeEgg(
            CommandContext<CommandSourceStack> ctx,
            int tierLevel,
            List<AffixArgumentType.AffixSpec> affixTypes
    ) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        ServerPlayer player = source.getPlayerOrException();

        EntityType<?> entityType = resolveEntity(ctx);
        ChampionTier tier = resolveTier(tierLevel, source);

        List<AffixInstance> affixes = buildAffixInstances(affixTypes, tier);
        List<ChampionData.AffixEntry> entries = affixes.stream()
                .flatMap(inst -> ChampionsApi.get().getAffixTypeId(inst.type())
                        .map(id -> new ChampionData.AffixEntry(id, inst.strength(), new net.minecraft.nbt.CompoundTag()))
                        .stream())
                .toList();
        ChampionData preset = new ChampionData(tier.id(), entries,
                List.of(), java.util.Optional.empty());
        ItemStack egg = ChampionEggItem.createPreset(entityType, preset);
        giveItem(player, egg);

        source.sendSuccess(() -> Component.translatable("commands.champions.egg.success",
                egg.getDisplayName()), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int executeEggRandom(CommandContext<CommandSourceStack> ctx)
            throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        ServerPlayer player = source.getPlayerOrException();

        EntityType<?> entityType = resolveEntity(ctx);
        ItemStack egg = ChampionEggItem.createRandom(entityType);
        giveItem(player, egg);

        source.sendSuccess(() -> Component.translatable("commands.champions.egg.success",
                egg.getDisplayName()), true);
        return Command.SINGLE_SUCCESS;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Resolves entity from argument, supporting:
     * - Direct entity ID: minecraft:zombie
     * - Category selector: @category:monster
     * - Archetype selector: @archetype:<id>
     */
    private static EntityType<?> resolveEntity(CommandContext<CommandSourceStack> ctx)
            throws CommandSyntaxException {
        ResourceLocation id = ResourceLocationArgument.getId(ctx, "entity");
//        String path = id.getPath();

        /*// @category:monster → pick random entity from that category
        if ("category".equals(id.getNamespace())) {
            MobCategory category = MobCategory.valueOf(path);
	        List<EntityType<?>> candidates = BuiltInRegistries.ENTITY_TYPE.stream()
                    .filter(t -> t.getCategory() == category)
                    .toList();
            if (candidates.isEmpty()) {
                throw UNKNOWN_ENTITY.create(id);
            }
            return candidates.get(ctx.getSource().getLevel().getRandom().nextInt(candidates.size()));
        }

        // @archetype:<id> → pick random entity from that archetype's entity filter
        if ("archetype".equals(id.getNamespace())) {
            ResourceLocation archetypeId = ResourceLocation.parse(path);
            var archetype = ChampionsRegistries.archetypes().get(archetypeId);
            if (archetype.isEmpty()) {
                throw UNKNOWN_ENTITY.create(id);
            }
            ServerLevel level = ctx.getSource().getLevel();
            List<EntityType<?>> candidates = BuiltInRegistries.ENTITY_TYPE.stream()
                    .filter(entityType -> {
                        // Create a dummy entity to test the filter
                        Entity dummy = entityType.create(level);
                        if (!(dummy instanceof LivingEntity living)) {
                            if (dummy != null) dummy.discard();
                            return false;
                        }
                        boolean matches = archetype.get().entityFilter().matches(living);
                        dummy.discard();
                        return matches;
                    })
                    .toList();
            if (candidates.isEmpty()) {
                throw UNKNOWN_ENTITY.create(id);
            }
            return candidates.get(level.getRandom().nextInt(candidates.size()));
        }*/

        // Direct entity ID lookup
        return BuiltInRegistries.ENTITY_TYPE.getOptional(id)
                .orElseThrow(() -> UNKNOWN_ENTITY.create(id));
    }

    private static ChampionTier resolveTier(int level, CommandSourceStack source)
            throws CommandSyntaxException {
        return ChampionsApi.get().getTierByLevel(level)
                .orElseThrow(() -> UNKNOWN_TIER.create(level));
    }

    private static List<AffixInstance> buildAffixInstances(List<AffixArgumentType.AffixSpec> specs, ChampionTier tier) {
        return specs.stream()
                .map(spec -> spec.toInstance(tier.level()))
                .collect(Collectors.toList());
    }

    private static Component buildLabel(ChampionTier tier, LivingEntity entity,
                                        List<AffixInstance> affixes) {
        String tierName = Component
                .translatableWithFallback("rank.champions.title." + tier.level(), "Tier " + tier.level())
                .getString();
        String affixList = affixes.isEmpty() ? "" : " [" +
                affixes.stream()
                        .flatMap(i -> ChampionsApi.get().getAffixTypeId(i.type())
                                .map(ResourceLocation::getPath).stream())
                        .collect(Collectors.joining(", ")) + "]";
        return Component.literal(tierName + " " + entity.getName().getString() + affixList);
    }

    /** Give item to player, drop at feet if inventory is full. */
    private static void giveItem(ServerPlayer player, ItemStack stack) {
        if (!player.getInventory().add(stack) && !stack.isEmpty()) {
            player.drop(stack, false);
        }
    }

    // ── Suggestions ───────────────────────────────────────────────────────────

    /**
     * Suggests tier levels (1, 2, 3…) with their display names as tooltips.
     * E.g. typing "2" shows "2 (Skilled)".
     */
    private static CompletableFuture<Suggestions> suggestTiers(SuggestionsBuilder builder) {
        String input = builder.getRemaining().toLowerCase();
        ChampionsApi.get().getTiers().stream()
                .sorted(Comparator.comparingInt(ChampionTier::level))
                .forEach(tier -> {
                    String val = String.valueOf(tier.level());
                    if (val.startsWith(input)) {
                        String displayName = Component
                                .translatableWithFallback(
                                        "rank.champions.title." + tier.level(),
                                        "Tier " + tier.level())
                                .getString();
                        builder.suggest(val, Component.literal(displayName));
                    }
                });
        return builder.buildFuture();
    }

    /**
     * Suggests entity IDs, @category:X selectors, and @archetype:X selectors.
     */
    private static CompletableFuture<Suggestions> suggestEntitiesAndSpecial(SuggestionsBuilder builder) {
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

    private static String availableLevels() {
        return ChampionsApi.get().getTiers().stream()
                .sorted(Comparator.comparingInt(ChampionTier::level))
                .map(t -> String.valueOf(t.level()))
                .reduce((a, b) -> a + ", " + b)
                .orElse("(none)");
    }
}
