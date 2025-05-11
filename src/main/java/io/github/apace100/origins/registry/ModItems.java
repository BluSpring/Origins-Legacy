package io.github.apace100.origins.registry;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.content.OrbOfOriginItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class ModItems {

    public static final Item ORB_OF_ORIGIN = new OrbOfOriginItem();

    public static void register() {
        Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(Origins.MODID, "orb_of_origin"), ORB_OF_ORIGIN);
    }
}
