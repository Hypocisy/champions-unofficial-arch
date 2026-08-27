package top.theillusivec4.champions.common.champion;

import dev.architectury.event.EventResult;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.theillusivec4.champions.api.ChampionsApi;
import top.theillusivec4.champions.api.affix.AffixInstance;
import top.theillusivec4.champions.api.affix.handler.event.SpawnEvent;
import top.theillusivec4.champions.api.champion.Champion;
import top.theillusivec4.champions.api.champion.ChampionTier;
import top.theillusivec4.champions.api.event.ChampionEvents;
import top.theillusivec4.champions.common.api.ChampionsRegistries;
import top.theillusivec4.champions.common.data.ArchetypeDataLoader;
import top.theillusivec4.champions.common.network.ChampionSyncData;
import top.theillusivec4.champions.common.network.PacketHandler;
import top.theillusivec4.champions.common.strategy.ArchetypeStrategy;
import top.theillusivec4.champions.common.strategy.ChampionBuildStrategy;
import top.theillusivec4.champions.platform.ChampionAttachmentProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Entry point for converting a {@link LivingEntity} into a champion.
 *
 * <p>Handles:</p>
 * <ul>
 *   <li>Affix list assembly via {@link ArchetypeStrategy}</li>
 *   <li>Tier assignment</li>
 *   <li>Affix list assembly via the active {@link ChampionBuildStrategy}</li>
 *   <li>Firing {@link SpawnChampionEvent} for third-party interception</li>
 *   <li>Writing the attachment and triggering initial goal setup + sync</li>
 * </ul>
 */
