package io.github.apace100.origins.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ConfirmOriginPacket(Identifier layerId, Identifier originId) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, ConfirmOriginPacket> CODEC = StreamCodec.composite(
        Identifier.STREAM_CODEC, ConfirmOriginPacket::layerId,
        Identifier.STREAM_CODEC, ConfirmOriginPacket::originId,
        ConfirmOriginPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ModPackets.CONFIRM_ORIGIN;
    }
}

