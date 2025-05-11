package io.github.apace100.origins.badge;

import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.calio.data.SerializableData;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.resources.ResourceLocation;

public record SpriteBadge(ResourceLocation spriteId) implements Badge {

    public SpriteBadge(SerializableData.Instance instance) {
        this(instance.getId("sprite"));
    }

    @Override
    public boolean hasTooltip() {
        return false;
    }

    @Override
    public List<ClientTooltipComponent> getTooltipComponents(PowerType<?> powerType, int widthLimit, float time, Font textRenderer) {
        return new ArrayList<>();
    }

    @Override
    public SerializableData.Instance toData(SerializableData.Instance instance) {
        instance.set("sprite", spriteId);
        return instance;
    }

    @Override
    public BadgeFactory getBadgeFactory() {
        return BadgeFactories.SPRITE;
    }

}
