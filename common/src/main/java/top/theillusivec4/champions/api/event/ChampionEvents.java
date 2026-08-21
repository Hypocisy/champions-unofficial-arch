package top.theillusivec4.champions.api.event;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import dev.architectury.event.EventResult;
import net.minecraft.resources.ResourceLocation;
import top.theillusivec4.champions.api.affix.AffixInstance;
import top.theillusivec4.champions.api.champion.Champion;
import top.theillusivec4.champions.api.champion.ChampionTier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ChampionEvents {

    public static final Event<PhaseCallback> PHASE =
            EventFactory.createEventResult();  // 支持取消
    public static final Event<SpawnCallback> SPAWN =
            EventFactory.createEventResult();

    @FunctionalInterface
    public interface PhaseCallback {
        /**
         * Fired on the event bus when a champion satisfies a phase condition and is about
         * to enter a new phase.
         *
         * <p>Cancelling this event prevents the phase from triggering. This can be used by other mods
         * or datapack-driven systems to suppress specific phases under custom conditions.</p>
         *
         * @param champion The champion entering the phase.
         * @param phaseId  The id of the phase being triggered (matches the id in the archetype datapack).
         * @return A result from an event, determines if the event should continue to other listeners, and determines the outcome of the event.
         */
        EventResult onPhase(Champion champion, ResourceLocation phaseId);
    }

    @FunctionalInterface
    public interface SpawnCallback {
        /**
         *
         * @param champion The champion being spawned. Read-only at this point — mutation goes through the affix list.
         * @param tier     The tier assigned to this champion.
         * @param ctx      The context of champion being spawned.
         * @return
         */
        EventResult onSpawn(Champion champion, ChampionTier tier, SpawnContext ctx);
    }


    public static final class SpawnContext {
        private final List<AffixInstance> affixes;

        public SpawnContext(List<AffixInstance> affixes) {
            this.affixes = new ArrayList<>(affixes);
        }

        /**
         * The mutable affix list. Modifications here are reflected in the champion
         * once the event completes (unless cancelled).
         */
        public List<AffixInstance> affixes() {
            return affixes;
        }

        /**
         * Convenience: add an affix to the list.
         */
        public void addAffix(AffixInstance instance) {
            affixes.add(instance);
        }

        /**
         * Convenience: remove all affixes of a given type.
         */
        public void removeAffixByType(Class<?> type) {
            affixes.removeIf(i -> i.type().getClass() == type);
        }

        /**
         * Returns an unmodifiable snapshot of the current affix list.
         */
        public List<AffixInstance> getAffixesSnapshot() {
            return Collections.unmodifiableList(affixes);
        }
    }

    private ChampionEvents() {
    }
}