public final class ChampionBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChampionBuilder.class);

    private final ChampionAttachmentProvider attachmentProvider;
    private final ArchetypeDataLoader archetypeLoader;

    public ChampionBuilder(
            ChampionAttachmentProvider attachmentProvider,
            ArchetypeDataLoader archetypeLoader
    ) {
        this.attachmentProvider = attachmentProvider;
        this.archetypeLoader = archetypeLoader;
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Attempt to make {@code entity} a champion using an explicit, caller-supplied affix list.
     *
     * <p>Skips the build strategy entirely — useful for the {@code /champions summon} command
     * where the operator specifies exact affixes. The SPAWN event is still fired so third-party
     * listeners can intercept or cancel as normal.</p>
     *
     * <p>If {@code explicitAffixes} is empty the champion is created with no affixes (bare tier).</p>
     *
     * <p>Phase-driven spawns pass the source archetype id through so the new champion can still
     * trigger archetype phases (e.g. egg respawns, mob-split children).{@code null} disables phases.</p>
     */
    public Optional<Champion.Server> trySpawnWithAffixes(
            LivingEntity entity,
            ChampionTier tier,
            List<AffixInstance> explicitAffixes,
            RandomSource random,
            ResourceLocation archetypeId
    ) {
        if (entity.level().isClientSide()) {
            throw new IllegalStateException("ChampionBuilder must only be called server-side.");
        }

        // Fire SPAWN event — listeners can still modify or cancel
        ChampionEvents.SpawnContext ctx = new ChampionEvents.SpawnContext(
                new ArrayList<>(explicitAffixes));
        EventResult result = ChampionEvents.SPAWN.invoker()
                .onSpawn(buildTemporaryView(entity, tier, explicitAffixes), tier, ctx);

        if (result.isFalse()) {
            LOGGER.debug("[Champions] SpawnChampionEvent cancelled (explicit affixes) for {}",
                    entity.getType());
            return Optional.empty();
        }

        List<AffixInstance> finalAffixes = ctx.affixes();

        // Tear down existing champion state if entity is being re-rolled
        ChampionData data = toChampionData(tier, finalAffixes, archetypeId);
        attachmentProvider.getServer(entity).ifPresent(existing -> {
            resetModifiers(existing);
            existing.baseAffixes().forEach(inst -> teardown(entity, existing, inst));
        });

        writeData(entity, data);

        Optional<Champion.Server> champion = attachmentProvider.getServer(entity);

        champion.ifPresent(c -> {
            finalAffixes.forEach(inst -> {
                if (entity instanceof Mob mob) {
                    inst.type().execute(inst, (reg, _data) ->
                            reg.setupGoals(c, inst, _data, mob.goalSelector));
                }
            });
            // Note: triggered phase effects are already restored by the attachment
            // provider when the server view is built (getServer) — restoring again here
            // would stack the same phase affixes/effects onto the live list.
            applyModifiers(c, tier);
        });

        champion.ifPresent(c -> {
            if (!(c.entity().level() instanceof ServerLevel server)) return;
            GlobalDispatcher.dispatch(SpawnEvent.class, c, new SpawnEvent(server));
        });

        champion.ifPresent(c ->
                PacketHandler.Holder.get()
                        .syncChampionToTrackers(entity, ChampionSyncData.from(c)));

        LOGGER.debug("[Champions] Spawned champion {} with tier={} explicit affixes={}",
                entity.getType(), tier.id(), finalAffixes.size());

        return champion;
    }

    /**
     * Attempt to make {@code entity} a champion at the given tier.
     *
     * <p>Returns empty if the spawn is cancelled via {@link ChampionEvents.SpawnCallback},
     * or if no affixes are assigned and the tier would produce a trivial champion.</p>
     */
    public Optional<Champion.Server> trySpawn(
            LivingEntity entity,
            ChampionTier tier,
            RandomSource random
    ) {
        if (entity.level().isClientSide()) {
            throw new IllegalStateException("ChampionBuilder must only be called server-side.");
        }

        // 1. Build affix list via active strategy
        ChampionBuildStrategy strategy = selectStrategy();
        ChampionBuildStrategy.BuildResult buildResult = strategy.build(entity, tier, random);
        List<AffixInstance> affixes = buildResult.affixes();
        ResourceLocation archetypeId = buildResult.archetypeId();

        // 2. Fire SPAWN event — third-party mods can modify or cancel
        ChampionEvents.SpawnContext ctx = new ChampionEvents.SpawnContext(affixes);
        EventResult result = ChampionEvents.SPAWN.invoker()
                .onSpawn(buildTemporaryView(entity, tier, affixes), tier, ctx);

        if (result.isFalse()) {
            LOGGER.debug("[Champions] SpawnChampionEvent cancelled for {}", entity.getType());
            return Optional.empty();
        }

        List<AffixInstance> finalAffixes = ctx.affixes();

        // 3. Write attachment
        ChampionData data = toChampionData(tier, finalAffixes, archetypeId);
        attachmentProvider.getServer(entity).ifPresent(existing -> {
            // Already a champion — this is a re-roll, tear down old goals and modifiers first
            resetModifiers(existing);
            existing.baseAffixes().forEach(inst -> teardown(entity, existing, inst));
        });

        // Write raw data to storage so the view can be built
        writeData(entity, data);

        // 4. Rebuild view from storage (picks up save/sync callbacks from provider)
        Optional<Champion.Server> champion = attachmentProvider.getServer(entity);

        // 5. Trigger goal setup for all affixes, then apply attribute scaling
        champion.ifPresent(c ->
                {
                    finalAffixes.forEach(inst -> {
                        if (entity instanceof Mob mob) {
                            inst.type().execute(inst, (reg, _data) -> {
                                reg.setupGoals(c, inst, _data, mob.goalSelector);
                            });
                        }
                    });
                    // Triggered phase effects are already restored by the attachment
                    // provider when the server view is built (getServer).
                    applyModifiers(c, tier);
                }
        );

        champion.ifPresent(c -> {
            if (!(c.entity().level() instanceof ServerLevel server)) return;
            GlobalDispatcher.dispatch(SpawnEvent.class, c, new SpawnEvent(server));
        });
        // 6. Initial sync to tracking players
        champion.ifPresent(c ->
                PacketHandler.Holder.get()
                        .syncChampionToTrackers(entity, ChampionSyncData.from(c))
        );

        LOGGER.debug("[Champions] Spawned champion {} with tier={} affixes={}",
                entity.getType(), tier.id(), finalAffixes.size());

        return champion;
    }

    // ── Strategy selection ────────────────────────────────────────────────────

    private ChampionBuildStrategy selectStrategy() {
        return new ArchetypeStrategy(archetypeLoader);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    public static ChampionData toChampionData(ChampionTier tier, List<AffixInstance> affixes, ResourceLocation archetypeId) {
        return toChampionData(tier, affixes, archetypeId, List.of());
    }

    /**
     * Build a {@link ChampionData} snapshot from a live affix list.
     *
     * <p>Used when reconstructing a {@code ChampionData} from an existing champion
     * (e.g. the client pick-block egg) — preserves the archetype id and already-triggered
     * phase ids so the rebuilt champion keeps its phase state.</p>
     */
    public static ChampionData toChampionData(
            ChampionTier tier,
            List<AffixInstance> affixes,
            ResourceLocation archetypeId,
            List<ResourceLocation> triggeredPhases
    ) {
        List<ChampionData.AffixEntry> entries = affixes.stream()
                .flatMap(inst -> ChampionsApi.get().getAffixTypeId(inst.type())
                        .map(id -> new ChampionData.AffixEntry(
                                id, inst.strength(), inst.save()))
                        .stream())
                .toList();
        return new ChampionData(tier.id(), entries, triggeredPhases, Optional.ofNullable(archetypeId));
    }

    private void writeData(LivingEntity entity, ChampionData data) {
        ChampionAttachmentProvider.Holder.get().setServer(entity, data);
    }

    // ── Attribute modifiers ───────────────────────────────────────────────────

    /**
     * Apply tier-scaled attribute modifiers from {@code modifier_setting} datapacks.
     *
     * <p>growthFactor is derived from the champion's tier level relative to the highest
     * registered tier — tier 1 of 5 yields 0.2, tier 5 of 5 yields 1.0.</p>
     *
     * <p>Modifier id pattern: {@code champions:<namespace>_<path>_modifier}
     * (namespaced so {@link #resetModifiers} can later strip them all out).</p>
     */
    private static void applyModifiers(Champion.Server champion, ChampionTier tier) {
        Collection<ChampionTier> allTiers = ChampionsApi.get().getTiers();
        int maxLevel = allTiers.stream().mapToInt(ChampionTier::level).max().orElse(1);
        float growthFactor = maxLevel > 0 ? (float) tier.level() / maxLevel : 1f;

        if (growthFactor == 0f) return;

        LivingEntity entity = champion.entity();

        ChampionsRegistries.modifiers().getLoadedData().forEach((fileKey, setting) -> {
            if (!setting.enable()) return;
            boolean matches = setting.modifierCondition()
                    .map(cond -> cond.test(champion))
                    .orElse(true);
            if (!matches) return;

            var attrHolder = BuiltInRegistries.ATTRIBUTE.getHolder(setting.attributeType());
            attrHolder.ifPresent(attr -> {
                var attrInstance = entity.getAttributes().getInstance(attr);
                if (attrInstance == null) return;

                // Modifier id: champions:<namespace>_<path>_modifier
                // (same derivation as the old project's Utils.getLocation call)
                ResourceLocation settingPath = setting.attributeType();
                ResourceLocation modId = ResourceLocation.fromNamespaceAndPath(
                        "champions",
                        settingPath.getNamespace() + "_" + settingPath.getPath().replace('/', '_') + "_modifier"
                );

                double amount = setting.setting().getFirst() * growthFactor;
                AttributeModifier.Operation op = setting.setting().getSecond();

                attrInstance.addOrReplacePermanentModifier(
                        new AttributeModifier(modId, amount, op));

                // Keep health in sync after increasing max_health
                if (attr.value() == Attributes.MAX_HEALTH.value()) {
                    entity.setHealth(entity.getMaxHealth());
                }
            });
        });
    }

    /**
     * Remove all attribute modifiers previously applied by {@link #applyModifiers}.
     * Called before a re-roll so stale modifiers don't stack.
     *
     * <p><b>Convention:</b> every modifier applied by {@code applyModifiers} uses
     * {@code champions} as its id namespace (pattern {@code champions:<ns>_<path>_modifier}).
     * Removal scans by that namespace rather than by exact id.  This means any modifier from
     * another mod or datapack that also uses the {@code champions} namespace would be removed
     * here — a known limitation of the convention-based approach.  If that becomes an issue,
     * the fix is to persist the exact modifier ids in the attachment and remove by id.</p>
     */
    private static void resetModifiers(Champion.Server champion) {
        LivingEntity entity = champion.entity();
        ChampionsRegistries.modifiers().getLoadedData().forEach((fileKey, setting) -> {
            if (!setting.enable()) return;
            var attrHolder = BuiltInRegistries.ATTRIBUTE.getHolder(setting.attributeType());
            attrHolder.ifPresent(attr -> {
                var attrInstance = entity.getAttributes().getInstance(attr);
                if (attrInstance == null) return;
                // Collect first to avoid ConcurrentModificationException
                List<AttributeModifier> toRemove = attrInstance.getModifiers().stream()
                        .filter(m -> "champions".equals(m.id().getNamespace()))
                        .toList();
                toRemove.forEach(attrInstance::removeModifier);
            });
        });
    }

    private static void teardown(LivingEntity entity, Champion.Server champion,
                                 AffixInstance inst) {
        if (entity instanceof Mob mob) {
            inst.type().execute(inst, (reg, data) -> {
                reg.teardownGoals(champion, inst, data, mob.goalSelector);
            });
        }
    }

    /**
     * Minimal view used only to populate SpawnChampionEvent — no callbacks.
     */
    private static Champion buildTemporaryView(
            LivingEntity entity, ChampionTier tier, List<AffixInstance> affixes
    ) {
        return new ChampionView.Client(entity, tier, affixes);
    }
}
