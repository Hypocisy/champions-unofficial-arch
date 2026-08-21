package top.theillusivec4.champions.api.affix.handler.event;

/**
 * Fired every server tick for each champion.
 * Use {@link #every(int)} for cheap throttling inside handlers.
 */
public final class TickEvent {
    private final int tickCount;

    public TickEvent(int tickCount) {
        this.tickCount = tickCount;
    }

    public int tickCount() {
        return tickCount;
    }

    /**
     * Returns true once every {@code ticks} ticks.
     */
    public boolean every(int ticks) {
        return tickCount % ticks == 0;
    }
}
