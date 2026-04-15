package io.github.apace100.origins.data;

import io.github.apace100.calio.data.SerializableDataType;
import io.github.apace100.calio.data.SerializableDataTypes;
import io.github.apace100.calio.util.LazyItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;

public final class CompatibilityDataTypes {
    public static final SerializableDataType<ItemStackTemplate> ITEM_OR_ITEM_STACK_TEMPLATE = new SerializableDataType<>(ItemStackTemplate.class,
        SerializableDataTypes.ITEM_STACK_TEMPLATE::send, SerializableDataTypes.ITEM_STACK_TEMPLATE::receive, (jsonElement, provider) -> {
            if(jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isString()) {
                Item item = SerializableDataTypes.ITEM.read(jsonElement, provider);
                return new ItemStackTemplate(item);
            }
            return SerializableDataTypes.ITEM_STACK_TEMPLATE.read(jsonElement, provider);
        });

    public static final SerializableDataType<LazyItemStack> ITEM_OR_ITEM_STACK = new SerializableDataType<>(LazyItemStack.class,
        SerializableDataTypes.ITEM_STACK::send, SerializableDataTypes.ITEM_STACK::receive, (jsonElement, provider) -> {
        if(jsonElement.isJsonPrimitive() && jsonElement.getAsJsonPrimitive().isString()) {
            Item item = SerializableDataTypes.ITEM.read(jsonElement, provider);
            return new LazyItemStack(item.builtInRegistryHolder(), 1);
        }
        return SerializableDataTypes.ITEM_STACK.read(jsonElement, provider);
    });
}
