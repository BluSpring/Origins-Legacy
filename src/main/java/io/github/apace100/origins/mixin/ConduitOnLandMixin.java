package io.github.apace100.origins.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.apace100.origins.power.OriginsPowerTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.ConduitBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ConduitBlockEntity.class)
public class ConduitOnLandMixin {

    @WrapOperation(method = "applyEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isInWaterOrRain()Z"))
    private static boolean allowConduitPowerOnLand(Player playerEntity, Operation<Boolean> original) {
        return original.call(playerEntity) || OriginsPowerTypes.CONDUIT_POWER_ON_LAND.isActive(playerEntity);
    }
}