package io.github.apace100.origins.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.origin.Origin;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public class ChoseOriginCriterion extends SimpleCriterionTrigger<ChoseOriginCriterion.Conditions> {
    public static final Codec<ChoseOriginCriterion.Conditions> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            EntityPredicate.ADVANCEMENT_CODEC
                .optionalFieldOf("player")
                .forGetter(Conditions::player),
            Identifier.CODEC
                .fieldOf("origin")
                .forGetter(conditions -> conditions.originId)
        )
            .apply(instance, Conditions::new)
    );

    public static ChoseOriginCriterion INSTANCE = new ChoseOriginCriterion();

    private static final Identifier ID = Identifier.fromNamespaceAndPath(Origins.MODID, "chose_origin");

    public void trigger(ServerPlayer player, Origin origin) {
        this.trigger(player, (conditions -> conditions.matches(origin)));
    }

    @Override
    public Codec<Conditions> codec() {
        return CODEC;
    }

    public static class Conditions implements SimpleCriterionTrigger.SimpleInstance {
        private final Identifier originId;
        private final Optional<ContextAwarePredicate> player;

        public Conditions(Optional<ContextAwarePredicate> player, Identifier originId) {
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
