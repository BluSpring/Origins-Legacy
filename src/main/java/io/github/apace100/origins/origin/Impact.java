package io.github.apace100.origins.origin;

import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;

public enum Impact {
	
	NONE(0, "none", ChatFormatting.GRAY),
	LOW(1, "low", ChatFormatting.GREEN),
	MEDIUM(2, "medium", ChatFormatting.YELLOW),
	HIGH(3, "high", ChatFormatting.RED);

	public static final StreamCodec<ByteBuf, Impact> STREAM_CODEC = ByteBufCodecs.idMapper(ByIdMap.continuous(Impact::ordinal, Impact.values(), ByIdMap.OutOfBoundsStrategy.CLAMP), Impact::ordinal);

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
