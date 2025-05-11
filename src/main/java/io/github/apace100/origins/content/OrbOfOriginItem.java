package io.github.apace100.origins.content;

import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.networking.ModPackets;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.origin.OriginRegistry;
import io.github.apace100.origins.registry.ModComponents;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrbOfOriginItem extends Item {

    public OrbOfOriginItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.RARE));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        if(!world.isClientSide) {
            OriginComponent component = ModComponents.ORIGIN.get(user);
            Map<OriginLayer, Origin> targets = getTargets(stack);
            if(targets.size() > 0) {
                for(Map.Entry<OriginLayer, Origin> target : targets.entrySet()) {
                    component.setOrigin(target.getKey(), target.getValue());
                }
            } else {
                for (OriginLayer layer : OriginLayers.getLayers()) {
                    if(layer.isEnabled()) {
                        component.setOrigin(layer, Origin.EMPTY);
                    }
                }
            }
            component.checkAutoChoosingLayers(user, false);
            component.sync();
            FriendlyByteBuf data = new FriendlyByteBuf(Unpooled.buffer());
            data.writeBoolean(false);
            ServerPlayNetworking.send((ServerPlayer) user, ModPackets.OPEN_ORIGIN_SCREEN, data);
        }
        if(!user.isCreative()) {
            stack.shrink(1);
        }
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag context) {
        Map<OriginLayer, Origin> targets = getTargets(stack);
        for(Map.Entry<OriginLayer, Origin> target : targets.entrySet()) {
            if(target.getValue() == Origin.EMPTY) {
                tooltip.add(Component.translatable("item.origins.orb_of_origin.layer_generic",
                    Component.translatable(target.getKey().getTranslationKey())).withStyle(ChatFormatting.GRAY));
            } else {
                tooltip.add(Component.translatable("item.origins.orb_of_origin.layer_specific",
                    Component.translatable(target.getKey().getTranslationKey()),
                    target.getValue().getName()).withStyle(ChatFormatting.GRAY));
            }
        }
    }

    private Map<OriginLayer, Origin> getTargets(ItemStack stack) {
        HashMap<OriginLayer, Origin> targets = new HashMap<>();
        if(!stack.hasTag()) {
            return targets;
        }
        CompoundTag nbt = stack.getTag();
        if(!nbt.contains("Targets", NbtType.LIST)) {
            return targets;
        }
        ListTag targetList = (ListTag)nbt.get("Targets");
        for (Tag nbtElement : targetList) {
            if(nbtElement instanceof CompoundTag targetNbt) {
                if(targetNbt.contains("Layer", NbtType.STRING)) {
                    try {
                        ResourceLocation id = new ResourceLocation(targetNbt.getString("Layer"));
                        OriginLayer layer = OriginLayers.getLayer(id);
                        Origin origin = Origin.EMPTY;
                        if(targetNbt.contains("Origin", NbtType.STRING)) {
                            ResourceLocation originId = new ResourceLocation(targetNbt.getString("Origin"));
                            origin = OriginRegistry.get(originId);
                        }
                        if(layer.isEnabled() && (layer.contains(origin) || origin.isSpecial())) {
                            targets.put(layer, origin);
                        }
                    } catch (Exception e) {
                        // no op
                    }
                }
            }
        }
        return targets;
    }
}
