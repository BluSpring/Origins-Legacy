package io.github.apace100.origins.origin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.calio.data.MultiJsonDataLoader;
import io.github.apace100.origins.Origins;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class OriginManager extends MultiJsonDataLoader implements PreparableReloadListener {
	
	private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();

	private HolderLookup.Provider provider;

	public OriginManager() {
		super(GSON, "origins");
	}

	@Override
	public void prepareSharedState(SharedState currentReload) {
		super.prepareSharedState(currentReload);
		this.provider = currentReload.get(ResourceLoader.REGISTRY_LOOKUP_KEY);
	}

	@Override
	protected void apply(Map<Identifier, List<JsonElement>> loader, ResourceManager manager, ProfilerFiller profiler) {
		OriginRegistry.reset();
		AtomicBoolean hasConfigChanged = new AtomicBoolean(false);
		loader.forEach((id, jel) -> {
			jel.forEach(je -> {
				try {
					Origin origin = Origin.fromJson(id, je.getAsJsonObject(), this.provider);
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
}
