package io.github.apace100.origins.origin;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;

public class OriginRegistry {

    private static final HashMap<ResourceLocation, Origin> idToOrigin = new HashMap<>();

    public static Origin register(Origin origin) {
        return register(origin.getIdentifier(), origin);
    }

    public static Origin register(ResourceLocation id, Origin origin) {
        if(idToOrigin.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate origin id tried to register: '" + id.toString() + "'");
        }
        idToOrigin.put(id, origin);
        return origin;
    }

    protected static Origin update(ResourceLocation id, Origin origin) {
        if(idToOrigin.containsKey(id)) {
            Origin old = idToOrigin.get(id);
            idToOrigin.remove(id);
        }
        return register(id, origin);
    }

    public static int size() {
        return idToOrigin.size();
    }

    public static Stream<ResourceLocation> identifiers() {
        return idToOrigin.keySet().stream();
    }

    public static Iterable<Map.Entry<ResourceLocation, Origin>> entries() {
        return idToOrigin.entrySet();
    }

    public static Iterable<Origin> values() {
        return idToOrigin.values();
    }

    public static Origin get(ResourceLocation id) {
        if(!idToOrigin.containsKey(id)) {
            throw new IllegalArgumentException("Could not get origin from id '" + id.toString() + "', as it was not registered!");
        }
        Origin origin = idToOrigin.get(id);
        return origin;
    }

    public static boolean contains(ResourceLocation id) {
        return idToOrigin.containsKey(id);
    }

    public static boolean contains(Origin origin) {
        return contains(origin.getIdentifier());
    }

    public static void clear() {
        idToOrigin.clear();
    }

    public static void reset() {
        clear();
        register(Origin.EMPTY);
    }

    public static void remove(ResourceLocation id) {
        idToOrigin.remove(id);
    }
}
