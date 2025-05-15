package io.github.apace100.origins.util;

import com.mojang.datafixers.util.Function10;
import net.minecraft.network.codec.StreamCodec;

import java.util.function.Function;

public class ByteBufUtils {
    public static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> StreamCodec<B, C> composite(
        StreamCodec<? super B, T1> codec1,
        Function<C, T1> getter1,
        StreamCodec<? super B, T2> codec2,
        Function<C, T2> getter2,
        StreamCodec<? super B, T3> codec3,
        Function<C, T3> getter3,
        StreamCodec<? super B, T4> codec4,
        Function<C, T4> getter4,
        StreamCodec<? super B, T5> codec5,
        Function<C, T5> getter5,
        StreamCodec<? super B, T6> codec6,
        Function<C, T6> getter6,
        StreamCodec<? super B, T7> codec7,
        Function<C, T7> getter7,
        StreamCodec<? super B, T8> codec8,
        Function<C, T8> getter8,
        StreamCodec<? super B, T9> codec9,
        Function<C, T9> getter9,
        StreamCodec<? super B, T10> codec10,
        Function<C, T10> getter10,
        Function10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, C> factory
    ) {
        return new StreamCodec<B, C>() {
            @Override
            public C decode(B object) {
                T1 object2 = codec1.decode(object);
                T2 object3 = codec2.decode(object);
                T3 object4 = codec3.decode(object);
                T4 object5 = codec4.decode(object);
                T5 object6 = codec5.decode(object);
                T6 object7 = codec6.decode(object);
                T7 object8 = codec7.decode(object);
                T8 object9 = codec8.decode(object);
                T9 object10 = codec9.decode(object);
                T10 object11 = codec10.decode(object);
                return factory.apply(object2, object3, object4, object5, object6, object7, object8, object9, object10, object11);
            }

            @Override
            public void encode(B object, C object2) {
                codec1.encode(object, (T1)getter1.apply(object2));
                codec2.encode(object, (T2)getter2.apply(object2));
                codec3.encode(object, (T3)getter3.apply(object2));
                codec4.encode(object, (T4)getter4.apply(object2));
                codec5.encode(object, (T5)getter5.apply(object2));
                codec6.encode(object, (T6)getter6.apply(object2));
                codec7.encode(object, (T7)getter7.apply(object2));
                codec8.encode(object, (T8)getter8.apply(object2));
                codec9.encode(object, (T9)getter9.apply(object2));
                codec10.encode(object, (T10)getter10.apply(object2));
            }
        };
    }
}
