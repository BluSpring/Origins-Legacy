package io.github.apace100.origins.mixin;

import io.github.apace100.origins.networking.LayerListPacket;
import io.github.apace100.origins.networking.OpenOriginScreenPacket;
import io.github.apace100.origins.networking.OriginListPacket;
import net.minecraft.server.network.CommonListenerCookie;
import org.ladysnake.cca.api.v3.component.ComponentProvider;
import io.github.apace100.origins.badge.BadgeManager;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.networking.ModPackets;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.origin.OriginRegistry;
import io.github.apace100.origins.registry.ModComponents;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PlayerList.class)
public abstract class LoginMixin {

	@Shadow public abstract List<ServerPlayer> getPlayers();

	@Inject(at = @At("TAIL"), method = "placeNewPlayer")
	private void openOriginsGui(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci) {
		OriginComponent component = ModComponents.ORIGIN.get(player);

		ServerPlayNetworking.send(player, new OriginListPacket(OriginRegistry.get()));
		ServerPlayNetworking.send(player, new LayerListPacket(OriginLayers.getLayers().stream().toList()));

		BadgeManager.sync(player);

		List<ServerPlayer> playerList = getPlayers();
		playerList.forEach(spe -> ModComponents.ORIGIN.syncWith(spe, (ComponentProvider)player));
		OriginComponent.sync(player);
		if(!component.hasAllOrigins()) {
			if(component.checkAutoChoosingLayers(player, true)) {
				component.sync();
			}
			if(component.hasAllOrigins()) {
				OriginComponent.onChosen(player, false);
			} else {
				ServerPlayNetworking.send(player, new OpenOriginScreenPacket(true));
			}
		}
	}
}
