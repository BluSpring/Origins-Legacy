package io.github.apace100.origins.networking;

import io.github.apace100.apoli.networking.PowerListPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record PowersAndOriginsPacket(
    PowerListPacket powers,
    OriginListPacket origins
) implements CustomPacketPayload {
    public static final StreamCodec<RegistryFriendlyByteBuf, PowersAndOriginsPacket> CODEC = StreamCodec.composite(
        PowerListPacket.CODEC, PowersAndOriginsPacket::powers,
        OriginListPacket.CODEC, PowersAndOriginsPacket::origins,
        PowersAndOriginsPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ModPackets.POWERS_AND_ORIGINS;
    }
}
