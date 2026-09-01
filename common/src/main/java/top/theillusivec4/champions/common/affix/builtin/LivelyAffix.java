package top.theillusivec4.champions.common.affix.builtin;

import net.minecraft.nbt.CompoundTag;
import top.theillusivec4.champions.api.affix.AffixType;
import top.theillusivec4.champions.api.affix.IAffixData;
import top.theillusivec4.champions.api.affix.handler.HandlerRegistry;
import top.theillusivec4.champions.api.affix.handler.event.DamageEvent;
import top.theillusivec4.champions.api.affix.handler.event.TickEvent;
import top.theillusivec4.champions.common.config.ChampionsConfig;

/**
 * Regenerates health when the champion has not taken damage recently.
 * Strength scales both heal amount and reduces cooldown.
 *
 * <p>Logic: record the world time on each hit; heal every 20 ticks if enough
 * time has passed since the last hit.</p>
 */
public final class LivelyAffix extends AffixType<LivelyAffix.Data> {

    @Override
    public Data createData() {
        return new Data();
    }

    @Override
    public void registerHandlers(HandlerRegistry<Data> registry) {

        // Record last damage time
        registry.on(DamageEvent.class, (champion, data, strength, evt) ->
                data.lastDamageTime = champion.entity().level().getGameTime()
        );

        // Tick: heal if cooldown elapsed
        registry.on(TickEvent.class, (champion, data, strength, evt) -> {
            if (!evt.every(20)) return;

            long cooldownTicks = ChampionsConfig.livelyCooldown * 20L
                    / Math.max(strength, 1);
            long timeSinceHit = champion.entity().level().getGameTime() - data.lastDamageTime;

            if (timeSinceHit < cooldownTicks) return;

            double healAmount = ChampionsConfig.livelyHealAmount * strength;

            // Passive multiplier when the mob hasn't been aggro'd for a while
            if (champion.entity().getNoActionTime() >= 100) {
                healAmount *= ChampionsConfig.livelyPassiveMultiplier;
            }

            champion.entity().heal((float) healAmount);
        });
    }

    // ── Data ──────────────────────────────────────────────────────────────────

    public static class Data implements IAffixData {
        public long lastDamageTime = Long.MIN_VALUE;

        @Override
        public void write(CompoundTag tag) {
            tag.putLong("last_damage", lastDamageTime);
        }

        @Override
        public void read(CompoundTag tag) {
            lastDamageTime = tag.getLong("last_damage");
        }
    }
}
