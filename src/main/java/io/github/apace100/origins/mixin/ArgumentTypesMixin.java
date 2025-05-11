package io.github.apace100.origins.mixin;

import com.mojang.brigadier.arguments.ArgumentType;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.command.LayerArgumentType;
import io.github.apace100.origins.command.OriginArgumentType;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ArgumentTypeInfos.class)
public abstract class ArgumentTypesMixin {
    @Shadow
    private static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>> ArgumentTypeInfo<A, T> register(Registry<ArgumentTypeInfo<?, ?>> registry, String string, Class<? extends A> clazz, ArgumentTypeInfo<A, T> argumentSerializer) {
        throw new AssertionError("Mixins for basic functionality are fun.");
    }

    @Inject(method = "bootstrap", at = @At("RETURN"))
    private static void registerApoliArgumentTypes(Registry<ArgumentTypeInfo<?, ?>> registry, CallbackInfoReturnable<ArgumentTypeInfo<?, ?>> cir) {
        register(registry, Origins.MODID + ":origin", OriginArgumentType.class, SingletonArgumentInfo.contextFree(OriginArgumentType::origin));
        register(registry, Origins.MODID + ":layer", LayerArgumentType.class, SingletonArgumentInfo.contextFree(LayerArgumentType::layer));
    }
}
