package io.github.apace100.origins.networking;

import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginHolder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record OriginListPacket(Map<ResourceLocation, OriginHolder> origins) implements CustomPacketPayload {
    public static final StreamCodec<RegistryFriendlyByteBuf, OriginListPacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, OriginHolder.STREAM_CODEC), OriginListPacket::origins,
        OriginListPacket::new
    );

    public static OriginListPacket fromOriginsMap(Map<ResourceLocation, Origin> origins) {
        var holderMap = new HashMap<ResourceLocation, OriginHolder>();

        origins.forEach((id, origin) -> {
            holderMap.put(id, new OriginHolder(origin.getIdentifier(), origin.getDisplayItem(), origin.getImpact(), origin.getOrder(), origin.getLoadingPriority(), origin.isChoosable(), origin.getPowerIds(), origin.getOrCreateNameTranslationKey(), origin.getOrCreateDescriptionTranslationKey(), origin.getUpgrades()));
        });

        return new OriginListPacket(holderMap);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ModPackets.ORIGIN_LIST;
    }
}
