package io.github.apace100.origins.badge;

import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.calio.data.SerializableData;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

public record TooltipBadge(ResourceLocation spriteId, Component text) implements Badge {

    public TooltipBadge(SerializableData.Instance instance) {
        this(instance.getId("sprite"), instance.get("text"));
    }

    @Override
    public boolean hasTooltip() {
        return true;
    }

    public static void addLines(List<ClientTooltipComponent> tooltips, Component text, Font textRenderer, int widthLimit) {
        if(textRenderer.width(text) > widthLimit) {
            for(FormattedCharSequence orderedText : textRenderer.split(text, widthLimit)) {
                tooltips.add(new ClientTextTooltip(orderedText));
            }
        } else {
            tooltips.add(new ClientTextTooltip(text.getVisualOrderText()));
        }
    }

    @Override
    public List<ClientTooltipComponent> getTooltipComponents(PowerType<?> powerType, int widthLimit, float time, Font textRenderer) {
        List<ClientTooltipComponent> tooltips = new LinkedList<>();
        addLines(tooltips, text, textRenderer, widthLimit);
        return tooltips;
    }

    @Override
    public SerializableData.Instance toData(SerializableData.Instance instance) {
        instance.set("sprite", spriteId);
        instance.set("text", text);
        return instance;
    }

    @Override
    public BadgeFactory getBadgeFactory() {
        return BadgeFactories.TOOLTIP;
    }

}
