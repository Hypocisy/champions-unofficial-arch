package top.theillusivec4.champions.common.client.screen.editor.picker;

import net.minecraft.network.chat.Component;

/**
 * One selectable row in {@link RegistryPickerScreen}.
 *
 * @param id      canonical id written into JSON (e.g. {@code minecraft:zombie})
 * @param display human-readable label (may be a translatable component)
 */
public record PickerEntry(String id, Component display) {

    public static PickerEntry of(String id) {
        return new PickerEntry(id, Component.literal(id));
    }

    public static PickerEntry of(String id, String display) {
        return new PickerEntry(id, Component.literal(display));
    }
}
