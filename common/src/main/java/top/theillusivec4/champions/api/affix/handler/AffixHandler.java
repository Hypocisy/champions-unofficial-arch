package top.theillusivec4.champions.api.affix.handler;

import top.theillusivec4.champions.api.affix.IAffixData;
import top.theillusivec4.champions.api.champion.Champion;

/**
 * A handler that responds to a specific event type {@code E} on behalf of an affix.
 *
 * <p>Registered via {@link HandlerRegistry#on(Class, AffixHandler)} inside
 * {@link top.theillusivec4.champions.api.affix.AffixType#registerHandlers(HandlerRegistry)}.</p>
 *
 * @param <D> the affix's per-champion data type
 * @param <E> the internal event type this handler responds to
 */
@FunctionalInterface
public interface AffixHandler<D extends IAffixData, E> {

    /**
     * @param champion the champion the affix is attached to
     * @param data     the live per-champion state for this affix instance
     * @param strength the strength of this affix instance (1–5)
     * @param event    the event payload
     */
    void handle(Champion champion, D data, int strength, E event);
}
