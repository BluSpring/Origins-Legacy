package io.github.apace100.origins.registry;

import io.github.apace100.origins.Origins;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModTags {

    public static final TagKey<Item> MEAT = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(Origins.MODID, "meat"));
    public static final TagKey<Block> UNPHASABLE = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(Origins.MODID, "unphasable"));
    public static final TagKey<Block> NATURAL_STONE = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(Origins.MODID, "natural_stone"));
    public static final TagKey<Item> RANGED_WEAPONS = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(Origins.MODID, "ranged_weapons"));

    public static void register() {

    }
}
