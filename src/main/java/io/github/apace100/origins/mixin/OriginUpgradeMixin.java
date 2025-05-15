package io.github.apace100.origins.mixin;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginRegistry;
import io.github.apace100.origins.origin.OriginUpgrade;
import io.github.apace100.origins.registry.ModComponents;
import net.minecraft.advancements.AdvancementHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.network.chat.Component;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;

@Mixin(PlayerAdvancements.class)
public class OriginUpgradeMixin {

    @Shadow
    private ServerPlayer player;

    @Inject(method = "award", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerAdvancements;unregisterListeners(Lnet/minecraft/advancements/AdvancementHolder;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void checkOriginUpgrade(AdvancementHolder advancement, String criterionKey, CallbackInfoReturnable<Boolean> cir, boolean bl, AdvancementProgress advancementProgress, boolean bl2) {
        if(advancementProgress.isDone()) {
            Origin.get(player).forEach((layer, o) -> {
                Optional<OriginUpgrade> upgrade = o.getUpgrade(advancement);
                if(upgrade.isPresent()) {
                    try {
                        Origin upgradeTo = OriginRegistry.get(upgrade.get().getUpgradeToOrigin());
                        if(upgradeTo != null) {
                            OriginComponent component = ModComponents.ORIGIN.get(player);
                            component.setOrigin(layer, upgradeTo);
                            component.sync();
                            String announcement = upgrade.get().getAnnouncement();
                            if (!announcement.isEmpty()) {
                                player.displayClientMessage(Component.translatable(announcement).withStyle(ChatFormatting.GOLD), false);
                            }
                        }
                    } catch(IllegalArgumentException e) {
                        Origins.LOGGER.error("Could not perform Origins upgrade from " + o.getIdentifier().toString() + " to " + upgrade.get().getUpgradeToOrigin().toString() + ", as the upgrade origin did not exist!");
                    }
                }
            });
        }
    }
}
