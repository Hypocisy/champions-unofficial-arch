package top.theillusivec4.champions.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import top.theillusivec4.champions.common.utils.Utils;

/**
 * S2C: server sends the full editor payload when a player opens /champions editor.
 */
public record OpenEditorPacket(EditorPayload payload) {

    public static final ResourceLocation ID = Utils.key("open_editor");

    public void encode(FriendlyByteBuf buf) {
        payload.encode(buf);
    }

    public static OpenEditorPacket decode(FriendlyByteBuf buf) {
        return new OpenEditorPacket(EditorPayload.decode(buf));
    }
}
