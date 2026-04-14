package io.github.apace100.origins.screen;

import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.badge.Badge;
import io.github.apace100.origins.badge.BadgeManager;
import io.github.apace100.origins.origin.Impact;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

import java.util.LinkedList;
import java.util.List;

public class OriginDisplayScreen extends Screen {

    private static final Identifier WINDOW = Identifier.fromNamespaceAndPath(Origins.MODID, "textures/gui/choose_origin.png");
    private Origin origin;
    private OriginLayer layer;
    private boolean isOriginRandom;
    private Component randomOriginText;

    protected static final int windowWidth = 176;
    protected static final int windowHeight = 182;
    protected int scrollPos = 0;
    private int currentMaxScroll = 0;
    private float time = 0;

    protected int guiTop, guiLeft;

    protected final boolean showDirtBackground;

    private final LinkedList<RenderedBadge> renderedBadges = new LinkedList<>();

    public OriginDisplayScreen(Component title, boolean showDirtBackground) {
        super(title);
        this.showDirtBackground = showDirtBackground;
    }

    public void showOrigin(Origin origin,OriginLayer layer, boolean isRandom) {
        this.origin = origin;
        this.layer = layer;
        this.isOriginRandom = isRandom;
        this.scrollPos = 0;
        this.time = 0;
    }

    public void setRandomOriginText(Component text) {
        this.randomOriginText = text;
    }

    @Override
    protected void init() {
        super.init();
        guiLeft = (this.width - windowWidth) / 2;
        guiTop = (this.height - windowHeight) / 2;
    }

    public Origin getCurrentOrigin() {
        return origin;
    }

