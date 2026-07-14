package io.github.apace100.origins.networking;

import io.github.apace100.apoli.power.PowerTypeRegistry;
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
import io.netty.channel.ChannelFutureListener;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.FriendlyByteBufs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
            ClientPlayNetworking.registerReceiver(ModPackets.POWERS_AND_ORIGINS, (packet, ctx) -> {
                PowerTypeRegistry.clear();
                packet.powers().factories().forEach(PowerTypeRegistry::register);
                receiveOriginList(packet.origins(), ctx);
            });
        }));
    }

    @Environment(EnvType.CLIENT)
    private static void receiveOriginConfirmation(ConfirmOriginPacket packet, ClientPlayNetworking.Context context) {
        OriginLayer layer = OriginLayers.getLayer(packet.layerId());
        Origin origin = OriginRegistry.get(packet.originId());
        context.client().execute(() -> {
            OriginComponent component = ModComponents.ORIGIN.get(context.player());
            component.setOrigin(layer, origin);
            if(context.client().gui.screen() instanceof WaitForNextLayerScreen layerScreen) {
                layerScreen.openSelection();
            }
        });
    }

    @Environment(EnvType.CLIENT)
    private static CompletableFuture<FriendlyByteBuf> handleHandshake(Minecraft minecraftClient, ClientHandshakePacketListenerImpl clientLoginNetworkHandler, FriendlyByteBuf packetByteBuf, Consumer<ChannelFutureListener> genericFutureListenerConsumer) {
        FriendlyByteBuf buf = FriendlyByteBufs.create();
        buf.writeInt(Origins.SEMVER.length);
        for(int i = 0; i < Origins.SEMVER.length; i++) {
            buf.writeInt(Origins.SEMVER[i]);
        }
        OriginsClient.isServerRunningOrigins = true;
        return CompletableFuture.completedFuture(buf);
    }

    @Environment(EnvType.CLIENT)
    private static void openOriginScreen(OpenOriginScreenPacket packet, ClientPlayNetworking.Context context) {
        boolean showDirtBackground = packet.showDirtBackground();
        context.client().execute(() -> {
            ArrayList<OriginLayer> layers = new ArrayList<>();
            OriginComponent component = ModComponents.ORIGIN.get(context.player());
            OriginLayers.getLayers().forEach(layer -> {
                if(layer.isEnabled() && !component.hasOrigin(layer)) {
                    layers.add(layer);
                }
            });
            Collections.sort(layers);
            context.client().gui.setScreen(new ChooseOriginScreen(layers, 0, showDirtBackground));
        });
    }

    @Environment(EnvType.CLIENT)
    private static void receiveOriginList(OriginListPacket packet, ClientPlayNetworking.Context context) {
        try {
            context.client().execute(() -> {
                OriginsClient.isServerRunningOrigins = true;
                OriginRegistry.reset();
                packet.origins().forEach(OriginRegistry::register);
            });
        } catch (Exception e) {
            Origins.LOGGER.error(e);
        }
    }

    @Environment(EnvType.CLIENT)
    private static void receiveLayerList(LayerListPacket packet, ClientPlayNetworking.Context context) {
        try {
            context.client().execute(() -> {
                OriginLayers.clear();
                packet.layers().forEach(OriginLayers::add);
                OriginDataLoadedCallback.EVENT.invoker().onDataLoaded(true);
            });
        } catch (Exception e) {
            Origins.LOGGER.error(e);
        }
    }

    @Environment(EnvType.CLIENT)
    private static void receiveBadgeList(BadgeListPacket packet, ClientPlayNetworking.Context context) {
        try {
            context.client().execute(() -> {
                BadgeManager.clear();
                for(Map.Entry<Identifier, List<Badge>> badgeEntry : packet.badges().entrySet()) {
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
