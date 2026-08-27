package top.theillusivec4.champions.fabric;


import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.fml.config.ModConfig;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.champions.api.ChampionsApi;
import top.theillusivec4.champions.api.affix.AffixInstance;
import top.theillusivec4.champions.api.affix.AffixType;
import top.theillusivec4.champions.api.affix.IAffixData;
import top.theillusivec4.champions.api.champion.Champion;
import top.theillusivec4.champions.common.api.ChampionsRegistries;
import top.theillusivec4.champions.common.champion.ChampionSpawnHandler;
import top.theillusivec4.champions.common.command.AffixArgumentType;
import top.theillusivec4.champions.common.command.ChampionCommand;
import top.theillusivec4.champions.common.config.ChampionConfigSpec;
import top.theillusivec4.champions.common.loot.ChampionLootConditions;
import top.theillusivec4.champions.common.data.TierDataLoader;
import top.theillusivec4.champions.common.network.PacketHandler;
import top.theillusivec4.champions.fabric.event.FabricChampionEventsHandler;
import top.theillusivec4.champions.fabric.integration.dispenser.ChampionEggDispenseBehavior;
import top.theillusivec4.champions.fabric.network.FabricPacketHandler;
import top.theillusivec4.champions.fabric.platform.FabricAttachmentProvider;
import top.theillusivec4.champions.fabric.registry.*;
import top.theillusivec4.champions.platform.ChampionAttachmentProvider;

/**
 * Fabric mod entry point.
 *
 * <p>Mirrors {@code ChampionsNeoForge} lifecycle — same four responsibilities,
 * different platform hooks.</p>
 */
public final class ChampionsFabric implements ModInitializer {

    public static final String MOD_ID = "champions";

    /** Held so the reload listener can push updated tiers to online players. */
    private MinecraftServer currentServer = null;

