package top.theillusivec4.champions.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * C2S: client sends edited JSON back to the server on Save.
 */
public record SaveEditorPacket(EditorPayload payload) implements CustomPacketPayload {

    public static final Type<SaveEditorPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath("champions", "save_editor"));

    public static final StreamCodec<FriendlyByteBuf, SaveEditorPacket> STREAM_CODEC =
            EditorPayload.STREAM_CODEC.map(SaveEditorPacket::new, SaveEditorPacket::payload);

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
