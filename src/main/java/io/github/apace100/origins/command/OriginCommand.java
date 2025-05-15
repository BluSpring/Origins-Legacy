package io.github.apace100.origins.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.networking.ModPackets;
import io.github.apace100.origins.networking.OpenOriginScreenPacket;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.origin.OriginRegistry;
import io.github.apace100.origins.registry.ModComponents;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class OriginCommand {

	private enum TargetType {
		INVOKER,
		SPECIFY
	}

	public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(
			literal("origin").requires(cs -> cs.hasPermission(2))
				.then(literal("set")
					.then(argument("targets", EntityArgument.players())
						.then(argument("layer", LayerArgumentType.layer())
							.then(argument("origin", OriginArgumentType.origin())
								.executes(OriginCommand::setOrigin))))
				)
				.then(literal("has")
					.then(argument("targets", EntityArgument.players())
						.then(argument("layer", LayerArgumentType.layer())
							.then(argument("origin", OriginArgumentType.origin())
								.executes(OriginCommand::hasOrigin))))
				)
				.then(literal("get")
					.then(argument("target", EntityArgument.player())
						.then(argument("layer", LayerArgumentType.layer())
							.executes(OriginCommand::getOrigin)
						)
					)
				)
				.then(literal("gui")
					.executes(commandContext -> OriginCommand.openMultipleLayerScreens(commandContext, TargetType.INVOKER))
					.then(argument("targets", EntityArgument.players())
						.executes(commandContext -> OriginCommand.openMultipleLayerScreens(commandContext, TargetType.SPECIFY))
						.then(argument("layer", LayerArgumentType.layer())
							.executes(OriginCommand::openSingleLayerScreen)
						)
					)
				)
				.then(literal("random")
					.executes(commandContext -> OriginCommand.randomizeOrigins(commandContext, TargetType.INVOKER))
					.then(argument("targets", EntityArgument.players())
						.executes(commandContext -> OriginCommand.randomizeOrigins(commandContext, TargetType.SPECIFY))
						.then(argument("layer", LayerArgumentType.layer())
							.executes(OriginCommand::randomizeOrigin)
						)
					)
				)
		);
	}

	/**
	 * 	Set the origin of the specified entities in the specified origin layer.
	 * 	@param commandContext the command context
	 * 	@return the number of players whose origin has been set
	 * 	@throws CommandSyntaxException if the entity is not found or if the entity is <b>not</b> an instance of {@link ServerPlayer}
	 */
	private static int setOrigin(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
		
		Collection<ServerPlayer> targets = EntityArgument.getPlayers(commandContext, "targets");
		OriginLayer originLayer = LayerArgumentType.getLayer(commandContext, "layer");
		Origin origin = OriginArgumentType.getOrigin(commandContext, "origin");
		CommandSourceStack serverCommandSource = commandContext.getSource();
		
		int processedTargets = 0;
		
		if (origin.equals(Origin.EMPTY) || originLayer.getOrigins().contains(origin.getIdentifier())) {
			
			for (ServerPlayer target : targets) {
				
				OriginComponent originComponent = ModComponents.ORIGIN.get(target);
				boolean hadOriginBefore = originComponent.hadOriginBefore();
				
				originComponent.setOrigin(originLayer, origin);
				originComponent.sync();
				
				OriginComponent.partialOnChosen(target, hadOriginBefore, origin);
				
				processedTargets++;
				
			}
			
			if (processedTargets == 1) serverCommandSource.sendSuccess(() -> Component.translatable("commands.origin.set.success.single", targets.iterator().next().getDisplayName().getString(), Component.translatable(originLayer.getTranslationKey()), origin.getName()), true);
			else {
				int finalProcessedTargets = processedTargets;
				serverCommandSource.sendSuccess(() -> Component.translatable("commands.origin.set.success.multiple", finalProcessedTargets, Component.translatable(originLayer.getTranslationKey()), origin.getName()), true);
			}
			
		}
		
		else serverCommandSource.sendFailure(Component.translatable("commands.origin.unregistered_in_layer", origin.getIdentifier(), originLayer.getIdentifier()));
		
		return processedTargets;
		
	}

	/**
	 * 	Check if the specified entities has the specified origin in the specified origin layer.
	 * 	@param commandContext the command context
	 * 	@return the number of players that has the specified origin in the specified origin layer
	 * 	@throws CommandSyntaxException if the entity is not found or if the entity is <b>not</b> an instance of {@link ServerPlayer}
	 */
	private static int hasOrigin(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
		
		Collection<ServerPlayer> targets = EntityArgument.getPlayers(commandContext, "targets");
		OriginLayer originLayer = LayerArgumentType.getLayer(commandContext, "layer");
		Origin origin = OriginArgumentType.getOrigin(commandContext, "origin");
		CommandSourceStack serverCommandSource = commandContext.getSource();
		
		int processedTargets = 0;
		
		if (origin.equals(Origin.EMPTY) || originLayer.getOrigins().contains(origin.getIdentifier())) {
			
			for (ServerPlayer target : targets) {
				OriginComponent originComponent = ModComponents.ORIGIN.get(target);
				if ((origin.equals(Origin.EMPTY) || originComponent.hasOrigin(originLayer)) && originComponent.getOrigin(originLayer).equals(origin)) processedTargets++;
			}
			
			if (processedTargets == 0) serverCommandSource.sendFailure(Component.translatable("commands.execute.conditional.fail"));
			else if (processedTargets == 1) serverCommandSource.sendSuccess(() -> Component.translatable("commands.execute.conditional.pass"), true);
			else {
				int finalProcessedTargets = processedTargets;
				serverCommandSource.sendSuccess(() -> Component.translatable("commands.execute.conditional.pass_count", finalProcessedTargets), true);
			}
			
		}
		
		else serverCommandSource.sendFailure(Component.translatable("commands.origin.unregistered_in_layer", origin.getIdentifier(), originLayer.getIdentifier()));
		
		return processedTargets;
		
	}

	/**
	 * 	Get the origin of the specified entity from the specified origin layer.
	 * 	@param commandContext the command context
	 * 	@return 1
	 * 	@throws CommandSyntaxException if the entity is not found or if the entity is <b>not</b> an instance of {@link ServerPlayer}
	 */
	private static int getOrigin(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
		
		ServerPlayer target = EntityArgument.getPlayer(commandContext, "target");
		CommandSourceStack serverCommandSource = commandContext.getSource();

		OriginComponent originComponent = ModComponents.ORIGIN.get(target);
		OriginLayer originLayer = LayerArgumentType.getLayer(commandContext, "layer");
		Origin origin = originComponent.getOrigin(originLayer);
		
		serverCommandSource.sendSuccess(() -> Component.translatable("commands.origin.get.result", target.getDisplayName().getString(), Component.translatable(originLayer.getTranslationKey()), origin.getName(), origin.getIdentifier()), true);
		
		return 1;
		
	}

	/**
	 * 	Open the 'Choose Origin' screen for the specified origin layer to the specified entities.
	 * 	@param commandContext the command context
	 * 	@return the number of players that had the 'Choose Origin' screen opened for them
	 * 	@throws CommandSyntaxException if the entity is not found or if the entity is not an instance of {@link ServerPlayer}
	 */
	private static int openSingleLayerScreen(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {

		CommandSourceStack serverCommandSource = commandContext.getSource();
		Collection<ServerPlayer> targets = EntityArgument.getPlayers(commandContext, "targets");
		OriginLayer originLayer = LayerArgumentType.getLayer(commandContext, "layer");

		for (ServerPlayer target : targets) {
			openLayerScreen(target, originLayer);
		}

		serverCommandSource.sendSuccess(() -> Component.translatable("commands.origin.gui.layer", targets.size(), Component.translatable(originLayer.getTranslationKey())), true);
		return targets.size();

	}

	/**
	 * 	Open the 'Choose Origin' screen for all the enabled origin layers to the specified entities.
	 * 	@param commandContext the command context
	 * 	@return the number of players that had the 'Choose Origin' screen opened for them
	 * 	@throws CommandSyntaxException if the entity is not found or if the entity is not an instance of {@link ServerPlayer}
	 */
	private static int openMultipleLayerScreens(CommandContext<CommandSourceStack> commandContext, TargetType targetType) throws CommandSyntaxException {

		CommandSourceStack serverCommandSource = commandContext.getSource();
		List<ServerPlayer> targets = new ArrayList<>();
		List<OriginLayer> originLayers = OriginLayers.getLayers().stream().toList();

		switch (targetType) {
			case INVOKER -> targets.add(serverCommandSource.getPlayerOrException());
			case SPECIFY -> targets.addAll(EntityArgument.getPlayers(commandContext, "targets"));
		}

		for (ServerPlayer target : targets) {
			for (OriginLayer originLayer : originLayers) {
				openLayerScreen(target, originLayer);
			}
		}

		serverCommandSource.sendSuccess(() -> Component.translatable("commands.origin.gui.all", targets.size()), false);
		return targets.size();

	}

	/**
	 * 	Randomize the origin of the specified entities in the specified origin layer.
	 * 	@param commandContext the command context
	 * 	@return the number of players that had their origin randomized in the specified origin layer
	 * 	@throws CommandSyntaxException if the entity is not found or if the entity is not an instance of {@link ServerPlayer}
	 */
	private static int randomizeOrigin(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {

		CommandSourceStack serverCommandSource = commandContext.getSource();
		Collection<ServerPlayer> targets = EntityArgument.getPlayers(commandContext, "targets");
		OriginLayer originLayer = LayerArgumentType.getLayer(commandContext, "layer");

		if (originLayer.isRandomAllowed()) {

			Origin origin = null;
			for (ServerPlayer target : targets) {
				origin = getRandomOrigin(target, originLayer);
			}

			if (targets.size() > 1) serverCommandSource.sendSuccess(() -> Component.translatable("commands.origin.random.success.multiple", targets.size(), Component.translatable(originLayer.getTranslationKey())), true);
			else if (targets.size() == 1) {
				Origin finalOrigin = origin;
				serverCommandSource.sendSuccess(() -> Component.translatable("commands.origin.random.success.single", targets.iterator().next().getDisplayName().getString(), finalOrigin.getName(), Component.translatable(originLayer.getTranslationKey())), false);
			}

			return targets.size();

		}

		else {
			serverCommandSource.sendFailure(Component.translatable("commands.origin.random.not_allowed", Component.translatable(originLayer.getTranslationKey())));
			return 0;
		}

	}

	/**
	 * 	Randomize the layers of the specified entities in all of the origin layers that allows to be randomized.
	 * 	@param commandContext the command context
	 * 	@return the number of players that had their layers randomized in all of the origin layers that allows to be randomized
	 * 	@throws CommandSyntaxException if the entity is not found or if the entity is not an instance of {@link ServerPlayer}
	 */
	private static int randomizeOrigins(CommandContext<CommandSourceStack> commandContext, TargetType targetType) throws CommandSyntaxException {

		CommandSourceStack serverCommandSource = commandContext.getSource();
		List<ServerPlayer> targets = new ArrayList<>();
		List<OriginLayer> originLayers = OriginLayers.getLayers().stream().filter(OriginLayer::isRandomAllowed).toList();

		switch (targetType) {
			case INVOKER -> targets.add(serverCommandSource.getPlayerOrException());
			case SPECIFY -> targets.addAll(EntityArgument.getPlayers(commandContext, "targets"));
		}

		for (ServerPlayer target : targets) {
			for (OriginLayer originLayer : originLayers) {
				getRandomOrigin(target, originLayer);
			}
		}

		serverCommandSource.sendSuccess(() -> Component.translatable("commands.origin.random.all", targets.size(), originLayers.size()), false);
		return targets.size();

	}

	private static void openLayerScreen(ServerPlayer target, OriginLayer originLayer) {

		OriginComponent originComponent = ModComponents.ORIGIN.get(target);

		if (originLayer.isEnabled()) originComponent.setOrigin(originLayer, Origin.EMPTY);

		originComponent.checkAutoChoosingLayers(target, false);
		originComponent.sync();

		ServerPlayNetworking.send(target, new OpenOriginScreenPacket(false));

	}

	private static Origin getRandomOrigin(ServerPlayer target, OriginLayer originLayer) {

		List<Origin> origins = originLayer.getRandomOrigins(target).stream().map(OriginRegistry::get).toList();
		OriginComponent originComponent = ModComponents.ORIGIN.get(target);
		Origin origin = origins.get(new Random().nextInt(origins.size()));

		boolean hadOriginBefore = originComponent.hadOriginBefore();
		boolean hadAllOrigins = originComponent.hasAllOrigins();

		originComponent.setOrigin(originLayer, origin);
		originComponent.checkAutoChoosingLayers(target, false);
		originComponent.sync();

		if (originComponent.hasAllOrigins() && !hadAllOrigins) OriginComponent.onChosen(target, hadOriginBefore);

		Origins.LOGGER.info(
			"Player {} was randomly assigned the origin {} for layer {}",
			target.getDisplayName().getString(),
			origin.getIdentifier().toString(),
			originLayer.getIdentifier().toString()
		);

		return origin;

	}
	
}
