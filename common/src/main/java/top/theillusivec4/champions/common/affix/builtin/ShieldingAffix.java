package top.theillusivec4.champions.common.affix.builtin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageTypes;
import top.theillusivec4.champions.api.affix.AffixInstance;
import top.theillusivec4.champions.api.affix.AffixType;
import top.theillusivec4.champions.api.affix.IAffixClientSync;
import top.theillusivec4.champions.api.affix.IAffixData;
import top.theillusivec4.champions.api.affix.handler.HandlerRegistry;
import top.theillusivec4.champions.api.affix.handler.event.HurtEvent;
import top.theillusivec4.champions.api.affix.handler.event.TickEvent;
import top.theillusivec4.champions.common.config.ChampionsConfig;

/**
 * Randomly activates a magic shield that blocks ALL incoming damage.
 *
 * <p>While shielded every incoming hit is cancelled — the same broad protection
 * as the original implementation. Only falling out of the world ({@code FELL_OUT_OF_WORLD})
 * bypasses the shield because it cannot be cancelled meaningfully.</p>
 *
 * <p>Activation chance scales with strength: {@code SHIELDING_CHANCE * (0.5 + strength * 0.1)}.
 * The shield toggles every 40 ticks. Shield state is synced to clients.</p>
 */
public final class ShieldingAffix extends AffixType<ShieldingAffix.Data>
        implements IAffixClientSync {

    @Override
    public Data createData() {
        return new Data();
    }

    @Override
    public void registerHandlers(HandlerRegistry<Data> registry) {

        // Tick: randomly toggle shield every 40 ticks
        registry.on(TickEvent.class, (champion, data, strength, evt) -> {
            if (!evt.every(40)) return;
            double chance = ChampionsConfig.shieldingChance * (0.5 + strength * 0.1);
            if (champion.entity().getRandom().nextDouble() >= chance) return;
            data.shielding = !data.shielding;
            // Sync is triggered by the next persist() call via the save-callback chain.
        });

        // Hurt: cancel ALL incoming damage while shielded, except void death
        registry.on(HurtEvent.class, (champion, data, strength, evt) -> {
            if (!data.shielding) return;
            // Fell-out-of-world bypasses the shield (same exception as original)
            if (evt.source().is(DamageTypes.FELL_OUT_OF_WORLD)) return;
            evt.cancel();
        });
    }

    // ── IAffixClientSync ──────────────────────────────────────────────────────

    @Override
    public CompoundTag writeClientData(AffixInstance instance) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("shielding", getData(instance).shielding);
        return tag;
    }

    @Override
    public void readClientData(AffixInstance instance, CompoundTag tag) {
        getData(instance).shielding = tag.getBoolean("shielding");
    }

    // ── Data ──────────────────────────────────────────────────────────────────

    public static class Data implements IAffixData {
        public boolean shielding = false;

        @Override
        public void write(CompoundTag tag) {
            tag.putBoolean("shielding", shielding);
//            System.out.println(shielding);
        }

        @Override
        public void read(CompoundTag tag) {
            shielding = tag.getBoolean("shielding");
//            System.out.println(shielding);
        }
    }
}
