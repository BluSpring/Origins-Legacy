package io.github.apace100.origins.networking;

import io.github.apace100.origins.Origins;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public class ModPackets {

    public static final ResourceLocation HANDSHAKE = Origins.identifier("handshake");

    public static final CustomPacketPayload.Type<OpenOriginScreenPacket> OPEN_ORIGIN_SCREEN = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Origins.MODID, "open_origin_screen"));
    public static final CustomPacketPayload.Type<ChooseOriginPacket> CHOOSE_ORIGIN = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Origins.MODID, "choose_origin"));
    //public static final CustomPacketPayload.Type<UseActivePowersPacket> USE_ACTIVE_POWERS = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Origins.MODID, "use_active_powers"));
    public static final CustomPacketPayload.Type<OriginListPacket> ORIGIN_LIST = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Origins.MODID, "origin_list"));
    public static final CustomPacketPayload.Type<LayerListPacket> LAYER_LIST = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Origins.MODID, "layer_list"));
    //public static final CustomPacketPayload.Type<PowerListPacket> POWER_LIST = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Origins.MODID, "power_list"));
    public static final CustomPacketPayload.Type<ChooseRandomOriginPacket> CHOOSE_RANDOM_ORIGIN = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Origins.MODID, "choose_random_origin"));
    public static final CustomPacketPayload.Type<ConfirmOriginPacket> CONFIRM_ORIGIN = new CustomPacketPayload.Type<>(Origins.identifier("confirm_origin"));
    //public static final CustomPacketPayload.Type<PlayerLandedPacket> PLAYER_LANDED = new CustomPacketPayload.Type<>(Origins.identifier("player_landed"));
    public static final CustomPacketPayload.Type<BadgeListPacket> BADGE_LIST = new CustomPacketPayload.Type<>(Origins.identifier("badge_list"));
    public static final CustomPacketPayload.Type<PowersAndOriginsPacket> POWERS_AND_ORIGINS = new CustomPacketPayload.Type<>(Origins.identifier("powers_and_origins"));
    
    public static void init() {
        PayloadTypeRegistry.playS2C().register(OPEN_ORIGIN_SCREEN, OpenOriginScreenPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(CHOOSE_ORIGIN, ChooseOriginPacket.CODEC);

        PayloadTypeRegistry.playS2C().register(ORIGIN_LIST, OriginListPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(LAYER_LIST, LayerListPacket.CODEC);
        PayloadTypeRegistry.playC2S().register(CHOOSE_RANDOM_ORIGIN, ChooseRandomOriginPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(CONFIRM_ORIGIN, ConfirmOriginPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(BADGE_LIST, BadgeListPacket.CODEC);

        PayloadTypeRegistry.playS2C().register(POWERS_AND_ORIGINS, PowersAndOriginsPacket.CODEC);
    }
}
