package io.github.apace100.origins.config;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.apace100.origins.Origins;
import net.minecraft.resources.Identifier;

import java.util.Map;

public record ServerConfig(
    boolean performVersionCheck,
    Map<Identifier, OriginConfig> origins
) {
    public static final Codec<ServerConfig> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.BOOL.optionalFieldOf("performVersionCheck", true)
                .forGetter(ServerConfig::performVersionCheck),
            Codec.simpleMap(
                Identifier.CODEC,
                Origins.OriginConfig.CODEC,
                new OriginsKeys()
            )
                .forGetter(ServerConfig::origins)
        )
            .apply(instance, ServerConfig::new)
    );
}
