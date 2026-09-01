package top.theillusivec4.champions.common.affix.builtin;

import net.minecraft.nbt.CompoundTag;
import top.theillusivec4.champions.api.affix.AffixType;
import top.theillusivec4.champions.api.affix.IAffixData;
import top.theillusivec4.champions.api.affix.handler.HandlerRegistry;
import top.theillusivec4.champions.api.affix.handler.event.HurtEvent;
import top.theillusivec4.champions.common.config.ChampionsConfig;
import top.theillusivec4.champions.common.phase.AffixTriggerTracker;

/**
 * Reduces damage when the champion is hit repeatedly with the same damage type.
 * Switching damage types resets the counter.
 *
 * <p>Strength scales both the per-hit reduction increment and the maximum cap.</p>
 */
public final class AdaptableAffix extends AffixType<AdaptableAffix.Data> {

    @Override
    public Data createData() {
        return new Data();
    }

    @Override
    public void registerHandlers(HandlerRegistry<Data> registry) {
        registry.on(HurtEvent.class, (champion, data, strength, evt) -> {
            String incomingType = evt.source().getMsgId();

            if (incomingType.equals(data.lastDamageType)) {
                // Same damage type — accumulate resistance.
                // Matches original: subtract (originalDamage * increment * count) from
                // currentDamage, then floor at (originalDamage * (1 - maxReduction)).
                data.count++;
                data.triggerCount++;

                float original  = evt.originalDamage();
                float increment = (float) (ChampionsConfig.adaptableReductionIncrement * strength);
                float maxReduction = (float) ChampionsConfig.adaptableMaxReduction;

                float reduced = evt.currentDamage() - original * increment * data.count;
                float floor   = original * (1f - maxReduction);
                evt.setDamage(Math.max(floor, reduced));
            } else {
                // New damage type — reset counter but still take full damage this hit
                data.lastDamageType = incomingType;
                data.count = 0;
            }
        });
    }

    // ── Data ──────────────────────────────────────────────────────────────────

    public static class Data implements IAffixData, AffixTriggerTracker {
        public String lastDamageType = "";
        public int count = 0;
        public int triggerCount = 0;

        @Override
        public int triggerCount() {
            return triggerCount;
        }

        @Override
        public void write(CompoundTag tag) {
            tag.putString("last_type", lastDamageType);
            tag.putInt("count", count);
            tag.putInt("triggers", triggerCount);
        }

        @Override
        public void read(CompoundTag tag) {
            lastDamageType = tag.getString("last_type");
            count = tag.getInt("count");
            triggerCount = tag.getInt("triggers");
        }
    }
}
