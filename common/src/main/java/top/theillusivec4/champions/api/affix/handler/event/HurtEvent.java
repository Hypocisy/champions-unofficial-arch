package top.theillusivec4.champions.api.affix.handler.event;

import net.minecraft.world.damagesource.DamageSource;

import java.util.function.Consumer;

/**
 * Fired when a champion entity is about to take damage.
 * Wraps NeoForge/Fabric {@code LivingIncomingDamageEvent}.
 *
 * <p>Handlers may modify the damage amount via {@link #setDamage(float)}
 * or cancel the event entirely via {@link #cancel()}.</p>
 */
public final class HurtEvent {

    private final DamageSource source;
    private final float originalDamage;
    private float currentDamage;
    private final Consumer<Float> setDamage;
    private final Runnable cancel;

    public HurtEvent(
            DamageSource source,
            float originalDamage,
            float currentDamage,
            Consumer<Float> setDamage,
            Runnable cancel
    ) {
        this.source = source;
        this.originalDamage = originalDamage;
        this.currentDamage = currentDamage;
        this.setDamage = setDamage;
        this.cancel = cancel;
    }

    public DamageSource source() {
        return source;
    }

    public float originalDamage() {
        return originalDamage;
    }

    public float currentDamage() {
        return currentDamage;
    }

    public void setDamage(float amount) {
        this.currentDamage = amount;
        setDamage.accept(amount);
    }

    public void cancel() {
        cancel.run();
    }
}
