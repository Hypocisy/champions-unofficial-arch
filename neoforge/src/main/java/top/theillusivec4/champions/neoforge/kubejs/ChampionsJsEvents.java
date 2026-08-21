package top.theillusivec4.champions.neoforge.kubejs;

import dev.architectury.event.EventResult;
import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.kubejs.script.ScriptType;
import net.minecraft.resources.ResourceLocation;
import top.theillusivec4.champions.api.ChampionsApi;
import top.theillusivec4.champions.api.affix.AffixInstance;
import top.theillusivec4.champions.api.champion.Champion;
import top.theillusivec4.champions.api.champion.ChampionTier;
import top.theillusivec4.champions.api.event.ChampionEvents;


/**
 * KubeJS event group. Scripts listen to these events using:
 *
 * <pre>{@code
 * // Fire for every champion spawn
 * ChampionsEvents.spawn(event => {
 *     if (event.tier().level() >= 4) {
 *         event.addAffix('champions:enkindling', 3);
 *     }
 * });
 *
 * // Fire when a specific phase triggers — filter by phase id
 * ChampionsEvents.phase('mymod:enrage', event => {
 *     event.champion().entity().level()
 *         .playSound(null, event.champion().entity(), SoundEvents.WITHER_SPAWN, ...);
 * });
 * }</pre>
 */
public final class ChampionsJsEvents {

    public static final EventGroup GROUP = EventGroup.of("ChampionsEvents");

    /**
     * Fired when a champion is spawned. Cancellable. Affix list is mutable.
     */
    public static final EventHandler SPAWN = GROUP.server("spawn",
            () -> SpawnChampionEventJs.class);

    /**
     * Fired when a champion enters a phase. Filter by phase id extra.
     */
    public static final EventHandler PHASE = GROUP.server("phase",
            () -> ChampionPhaseEventJs.class);

    // ── Forwarding from NeoForge events ──────────────────────────────────────

    public static void register() {
        ChampionEvents.SPAWN.register((champion, tier, ctx) -> {
            if (SPAWN.hasListeners()) {
                SpawnChampionEventJs js = new SpawnChampionEventJs(champion, tier, ctx);
                SPAWN.post(js);
                if (js.isCancelled()) return EventResult.interruptFalse();
            }
            return EventResult.pass();
        });

        ChampionEvents.PHASE.register((champion, phaseId) -> {
            if (PHASE.hasListeners()) {
                ChampionPhaseEventJs js = new ChampionPhaseEventJs(champion, phaseId);
                PHASE.post(ScriptType.SERVER, js);
                if (js.isCancelled()) return EventResult.interruptFalse();
            }
            return EventResult.pass();
        });
    }

    // ── JS event wrappers ─────────────────────────────────────────────────────

    /**
     * JS-facing wrapper for {@link SpawnChampionEvent}.
     * Exposes champion, tier, and affix manipulation in a script-friendly API.
     */
    public static final class SpawnChampionEventJs
            implements KubeEvent {


        private final Champion champion;
        private final ChampionTier tier;
        private final ChampionEvents.SpawnContext ctx;
        private boolean cancelled = false;

        SpawnChampionEventJs(Champion champion, ChampionTier tier, ChampionEvents.SpawnContext ctx) {
            this.champion = champion;
            this.tier = tier;
            this.ctx = ctx;
        }


        public ChampionTier getTier() {
            return tier;
        }

        public Champion getChampion() {
            return champion;
        }

        public ChampionEvents.SpawnContext getCtx() {
            return ctx;
        }

        /**
         * Add an affix by id and strength. No-op if the id is not registered.
         */
        public void addAffix(String affixId, int strength) {
            ChampionsApi.get()
                    .getAffixType(ResourceLocation.parse(affixId))
                    .ifPresent(type -> ctx.addAffix(new AffixInstance(type, strength))
                    );
        }

        /**
         * Remove all affixes of the given type id.
         */
        public void removeAffix(String affixId) {
            ChampionsApi.get()
                    .getAffixType(ResourceLocation.parse(affixId))
                    .ifPresent(type -> ctx.removeAffixByType(type.getClass()));
        }

        public boolean isCancelled() {
            return cancelled;
        }

        public void cancel() {
            cancelled = true;
        }
    }

    /**
     * JS-facing wrapper for {@link ChampionPhaseEvent}.
     */
    public static final class ChampionPhaseEventJs
            implements KubeEvent {


        private final Champion champion;
        private final ResourceLocation phaseId;
        private boolean cancelled = false;

        public ChampionPhaseEventJs(Champion champion, ResourceLocation phaseId) {
            this.champion = champion;
            this.phaseId = phaseId;
        }

        public Champion champion() {
            return champion;
        }

        public ResourceLocation phaseId() {
            return phaseId;
        }

        public boolean isCancelled() {
            return cancelled;
        }

        public void cancel() {
            cancelled = true;
        }
    }
}
