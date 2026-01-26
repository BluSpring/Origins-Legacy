package io.github.apace100.origins.registry;

import io.github.apace100.origins.Origins;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.damagesource.DamageType;

public class ModDamageSources {

    public static final ResourceKey<DamageType> NO_WATER_FOR_GILLS = ResourceKey.create(Registries.DAMAGE_TYPE, Origins.identifier("no_water_for_gills"));

    public static DamageSource getSource(HolderLookup.Provider registry, ResourceKey<DamageType> damageType) {
        return new DamageSource(registry.lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(damageType));
    }
}
