package io.github.apace100.origins.badge;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.apace100.apoli.integration.PostPowerLoadCallback;
import io.github.apace100.apoli.integration.PrePowerReloadCallback;
import io.github.apace100.apoli.power.*;
import io.github.apace100.calio.registry.DataObjectRegistry;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.integration.AutoBadgeCallback;
import io.github.apace100.origins.networking.BadgeListPacket;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class BadgeManager {
    public static final DataObjectRegistry<Badge> REGISTRY = new DataObjectRegistry.Builder<>(Origins.identifier("badge"), Badge.class)
        .readFromData("badges", true)
        .dataErrorHandler((id, exception) -> Origins.LOGGER.error("Failed to read badge " + id + ", caused by", exception))
        .defaultFactory(BadgeFactories.KEYBIND)
        .buildAndRegister();
    private static final Map<ResourceLocation, List<Badge>> BADGES = new HashMap<>();

    private static final ResourceLocation TOGGLE_BADGE_SPRITE = Origins.identifier("textures/gui/badge/toggle.png");
    private static final ResourceLocation ACTIVE_BADGE_SPRITE = Origins.identifier("textures/gui/badge/active.png");
    private static final ResourceLocation RECIPE_BADGE_SPRITE = Origins.identifier("textures/gui/badge/recipe.png");

    private static final ResourceLocation TOGGLE_BADGE_ID = Origins.identifier("toggle");
    private static final ResourceLocation ACTIVE_BADGE_ID = Origins.identifier("active");

    public static void init() {
        //register builtin badge types
        register(BadgeFactories.SPRITE);
        register(BadgeFactories.TOOLTIP);
        register(BadgeFactories.CRAFTING_RECIPE);
        register(BadgeFactories.KEYBIND);
        //register callbacks
        PrePowerReloadCallback.EVENT.register(BadgeManager::clear);
        PowerTypes.registerAdditionalData("badges", BadgeManager::readCustomBadges);
        PostPowerLoadCallback.EVENT.register(BadgeManager::readAutoBadges);
        AutoBadgeCallback.EVENT.register(BadgeManager::createAutoBadges);
    }

    public static void register(BadgeFactory factory) {
        REGISTRY.registerFactory(factory.id(), factory);
    }

    public static void putPowerBadge(ResourceLocation powerId, Badge badge) {
        List<Badge> badgeList = BADGES.computeIfAbsent(powerId, id -> new LinkedList<>());
        badgeList.add(badge);
    }

    public static List<Badge> getPowerBadges(ResourceLocation powerId) {
        return BADGES.computeIfAbsent(powerId, id -> new LinkedList<>());
    }

    public static void clear() {
        BADGES.clear();
    }

    public static void sync(ServerPlayer player) {
        REGISTRY.sync(player);
        ServerPlayNetworking.send(player, new BadgeListPacket(BADGES));
    }

    public static void readCustomBadges(ResourceLocation powerId, ResourceLocation factoryId, boolean isSubPower, JsonElement data, PowerType<?> powerType) {
        if(!(powerType.isHidden() || isSubPower)) {
            if(data.isJsonArray()) {
                BADGES.computeIfAbsent(powerId, id -> new LinkedList<>());
                for(JsonElement badgeJson : data.getAsJsonArray()) {
                    if(badgeJson.isJsonPrimitive()) {
                        ResourceLocation badgeId = ResourceLocation.tryParse(badgeJson.getAsString());
                        if(badgeId != null) {
                            Badge badge = REGISTRY.get(badgeId);
                            if(badge != null) {
                                putPowerBadge(powerId, badge);
                            } else {
                                Origins.LOGGER.error("\"badges\" field in power \"{}\" is referring to an undefined badge \"{}\"!", powerId, badgeId);
                            }
                        } else {
                            Origins.LOGGER.error("\"badges\" field in power \"{}\" is not a valid identifier!", powerId);
                        }
                    } else if(badgeJson.isJsonObject()) {
                        try {
                            putPowerBadge(powerId, REGISTRY.readDataObject(badgeJson));
                        } catch(Exception exception) {
                            Origins.LOGGER.error("\"badges\" field in power \"" + powerId
                                + "\" contained an JSON object entry that cannot be resolved!", exception);
                        }
                    } else {
                        Origins.LOGGER.error("\"badges\" field in power \"" + powerId
                            + "\" contained an entry that was a JSON array, which is not allowed!");
                    }
                }
            } else {
                Origins.LOGGER.error("\"badges\" field in power \"" + powerId + "\" should be an array.");
            }
        }
    }

    public static void readAutoBadges(ResourceLocation powerId, ResourceLocation factoryId, boolean isSubPower, JsonObject json, PowerType<?> powerType) {
        if(BADGES.containsKey(powerId) || powerType.isHidden() || isSubPower) {
            // No auto-badges should be created if:
            // - The power has custom badges defined in the data
            // - The power is hidden
            // - The power is a sub-power
            return;
        }
        if(powerType instanceof MultiplePowerType<?> mp) {
            // Multiple powers retrieve their automatic badges from all sub-powers
            List<Badge> badgeList = BADGES.computeIfAbsent(powerId, id -> new LinkedList<>());
            mp.getSubPowers().stream().map(PowerTypeRegistry::get).forEach(subPowerType ->
                AutoBadgeCallback.EVENT.invoker().createAutoBadge(subPowerType.getIdentifier(), subPowerType, badgeList)
            );
        } else {
            AutoBadgeCallback.EVENT.invoker()
                .createAutoBadge(powerId, powerType, BADGES.computeIfAbsent(powerId, id -> new LinkedList<>()));
        }
    }

    public static void createAutoBadges(ResourceLocation powerId, PowerType<?> powerType, List<Badge> badgeList) {
        Power power = powerType.create(null);
        if(power instanceof Active active) {
            boolean toggle = active instanceof TogglePower || active instanceof ToggleNightVisionPower;
            ResourceLocation autoBadgeId = toggle ? TOGGLE_BADGE_ID : ACTIVE_BADGE_ID;
            if(REGISTRY.containsId(autoBadgeId)) {
                badgeList.add(REGISTRY.get(autoBadgeId));
            } else {
                badgeList.add(new KeybindBadge(toggle ? TOGGLE_BADGE_SPRITE : ACTIVE_BADGE_SPRITE,
                    toggle ? "origins.gui.badge.toggle"
                        : "origins.gui.badge.active"
                ));
            }
        } else if(power instanceof RecipePower recipePower) {
            Recipe<CraftingInput> recipe = recipePower.getRecipe();
            String type = recipe instanceof ShapedRecipe ? "shaped" : "shapeless";
            badgeList.add(new CraftingRecipeBadge(RECIPE_BADGE_SPRITE, recipe,
                Component.translatable("origins.gui.badge.recipe.crafting." + type), null
            ));
        }
    }
}
