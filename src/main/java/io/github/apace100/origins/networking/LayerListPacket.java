package io.github.apace100.origins.networking;

import io.github.apace100.origins.origin.OriginLayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;

public record LayerListPacket(List<OriginLayer> layers) implements CustomPacketPayload {
    public static final StreamCodec<RegistryFriendlyByteBuf, LayerListPacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.<RegistryFriendlyByteBuf, OriginLayer>list().apply(OriginLayer.STREAM_CODEC), LayerListPacket::layers,
        LayerListPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ModPackets.LAYER_LIST;
    }
}
