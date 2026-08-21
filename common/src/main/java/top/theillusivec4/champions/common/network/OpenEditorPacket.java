package top.theillusivec4.champions.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * S2C: server sends the full editor payload when a player opens /champions editor.
 */
public record OpenEditorPacket(EditorPayload payload) implements CustomPacketPayload {

    public static final Type<OpenEditorPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath("champions", "open_editor"));

    public static final StreamCodec<FriendlyByteBuf, OpenEditorPacket> STREAM_CODEC =
            EditorPayload.STREAM_CODEC.map(OpenEditorPacket::new, OpenEditorPacket::payload);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() { return TYPE; }
}
