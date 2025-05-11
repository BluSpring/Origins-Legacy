package io.github.apace100.origins.component;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.origin.OriginRegistry;
import io.github.apace100.origins.util.ChoseOriginCriterion;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class PlayerOriginComponent implements OriginComponent {

    private Player player;
    private HashMap<OriginLayer, Origin> origins = new HashMap<>();

    private boolean hadOriginBefore = false;

    public PlayerOriginComponent(Player player) {
        this.player = player;
    }

    @Override
    public boolean hasAllOrigins() {
        return OriginLayers.getLayers().stream().allMatch(layer -> {
            return !layer.isEnabled() || layer.getOrigins(player).size() == 0 || (origins.containsKey(layer) && origins.get(layer) != null && origins.get(layer) != Origin.EMPTY);
        });
    }

    @Override
    public HashMap<OriginLayer, Origin> getOrigins() {
        return origins;
    }

    @Override
    public boolean hasOrigin(OriginLayer layer) {
        return origins != null && origins.containsKey(layer) && origins.get(layer) != null && origins.get(layer) != Origin.EMPTY;
    }

    @Override
    public Origin getOrigin(OriginLayer layer) {
        if(!origins.containsKey(layer)) {
            return null;
        }
        return origins.get(layer);
    }

    @Override
    public boolean hadOriginBefore() {
        return hadOriginBefore;
    }

    @Override
    public void setOrigin(OriginLayer layer, Origin origin) {
        Origin oldOrigin = getOrigin(layer);
        if(oldOrigin == origin) {
            return;
        }
        this.origins.put(layer, origin);
        PowerHolderComponent powerComponent = PowerHolderComponent.KEY.get(player);
        grantPowersFromOrigin(origin, powerComponent);
        if(oldOrigin != null) {
            powerComponent.removeAllPowersFromSource(oldOrigin.getIdentifier());
        }
        if(this.hasAllOrigins()) {
            this.hadOriginBefore = true;
        }
        if(player instanceof ServerPlayer spe) {
            ChoseOriginCriterion.INSTANCE.trigger(spe, origin);
        }
    }

    private void grantPowersFromOrigin(Origin origin, PowerHolderComponent powerComponent) {
        ResourceLocation source = origin.getIdentifier();
        for(PowerType<?> powerType : origin.getPowerTypes()) {
            if(!powerComponent.hasPower(powerType, source)) {
                powerComponent.addPower(powerType, source);
            }
        }
    }

    private void revokeRemovedPowers(Origin origin, PowerHolderComponent powerComponent) {
        ResourceLocation source = origin.getIdentifier();
        List<PowerType<?>> powersByOrigin = powerComponent.getPowersFromSource(source);
        powersByOrigin.stream().filter(p -> !origin.hasPowerType(p)).forEach(p -> powerComponent.removePower(p, source));
    }

    @Override
    public void readFromNbt(CompoundTag compoundTag) {
        if(player == null) {
            Origins.LOGGER.error("Player was null in `fromTag`! This is a bug!");
        }

        this.origins.clear();

        if(compoundTag.contains("Origin")) {
            try {
                OriginLayer defaultOriginLayer = OriginLayers.getLayer(new ResourceLocation(Origins.MODID, "origin"));
                this.origins.put(defaultOriginLayer, OriginRegistry.get(ResourceLocation.tryParse(compoundTag.getString("Origin"))));
            } catch(IllegalArgumentException e) {
                Origins.LOGGER.warn("Player " + player.getDisplayName().getContents() + " had old origin which could not be migrated: " + compoundTag.getString("Origin"));
            }
        } else {
            ListTag originLayerList = (ListTag) compoundTag.get("OriginLayers");
            if(originLayerList != null) {
                for(int i = 0; i < originLayerList.size(); i++) {
                    CompoundTag layerTag = originLayerList.getCompound(i);
                    ResourceLocation layerId = ResourceLocation.tryParse(layerTag.getString("Layer"));
                    OriginLayer layer = null;
                    try {
                        layer = OriginLayers.getLayer(layerId);
                    } catch(IllegalArgumentException e) {
                        Origins.LOGGER.warn("Could not find origin layer with id " + layerId.toString() + ", which existed on the data of player " + player.getDisplayName().getContents() + ".");
                    }
                    if(layer != null) {
                        ResourceLocation originId = ResourceLocation.tryParse(layerTag.getString("Origin"));
                        Origin origin = null;
                        try {
                            origin = OriginRegistry.get(originId);
                        } catch(IllegalArgumentException e) {
                            Origins.LOGGER.warn("Could not find origin with id " + originId.toString() + ", which existed on the data of player " + player.getDisplayName().getContents() + ".");
                            PowerHolderComponent powerComponent = PowerHolderComponent.KEY.get(player);
                            powerComponent.removeAllPowersFromSource(originId);
                        }
                        if(origin != null) {
                            if(!layer.contains(origin) && !origin.isSpecial()) {
                                Origins.LOGGER.warn("Origin with id " + origin.getIdentifier().toString() + " is not in layer " + layer.getIdentifier().toString() + " and is not special, but was found on " + player.getDisplayName().getContents() + ", setting to EMPTY.");
                                origin = Origin.EMPTY;
                                PowerHolderComponent powerComponent = PowerHolderComponent.KEY.get(player);
                                powerComponent.removeAllPowersFromSource(originId);
                            }
                            this.origins.put(layer, origin);
                        }
                    }
                }
            }
        }
        this.hadOriginBefore = compoundTag.getBoolean("HadOriginBefore");

        if(!player.level().isClientSide) {
            PowerHolderComponent powerComponent = PowerHolderComponent.KEY.get(player);
            for(Origin origin : origins.values()) {
                // Grants powers only if the player doesn't have them yet from the specific Origin source.
                // Needed in case the origin was set before the update to Apoli happened.
                grantPowersFromOrigin(origin, powerComponent);
            }
            for(Origin origin : origins.values()) {
                revokeRemovedPowers(origin, powerComponent);
            }

            // Compatibility with old worlds:
            // Loads power data from Origins tag, whereas new versions
            // store the data in the Apoli tag.
            if(compoundTag.contains("Powers")) {
                ListTag powerList = (ListTag) compoundTag.get("Powers");
                for(int i = 0; i < powerList.size(); i++) {
                    CompoundTag powerTag = powerList.getCompound(i);
                    ResourceLocation powerTypeId = ResourceLocation.tryParse(powerTag.getString("Type"));
                    try {
                        PowerType<?> type = PowerTypeRegistry.get(powerTypeId);
                        if(powerComponent.hasPower(type)) {
                            Tag data = powerTag.get("Data");
                            try {
                                powerComponent.getPower(type).fromTag(data);
                            } catch(ClassCastException e) {
                                // Occurs when power was overriden by data pack since last world load
                                // to be a power type which uses different data class.
                                Origins.LOGGER.warn("Data type of \"" + powerTypeId + "\" changed, skipping data for that power on player " + player.getName().getContents());
                            }
                        }
                    } catch(IllegalArgumentException e) {
                        Origins.LOGGER.warn("Power data of unregistered power \"" + powerTypeId + "\" found on player, skipping...");
                    }
                }
            }
        }
    }

    @Override
    public void onPowersRead() {
        // NO-OP
    }

    @Override
    public void writeToNbt(CompoundTag compoundTag) {
        ListTag originLayerList = new ListTag();
        for(Map.Entry<OriginLayer, Origin> entry : origins.entrySet()) {
            CompoundTag layerTag = new CompoundTag();
            layerTag.putString("Layer", entry.getKey().getIdentifier().toString());
            layerTag.putString("Origin", entry.getValue().getIdentifier().toString());
            originLayerList.add(layerTag);
        }
        compoundTag.put("OriginLayers", originLayerList);
        compoundTag.putBoolean("HadOriginBefore", this.hadOriginBefore);
    }

    @Override
    public void sync() {
        OriginComponent.sync(this.player);
    }
}
