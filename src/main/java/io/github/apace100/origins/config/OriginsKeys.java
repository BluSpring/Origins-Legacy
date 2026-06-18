package io.github.apace100.origins.config;

import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Keyable;

import java.util.stream.Stream;

public class OriginsKeys implements Keyable {
    @Override
    public <T> Stream<T> keys(DynamicOps<T> ops) {
        return Stream.empty();
    }
}
