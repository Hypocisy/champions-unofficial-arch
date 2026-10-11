package top.theillusivec4.champions.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.champions.api.ChampionsApi;
import top.theillusivec4.champions.api.affix.AffixInstance;
import top.theillusivec4.champions.api.champion.ChampionTier;
import top.theillusivec4.champions.common.api.ChampionsRegistries;
import top.theillusivec4.champions.common.champion.ChampionData;
import top.theillusivec4.champions.common.champion.ChampionSpawnHandler;
import top.theillusivec4.champions.common.utils.Utils;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Champion Egg item — spawns a champion with a preset or random build.
 *
 * <p>Two modes:</p>
 * <ul>
 *   <li><b>Preset</b>: stack has {@link ChampionItems#presetComponent()} set.
 *       Calls {@link top.theillusivec4.champions.common.champion.ChampionBuilder#trySpawnWithAffixes}
 *       to reproduce the exact tier + affix list.</li>
 *   <li><b>Random</b>: no preset component. Calls
 *       {@link top.theillusivec4.champions.common.champion.ChampionBuilder#trySpawn}
 *       for a normal archetype-driven build.</li>
 * </ul>
 */
public final class ChampionEggItem extends Item {

    public ChampionEggItem() {
        super(new Item.Properties().useItemDescriptionPrefix().setId(ResourceKey.create(Registries.ITEM, Utils.getLocation("egg"))));
    }

    // ── Display ───────────────────────────────────────────────────────────────

    @Override
    @NotNull
    public Component getName(ItemStack stack) {
        Optional<EntityType<?>> type = getEntityType(stack);
        Optional<ChampionTier> tier = getPresetTier(stack);

        MutableComponent root = Component.empty();

        // Add tier name with color
        if (tier.isPresent()) {
            ChampionTier t = tier.get();
            MutableComponent tierName = Component.translatableWithFallback(
                    "rank.champions.title." + t.level(), "Tier " + t.level())
                    .copy()
                    .withColor(t.display().color());
            root.append(tierName);
            root.append(" ");
        } else {
            root.append(Component.translatable("item.champions.egg.random")
                    .withStyle(ChatFormatting.AQUA));
            root.append(" ");
        }

        // Add entity name
        root.append(type.map(EntityType::getDescription)
                .orElse(Component.translatable("entity.minecraft.zombie")));

        // Add "Egg" suffix
        root.append(" ");
        root.append(Component.translatable("item.champions.egg"));

        return root;
    }

    @Override
    @ParametersAreNonnullByDefault
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack itemStack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        Optional<ChampionData> preset = getPreset(itemStack);
        if (preset.isPresent()) {
            ChampionData data = preset.get();
            if (data.baseAffixes().isEmpty()) {
                builder.accept(Component.translatable("item.champions.egg.no_affixes")
                        .withStyle(ChatFormatting.GRAY));
            } else {
                data.baseAffixes().forEach(entry ->
                        ChampionsApi.get().getAffixType(entry.typeId()).flatMap(type -> ChampionsApi.get().getAffixTypeId(type)).ifPresent(id -> {
                            Component line = Component.translatableWithFallback(
                                            "affix." + id.getNamespace() + "." + id.getPath() + ".name",
                                            id.getPath())
                                    .append(" ")
                                    .append(Component.literal("(" + entry.strength() + ")"))
                                    .copy()
                                    .withStyle(ChatFormatting.GRAY);
                            builder.accept(line);
                        }));
            }
        } else {
            builder.accept(Component.translatable("item.champions.egg.tooltip")
                    .withStyle(ChatFormatting.AQUA));
        }
    }

    // ── Use on block ──────────────────────────────────────────────────────────

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        Level world = ctx.getLevel();
        if (world.isClientSide()) return InteractionResult.SUCCESS;
        if (!(world instanceof ServerLevel serverLevel)) return InteractionResult.PASS;

        ItemStack stack = ctx.getItemInHand();
        Optional<EntityType<?>> typeOpt = getEntityType(stack);
        if (typeOpt.isEmpty()) return InteractionResult.PASS;

        BlockPos pos = ctx.getClickedPos();
        Direction face = ctx.getClickedFace();
        BlockState state = world.getBlockState(pos);
        BlockPos spawnPos = state.getCollisionShape(world, pos).isEmpty()
                ? pos : pos.relative(face);

        spawnChampion(typeOpt.get(), serverLevel, spawnPos, stack,
                ctx.getPlayer() != null && ctx.getPlayer().getAbilities().instabuild);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (world.isClientSide()) return InteractionResult.PASS;
        if (!(world instanceof ServerLevel serverLevel)) return InteractionResult.PASS;

        BlockHitResult hit = getPlayerPOVHitResult(world, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() != HitResult.Type.BLOCK) return InteractionResult.PASS;

        Optional<EntityType<?>> typeOpt = getEntityType(stack);
        if (typeOpt.isEmpty()) return InteractionResult.PASS;

        if (!world.mayInteract(player, hit.getBlockPos())) return InteractionResult.FAIL;

        spawnChampion(typeOpt.get(), serverLevel, hit.getBlockPos(), stack,
                player.getAbilities().instabuild);
        return InteractionResult.SUCCESS.heldItemTransformedTo(stack);
    }

    // ── Dispenser support (called by platform dispenser handler) ──────────────

    public void dispense(ServerLevel level, BlockPos pos, Direction facing, ItemStack stack) {
        Optional<EntityType<?>> typeOpt = getEntityType(stack);
        typeOpt.ifPresent(type -> spawnChampion(type, level, pos.relative(facing), stack, false));
    }

    // ── Data accessors ────────────────────────────────────────────────────────

    public static Optional<EntityType<?>> getEntityType(ItemStack stack) {
        Identifier id = stack.get(ChampionItems.entityTypeComponent());
        return BuiltInRegistries.ENTITY_TYPE.getOptional(id).or(()-> Optional.of(EntityType.ZOMBIE));
    }

    public static Optional<ChampionData> getPreset(ItemStack stack) {
        return Optional.ofNullable(stack.get(ChampionItems.presetComponent()));
    }

    public static Optional<ChampionTier> getPresetTier(ItemStack stack) {
        return getPreset(stack).flatMap(d -> ChampionsApi.get().getTier(d.tierId()));
    }

    public static ItemStack createPreset(EntityType<?> entityType, ChampionData preset) {
        ItemStack stack = new ItemStack(ChampionItems.egg());
        Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
        stack.set(ChampionItems.entityTypeComponent(), id);
        stack.set(ChampionItems.presetComponent(), preset);
        applyItemModelOrFallback(stack, entityType);
        return stack;
    }

    private static void applyItemModelOrFallback(ItemStack championEgg, EntityType<?> hasItemModel) {
        var entityNormalSpawnEgg = SpawnEggItem.byId(hasItemModel);
        if (entityNormalSpawnEgg.isPresent()) {
            ItemStack entityEggStack = new ItemStack(entityNormalSpawnEgg.get());
            Identifier itemModelLocation = entityEggStack.get(DataComponents.ITEM_MODEL);
            if (itemModelLocation == null) {
                itemModelLocation = SpawnEggItem.byId(EntityType.ZOMBIE).get().value().getDefaultInstance().get(DataComponents.ITEM_MODEL);
            }
            championEgg.set(DataComponents.ITEM_MODEL, itemModelLocation);
        }
    }

    public static ItemStack createRandom(EntityType<?> entityType) {
        ItemStack stack = new ItemStack(ChampionItems.egg());
        Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
        stack.set(ChampionItems.entityTypeComponent(), id);
        applyItemModelOrFallback(stack, entityType);
        return stack;
    }

    // ── Spawn logic ───────────────────────────────────────────────────────────

    private static void spawnChampion(EntityType<?> type, ServerLevel level,
                                      BlockPos pos, ItemStack stack, boolean creative) {
        var entity = type.create(level, null, pos, EntitySpawnReason.SPAWN_ITEM_USE, true, false);
        if (!(entity instanceof LivingEntity living)) return;

        RandomSource random = level.getRandom();
        Optional<ChampionData> preset = getPreset(stack);

        if (preset.isPresent()) {
            // Preset mode: reconstruct AffixInstance list and call trySpawnWithAffixes.
            // The preset was built from a live champion (pick-block / /champions egg) so it
            // carries its archetype id — pass it through so phase processing stays intact.
            ChampionData data = preset.get();
            ChampionsApi.get().getTier(data.tierId()).ifPresent(tier -> {
                List<AffixInstance> affixes = data.baseAffixes().stream()
                        .flatMap(entry -> ChampionsApi.get().getAffixType(entry.typeId())
                                .map(t -> new AffixInstance(t, entry.strength()))
                                .stream())
                        .toList();
                ChampionsRegistries.builder().trySpawnWithAffixes(
                        living, tier, affixes, random, data.archetypeId().orElse(null));
            });
        } else {
            // Random mode: weighted tier selection + normal archetype-driven build
            ChampionTier tier = ChampionSpawnHandler.selectRandomTier(random);
            if (tier != null) {
                ChampionsRegistries.builder().trySpawn(living, tier, random);
            }
        }

        level.addFreshEntity(living);
        if (!creative) stack.shrink(1);
    }
}
