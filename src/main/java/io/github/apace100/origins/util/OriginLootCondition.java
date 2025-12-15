package io.github.apace100.origins.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.registry.ModComponents;
import io.github.apace100.origins.registry.ModLoot;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

public class OriginLootCondition implements LootItemCondition {
    public static final MapCodec<OriginLootCondition> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            Identifier.CODEC
                .fieldOf("origin")
                .forGetter(e -> e.origin),
            Identifier.CODEC
                .optionalFieldOf("layer", null)
                .forGetter(e -> e.layer)
        )
            .apply(instance, OriginLootCondition::new)
    );

    private final Identifier origin;
    private final Identifier layer;

    private OriginLootCondition(Identifier origin) {
        this.origin = origin;
        this.layer = null;
    }

    private OriginLootCondition(Identifier origin, Identifier layer) {
        this.origin = origin;
        this.layer = layer;
    }

    public LootItemConditionType getType() {
        return ModLoot.ORIGIN_LOOT_CONDITION;
    }

    public boolean test(LootContext lootContext) {
        Optional<OriginComponent> optional = ModComponents.ORIGIN.maybeGet(lootContext.getOptionalParameter(LootContextParams.THIS_ENTITY));
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
        return builder(Identifier.parse(originId));
    }

    public static LootItemCondition.Builder builder(Identifier origin) {
        return () -> new OriginLootCondition(origin);
    }

    public static LootItemCondition.Builder builder(String originId, String layerId) {
        return builder(Identifier.parse(originId), Identifier.parse(layerId));
    }

    public static LootItemCondition.Builder builder(Identifier origin, Identifier layer) {
        return () -> new OriginLootCondition(origin, layer);
    }
}
