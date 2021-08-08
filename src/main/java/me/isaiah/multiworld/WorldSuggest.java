package me.isaiah.multiworld;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.world.World;
import net.minecraft.world.level.ServerWorldProperties;

public class WorldSuggest implements SuggestionProvider<ServerCommandSource> {

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        MinecraftServer mc = MultiworldMod.mc;
        List<String> names = new ArrayList<>();
        mc.getWorlds().forEach(world -> {
            String name = ((ServerWorldProperties) world.getLevelProperties()).getLevelName();
            if (names.contains(name)) {
                if (world.getRegistryKey() == World.NETHER) name = name + "_nether";
                if (world.getRegistryKey() == World.END) name = name + "_the_end";
            }
            names.add(name);
            builder.suggest(name);
        });
        
        mc.getWorldRegistryKeys().forEach(r -> {
            builder.suggest(r.getValue().toString());
         });

        return builder.buildFuture();
    }

}