    @Override
    public void onInitialize() {
        // 0. Register server config with Forge Config API Port and bake on server start/reload
        NeoForgeConfigRegistry.INSTANCE.register(MOD_ID, ModConfig.Type.SERVER, ChampionConfigSpec.SPEC);
        // SERVER_STARTING fires before the world loads — ensures config is baked
        // before the first entity-join hook triggers ChampionSpawnHandler.
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            ChampionConfigSpec.bakeAndApply();
            currentServer = server;
        });
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> currentServer = null);

        // 1. Boot the custom affix type registry
        FabricAffixTypeRegistry.bootstrap();
        FabricAffixTypeRegistry affixTypeRegistry = new FabricAffixTypeRegistry();

        // 1a. Register all built-in affix types
        FabricAffixBootStrap.registerAll();

        // 1b. Register custom mob effects (Wound, Paralysis)
        ModMobEffects.register();

        // 1c. Register bullet entity types (ArcticBullet, EnkindlingBullet)
        ModEntityTypes.register();

        // 1e. Register champion egg item and data components
        ModItems.register();

        // 1f. Register dispenser behavior for champion egg
        DispenserBlock.registerBehavior(ModItems.CHAMPION_EGG, ChampionEggDispenseBehavior.INSTANCE);

        // 1d. Register loot condition types
        ChampionLootConditions.registerFabric();

        // 2. Create the attachment provider (Cardinal Components registers itself
        //    via its own entrypoint — we just hold a reference here)
        FabricAttachmentProvider attachmentProvider = new FabricAttachmentProvider();
        ChampionAttachmentProvider.Holder.register(attachmentProvider);

        // 2a. Drop cached server views when entities are unloaded (death, chunk
        //     unload, dimension change) — otherwise the cache holds every champion
        //     entity that ever existed alive and grows without bound.
        ServerEntityEvents.ENTITY_UNLOAD.register((entity, level) -> {
            if (entity instanceof LivingEntity living) {
                attachmentProvider.onEntityUnload(living);
            }
        });

        FabricPacketHandler packetHandler = new FabricPacketHandler(attachmentProvider);
        PacketHandler.Holder.register(packetHandler);
        FabricPacketHandler.registerServerPayloads();
        FabricPacketHandler.registerServerEditorHandler();
        // 3. Wire the common API
        ChampionsRegistries.bootstrapCommon(affixTypeRegistry, attachmentProvider);

        // 4. Register custom argument types (must happen before CommandRegistrationCallback)
        ArgumentTypeRegistry.registerArgumentType(
                ResourceLocation.fromNamespaceAndPath(MOD_ID, "affix"),
                AffixArgumentType.class,
                SingletonArgumentInfo.contextFree(AffixArgumentType::affixes)
        );

        // 5. Register platform hooks
        FabricChampionEventsHandler.register();
        registerReloadListener();
        registerEntityJoinHook();
        registerPlayerLoginHook();
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) ->
                        ChampionCommand.register(dispatcher));
        ModParticles.register();
    }

    // ── Reload listener ───────────────────────────────────────────────────────

    /**
     * Wrap TierDataLoader in Fabric's SimpleSynchronousResourceReloadListener.
     *
     * <p>TierDataLoader is a Minecraft {@code SimplePreparableReloadListener} (async prepare,
     * sync apply). Fabric's {@code ResourceManagerHelper} also accepts the full async form
     * via {@code IdentifiableResourceReloadListener}, but the synchronous wrapper is
     * sufficient here since tier loading is lightweight.</p>
     */
    private void registerReloadListener() {
        TierDataLoader tierLoader = ChampionsRegistries.tiers();

        ResourceManagerHelper.get(PackType.SERVER_DATA)
                .registerReloadListener(new SimpleSynchronousResourceReloadListener() {
                    @Override
                    public ResourceLocation getFabricId() {
                        return ResourceLocation.fromNamespaceAndPath(MOD_ID, "tiers");
                    }
                    @Override
                    public void onResourceManagerReload(@NotNull ResourceManager manager) {
                        var prepared = tierLoader.prepare(manager, InactiveProfiler.INSTANCE);
                        tierLoader.apply(prepared, manager, InactiveProfiler.INSTANCE);
                        // Push updated tiers to all online players after reload
                        // (covers /reload and editor saves)
                        if (currentServer != null) {
                            currentServer.getPlayerList().getPlayers().forEach(p ->
                                    PacketHandler.Holder.get().syncTiersToPlayer(p));
                        }
                    }
                });

        // ── ArchetypeDataLoader ───────────────────────────────────────────────
        var archetypeLoader = ChampionsRegistries.archetypes();
        ResourceManagerHelper.get(PackType.SERVER_DATA)
                .registerReloadListener(new SimpleSynchronousResourceReloadListener() {
                    @Override
                    public ResourceLocation getFabricId() {
                        return ResourceLocation.fromNamespaceAndPath(MOD_ID, "archetypes");
                    }
                    @Override
                    public void onResourceManagerReload(@NotNull ResourceManager manager) {
                        var prepared = archetypeLoader.prepare(manager, InactiveProfiler.INSTANCE);
                        archetypeLoader.apply(prepared, manager, InactiveProfiler.INSTANCE);
                    }
                });

        // ── AttributesModifierDataLoader ──────────────────────────────────────
        var modifierLoader = ChampionsRegistries.modifiers();
        ResourceManagerHelper.get(PackType.SERVER_DATA)
                .registerReloadListener(new SimpleSynchronousResourceReloadListener() {
                    @Override
                    public ResourceLocation getFabricId() {
                        return ResourceLocation.fromNamespaceAndPath(MOD_ID, "modifier_setting");
                    }
                    @Override
                    public void onResourceManagerReload(@NotNull ResourceManager manager) {
                        var prepared = modifierLoader.prepare(manager, InactiveProfiler.INSTANCE);
                        modifierLoader.apply(prepared, manager, InactiveProfiler.INSTANCE);
                    }
                });
    }

    // ── Entity join ───────────────────────────────────────────────────────────

    /**
     * Re-trigger goal setup for champions loaded from disk, and evaluate freshly
     * spawned mobs for promotion. Fabric equivalent of NeoForge's EntityJoinLevelEvent.
     */
    private void registerEntityJoinHook() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
            if (!(entity instanceof LivingEntity living)) return;

            // Rebuild AI goals for champions on every load — goals are not persisted
            // and must be re-registered whenever the entity enters the world.
            ChampionsApi.get().getChampion(living).ifPresent(champion ->
                    champion.affixes().forEach(instance -> {
                        if (living instanceof Mob mob) {
                            setupGoalsForInstance(champion, instance.type(), instance, mob);
                        }
                    })
            );

            // Only attempt to promote freshly-spawned entities. Fabric's ENTITY_LOAD
            // fires for both new spawns and disk-loaded entities with no flag to tell
            // them apart, so we use tickCount: a natural spawn is still 0 at load time,
            // while a disk-loaded entity restores its persisted tickCount (> 0, since it
            // lived at least one tick before being saved). Re-rolling on every chunk load
            // would waste server-thread time and silently promote ordinary mobs with
            // increasing probability as their chunk is reloaded.
            if (living.tickCount > 0) return;
            ChampionSpawnHandler.trySpawn(living, level);
        });
    }

    private <D extends IAffixData> void setupGoalsForInstance(
            Champion champion,
            AffixType<D> type,       // <-- 在这里捕获 D
            AffixInstance instance,
            Mob mob
    ) {
        type.getRegistry().setupGoals(
                champion,
                instance,
                type.getData(instance),  // 返回 D ✓
                mob.goalSelector         // setupGoals 接收 D ✓
        );
    }

    // ── Player login ──────────────────────────────────────────────────────────

    /**
     * Sync tier data and champion data to a player when they join.
     * Fabric equivalent of NeoForge's OnDatapackSyncEvent (single-player case).
     */
    private void registerPlayerLoginHook() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            //1. 先同步 tier 数据（客户端需要先有 tier 才能解析后续的 champion 数据）
            PacketHandler.Holder.get().syncTiersToPlayer(player);
            // 2. 再同步当前已加载的所有 champion
            PacketHandler.Holder.get().syncAllChampionsToPlayer(player);
        });
    }
}
