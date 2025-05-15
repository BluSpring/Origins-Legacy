package io.github.apace100.origins.origin;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public enum Impact {
	
	NONE(0, "none", ChatFormatting.GRAY),
	LOW(1, "low", ChatFormatting.GREEN),
	MEDIUM(2, "medium", ChatFormatting.YELLOW),
	HIGH(3, "high", ChatFormatting.RED);
	
	private int impactValue;
	private String translationKey;
	private ChatFormatting textStyle;

	private Impact(int impactValue, String translationKey, ChatFormatting textStyle) {
		this.translationKey = "origins.gui.impact." + translationKey;
		this.impactValue = impactValue;
		this.textStyle = textStyle;
	}
	
	public int getImpactValue() {
		return impactValue;
	}
	
	public String getTranslationKey() {
		return translationKey;
	}
	
	public ChatFormatting getTextStyle() {
		return textStyle;
	}
	
	public MutableComponent getTextComponent() {
		return Component.translatable(getTranslationKey()).withStyle(getTextStyle());
	}
	
	public static Impact getByValue(int impactValue) {
		return Impact.values()[impactValue];
	}
}
