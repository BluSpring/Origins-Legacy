package io.github.apace100.origins.util;

import io.github.apace100.apoli.power.*;
import java.util.HashMap;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

public class PowerKeyManager {

    private static final HashMap<ResourceLocation, String> KEY_CACHE = new HashMap<>();

    public static void clearCache() {
        KEY_CACHE.clear();
    }

    public static String getKeyIdentifier(ResourceLocation powerId) {
        if(KEY_CACHE.containsKey(powerId)) {
            return KEY_CACHE.get(powerId);
        }
        String key = getKeyFromPower(powerId);
        KEY_CACHE.put(powerId, key);
        return key;
    }

    private static String getKeyFromPower(ResourceLocation powerId) {
        if(PowerTypeRegistry.contains(powerId)) {
            PowerType<?> powerType = PowerTypeRegistry.get(powerId);
            Power power = powerType.create(null);
            String key = "";
            if (power instanceof Active) {
                key = ((Active) power).getKey().key;
            } else if (powerType instanceof MultiplePowerType<?>) {
                List<ResourceLocation> subs = ((MultiplePowerType<?>) powerType).getSubPowers();
                for (ResourceLocation sub : subs) {
                    String subKey = getKeyFromPower(sub);
                    if (!subKey.isEmpty()) {
                        return subKey;
                    }
                }
            }
            return key.equals("none") ? "key.layers.primary_active" : key;
        }
        return "";
    }
}
