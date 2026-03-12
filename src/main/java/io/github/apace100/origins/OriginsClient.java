package io.github.apace100.origins;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.apace100.apoli.ApoliClient;
import io.github.apace100.apoli.integration.PowerClearCallback;
import io.github.apace100.origins.networking.ModPacketsS2C;
import io.github.apace100.origins.registry.ModBlocks;
import io.github.apace100.origins.registry.ModEntities;
import io.github.apace100.origins.screen.ViewOriginScreen;
import io.github.apace100.origins.util.PowerKeyManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import org.lwjgl.glfw.GLFW;

public class OriginsClient implements ClientModInitializer {
    public static final KeyMapping.Category ORIGINS_CATEGORY = KeyMapping.Category.register(Origins.identifier("origins"));

    public static KeyMapping usePrimaryActivePowerKeybind;
    public static KeyMapping useSecondaryActivePowerKeybind;
    public static KeyMapping viewCurrentOriginKeybind;

    public static boolean isServerRunningOrigins = false;

    @Override
    @Environment(EnvType.CLIENT)
    public void onInitializeClient() {
        BlockRenderLayerMap.putBlock(ModBlocks.TEMPORARY_COBWEB, ChunkSectionLayer.CUTOUT);

        EntityRendererRegistry.register(ModEntities.ENDERIAN_PEARL, ThrownItemRenderer::new);

        ModPacketsS2C.register();

        usePrimaryActivePowerKeybind = new KeyMapping("key.origins.primary_active", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_G, ORIGINS_CATEGORY);
        useSecondaryActivePowerKeybind = new KeyMapping("key.origins.secondary_active", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, ORIGINS_CATEGORY);
        viewCurrentOriginKeybind = new KeyMapping("key.origins.view_origin", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_O, ORIGINS_CATEGORY);

        ApoliClient.registerPowerKeybinding("key.layers.primary_active", usePrimaryActivePowerKeybind);
        ApoliClient.registerPowerKeybinding("key.layers.secondary_active", useSecondaryActivePowerKeybind);
        ApoliClient.registerPowerKeybinding("primary", usePrimaryActivePowerKeybind);
        ApoliClient.registerPowerKeybinding("secondary", useSecondaryActivePowerKeybind);

        // "none" is the default key used when none is specified.
        ApoliClient.registerPowerKeybinding("none", usePrimaryActivePowerKeybind);

        KeyBindingHelper.registerKeyBinding(usePrimaryActivePowerKeybind);
        KeyBindingHelper.registerKeyBinding(useSecondaryActivePowerKeybind);
        KeyBindingHelper.registerKeyBinding(viewCurrentOriginKeybind);

        ClientTickEvents.START_CLIENT_TICK.register(tick -> {
            while(viewCurrentOriginKeybind.consumeClick()) {
                if(!(Minecraft.getInstance().screen instanceof ViewOriginScreen)) {
                    Minecraft.getInstance().setScreen(new ViewOriginScreen());
                }
            }
        });

        PowerClearCallback.EVENT.register(PowerKeyManager::clearCache);
    }
}
