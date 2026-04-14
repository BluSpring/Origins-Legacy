package io.github.apace100.origins.registry;

import com.mojang.serialization.MapCodec;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.util.OriginLootCondition;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetEnchantmentsFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

public class ModLoot {

    private static final Identifier DUNGEON_LOOT = Identifier.withDefaultNamespace("chests/simple_dungeon");
    private static final Identifier STRONGHOLD_LIBRARY = Identifier.withDefaultNamespace("chests/stronghold_library");
    private static final Identifier MINESHAFT = Identifier.withDefaultNamespace("chests/abandoned_mineshaft");
    private static final Identifier WATER_RUIN = Identifier.withDefaultNamespace("chests/underwater_ruin_small");

    public static final MapCodec<? extends LootItemCondition> ORIGIN_LOOT_CONDITION = registerLootCondition("origin", OriginLootCondition.CODEC);

    private static MapCodec<? extends LootItemCondition> registerLootCondition(String path, MapCodec<? extends LootItemCondition> serializer) {
        return Registry.register(BuiltInRegistries.LOOT_CONDITION_TYPE, Origins.identifier(path), serializer);
    }

    public static void registerLootTables() {
        LootTableEvents.MODIFY.register(((resourceKey, tableBuilder, source, registries) -> {
            if (!source.isBuiltin()) {
                return;
            }
            var waterProtection = registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(ModEnchantments.WATER_PROTECTION);
            var identifier = resourceKey.identifier();
            if (DUNGEON_LOOT.equals(identifier)) {
                LootPool.Builder lootPool = new LootPool.Builder();
                lootPool.setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(Items.ENCHANTED_BOOK)
                        .setWeight(20)
                        .apply(new SetEnchantmentsFunction.Builder().withEnchantment(waterProtection, ConstantValue.exactly(1f))))
                    .add(LootItem.lootTableItem(Items.ENCHANTED_BOOK)
                        .setWeight(10)
                        .apply(new SetEnchantmentsFunction.Builder().withEnchantment(waterProtection, ConstantValue.exactly(2f))))
                    .add(EmptyLootItem.emptyItem().setWeight(80));
                tableBuilder.withPool(lootPool);
            } else if (STRONGHOLD_LIBRARY.equals(identifier)) {
                LootPool.Builder lootPool = new LootPool.Builder();
                lootPool.setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(Items.ENCHANTED_BOOK)
                        .setWeight(20)
                        .apply(new SetEnchantmentsFunction.Builder().withEnchantment(waterProtection, ConstantValue.exactly(2f))))
                    .add(LootItem.lootTableItem(Items.ENCHANTED_BOOK)
                        .setWeight(10)
                        .apply(new SetEnchantmentsFunction.Builder().withEnchantment(waterProtection, ConstantValue.exactly(3f))))
                    .add(EmptyLootItem.emptyItem().setWeight(80));
                tableBuilder.withPool(lootPool);
            } else if (MINESHAFT.equals(identifier)) {
                LootPool.Builder lootPool = new LootPool.Builder();
                lootPool.setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(Items.ENCHANTED_BOOK)
                        .setWeight(20)
                        .apply(new SetEnchantmentsFunction.Builder().withEnchantment(waterProtection, ConstantValue.exactly(1f))))
                    .add(LootItem.lootTableItem(Items.ENCHANTED_BOOK)
                        .setWeight(5)
                        .apply(new SetEnchantmentsFunction.Builder().withEnchantment(waterProtection, ConstantValue.exactly(2f))))
                    .add(EmptyLootItem.emptyItem().setWeight(90));
                tableBuilder.withPool(lootPool);
            } else if (WATER_RUIN.equals(identifier)) {
                LootPool.Builder lootPool = new LootPool.Builder();
                lootPool.setRolls(ConstantValue.exactly(1))
                    .add(LootItem.lootTableItem(Items.ENCHANTED_BOOK)
                        .setWeight(10)
                        .apply(new SetEnchantmentsFunction.Builder().withEnchantment(waterProtection, ConstantValue.exactly(1f))))
                    .add(LootItem.lootTableItem(Items.ENCHANTED_BOOK)
                        .setWeight(20)
                        .apply(new SetEnchantmentsFunction.Builder().withEnchantment(waterProtection, ConstantValue.exactly(2f))))
                    .add(EmptyLootItem.emptyItem().setWeight(110));
                tableBuilder.withPool(lootPool);
            }
        }));
    }
}
