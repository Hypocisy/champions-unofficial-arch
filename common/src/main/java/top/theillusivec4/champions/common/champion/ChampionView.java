package top.theillusivec4.champions.common.champion;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import top.theillusivec4.champions.api.ChampionsApi;
import top.theillusivec4.champions.api.affix.AffixInstance;
import top.theillusivec4.champions.api.affix.AffixType;
import top.theillusivec4.champions.api.champion.Champion;
import top.theillusivec4.champions.api.champion.ChampionTier;

import java.util.*;
import java.util.function.Consumer;

/**
 * Common implementations of {@link Champion.Server} and {@link Champion.Client}.
 *
 * <p>Platform providers ({@code NeoForgeAttachmentProvider}, {@code FabricAttachmentProvider})
 * instantiate these after reading from their respective storage backends.
 * Neither class contains any platform-specific code.</p>
 */
public final class ChampionView {

    private ChampionView() {
    }

    // ── Server ────────────────────────────────────────────────────────────────

    /**
     * Server-side champion view. Owns the mutable affix lists and delegates
     * persistence to a {@code saveCallback} supplied by the platform provider.
     */
    public static final class Server implements Champion.Server {

        private final LivingEntity entity;
        private final ChampionTier tier;
        private final List<AffixInstance> baseAffixes;
        private final List<AffixInstance> liveAffixes;
        private final Consumer<ChampionData> saveCallback;
        private final Runnable syncCallback;
        private final Optional<ResourceLocation> archetypeId;
        private final Set<String> triggeredPhaseIds = new HashSet<>();
        /**
         * @param entity           the champion mob
         * @param tier             assigned tier
         * @param baseAffixes      affixes from spawn (mutable copy — will be stored on save)
         * @param liveAffixes      current live list (base + phase additions)
         * @param saveCallback     called whenever the state changes, platform writes back to storage
         * @param syncCallback     called after every mutation to push updated state to tracking clients
         */
        public Server(
                LivingEntity entity,
                ChampionTier tier,
                List<AffixInstance> baseAffixes,
                List<AffixInstance> liveAffixes,
                Consumer<ChampionData> saveCallback,
                Runnable syncCallback,
                List<ResourceLocation> triggeredPhases,
                Optional<ResourceLocation> archetypeId) {
            this.entity = entity;
            this.tier = tier;
            this.baseAffixes = new ArrayList<>(baseAffixes);
            this.liveAffixes = new ArrayList<>(liveAffixes);
            this.saveCallback = saveCallback;
            this.syncCallback = syncCallback;
            this.archetypeId = archetypeId;
            triggeredPhases.forEach(id -> this.triggeredPhaseIds.add(id.toString()));
        }

        // ── Champion ─────────────────────────────────────────────────────────────

        @Override
        public LivingEntity entity() {
            return entity;
        }

        @Override
        public ChampionTier tier() {
            return tier;
        }

        @Override
        public List<AffixInstance> affixes() {
            return Collections.unmodifiableList(liveAffixes);
        }

        @Override
        public List<AffixInstance> baseAffixes() {
            return Collections.unmodifiableList(baseAffixes);
        }

        @Override
        public Optional<ResourceLocation> archetypeId() {
            return archetypeId;
        }

        // ── Mutation ──────────────────────────────────────────────────────────────

        @Override
        public void addAffix(AffixInstance instance) {
            liveAffixes.add(instance);
            triggerGoalSetup(instance);
            persist();
        }

        /**
         * Add an affix to the live list without triggering goal setup or sync.
         * Used during deserialization restore — entity has not joined the world yet.
         */
        public void addAffixSilently(AffixInstance instance) {
            liveAffixes.add(instance);
        }

        @Override
        public void removeAffix(AffixInstance instance) {
            if (liveAffixes.remove(instance)) {
                triggerGoalTeardown(instance);
                persist();
            }
        }

        @Override
        public void removeAffixByType(AffixType<?> type) {
            List<AffixInstance> toRemove = liveAffixes.stream()
                    .filter(i -> i.type() == type)
                    .toList();
            toRemove.forEach(i -> {
                liveAffixes.remove(i);
                triggerGoalTeardown(i);
            });
            if (!toRemove.isEmpty()) persist();
        }

        // ── Goal setup/teardown ────────────────────────────────────────────────────

        /**
         * Trigger goal setup for a newly added instance.
         * Skipped if the entity has no goal selector (e.g. players).
         */
        private void triggerGoalSetup(AffixInstance instance) {
            if (entity instanceof Mob mob) {
                instance.type().execute(instance, (reg, data) ->
                        reg.setupGoals(this, instance, data, mob.goalSelector)
                );
            }
        }

        private void triggerGoalTeardown(AffixInstance instance) {
            if (entity instanceof Mob mob) {
                instance.type().execute(instance, (reg, data) ->
                        reg.teardownGoals(this, instance, data, mob.goalSelector)
                );

            }
        }

        // ── Persistence ───────────────────────────────────────────────────────────

        /**
         * Serialise current state, notify the platform provider to write it back,
         * then push the updated state to all tracking clients.
         * Called after every mutation.
         */
        private void persist() {
            ChampionData data = toData();
            saveCallback.accept(data);
            syncCallback.run();
        }

        public void persistRuntimeState(boolean syncClient) {
            saveCallback.accept(toData());
            if (syncClient) {
                syncCallback.run();
            }
        }

        /**
         * Called by PhaseProcessor after new phases fire.
         */
        public void updateTriggeredPhases(
                List<ResourceLocation> phases
        ) {
            triggeredPhaseIds.clear();
            phases.forEach(id -> triggeredPhaseIds.add(id.toString()));
            persist();
        }

        public Set<String> getTriggeredPhaseIds() {
            return Collections.unmodifiableSet(triggeredPhaseIds);
        }

        public ChampionData toData() {
            List<ChampionData.AffixEntry> entries = baseAffixes.stream()
                    .map(instance -> {
                        var idOpt = ChampionsApi.get()
                                .getAffixTypeId(instance.type());
	                    return idOpt.map(resourceLocation -> new ChampionData.AffixEntry(
			                    resourceLocation, instance.strength(), instance.save())).orElse(null);
                    })
                    .filter(Objects::nonNull)
                    .toList();

            var triggeredList = triggeredPhaseIds.stream()
                    .map(ResourceLocation::parse)
                    .toList();

            return new ChampionData(tier.id(), entries, triggeredList, this.archetypeId);
        }
    }

    // ── Client ────────────────────────────────────────────────────────────────

    /**
     * Client-side champion view. Rebuilt entirely from sync packets.
     * Affix data is default-initialised — the client only needs type + strength for rendering.
     */
    public static final class Client implements Champion.Client {

        private final LivingEntity entity;
        private final ChampionTier tier;
        private final List<AffixInstance> affixes;

        public Client(LivingEntity entity, ChampionTier tier, List<AffixInstance> affixes) {
            this.entity = entity;
            this.tier = tier;
            this.affixes = List.copyOf(affixes);
        }

        @Override
        public LivingEntity entity() {
            return entity;
        }

        @Override
        public ChampionTier tier() {
            return tier;
        }

        @Override
        public List<AffixInstance> affixes() {
            return affixes;
        }
    }
}
