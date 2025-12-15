package io.github.apace100.origins.networking;

import io.github.apace100.origins.origin.Origin;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record OriginListPacket(Map<Identifier, Origin> origins) implements CustomPacketPayload {
    public static final StreamCodec<RegistryFriendlyByteBuf, OriginListPacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.map(HashMap::new, Identifier.STREAM_CODEC, Origin.STREAM_CODEC), OriginListPacket::origins,
        OriginListPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ModPackets.ORIGIN_LIST;
    }
}
