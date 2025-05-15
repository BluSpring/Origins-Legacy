package io.github.apace100.origins.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.apace100.origins.power.OriginsPowerTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public abstract class LikeWaterMixin extends Entity {

    public LikeWaterMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @WrapOperation(method = "travelInFluid", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getFluidFallingAdjustedMovement(DZLnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;"))
    public Vec3 method_26317Proxy(LivingEntity instance, double gravity, boolean isFalling, Vec3 deltaMovement, Operation<Vec3> original) {
        Vec3 oldReturn = original.call(instance, gravity, isFalling, deltaMovement);
        if(OriginsPowerTypes.LIKE_WATER.isActive(this)) {
            if (Math.abs(deltaMovement.y - gravity / 16.0D) < 0.025D) {
                return new Vec3(oldReturn.x, 0, oldReturn.z);
            }
        }
        return oldReturn;
    }
}
