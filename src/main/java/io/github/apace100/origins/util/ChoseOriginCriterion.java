package io.github.apace100.origins.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.origin.Origin;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;

public class ChoseOriginCriterion extends SimpleCriterionTrigger<ChoseOriginCriterion.Conditions> {

    public static ChoseOriginCriterion INSTANCE = new ChoseOriginCriterion();

    private static final ResourceLocation ID = new ResourceLocation(Origins.MODID, "chose_origin");

    @Override
    protected Conditions createInstance(JsonObject obj, ContextAwarePredicate playerPredicate, DeserializationContext predicateDeserializer) {
        ResourceLocation id = ResourceLocation.tryParse(GsonHelper.getAsString(obj, "origin"));
        return new Conditions(playerPredicate, id);
    }

    public void trigger(ServerPlayer player, Origin origin) {
        this.trigger(player, (conditions -> conditions.matches(origin)));
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public static class Conditions extends AbstractCriterionTriggerInstance {
        private final ResourceLocation originId;

        public Conditions(ContextAwarePredicate player, ResourceLocation originId) {
            super(ChoseOriginCriterion.ID, player);
            this.originId = originId;
        }

        public boolean matches(Origin origin) {
            return origin.getIdentifier().equals(originId);
        }

        public JsonObject serializeToJson(SerializationContext predicateSerializer) {
            JsonObject jsonObject = super.serializeToJson(predicateSerializer);
            jsonObject.add("origin", new JsonPrimitive(originId.toString()));
            return jsonObject;
        }
    }
}
