package io.github.apace100.origins.networking;

import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.OriginsClient;
import io.github.apace100.origins.badge.Badge;
import io.github.apace100.origins.badge.BadgeManager;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.integration.OriginDataLoadedCallback;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.origin.OriginRegistry;
import io.github.apace100.origins.registry.ModComponents;
import io.github.apace100.origins.screen.ChooseOriginScreen;
import io.github.apace100.origins.screen.WaitForNextLayerScreen;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ModPacketsS2C {

    @Environment(EnvType.CLIENT)
    public static void register() {
        ClientLoginNetworking.registerGlobalReceiver(ModPackets.HANDSHAKE, ModPacketsS2C::handleHandshake);
        ClientPlayConnectionEvents.INIT.register(((clientPlayNetworkHandler, minecraftClient) -> {
            ClientPlayNetworking.registerReceiver(ModPackets.OPEN_ORIGIN_SCREEN, ModPacketsS2C::openOriginScreen);
            ClientPlayNetworking.registerReceiver(ModPackets.ORIGIN_LIST, ModPacketsS2C::receiveOriginList);
            ClientPlayNetworking.registerReceiver(ModPackets.LAYER_LIST, ModPacketsS2C::receiveLayerList);
            ClientPlayNetworking.registerReceiver(ModPackets.CONFIRM_ORIGIN, ModPacketsS2C::receiveOriginConfirmation);
            ClientPlayNetworking.registerReceiver(ModPackets.BADGE_LIST, ModPacketsS2C::receiveBadgeList);
        }));
    }

    @Environment(EnvType.CLIENT)
    private static void receiveOriginConfirmation(Minecraft minecraftClient, ClientPacketListener clientPlayNetworkHandler, FriendlyByteBuf packetByteBuf, PacketSender packetSender) {
        OriginLayer layer = OriginLayers.getLayer(packetByteBuf.readResourceLocation());
        Origin origin = OriginRegistry.get(packetByteBuf.readResourceLocation());
        minecraftClient.execute(() -> {
            OriginComponent component = ModComponents.ORIGIN.get(minecraftClient.player);
            component.setOrigin(layer, origin);
            if(minecraftClient.screen instanceof WaitForNextLayerScreen) {
                ((WaitForNextLayerScreen)minecraftClient.screen).openSelection();
            }
        });
    }

    @Environment(EnvType.CLIENT)
    private static CompletableFuture<FriendlyByteBuf> handleHandshake(Minecraft minecraftClient, ClientHandshakePacketListenerImpl clientLoginNetworkHandler, FriendlyByteBuf packetByteBuf, Consumer<GenericFutureListener<? extends Future<? super Void>>> genericFutureListenerConsumer) {
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeInt(Origins.SEMVER.length);
        for(int i = 0; i < Origins.SEMVER.length; i++) {
            buf.writeInt(Origins.SEMVER[i]);
        }
        OriginsClient.isServerRunningOrigins = true;
        return CompletableFuture.completedFuture(buf);
    }

    @Environment(EnvType.CLIENT)
    private static void openOriginScreen(Minecraft minecraftClient, ClientPacketListener clientPlayNetworkHandler, FriendlyByteBuf packetByteBuf, PacketSender packetSender) {
        boolean showDirtBackground = packetByteBuf.readBoolean();
        minecraftClient.execute(() -> {
            ArrayList<OriginLayer> layers = new ArrayList<>();
            OriginComponent component = ModComponents.ORIGIN.get(minecraftClient.player);
            OriginLayers.getLayers().forEach(layer -> {
                if(layer.isEnabled() && !component.hasOrigin(layer)) {
                    layers.add(layer);
                }
            });
            Collections.sort(layers);
            minecraftClient.setScreen(new ChooseOriginScreen(layers, 0, showDirtBackground));
        });
    }

    @Environment(EnvType.CLIENT)
    private static void receiveOriginList(Minecraft minecraftClient, ClientPacketListener clientPlayNetworkHandler, FriendlyByteBuf packetByteBuf, PacketSender packetSender) {
        try {
            ResourceLocation[] ids = new ResourceLocation[packetByteBuf.readInt()];
            SerializableData.Instance[] origins = new SerializableData.Instance[ids.length];
            for(int i = 0; i < origins.length; i++) {
                ids[i] = ResourceLocation.tryParse(packetByteBuf.readUtf());
                origins[i] = Origin.DATA.read(packetByteBuf);
            }
            minecraftClient.execute(() -> {
                OriginsClient.isServerRunningOrigins = true;
                OriginRegistry.reset();
                for(int i = 0; i < ids.length; i++) {
                    OriginRegistry.register(ids[i], Origin.createFromData(ids[i], origins[i]));
                }
            });
        } catch (Exception e) {
            Origins.LOGGER.error(e);
        }
    }

    @Environment(EnvType.CLIENT)
    private static void receiveLayerList(Minecraft minecraftClient, ClientPacketListener clientPlayNetworkHandler, FriendlyByteBuf packetByteBuf, PacketSender packetSender) {
        try {
            int layerCount = packetByteBuf.readInt();
            OriginLayer[] layers = new OriginLayer[layerCount];
            for(int i = 0; i < layerCount; i++) {
                layers[i] = OriginLayer.read(packetByteBuf);
            }
            minecraftClient.execute(() -> {
                OriginLayers.clear();
                for(int i = 0; i < layerCount; i++) {
                    OriginLayers.add(layers[i]);
                }
                OriginDataLoadedCallback.EVENT.invoker().onDataLoaded(true);
            });
        } catch (Exception e) {
            Origins.LOGGER.error(e);
        }
    }

    @Environment(EnvType.CLIENT)
    private static void receiveBadgeList(Minecraft minecraftClient, ClientPacketListener clientPlayNetworkHandler, FriendlyByteBuf packetByteBuf, PacketSender packetSender) {
        try {
            HashMap<ResourceLocation, List<Badge>> badges = new HashMap<>();
            int count = packetByteBuf.readInt();
            for(int i = 0; i < count; i++) {
                ResourceLocation powerId = packetByteBuf.readResourceLocation();
                List<Badge> badgeList = new LinkedList<>();
                int badgeCount = packetByteBuf.readInt();
                for(int j = 0; j < badgeCount; j++) {
                    Badge badge = BadgeManager.REGISTRY.receiveDataObject(packetByteBuf);
                    badgeList.add(badge);
                }
                badges.put(powerId, badgeList);
            }
            minecraftClient.execute(() -> {
                BadgeManager.clear();
                for(Map.Entry<ResourceLocation, List<Badge>> badgeEntry : badges.entrySet()) {
                    for(Badge badge : badgeEntry.getValue()) {
                        BadgeManager.putPowerBadge(badgeEntry.getKey(), badge);
                    }
                }
            });
        } catch (Exception e) {
            Origins.LOGGER.error(e);
        }
    }
}
