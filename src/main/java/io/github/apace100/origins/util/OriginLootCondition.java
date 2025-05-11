package io.github.apace100.origins.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.registry.ModComponents;
import io.github.apace100.origins.registry.ModLoot;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public class OriginLootCondition implements LootItemCondition {
    private final ResourceLocation origin;
    private final ResourceLocation layer;

    private OriginLootCondition(ResourceLocation origin) {
        this.origin = origin;
        this.layer = null;
    }

    private OriginLootCondition(ResourceLocation origin, ResourceLocation layer) {
        this.origin = origin;
        this.layer = layer;
    }

    public LootItemConditionType getType() {
        return ModLoot.ORIGIN_LOOT_CONDITION;
    }

    public boolean test(LootContext lootContext) {
        Optional<OriginComponent> optional = ModComponents.ORIGIN.maybeGet(lootContext.getParamOrNull(LootContextParams.THIS_ENTITY));
        if(optional.isPresent()) {
            OriginComponent component = optional.get();
            HashMap<OriginLayer, Origin> map = component.getOrigins();
            boolean matches = false;
            for (Map.Entry<OriginLayer, Origin> entry: map.entrySet()) {
                if (layer != null) {
                    if (entry.getKey().getIdentifier().equals(layer) && entry.getValue().getIdentifier().equals(origin)) {
                        matches = true;
                        break;
                    }
                }
                else {
                    if (entry.getValue().getIdentifier().equals(origin)) {
                        matches = true;
                        break;
                    }
                }
            }
            return matches;
        }
        return false;
    }

    public static LootItemCondition.Builder builder(String originId) {
        return builder(new ResourceLocation(originId));
    }

    public static LootItemCondition.Builder builder(ResourceLocation origin) {
        return () -> new OriginLootCondition(origin);
    }

    public static LootItemCondition.Builder builder(String originId, String layerId) {
        return builder(new ResourceLocation(originId), new ResourceLocation(layerId));
    }

    public static LootItemCondition.Builder builder(ResourceLocation origin, ResourceLocation layer) {
        return () -> new OriginLootCondition(origin, layer);
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<OriginLootCondition> {
        @Override
        public void serialize(JsonObject jsonObject, OriginLootCondition originLootCondition, JsonSerializationContext jsonSerializationContext) {
            jsonObject.addProperty("origin", originLootCondition.origin.toString());
            if (originLootCondition.layer != null) {
                jsonObject.addProperty("layer", originLootCondition.layer.toString());
            }
        }

        @Override
        public OriginLootCondition deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            ResourceLocation origin = new ResourceLocation(GsonHelper.getAsString(jsonObject, "origin"));
            if (jsonObject.has("layer")) {
                ResourceLocation layer = new ResourceLocation(GsonHelper.getAsString(jsonObject, "layer"));
                return new OriginLootCondition(origin, layer);
            }
            return new OriginLootCondition(origin);
        }
    }
}
