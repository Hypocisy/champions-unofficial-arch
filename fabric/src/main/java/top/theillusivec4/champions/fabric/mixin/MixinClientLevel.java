package top.theillusivec4.champions.fabric.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Placeholder for client-side mixins.
 * Currently empty — reserved for future client rendering hooks that
 * cannot be expressed via Fabric API events alone.
 */
@Mixin(ClientLevel.class)
public abstract class MixinClientLevel {
    // future: particle injection, boss bar rendering hooks, etc.
}
