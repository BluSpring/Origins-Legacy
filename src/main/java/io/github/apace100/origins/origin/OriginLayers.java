package io.github.apace100.origins.origin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.apace100.calio.data.MultiJsonDataLoader;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.integration.OriginDataLoadedCallback;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.*;
import java.util.stream.Collectors;

public class OriginLayers extends MultiJsonDataLoader implements IdentifiableResourceReloadListener {

    private static final HashMap<ResourceLocation, OriginLayer> layers = new HashMap<>();
    private static int minLayerPriority = Integer.MIN_VALUE;

    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();

    public OriginLayers() {
        super(GSON, "origin_layers");
    }

    @Override
    protected void apply(Map<ResourceLocation, List<JsonElement>> loader, ResourceManager manager, ProfilerFiller profiler) {
        clear();
        HashMap<ResourceLocation, HashMap<Integer, List<JsonObject>>> layers = new HashMap<>();
        // Load phase
        loader.forEach((id, jel) -> {
            minLayerPriority = Integer.MIN_VALUE;
            jel.forEach(je -> {
                try {
                    Origins.LOGGER.info("Trying to read layer file: " + id);
                    JsonObject jo = je.getAsJsonObject();
                    boolean replace = GsonHelper.getAsBoolean(jo, "replace", false);
                    int priority = GsonHelper.getAsInt(jo, "loading_priority", 0);
                    if(priority >= minLayerPriority) {
                        HashMap<Integer, List<JsonObject>> inner = layers.computeIfAbsent(id, ident -> new HashMap<>());
                        List<JsonObject> layerList = inner.computeIfAbsent(priority, prio -> new LinkedList<>());
                        if(replace) {
                            layerList.clear();
                            minLayerPriority = priority + 1;
                        }
                        layerList.add(jo);
                    }
                } catch (Exception e) {
                    Origins.LOGGER.error("There was a problem reading Origin layer file " + id.toString() + " (skipping): " + e.getMessage());
                }
            });
        });
        // Merge phase
        for (Map.Entry<ResourceLocation, HashMap<Integer, List<JsonObject>>> layerToLoad : layers.entrySet()) {
            ResourceLocation layerId = layerToLoad.getKey();
            List<Integer> keys = layerToLoad.getValue().keySet().stream().sorted().collect(Collectors.toList());
            OriginLayer layer = null;
            for(Integer key : keys) {
                for(JsonObject jo : layerToLoad.getValue().get(key)) {
                    if(layer == null) {
                        layer = OriginLayer.fromJson(layerId, jo);
                    } else {
                        layer.merge(jo);
                    }
                }
            }
            OriginLayers.layers.put(layerId, layer);
        }
        Origins.LOGGER.info("Finished loading origin layers from data files. Read " + layers.size() + " layers.");
        OriginDataLoadedCallback.EVENT.invoker().onDataLoaded(false);
    }

    public static OriginLayer getLayer(ResourceLocation id) {
        if (!layers.containsKey(id)) throw new IllegalArgumentException("Could not get layer from id '" + id.toString() + "', as it doesn't exist!");
        else return layers.get(id);
    }

    public static Collection<OriginLayer> getLayers() {
        return layers.values();
    }

    public static int size() {
        return layers.size();
    }

    public static void clear() {
        layers.clear();
    }

    public static void add(OriginLayer layer) {
        layers.put(layer.getIdentifier(), layer);
    }

    @Override
    public ResourceLocation getFabricId() {
        return ResourceLocation.fromNamespaceAndPath(Origins.MODID, "origin_layers");
    }

    @Override
    public Collection<ResourceLocation> getFabricDependencies() {
        return Set.of(Origins.identifier("origins"));
    }
}
