package io.github.apace100.origins;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.serialization.JsonOps;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.util.NamespaceAlias;
import io.github.apace100.origins.badge.BadgeManager;
import io.github.apace100.origins.command.OriginCommand;
import io.github.apace100.origins.component.OriginTargetsComponent;
import io.github.apace100.origins.config.ServerConfig;
import io.github.apace100.origins.networking.ModPackets;
import io.github.apace100.origins.networking.ModPacketsC2S;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.origin.OriginManager;
import io.github.apace100.origins.power.OriginsEntityConditions;
import io.github.apace100.origins.power.OriginsPowerTypes;
import io.github.apace100.origins.registry.*;
import io.github.apace100.origins.util.ChoseOriginCriterion;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.CreativeModeTabs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class Origins implements ModInitializer {

	public static final String MODID = "origins";
	public static final String LEGACY_MODID = "origins_legacy";

	public static String VERSION = "";
	public static int[] SEMVER;
	public static final Logger LOGGER = LogManager.getLogger(Origins.class);

	public static ServerConfig config;
    private static final Gson GSON = new GsonBuilder()
        .disableHtmlEscaping()
        .setPrettyPrinting()
        .create();

	@Override
	public void onInitialize() {
		if (FabricLoader.getInstance().isDevelopmentEnvironment() && System.getProperty("origins.audit", "false").equalsIgnoreCase("true")) {
			MixinEnvironment.getCurrentEnvironment().audit();
		}

        registerResourceListeners();

		ModPackets.init();
		FabricLoader.getInstance().getModContainer(MODID).ifPresent(modContainer -> {
			VERSION = modContainer.getMetadata().getVersion().getFriendlyString();
			if(VERSION.contains("+")) {
				VERSION = VERSION.split("\\+")[0];
			}
			if(VERSION.contains("-")) {
				VERSION = VERSION.split("-")[0];
			}
			String[] splitVersion = VERSION.split("\\.");
			SEMVER = new int[splitVersion.length];
			for(int i = 0; i < SEMVER.length; i++) {
				SEMVER[i] = Integer.parseInt(splitVersion[i]);
			}
		});
		LOGGER.info("Origins " + VERSION + " is initializing. Have fun!");

		NamespaceAlias.addAlias(MODID, Apoli.MODID);
		NamespaceAlias.addAlias(LEGACY_MODID, Apoli.LEGACY_MODID);

		OriginsPowerTypes.register();
		OriginsEntityConditions.register();

		ModBlocks.register();
		ModItems.register();
		ModTags.register();
		ModPacketsC2S.register();
		ModEnchantments.register();
		ModEntities.register();
		ModLoot.registerLootTables();
		Origin.init();

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			OriginCommand.register(dispatcher);
		});
		CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register((content) -> {
			content.accept(ModItems.ORB_OF_ORIGIN);
		});

		Registry.register(BuiltInRegistries.TRIGGER_TYPES, Origins.identifier("choose_origin"), ChoseOriginCriterion.INSTANCE);
		Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, Origins.identifier("origin_targets"), OriginTargetsComponent.TYPE);
	}

	public static void serializeConfig() {
		try {
            var path = FabricLoader.getInstance().getConfigDir().resolve(MODID + "_server.json");
            var json = ServerConfig.CODEC.encodeStart(JsonOps.INSTANCE, config).getOrThrow();
            Files.writeString(path, GSON.toJson(json), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
		} catch (Throwable e) {
			Origins.LOGGER.error("Failed serialization of config file: " + e.getMessage());
		}
	}

	public static Identifier identifier(String path) {
		return Identifier.fromNamespaceAndPath(Origins.MODID, path);
	}

	public static Identifier legacy(String path) {
		return Identifier.fromNamespaceAndPath(LEGACY_MODID, path);
	}

	public void registerResourceListeners() {
        ResourceLoader loader = ResourceLoader.get(PackType.SERVER_DATA);
        Identifier powerData = Apoli.identifier("powers");
        Identifier originData = Origins.identifier("origins");
        Identifier layerData = Origins.identifier("origin_layers");

        loader.registerReloadListener(originData, new OriginManager());
        loader.addListenerOrdering(powerData, originData);

        loader.registerReloadListener(layerData, new OriginLayers());
        loader.addListenerOrdering(originData, layerData);

        BadgeManager.init();

        IdentifiableResourceReloadListener badgeLoader = BadgeManager.REGISTRY.getLoader();
        loader.registerReloadListener(badgeLoader.getFabricId(), badgeLoader);
        loader.addListenerOrdering(powerData, badgeLoader.getFabricId());

        loader.addListenerOrdering(badgeLoader.getFabricId(), powerData);
	}
}
