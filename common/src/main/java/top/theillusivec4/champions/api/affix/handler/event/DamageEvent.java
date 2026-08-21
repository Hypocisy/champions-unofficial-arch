package top.theillusivec4.champions.api.affix.handler.event;

import net.minecraft.world.damagesource.DamageSource;

import java.util.function.Consumer;

/**
 * Fired after incoming damage is calculated but before it is applied.
 */
public final class DamageEvent {
    private final DamageSource source;
    private final float originalDamage;
    private float currentDamage;
    private final Consumer<Float> setDamage;

    public DamageEvent(DamageSource source, float originalDamage, Consumer<Float> setDamage) {
        this.source = source;
        this.originalDamage = originalDamage;
        this.currentDamage = originalDamage;
        this.setDamage = setDamage;
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
}
