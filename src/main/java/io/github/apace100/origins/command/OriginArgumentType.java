package io.github.apace100.origins.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.apace100.origins.origin.Origin;
import io.github.apace100.origins.origin.OriginLayer;
import io.github.apace100.origins.origin.OriginLayers;
import io.github.apace100.origins.origin.OriginRegistry;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class OriginArgumentType implements ArgumentType<Identifier> {

   public static final DynamicCommandExceptionType ORIGIN_NOT_FOUND = new DynamicCommandExceptionType(
       o -> Component.translatable("commands.origin.origin_not_found", o)
   );

   public static OriginArgumentType origin() {
      return new OriginArgumentType();
   }

   public Identifier parse(StringReader stringReader) throws CommandSyntaxException {
      return Identifier.read(stringReader);
   }

   public static Origin getOrigin(CommandContext<CommandSourceStack> context, String argumentName) throws CommandSyntaxException {

      Identifier id = context.getArgument(argumentName, Identifier.class);

      try {
         return OriginRegistry.get(id);
      }

      catch(IllegalArgumentException e) {
         throw ORIGIN_NOT_FOUND.create(id);
      }

   }

   @Override
   public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {

      List<Identifier> availableOrigins = new ArrayList<>();

      try {
          Identifier originLayerId = context.getArgument("layer", Identifier.class);
          OriginLayer originLayer = OriginLayers.getLayer(originLayerId);

          availableOrigins.add(Origin.EMPTY.getIdentifier());
          if (originLayer != null) availableOrigins.addAll(originLayer.getOrigins());
      }

      catch(IllegalArgumentException ignored) {}

      return SharedSuggestionProvider.suggestResource(availableOrigins.stream(), builder);

   }

}