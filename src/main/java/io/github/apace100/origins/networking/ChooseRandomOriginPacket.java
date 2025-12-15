package io.github.apace100.origins.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ChooseRandomOriginPacket(Identifier layerId) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, ChooseRandomOriginPacket> CODEC = StreamCodec.composite(
        Identifier.STREAM_CODEC, ChooseRandomOriginPacket::layerId,
        ChooseRandomOriginPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ModPackets.CHOOSE_RANDOM_ORIGIN;
    }
}
