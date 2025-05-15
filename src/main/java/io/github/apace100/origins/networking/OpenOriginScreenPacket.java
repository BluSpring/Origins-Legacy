package io.github.apace100.origins.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record OpenOriginScreenPacket(boolean showDirtBackground) implements CustomPacketPayload {
    public static final StreamCodec<FriendlyByteBuf, OpenOriginScreenPacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.BOOL, OpenOriginScreenPacket::showDirtBackground,
        OpenOriginScreenPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ModPackets.OPEN_ORIGIN_SCREEN;
    }
}
