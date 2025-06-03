package io.github.apace100.origins.screen.tooltip;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.apace100.origins.Origins;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**A {@link ClientTooltipComponent} used for {@link io.github.apace100.origins.badge.CraftingRecipeBadge}
 * Draws a snapshot of a 3x3 crafting recipe in the tooltip*/
public class CraftingRecipeTooltipComponent implements ClientTooltipComponent {
    private final int recipeWidth;
    private final NonNullList<ItemStack> inputs;
    private final ItemStack output;
    private static final ResourceLocation TEXTURE = Origins.identifier("textures/gui/tooltip/recipe_tooltip.png");

    public CraftingRecipeTooltipComponent(int recipeWidth, NonNullList<ItemStack> inputs, ItemStack output) {
        this.recipeWidth = recipeWidth;
        this.inputs = inputs;
        this.output = output;
    }

    @Override
    public int getHeight() {
        return 68;
    }

    @Override
    public int getWidth(Font textRenderer) {
        return 130;
    }

    @Override
    public void renderImage(Font textRenderer, int x, int y, GuiGraphics context) {
        this.drawBackground(context, x, y);
        for(int column = 0; column < 3; ++column) {
            for(int row = 0; row < 3; ++row) {
                int index = column + row * recipeWidth;
                int slotX = x + 8 + column * 18;
                int slotY = y + 8 + row * 18;
                ItemStack stack = column >= recipeWidth ? ItemStack.EMPTY : inputs.get(index);
                context.renderItem(stack, slotX, slotY);
                context.renderItemDecorations(textRenderer, stack, slotX, slotY);
            }
        }
        context.renderItem(output, x + 101, y + 25);
        context.renderItemDecorations(textRenderer, output, x + 101, y + 25);
    }

    public void drawBackground(GuiGraphics context, int x, int y) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        context.blit(TEXTURE, x, y, 0, 0, 130, 86, 256, 256);
    }

}
