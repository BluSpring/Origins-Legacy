package io.github.apace100.origins.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.Map;

public record OriginConfig(
    boolean enabled,
    Map<Identifier, Boolean> powers
) {
    public static final Codec<OriginConfig> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.BOOL.optionalFieldOf("enabled", true)
                .forGetter(OriginConfig::enabled),
            Codec.simpleMap(Identifier.CODEC, Codec.BOOL, new OriginsKeys())
                .forGetter(OriginConfig::powers)
        )
            .apply(instance, OriginConfig::new)
    );
}
