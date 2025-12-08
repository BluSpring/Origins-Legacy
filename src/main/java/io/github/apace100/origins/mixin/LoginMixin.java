package io.github.apace100.origins.mixin;

import io.github.apace100.apoli.networking.PowerListPacket;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import io.github.apace100.origins.badge.BadgeManager;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.networking.LayerListPacket;
import io.github.apace100.origins.networking.OpenOriginScreenPacket;
import io.github.apace100.origins.networking.OriginListPacket;
import io.github.apace100.origins.networking.PowersAndOriginsPacket;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.origin.OriginRegistry;
import io.github.apace100.origins.registry.ModComponents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.ladysnake.cca.api.v3.component.ComponentProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.List;

@Mixin(PlayerList.class)
public abstract class LoginMixin {

	@Shadow public abstract List<ServerPlayer> getPlayers();

	@Inject(at = @At("TAIL"), method = "placeNewPlayer")
	private void openOriginsGui(Connection connection, ServerPlayer player, CommonListenerCookie cookie, CallbackInfo ci) {
		OriginComponent component = ModComponents.ORIGIN.get(player);

		var origins = new HashMap<>(OriginRegistry.get());
		origins.remove(Origin.EMPTY.getIdentifier());

		ServerPlayNetworking.send(player, new PowersAndOriginsPacket(new PowerListPacket(new HashMap<>(PowerTypeRegistry.get())), new OriginListPacket(origins)));

		ServerPlayNetworking.send(player, new LayerListPacket(OriginLayers.getLayers().stream().map(layer -> {
			if(layer.isEnabled()) {
				if(!component.hasOrigin(layer)) {
					component.setOrigin(layer, Origin.EMPTY);
				}
			}

			return layer;
		}).toList()));

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
