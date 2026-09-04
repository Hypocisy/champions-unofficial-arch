package top.theillusivec4.champions.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import top.theillusivec4.champions.common.utils.Utils;

/**
 * C2S: client sends edited JSON back to the server on Save.
 */
public record SaveEditorPacket(EditorPayload payload) {

    public static final ResourceLocation ID = Utils.key("save_editor");

    public void encode(FriendlyByteBuf buf) {
        payload.encode(buf);
    }

    public static SaveEditorPacket decode(FriendlyByteBuf buf) {
        return new SaveEditorPacket(EditorPayload.decode(buf));
    }
}
