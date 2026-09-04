package top.theillusivec4.champions.common.phase;

import dev.architectury.event.EventResult;
import net.minecraft.resources.ResourceLocation;
import top.theillusivec4.champions.api.champion.Champion;
import top.theillusivec4.champions.api.event.ChampionEvents;
import top.theillusivec4.champions.common.api.ChampionsRegistries;
import top.theillusivec4.champions.common.archetype.ChampionArchetype;
import top.theillusivec4.champions.common.champion.ChampionView;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Evaluates {@link ChampionPhase} conditions and applies effects.
 *
 * <p>Called from the tick event handler every 10 ticks per champion (not every tick,
 * since phase conditions typically involve health checks or timers that don't need
 * sub-second granularity).</p>
 *
 * <p>All state needed for evaluation comes from the {@link Champion} argument —
 * this class is stateless and can be called from any context.</p>
 */
public final class PhaseProcessor {

    private PhaseProcessor() {
    }

    /**
     * Evaluate all phases for {@code champion} and trigger any whose conditions are met.
     *
     * @param champion        the server-side champion view
     * @param triggeredPhases the set of phase ids already triggered (read+write)
     */
    public static void process(Champion.Server champion, Set<String> triggeredPhases) {
        if (champion.entity().level().isClientSide()) return;

        ResourceLocationSet triggered = new ResourceLocationSet(triggeredPhases);

        // O(1) archetype lookup straight from the view — no serialization needed.
        // (The old path called champion.toData() here, which serialised every base
        // affix's NBT just to read back the archetype id.)
        Optional<ResourceLocation> archetypeId = champion.archetypeId();
        if (archetypeId.isEmpty()) return;

        ChampionArchetype archetype = ChampionsRegistries.archetypes()
                .get(archetypeId.get())
                .orElse(null);

        if (archetype == null || archetype.phases().isEmpty()) return;

        for (ChampionPhase phase : archetype.phases()) {
            boolean alreadyTriggered = triggered.contains(phase.id().toString());

            // Skip non-repeatable phases that have already fired
            if (alreadyTriggered && !phase.repeatable()) continue;

            // Evaluate condition
            if (!phase.condition().test(champion)) continue;

            // Fire cancellable NeoForge event — third-party mods can suppress this phase
            EventResult result = ChampionEvents.PHASE.invoker().onPhase(champion, phase.id());
            if (result.isFalse()) continue;

            // Apply all effects
            phase.effects().forEach(effect -> effect.apply(champion));
            triggered.add(phase.id().toString());
        }

        // Persist updated triggeredPhases back into ChampionData
        if (triggered.modified()) {
            persistTriggeredPhases(champion, triggered.toResourceLocations());
        }
    }

    // ── Restore on load ───────────────────────────────────────────────────────

    /**
     * Silently re-apply effects for all phases that were triggered in a previous session.
     * Called during {@link ChampionView.Server}
     * reconstruction, before the entity joins the world.
     */
    public static void restoreTriggeredPhases(Champion.Server champion) {
        if (!(champion instanceof ChampionView.Server s)) return;
        Optional<ResourceLocation> archetypeId = s.archetypeId();
        if (archetypeId.isEmpty()) return;

        ChampionArchetype archetype = ChampionsRegistries.archetypes()
                .get(archetypeId.get())
                .orElse(null);

        if (archetype == null) return;

        Set<String> triggeredIds = s.getTriggeredPhaseIds();

        archetype.phases().stream()
                .filter(phase -> triggeredIds.contains(phase.id().toString()))
                .forEach(phase ->
                        phase.effects().forEach(effect -> effect.restoreQuietly(champion))
                );
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static void persistTriggeredPhases(
            Champion.Server champion,
            List<ResourceLocation> phases
    ) {
        if (champion instanceof ChampionView.Server s) {
            s.updateTriggeredPhases(phases);
        }
    }

    // ── Internal helper ───────────────────────────────────────────────────────

    private static final class ResourceLocationSet {
        private final Set<String> set;
        private boolean modified = false;

        ResourceLocationSet(Set<String> initial) {
            this.set = new HashSet<>(initial);
        }

        boolean contains(String id) {
            return set.contains(id);
        }

        void add(String id) {
            if (set.add(id)) modified = true;
        }

        boolean modified() {
            return modified;
        }

        List<ResourceLocation> toResourceLocations() {
            return set.stream()
                    .map(ResourceLocation::new)
                    .toList();
        }
    }
}
