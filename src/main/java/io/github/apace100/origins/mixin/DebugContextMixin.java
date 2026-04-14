package io.github.apace100.origins.mixin;

import com.mojang.blaze3d.platform.Window;
import com.moulberry.mixinconstraints.annotations.IfDevEnvironment;
import org.spongepowered.asm.mixin.Mixin;

@IfDevEnvironment
@Mixin(Window.class)
public class DebugContextMixin {
    /*@Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwCreateWindow(IILjava/lang/CharSequence;JJ)J"))
    private void origins_legacy$enableDebugContext(WindowEventHandler eventHandler, ScreenManager screenManager, DisplayData displayData, String preferredFullscreenVideoMode, String title, CallbackInfo ci) {
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_DEBUG, GLFW.GLFW_TRUE);
    }*/
}
