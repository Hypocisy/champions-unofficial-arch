package top.theillusivec4.champions.api.affix.handler.event;

import java.util.function.Consumer;

/**
 * Fired when a champion is about to be healed.
 */
public final class HealEvent {
    private final float originalAmount;
    private float currentAmount;
    private final Consumer<Float> setAmount;

    public HealEvent(float originalAmount, Consumer<Float> setAmount) {
        this.originalAmount = originalAmount;
        this.currentAmount = originalAmount;
        this.setAmount = setAmount;
    }

    public float originalAmount() {
        return originalAmount;
    }

    public float currentAmount() {
        return currentAmount;
    }

    public void setAmount(float amount) {
        this.currentAmount = amount;
        setAmount.accept(amount);
    }
}
