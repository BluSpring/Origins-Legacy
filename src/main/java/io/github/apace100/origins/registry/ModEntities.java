package io.github.apace100.origins.registry;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.entity.EnderianPearlEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntities {

    public static final EntityType<EnderianPearlEntity> ENDERIAN_PEARL;

    static {
        ENDERIAN_PEARL = FabricEntityTypeBuilder.<EnderianPearlEntity>create(MobCategory.MISC, EnderianPearlEntity::new).dimensions(EntityDimensions.fixed(0.25f, 0.25f)).trackable(64, 10).build();
    }

    public static void register() {
        Registry.register(BuiltInRegistries.ENTITY_TYPE, new ResourceLocation(Origins.MODID, "enderian_pearl"), ENDERIAN_PEARL);
    }
}
