package top.theillusivec4.champions.neoforge;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import top.theillusivec4.champions.api.ChampionsApi;
import top.theillusivec4.champions.common.api.ChampionsRegistries;
import top.theillusivec4.champions.common.champion.ChampionSpawnHandler;
import top.theillusivec4.champions.common.command.ChampionCommand;
import top.theillusivec4.champions.common.config.ChampionConfigSpec;
import top.theillusivec4.champions.common.item.ChampionItems;
import top.theillusivec4.champions.common.network.PacketHandler;
import top.theillusivec4.champions.neoforge.integration.dispenser.ChampionEggDispenseBehavior;
import top.theillusivec4.champions.neoforge.integration.theoneprobe.TheOneProbePlugin.RegisterFunction;
import top.theillusivec4.champions.neoforge.network.NeoForgePacketHandler;
import top.theillusivec4.champions.neoforge.platform.NeoForgeAttachmentProvider;
import top.theillusivec4.champions.neoforge.registry.*;
import top.theillusivec4.champions.platform.ChampionAttachmentProvider;

/**
 * NeoForge mod entry point.
 *
 * <p>Responsibilities:</p>
 * <ul>
 *   <li>Boot the NeoForge affix type registry</li>
 *   <li>Create the attachment provider</li>
 *   <li>Hand off to {@link ChampionsRegistries#bootstrapCommon}</li>
 *   <li>Register NeoForge-specific event hooks (reload listener, sync, entity join)</li>
 * </ul>
 */
@Mod(ChampionsNeoForge.MOD_ID)
public final class ChampionsNeoForge {

    public static final String MOD_ID = "champions";

    private final NeoForgeAffixTypeRegistry affixTypeRegistry;
    private final NeoForgeAttachmentProvider attachmentProvider;
    private final NeoForgePacketHandler packetHandler;

    public ChampionsNeoForge(IEventBus modBus) {
        // 0. Register server config and bake on load/reload
        ModLoadingContext.get().getActiveContainer().registerConfig(ModConfig.Type.SERVER, ChampionConfigSpec.SPEC);
        modBus.addListener(this::onConfigLoad);
        modBus.addListener(this::onConfigReload);

        // 1. Boot NeoForge affix type registry (registers DeferredRegister to modBus)
        NeoForgeAffixTypeRegistry.bootstrap(modBus);
        affixTypeRegistry = new NeoForgeAffixTypeRegistry();

        // 2. Boot attachment type registration
        attachmentProvider = new NeoForgeAttachmentProvider(modBus);

        ChampionAttachmentProvider.Holder.register(attachmentProvider);
        // 3. Boot packet handler — registers payloads + client handlers
        packetHandler = new NeoForgePacketHandler(modBus, attachmentProvider);
        PacketHandler.Holder.register(packetHandler);

        // 4. NeoForge game event bus hooks
        NeoForge.EVENT_BUS.addListener(this::onAddReloadListeners);
        NeoForge.EVENT_BUS.addListener(this::onDatapackSync);
        NeoForge.EVENT_BUS.addListener(this::onEntityJoin);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
        ModParticleTypes.register(modBus);
        ModMobEffects.register(modBus);
        ModEntityTypes.register(modBus);
        ModItems.register(modBus);
        ModLootConditions.register(modBus);
        ModArgumentTypes.register(modBus);
        // 6. Wire the common API — must be last so all dependencies are ready
        ChampionsRegistries.bootstrapCommon(affixTypeRegistry, attachmentProvider);
        // 7. Wire ChampionEffects, ChampionEntityTypes and loot conditions after registry freeze
        modBus.addListener((FMLCommonSetupEvent e) -> {
            ModMobEffects.wireCommon();
            ModEntityTypes.wireCommon();
            ModLootConditions.wireCommon();
            ModItems.wireCommon();
            // Register dispenser behavior for champion egg
            e.enqueueWork(() -> DispenserBlock.registerBehavior(
                    ChampionItems.egg(),
                    ChampionEggDispenseBehavior.INSTANCE));
            // The One Probe — guard against missing dep at runtime
            e.enqueueWork(() -> {
                try {
                    Class.forName("mcjty.theoneprobe.api.ITheOneProbe");
                    InterModComms.sendTo("theoneprobe", "getTheOneProbe",
                            RegisterFunction::new);
                } catch (ClassNotFoundException ignored) {
                }
            });
        });
    }


    // ── NeoForge event bus ────────────────────────────────────────────────────

    /**
     * Register the TierDataLoader as a server-side reload listener.
     * Fired whenever datapacks are (re)loaded.
     */
    private void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(ChampionsRegistries.tiers());
        event.addListener(ChampionsRegistries.archetypes());
        event.addListener(ChampionsRegistries.modifiers());
    }

    /**
     * Sync tier and champion data to players on login or datapack reload.
     *
     * <p>Order matters: tier data must arrive before champion data so the client
     * can resolve tier ids when rebuilding champion views.</p>
     */
    private void onDatapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() != null) {
            // Single player logging in
            syncToPlayer(event.getPlayer());
        } else {
            // Full datapack reload — sync to everyone
            event.getPlayerList().getPlayers().forEach(this::syncToPlayer);
        }
    }

    /**
     * After an entity joins the level, re-trigger goal setup for champions loaded
     * from disk. Goals are not persisted — they must be re-registered on each load.
     */
    private void onEntityJoin(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (!(event.getEntity() instanceof LivingEntity living)) return;

        // Rebuild AI goals for champions loaded from disk — goals are not persisted
        // and must be re-registered on every join regardless of spawn origin.
        ChampionsApi.get().getChampion(living).ifPresent(champion ->
                champion.affixes().forEach(instance -> {
                    if (living instanceof Mob mob) {
                        instance.type().execute(instance, (reg, data) -> {
                            reg.setupGoals(champion, instance, data, mob.goalSelector);
                        });
                    }
                })
        );

        // Only attempt to promote freshly-spawned entities. Entities loaded from disk
        // were already evaluated at their original spawn — re-rolling on every chunk
        // load would both waste server-thread time and silently promote ordinary mobs
        // with increasing probability as their chunk is reloaded.
        if (event.loadedFromDisk()) return;
        ChampionSpawnHandler.trySpawn(living, level);
    }

    private void onConfigLoad(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == ChampionConfigSpec.SPEC) {
            ChampionConfigSpec.bakeAndApply();
        }
    }

    private void onConfigReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == ChampionConfigSpec.SPEC) {
            ChampionConfigSpec.bakeAndApply();
        }
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        ChampionCommand.register(event.getDispatcher());
    }

    // ── Sync helpers ──────────────────────────────────────────────────────────

    private void syncToPlayer(ServerPlayer player) {
        packetHandler.syncTiersToPlayer(player);
        packetHandler.syncAllChampionsToPlayer(player);
    }
}
