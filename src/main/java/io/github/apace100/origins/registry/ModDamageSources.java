package io.github.apace100.origins.registry;

import io.github.apace100.origins.Origins;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageType;

public class ModDamageSources {

    public static final ResourceKey<DamageType> NO_WATER_FOR_GILLS = ResourceKey.create(Registries.DAMAGE_TYPE, Origins.identifier("no_water_for_gills"));

    private static final Map<ResourceKey<DamageType>, DamageSource> damageSourceCache = new HashMap<>();

    public static DamageSource getSource(DamageSources damageSources, ResourceKey<DamageType> damageType) {
        return damageSourceCache.computeIfAbsent(damageType, damageSources::source);
    }
}
