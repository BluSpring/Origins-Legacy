package io.github.apace100.origins.badge;

import com.mojang.serialization.MapCodec;
import io.github.apace100.apoli.power.PowerType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import java.util.List;

public interface Badge {
    StreamCodec<RegistryFriendlyByteBuf, Badge> STREAM_CODEC = StreamCodec.of((buf, value) -> value.writeBuf(buf), BadgeManager.REGISTRY::receiveDataObject);
    
    Identifier spriteId();
    
    boolean hasTooltip();
    
    @Environment(EnvType.CLIENT)
    List<ClientTooltipComponent> getTooltipComponents(PowerType<?> powerType, int widthLimit, float time, Font textRenderer);

    MapCodec<? extends Badge> codec();
}
