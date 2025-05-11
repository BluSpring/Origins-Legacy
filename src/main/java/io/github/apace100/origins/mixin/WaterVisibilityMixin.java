package io.github.apace100.origins.mixin;

import com.mojang.authlib.GameProfile;
import io.github.apace100.origins.power.OriginsPowerTypes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(LocalPlayer.class)
public abstract class WaterVisibilityMixin extends AbstractClientPlayer {

    public WaterVisibilityMixin(ClientLevel world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(at = @At("HEAD"), method = "getWaterVision", cancellable = true)
    private void getUnderwaterVisibility(CallbackInfoReturnable<Float> info) {
        if(OriginsPowerTypes.WATER_VISION.isActive(this)) {
            info.setReturnValue(1.0F);
        }
    }
}
