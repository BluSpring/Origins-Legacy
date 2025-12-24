package io.github.apace100.origins.content;

import io.github.apace100.origins.Origins;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.component.OriginTargetsComponent;
import io.github.apace100.origins.networking.OpenOriginScreenPacket;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.origin.OriginRegistry;
import io.github.apace100.origins.registry.ModComponents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class OrbOfOriginItem extends Item {

    public OrbOfOriginItem() {
        super(new Item.Properties().stacksTo(1).rarity(Rarity.RARE).setId(ResourceKey.create(Registries.ITEM, Origins.identifier("orb_of_origin"))));
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        if(!world.isClientSide()) {
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
            ServerPlayNetworking.send((ServerPlayer) user, new OpenOriginScreenPacket(false));
        }
        if(!user.isCreative()) {
            stack.shrink(1);
        }
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        Map<OriginLayer, Origin> targets = getTargets(stack);
        for(Map.Entry<OriginLayer, Origin> target : targets.entrySet()) {
            if(target.getValue() == Origin.EMPTY) {
                tooltipAdder.accept(Component.translatable("item.origins.orb_of_origin.layer_generic",
                    Component.translatable(target.getKey().getTranslationKey())).withStyle(ChatFormatting.GRAY));
            } else {
                tooltipAdder.accept(Component.translatable("item.origins.orb_of_origin.layer_specific",
                    Component.translatable(target.getKey().getTranslationKey()),
                    target.getValue().getName()).withStyle(ChatFormatting.GRAY));
            }
        }
    }

    private Map<OriginLayer, Origin> getTargets(ItemStack stack) {
        HashMap<OriginLayer, Origin> targets = new HashMap<>();
        if(!stack.has(OriginTargetsComponent.TYPE)) {
            return targets;
        }
        var targetList = stack.get(OriginTargetsComponent.TYPE).targets();
        for (OriginTargetsComponent.OriginTarget target : targetList) {
            OriginLayer layer = OriginLayers.getLayer(target.layer());
            Origin origin = target.origin().map(OriginRegistry::get).orElse(Origin.EMPTY);

            if (layer.isEnabled() && (layer.contains(origin) || origin.isSpecial())) {
                targets.put(layer, origin);
            }
        }
        return targets;
    }
}
