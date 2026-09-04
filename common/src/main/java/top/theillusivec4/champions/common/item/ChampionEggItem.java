package top.theillusivec4.champions.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
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

import java.util.List;
import java.util.Optional;

import static net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE;

/**
 * Champion Egg item — spawns a champion with a preset or random build.
 *
 * <p>Two modes:</p>
 * <ul>
 *   <li><b>Preset</b>: stack carries a {@link ChampionData} preset in its NBT.
 *       Calls {@link top.theillusivec4.champions.common.champion.ChampionBuilder#trySpawnWithAffixes}
 *       to reproduce the exact tier + affix list.</li>
 *   <li><b>Random</b>: no preset component. Calls
 *       {@link top.theillusivec4.champions.common.champion.ChampionBuilder#trySpawn}
 *       for a normal archetype-driven build.</li>
 * </ul>
 */
public final class ChampionEggItem extends Item {

    public ChampionEggItem() {
        super(new Item.Properties().stacksTo(64));
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
                    .withStyle(style -> style.withColor(TextColor.fromRgb(t.display().color() & 0xFFFFFF)));
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
    public void appendHoverText(ItemStack stack, Level level,
                                List<Component> tooltip, TooltipFlag flag) {
        Optional<ChampionData> preset = getPreset(stack);
        if (preset.isPresent()) {
            ChampionData data = preset.get();
            if (data.baseAffixes().isEmpty()) {
                tooltip.add(Component.translatable("item.champions.egg.no_affixes")
                        .withStyle(ChatFormatting.GRAY));
            } else {
                data.baseAffixes().forEach(entry ->
                        ChampionsApi.get().getAffixType(entry.typeId()).ifPresent(type ->
                                ChampionsApi.get().getAffixTypeId(type).ifPresent(id -> {
                                    Component line = Component.translatableWithFallback(
                                            "affix." + id.getNamespace() + "." + id.getPath() + ".name",
                                            id.getPath())
                                            .append(" ")
                                            .append(Component.literal("(" + entry.strength() + ")"))
                                            .copy()
                                            .withStyle(ChatFormatting.GRAY);
                                    tooltip.add(line);
                                })));
            }
        } else {
            tooltip.add(Component.translatable("item.champions.egg.tooltip")
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
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (world.isClientSide()) return InteractionResultHolder.pass(stack);
        if (!(world instanceof ServerLevel serverLevel)) return InteractionResultHolder.pass(stack);

        BlockHitResult hit = getPlayerPOVHitResult(world, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() != HitResult.Type.BLOCK) return InteractionResultHolder.pass(stack);

        Optional<EntityType<?>> typeOpt = getEntityType(stack);
        if (typeOpt.isEmpty()) return InteractionResultHolder.pass(stack);

        if (!world.mayInteract(player, hit.getBlockPos())) return InteractionResultHolder.fail(stack);

        spawnChampion(typeOpt.get(), serverLevel, hit.getBlockPos(), stack,
                player.getAbilities().instabuild);
        return InteractionResultHolder.success(stack);
    }

    // ── Dispenser support (called by platform dispenser handler) ──────────────

    public void dispense(ServerLevel level, BlockPos pos, Direction facing, ItemStack stack) {
        Optional<EntityType<?>> typeOpt = getEntityType(stack);
        typeOpt.ifPresent(type -> spawnChampion(type, level, pos.relative(facing), stack, false));
    }

    // ── Data accessors ────────────────────────────────────────────────────────
    // 1.20.1 has no data components — the egg's payload lives in stack NBT.

    private static final String TAG_ENTITY_TYPE = "EntityType";
    private static final String TAG_PRESET = "Preset";

    public static Optional<EntityType<?>> getEntityType(ItemStack stack) {
        if (!stack.hasTag()) return Optional.empty();
        String id = stack.getTag().getString(TAG_ENTITY_TYPE);
        if (id.isEmpty() || !ResourceLocation.isValidResourceLocation(id)) return Optional.empty();
        return ENTITY_TYPE.getOptional(new ResourceLocation(id));
    }

    public static Optional<ChampionData> getPreset(ItemStack stack) {
        if (!stack.hasTag()) return Optional.empty();
        CompoundTag presetTag = stack.getTag().getCompound(TAG_PRESET);
        if (presetTag.isEmpty()) return Optional.empty();
        return ChampionData.CODEC.parse(NbtOps.INSTANCE, presetTag).result();
    }

    public static Optional<ChampionTier> getPresetTier(ItemStack stack) {
        return getPreset(stack).flatMap(d -> ChampionsApi.get().getTier(d.tierId()));
    }

    public static ItemStack createPreset(EntityType<?> entityType, ChampionData preset) {
        ItemStack stack = new ItemStack(ChampionItems.egg());
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(TAG_ENTITY_TYPE, ENTITY_TYPE.getKey(entityType).toString());
        ChampionData.CODEC.encodeStart(NbtOps.INSTANCE, preset).result()
                .ifPresent(encoded -> tag.put(TAG_PRESET, encoded));
        return stack;
    }

    public static ItemStack createRandom(EntityType<?> entityType) {
        ItemStack stack = new ItemStack(ChampionItems.egg());
        stack.getOrCreateTag().putString(TAG_ENTITY_TYPE, ENTITY_TYPE.getKey(entityType).toString());
        return stack;
    }

    // ── Spawn logic ───────────────────────────────────────────────────────────

    private static void spawnChampion(EntityType<?> type, ServerLevel level,
                                      BlockPos pos, ItemStack stack, boolean creative) {
        var entity = type.create(level, null, null, pos, MobSpawnType.SPAWN_EGG, true, false);
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
