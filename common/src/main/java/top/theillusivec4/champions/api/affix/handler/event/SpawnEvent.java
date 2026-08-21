package top.theillusivec4.champions.api.affix.handler.event;

import net.minecraft.server.level.ServerLevel;

/**
 * Fired once when a champion is first assigned its affixes, before it joins the world.
 * Use this for one-time initialization that requires world access.
 */
public final class SpawnEvent {
    private final ServerLevel level;

    public SpawnEvent(ServerLevel level) {
        this.level = level;
    }

    public ServerLevel level() {
        return level;
    }
}
