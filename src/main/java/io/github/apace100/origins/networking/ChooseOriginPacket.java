package io.github.apace100.origins.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ChooseOriginPacket(Identifier originId, Identifier layerId) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, ChooseOriginPacket> CODEC = StreamCodec.composite(
        Identifier.STREAM_CODEC, ChooseOriginPacket::originId,
        Identifier.STREAM_CODEC, ChooseOriginPacket::layerId,
        ChooseOriginPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ModPackets.CHOOSE_ORIGIN;
    }
}
