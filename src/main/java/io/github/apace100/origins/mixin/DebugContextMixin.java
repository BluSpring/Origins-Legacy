package io.github.apace100.origins.mixin;

import com.mojang.blaze3d.platform.DisplayData;
import com.mojang.blaze3d.platform.ScreenManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.WindowEventHandler;
import com.moulberry.mixinconstraints.annotations.IfDevEnvironment;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@IfDevEnvironment
@Mixin(Window.class)
public class DebugContextMixin {
    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwCreateWindow(IILjava/lang/CharSequence;JJ)J"))
    private void origins_legacy$enableDebugContext(WindowEventHandler eventHandler, ScreenManager screenManager, DisplayData displayData, String preferredFullscreenVideoMode, String title, CallbackInfo ci) {
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_DEBUG, GLFW.GLFW_TRUE);
    }
}
