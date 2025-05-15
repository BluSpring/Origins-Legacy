package io.github.apace100.origins.registry;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.entity.EnderianPearlEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntities {

    public static final EntityType<EnderianPearlEntity> ENDERIAN_PEARL = Registry.register(BuiltInRegistries.ENTITY_TYPE, Origins.identifier("enderian_pearl"),
        EntityType.Builder.<EnderianPearlEntity>of(EnderianPearlEntity::new, MobCategory.MISC)
            .sized(0.25f, 0.25f)
            .clientTrackingRange(64)
            .updateInterval(10)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, Origins.identifier("enderian_pearl")))
    );

    public static void register() {
    }
}
