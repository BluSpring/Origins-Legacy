package io.github.apace100.origins.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.apace100.origins.origin.Origin;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public record OriginTargetsComponent(
    List<OriginTarget> targets
) {
    public static final Codec<OriginTargetsComponent> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            OriginTarget.CODEC.listOf()
                .fieldOf("Targets")
                .forGetter(OriginTargetsComponent::targets)
        )
            .apply(instance, OriginTargetsComponent::new)
    );

    public static final StreamCodec<FriendlyByteBuf, OriginTargetsComponent> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.<FriendlyByteBuf, OriginTarget>list().apply(OriginTarget.STREAM_CODEC), OriginTargetsComponent::targets,
        OriginTargetsComponent::new
    );

    public static final DataComponentType<OriginTargetsComponent> TYPE = DataComponentType.<OriginTargetsComponent>builder()
        .persistent(CODEC)
        .networkSynchronized(STREAM_CODEC)
        .build();

    public record OriginTarget(
        ResourceLocation layer,
        Optional<ResourceLocation> origin
    ) {
        public static final Codec<OriginTarget> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                ResourceLocation.CODEC
                    .fieldOf("Layer")
                    .forGetter(OriginTarget::layer),
                ResourceLocation.CODEC
                    .optionalFieldOf("Layer")
                    .forGetter(OriginTarget::origin)
            )
                .apply(instance, OriginTarget::new)
        );

        public static final StreamCodec<FriendlyByteBuf, OriginTarget> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, OriginTarget::layer,
            ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), OriginTarget::origin,
            OriginTarget::new
        );
    }
}
