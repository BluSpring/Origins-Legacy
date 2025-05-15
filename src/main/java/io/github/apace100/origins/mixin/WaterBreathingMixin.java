package io.github.apace100.origins.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import io.github.apace100.apoli.mixin.EntityAccessor;
import io.github.apace100.origins.power.OriginsPowerTypes;
import io.github.apace100.origins.registry.ModDamageSources;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public final class WaterBreathingMixin {

    @Mixin(LivingEntity.class)
    public static abstract class CanBreatheInWater extends Entity {

        public CanBreatheInWater(EntityType<?> type, Level world) {
            super(type, world);
        }

        @Inject(at = @At("HEAD"), method = "canBreatheUnderwater", cancellable = true)
        public void doWaterBreathing(CallbackInfoReturnable<Boolean> info) {
            if(OriginsPowerTypes.WATER_BREATHING.isActive(this)) {
                info.setReturnValue(true);
            }
        }
    }

    @Mixin(Player.class)
    public static abstract class UpdateAir extends LivingEntity {

        protected UpdateAir(EntityType<? extends LivingEntity> entityType, Level world) {
            super(entityType, world);
        }

        @Inject(at = @At("TAIL"), method = "tick")
        private void tick(CallbackInfo info) {
            if(OriginsPowerTypes.WATER_BREATHING.isActive(this)) {
                if(!this.isEyeInFluid(FluidTags.WATER) && !this.hasEffect(MobEffects.WATER_BREATHING) && !this.hasEffect(MobEffects.CONDUIT_POWER)) {
                    if(!((EntityAccessor) this).callIsInRain()) {
                        int landGain = this.increaseAirSupply(0);
                        this.setAirSupply(this.decreaseAirSupply(this.getAirSupply()) - landGain);
                        if (this.getAirSupply() == -20) {
                            this.setAirSupply(0);

                            for(int i = 0; i < 8; ++i) {
                                double f = this.random.nextDouble() - this.random.nextDouble();
                                double g = this.random.nextDouble() - this.random.nextDouble();
                                double h = this.random.nextDouble() - this.random.nextDouble();
                                this.level().addParticle(ParticleTypes.BUBBLE, this.getRandomX(0.5), this.getEyeY() + this.random.nextGaussian() * 0.08D, this.getRandomZ(0.5), f * 0.5F, g * 0.5F + 0.25F, h * 0.5F);
                            }

                            this.hurt(ModDamageSources.getSource(damageSources(), ModDamageSources.NO_WATER_FOR_GILLS), 2.0F);
                        }
                    } else {
                        int landGain = this.increaseAirSupply(0);
                        this.setAirSupply(this.getAirSupply() - landGain);
                    }
                } else if(this.getAirSupply() < this.getMaxAirSupply()){
                    this.setAirSupply(this.increaseAirSupply(this.getAirSupply()));
                }
            }
        }

        @ModifyExpressionValue(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isEyeInFluid(Lnet/minecraft/tags/TagKey;)Z"), method = "tick")
        public boolean isSubmergedInProxy(boolean submerged) {
            if(OriginsPowerTypes.WATER_BREATHING.isActive(this)) {
                return !submerged;
            }
            return submerged;
        }
    }
}