    public OriginLayer getCurrentLayer() {
        return layer;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        if(showDirtBackground) {
            extractMenuBackgroundTexture(guiGraphics, Identifier.withDefaultNamespace("textures/block/dirt.png"), 0, 0, 0.0F, 0.0F, width, height); // Use the dirt texture as a fallback
            extractTransparentBackground(guiGraphics);
            extractMenuBackground(guiGraphics); // If the player has a menu background, render that as an overlay I guess?
        } else {
            super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        renderedBadges.clear();
        this.time += delta;
        super.extractRenderState(context, mouseX, mouseY, delta);
        this.renderOriginWindow(context, mouseX, mouseY);
        if(origin != null) {
            renderScrollbar(context, mouseX, mouseY);
            renderBadgeTooltip(context, mouseX, mouseY);
        }
    }

    private void renderScrollbar(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        if(!canScroll()) {
            return;
        }
        context.blit(RenderPipelines.GUI_TEXTURED, WINDOW, guiLeft + 155, guiTop + 35, 188, 24, 8, 134, 256, 256);
        int scrollbarY = 36;
        int maxScrollbarOffset = 141;
        int u = 176;
        float part = scrollPos / (float)currentMaxScroll;
        scrollbarY += (maxScrollbarOffset - scrollbarY) * part;
        if(scrolling) {
            u += 6;
        } else if(mouseX >= guiLeft + 156 && mouseX < guiLeft + 156 + 6) {
            if(mouseY >= guiTop + scrollbarY && mouseY < guiTop + scrollbarY + 27) {
                u += 6;
            }
        }
        context.blit(RenderPipelines.GUI_TEXTURED, WINDOW, guiLeft + 156, guiTop + scrollbarY, u, 24, 6, 27, 256, 256);
    }

    private boolean scrolling = false;
    private int scrollDragStart = 0;
    private double mouseDragStart = 0;

    private boolean canScroll() {
        return origin != null && currentMaxScroll > 0;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        scrolling = false;
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if(canScroll()) {
            scrolling = false;
            int scrollbarY = 36;
            int maxScrollbarOffset = 141;
            float part = scrollPos / (float)currentMaxScroll;
            scrollbarY += (maxScrollbarOffset - scrollbarY) * part;
            if(event.x() >= guiLeft + 156 && event.x() < guiLeft + 156 + 6) {
                if(event.y() >= guiTop + scrollbarY && event.y() < guiTop + scrollbarY + 27) {
                    scrolling = true;
                    scrollDragStart = scrollbarY;
                    mouseDragStart = event.y();
                    return true;
                }
            }
        }
        return super.mouseClicked(event, isDoubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        if(this.scrolling) {
            int delta = (int)(event.y() - mouseDragStart);
            int newScrollPos = (int)Math.max(36, Math.min(141, scrollDragStart + delta));
            float part = (newScrollPos - 36) / (float)(141 - 36);
            scrollPos = (int)(part * currentMaxScroll);
        }
        return super.mouseDragged(event, deltaX, deltaY);
    }

    private void renderBadgeTooltip(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        for(RenderedBadge rb : renderedBadges) {
            if(mouseX >= rb.x &&
               mouseX < rb.x + 9 &&
               mouseY >= rb.y &&
               mouseY < rb.y + 9 &&
               rb.hasTooltip()) {
                int widthLimit = width - mouseX - 24;
                context.tooltip(font, rb.getTooltipComponents(font, widthLimit), mouseX, mouseY, DefaultTooltipPositioner.INSTANCE, null);
            }
        }
    }

    protected Component getTitleText() {
        return Component.nullToEmpty("Origins");
    }

    private void renderOriginWindow(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        //RenderSystem.enableBlend();
        renderWindowBackground(context, 16, 0);
        if(origin != null) {
            //context.enableScissor(guiLeft, guiTop, guiLeft + windowWidth, guiTop + windowHeight);
            this.renderOriginContent(context, mouseX, mouseY);
            //context.disableScissor();
        }
        context.blit(RenderPipelines.GUI_TEXTURED, WINDOW, guiLeft, guiTop, 0, 0, windowWidth, windowWidth, windowWidth, windowHeight, 256, 256);
        if(origin != null) {
            context.pose().pushMatrix();
            //context.pose().translate(0, 0, 5);
            renderOriginName(context);
            //RenderSystem.setShaderTexture(0, WINDOW);
            this.renderOriginImpact(context, mouseX, mouseY);
            context.pose().popMatrix();
            Component title = getTitleText();
            context.centeredText(this.font, title.getString(), width / 2, guiTop - 15, 0xFFFFFFFF);
        }
        //RenderSystem.disableBlend();
    }

    private void renderOriginImpact(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        Impact impact = getCurrentOrigin().getImpact();
        int impactValue = impact.getImpactValue();
        int wOffset = impactValue * 8;
        for(int i = 0; i < 3; i++) {
            if(i < impactValue) {
                context.blit(RenderPipelines.GUI_TEXTURED, WINDOW, guiLeft + 128 + i * 10, guiTop + 19, windowWidth + wOffset, 16, 8, 8, 256, 256);
            } else {
                context.blit(RenderPipelines.GUI_TEXTURED, WINDOW, guiLeft + 128 + i * 10, guiTop + 19, windowWidth, 16, 8, 8, 256, 256);
            }
        }
        if(mouseX >= guiLeft + 128 && mouseX <= guiLeft + 158
            && mouseY >= guiTop + 19 && mouseY <= guiTop + 27) {
            MutableComponent ttc = Component.translatable(Origins.MODID + ".gui.impact.impact").append(": ").append(impact.getTextComponent());
            context.setTooltipForNextFrame(this.font, ttc, mouseX, mouseY);
        }
    }

    private void renderOriginName(GuiGraphicsExtractor context) {
        FormattedText originName = font.substrByWidth(getCurrentOrigin().getName(), windowWidth - 36);
        context.text(font, originName.getString(), guiLeft + 39, guiTop + 19, 0xFFFFFFFF);
        ItemStack is = getCurrentOrigin().getDisplayItem();
        context.item(is, guiLeft + 15, guiTop + 15);
    }

    private void renderWindowBackground(GuiGraphicsExtractor context, int offsetYStart, int offsetYEnd) {
        int border = 13;
        int endX = guiLeft + windowWidth - border;
        int endY = guiTop + windowHeight - border;
        for(int x = guiLeft; x < endX; x += 16) {
            for(int y = guiTop + offsetYStart; y < endY + offsetYEnd; y += 16) {
                context.blit(RenderPipelines.GUI_TEXTURED, WINDOW, x, y, windowWidth, 0, Math.max(16, endX - x), Math.max(16, endY + offsetYEnd - y), 256, 256);
            }
        }
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        boolean retValue = super.mouseScrolled(x, y, scrollX, scrollY);
        int np = this.scrollPos - (int)scrollY * 4;
        this.scrollPos = np < 0 ? 0 : Math.min(np, this.currentMaxScroll);
        return retValue;
    }

    private void renderOriginContent(GuiGraphicsExtractor context, int mouseX, int mouseY) {

        int textWidth = windowWidth - 48;
        // Without this code, the text may not cover the whole width of the window
        // if the scrollbar isn't shown. However with this code, you'll see 1 frame
        // of misaligned text because the text length (and whether scrolling is enabled)
        // is only evaluated on first render. :(
        /*if(!canScroll()) {
            textWidth += 12;
        }*/

        Origin origin = getCurrentOrigin();
        int x = guiLeft + 18;
        int y = guiTop + 45;
        int startY = y;
        int endY = y - 72 + windowHeight;
        y -= scrollPos;

        Component orgDesc = origin.getDescription();
        List<FormattedCharSequence> descLines = font.split(orgDesc, textWidth);
        for(FormattedCharSequence line : descLines) {
            if(y >= startY - 18 && y <= endY + 12) {
                context.text(font, line, x + 2, y - 6, 0xFFCCCCCC, false);
            }
            y += 12;
        }

        if(isOriginRandom) {
            List<FormattedCharSequence> drawLines = font.split(randomOriginText, textWidth);
            for(FormattedCharSequence line : drawLines) {
                y += 12;
                if(y >= startY - 24 && y <= endY + 12) {
                    context.text(font, line, x + 2, y, 0xFFCCCCCC, false);
                }
            }
            y += 14;
        } else {
            for(PowerType<?> p : origin.getPowerTypes()) {
                if(p.isHidden()) {
                    continue;
                }
                FormattedCharSequence name = Language.getInstance().getVisualOrder(font.substrByWidth(p.getName().withStyle(ChatFormatting.UNDERLINE), textWidth));
                Component desc = p.getDescription();
                List<FormattedCharSequence> drawLines = font.split(desc, textWidth);
                if(y >= startY - 24 && y <= endY + 12) {
                    context.text(font, name, x, y, 0xFFFFFFFF, false);
                    int tw = font.width(name);
                    List<Badge> badges = BadgeManager.getPowerBadges(p.getIdentifier());
                    int xStart = x + tw + 4;
                    int bi = 0;
                    for(Badge badge : badges) {
                        RenderedBadge renderedBadge = new RenderedBadge(p, badge,xStart + 10 * bi, y - 1);
                        renderedBadges.add(renderedBadge);
                        context.blit(RenderPipelines.GUI_TEXTURED, badge.spriteId(), xStart + 10 * bi, y - 1, 0, 0, 9, 9, 9, 9, 9, 9);
                        bi++;
                    }
                }
                for(FormattedCharSequence line : drawLines) {
                    y += 12;
                    if(y >= startY - 24 && y <= endY + 12) {
                        context.text(font, line, x + 2, y, 0xFFCCCCCC, false);
                    }
                }

                y += 14;

            }
        }
        y += scrollPos;
        currentMaxScroll = y - 14 - (guiTop + 158) + 10;
        if(currentMaxScroll < 0) {
            currentMaxScroll = 0;
        }
    }

    private class RenderedBadge {
        private final PowerType<?> powerType;
        private final Badge badge;
        private final int x;
        private final int y;

        public RenderedBadge(PowerType<?> powerType, Badge badge, int x, int y) {
            this.powerType = powerType;
            this.badge = badge;
            this.x = x;
            this.y = y;
        }

        public boolean hasTooltip() {
            return badge.hasTooltip();
        }

        public List<ClientTooltipComponent> getTooltipComponents(Font textRenderer, int widthLimit) {
            return badge.getTooltipComponents(powerType, widthLimit, OriginDisplayScreen.this.time, textRenderer);
        }

    }

}
