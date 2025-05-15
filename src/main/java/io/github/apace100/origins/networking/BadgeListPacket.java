package io.github.apace100.origins.networking;

import io.github.apace100.origins.badge.Badge;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record BadgeListPacket(Map<ResourceLocation, List<Badge>> badges) implements CustomPacketPayload {
    public static final StreamCodec<RegistryFriendlyByteBuf, BadgeListPacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, ByteBufCodecs.<RegistryFriendlyByteBuf, Badge>list().apply(Badge.STREAM_CODEC)), BadgeListPacket::badges,
        BadgeListPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ModPackets.BADGE_LIST;
    }
}
