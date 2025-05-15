package io.github.apace100.origins.registry;

import io.github.apace100.origins.Origins;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

public class ModEnchantments {

    public static final ResourceKey<Enchantment> WATER_PROTECTION = ResourceKey.create(Registries.ENCHANTMENT, Origins.identifier("water_protection"));

    public static void register() {

    }
}
