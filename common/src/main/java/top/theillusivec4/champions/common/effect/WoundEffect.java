package top.theillusivec4.champions.common.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Wound debuff applied by {@link top.theillusivec4.champions.common.affix.builtin.StatelessCombatAffixes.WoundingAffix}.
 *
 * <p>While active on a target:</p>
 * <ul>
 *   <li>Healing is halved (intercepted in platform event bridges).</li>
 *   <li>Incoming damage is multiplied by 1.5× (intercepted in platform event bridges).</li>
 * </ul>
 *
 * <p>The effect itself is a passive marker — no per-tick logic.
 * The actual reduction/amplification is applied by global listeners registered
 * in {@code ChampionEventsHandler} (NeoForge) and {@code FabricChampionEventsHandler} (Fabric).</p>
 */
public final class WoundEffect extends MobEffect {

    public WoundEffect() {
        super(MobEffectCategory.HARMFUL, 0xFF4444);  // red
    }
}
