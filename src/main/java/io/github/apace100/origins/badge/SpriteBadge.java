package io.github.apace100.origins.badge;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.apace100.apoli.power.PowerType;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public record SpriteBadge(Identifier spriteId) implements Badge {
    public static final MapCodec<SpriteBadge> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(
            Identifier.CODEC.fieldOf("sprite")
                .forGetter(SpriteBadge::spriteId)
        )
            .apply(instance, SpriteBadge::new)
    );

    @Override
    public boolean hasTooltip() {
        return false;
    }

    @Override
    public List<ClientTooltipComponent> getTooltipComponents(PowerType<?> powerType, int widthLimit, float time, Font textRenderer) {
        return new ArrayList<>();
    }

    @Override
    public MapCodec<? extends Badge> codec() {
        return CODEC;
    }
}
