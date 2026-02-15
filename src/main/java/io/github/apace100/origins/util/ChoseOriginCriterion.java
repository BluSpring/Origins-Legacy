package io.github.apace100.origins.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.origin.Origin;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class ChoseOriginCriterion extends SimpleCriterionTrigger<ChoseOriginCriterion.Conditions> {
    public static final Codec<ChoseOriginCriterion.Conditions> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            EntityPredicate.ADVANCEMENT_CODEC
                .optionalFieldOf("player")
                .forGetter(Conditions::player),
            ResourceLocation.CODEC
                .fieldOf("origin")
                .forGetter(conditions -> conditions.originId)
        )
            .apply(instance, Conditions::new)
    );

    public static ChoseOriginCriterion INSTANCE = new ChoseOriginCriterion();

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Origins.MODID, "chose_origin");

    public void trigger(ServerPlayer player, Origin origin) {
        this.trigger(player, (conditions -> conditions.matches(origin)));
    }

    @Override
    public Codec<Conditions> codec() {
        return CODEC;
    }

    public static class Conditions implements SimpleCriterionTrigger.SimpleInstance {
        private final ResourceLocation originId;
        private final Optional<ContextAwarePredicate> player;

        public Conditions(Optional<ContextAwarePredicate> player, ResourceLocation originId) {
            this.player = player;
            this.originId = originId;
        }

        public boolean matches(Origin origin) {
            return origin.getIdentifier().equals(originId);
        }

        @Override
        public Optional<ContextAwarePredicate> player() {
            return player;
        }
    }
}
