package io.github.apace100.origins.origin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import io.github.apace100.apoli.Apoli;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.calio.data.MultiJsonDataLoader;
import io.github.apace100.origins.Origins;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class OriginManager extends MultiJsonDataLoader implements IdentifiableResourceReloadListener {
	
	private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();

	public OriginManager() {
		super(GSON, "layers");
	}

	@Override
	protected void apply(Map<ResourceLocation, List<JsonElement>> loader, ResourceManager manager, ProfilerFiller profiler) {
		OriginRegistry.reset();
		AtomicBoolean hasConfigChanged = new AtomicBoolean(false);
		loader.forEach((id, jel) -> {
			jel.forEach(je -> {
				try {
					Origin origin = Origin.fromJson(id, je.getAsJsonObject());
					if(!OriginRegistry.contains(id)) {
						OriginRegistry.register(id, origin);
					} else {
						if(OriginRegistry.get(id).getLoadingPriority() < origin.getLoadingPriority()) {
							OriginRegistry.update(id, origin);
						}
					}
				} catch(Exception e) {
					Origins.LOGGER.error("There was a problem reading Origin file " + id.toString() + " (skipping): " + e.getMessage());
				}
			});
			if(OriginRegistry.contains(id)) {
				Origin origin = OriginRegistry.get(id);
				hasConfigChanged.set(hasConfigChanged.get() | Origins.config.addToConfig(origin));
				if(Origins.config.isOriginDisabled(id)) {
					OriginRegistry.remove(id);
				} else {
					LinkedList<PowerType<?>> allPowers = new LinkedList<>();
					origin.getPowerTypes().forEach(allPowers::add);
					for(PowerType<?> powerType : allPowers) {
						if(Origins.config.isPowerDisabled(id, powerType.getIdentifier())) {
							origin.removePowerType(powerType);
						}
					}
				}
			}
		});
		Origins.LOGGER.info("Finished loading layers from data files. Registry contains " + OriginRegistry.size() + " layers.");
		if(hasConfigChanged.get()) {
			Origins.serializeConfig();
		}
	}

	@Override
	public ResourceLocation getFabricId() {
		return ResourceLocation.fromNamespaceAndPath(Origins.MODID, "layers");
	}

	@Override
	public Collection<ResourceLocation> getFabricDependencies() {
		return Set.of(Apoli.identifier("powers"));
	}
}
