package io.github.apace100.origins.registry;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.enchantment.WaterProtectionEnchantment;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class ModEnchantments {

    public static final Enchantment WATER_PROTECTION = new WaterProtectionEnchantment(Enchantment.Rarity.RARE, EnchantmentCategory.ARMOR, new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET});

    public static void register() {
        register("water_protection", WATER_PROTECTION);
    }

    private static Enchantment register(String path, Enchantment enchantment) {
        Registry.register(BuiltInRegistries.ENCHANTMENT, new ResourceLocation(Origins.MODID, path), enchantment);
        return enchantment;
    }
}
