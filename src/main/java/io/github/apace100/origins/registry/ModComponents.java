package io.github.apace100.origins.registry;

import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.origins.Origins;
import io.github.apace100.origins.component.OriginComponent;
import io.github.apace100.origins.component.PlayerOriginComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

public class ModComponents implements EntityComponentInitializer {

    public static final ComponentKey<OriginComponent> ORIGIN;

    static {
        ORIGIN = ComponentRegistry.getOrCreate(Identifier.fromNamespaceAndPath(Origins.MODID, "origin"), OriginComponent.class);
    }

    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.beginRegistration(Player.class, ORIGIN).after(PowerHolderComponent.KEY).respawnStrategy(RespawnCopyStrategy.CHARACTER).end(PlayerOriginComponent::new);
    }
}
