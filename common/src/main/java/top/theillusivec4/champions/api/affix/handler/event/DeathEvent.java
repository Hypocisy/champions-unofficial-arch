package top.theillusivec4.champions.api.affix.handler.event;

import net.minecraft.world.damagesource.DamageSource;

/**
 * Fired when a champion is about to die.
 * Cancelling prevents the death.
 */
public final class DeathEvent {
    private final DamageSource source;
    private final Runnable cancel;

    public DeathEvent(DamageSource source, Runnable cancel) {
        this.source = source;
        this.cancel = cancel;
    }

    public DamageSource source() {
        return source;
    }

    public void cancel() {
        cancel.run();
    }
}
