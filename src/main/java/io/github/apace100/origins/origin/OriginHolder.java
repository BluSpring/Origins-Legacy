package io.github.apace100.origins.origin;

import io.github.apace100.origins.data.OriginsDataTypes;
import io.github.apace100.origins.util.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record OriginHolder(ResourceLocation id, ItemStack icon, Impact impact, int order, int loadingPriority, boolean choosable, List<ResourceLocation> powerIds, String name, String description, List<OriginUpgrade> upgrades) {
    public static final StreamCodec<RegistryFriendlyByteBuf, OriginHolder> STREAM_CODEC = ByteBufUtils.composite(
        ResourceLocation.STREAM_CODEC, OriginHolder::id,
        ItemStack.OPTIONAL_STREAM_CODEC, OriginHolder::icon,
        Impact.STREAM_CODEC, OriginHolder::impact,
        ByteBufCodecs.VAR_INT, OriginHolder::order,
        ByteBufCodecs.VAR_INT, OriginHolder::loadingPriority,
        ByteBufCodecs.BOOL, OriginHolder::choosable,
        ByteBufCodecs.<ByteBuf, ResourceLocation>list().apply(ResourceLocation.STREAM_CODEC), OriginHolder::powerIds,
        ByteBufCodecs.STRING_UTF8, OriginHolder::name,
        ByteBufCodecs.STRING_UTF8, OriginHolder::description,
        ByteBufCodecs.<RegistryFriendlyByteBuf, OriginUpgrade>list().apply(OriginsDataTypes.UPGRADE.streamCodec()), OriginHolder::upgrades,
        OriginHolder::new
    );

    public Origin asOrigin() {
        return new Origin(this.id(), this.icon(), this.impact(), this.order(), this.loadingPriority(), this.choosable(), this.powerIds(), this.name(), this.description(), this.upgrades());
    }
}
