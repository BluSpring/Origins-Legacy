package io.github.apace100.origins.badge;

import io.github.apace100.apoli.power.*;
import io.github.apace100.calio.data.SerializableData;
import io.github.apace100.origins.util.PowerKeyManager;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

public record KeybindBadge(ResourceLocation spriteId, String text) implements Badge {

    public KeybindBadge(SerializableData.Instance instance) {
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
        Component keyText;
        keyText = ((MutableComponent)Component.nullToEmpty("["))
            .append(KeyMapping.createNameSupplier(PowerKeyManager.getKeyIdentifier(powerType.getIdentifier())).get())
            .append(Component.nullToEmpty("]"));
        addLines(tooltips, Component.translatable(text, keyText), textRenderer, widthLimit);
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
        return BadgeFactories.KEYBIND;
    }

}
