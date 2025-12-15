package io.github.apace100.origins.registry;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.content.TemporaryCobwebBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class ModBlocks {

    public static final Block TEMPORARY_COBWEB = new TemporaryCobwebBlock(BlockBehaviour.Properties.of()
        .mapColor(MapColor.WOOL).forceSolidOn().noCollission().requiresCorrectToolForDrops().strength(4.0F)
        .setId(ResourceKey.create(Registries.BLOCK, Origins.identifier("temporary_cobweb")))
    );

    public static void register() {
        register("temporary_cobweb", TEMPORARY_COBWEB, false);
    }

    private static void register(String blockName, Block block) {
        register(blockName, block, true);
    }

    private static void register(String blockName, Block block, boolean withBlockItem) {
        Registry.register(BuiltInRegistries.BLOCK, Identifier.fromNamespaceAndPath(Origins.MODID, blockName), block);
        if(withBlockItem) {
            Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(Origins.MODID, blockName), new BlockItem(block, new Item.Properties()));
        }
    }
}