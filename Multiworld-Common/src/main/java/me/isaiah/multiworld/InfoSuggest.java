package me.isaiah.multiworld;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import me.isaiah.multiworld.command.GameruleCommand;
import me.isaiah.multiworld.perm.Perm;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.world.World;
import net.minecraft.world.level.ServerWorldProperties;

public class InfoSuggest implements SuggestionProvider<ServerCommandSource> {

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        builder = builder.createOffset(builder.getInput().lastIndexOf(' ') + 1);

        String input = builder.getInput();
        String[] cmds = input.split(" ");

        ServerCommandSource plr = context.getSource();
        boolean ALL = Perm.has(plr, "multiworld.admin");

        if (cmds.length <= 1 || (cmds.length <= 2 && !input.endsWith(" "))) {
            String[] subcommands = {"tp", "list", "version", "create", "spawn", "setspawn", "gamerule"};
            for (String s : subcommands) {
                builder.suggest(s);
            }
            return builder.buildFuture();
        }
        
        if (cmds.length <= 2 || (cmds.length <= 3 && !input.endsWith(" "))) {
            if (cmds[1].equalsIgnoreCase("tp") && (ALL || Perm.has(plr, "multiworld.tp"))) {
                MinecraftServer mc = MultiworldMod.mc;
                List<String> names = new ArrayList<>();
                mc.getWorlds().forEach(world -> {
                    String name = ((ServerWorldProperties) world.getLevelProperties()).getLevelName();
                    if (names.contains(name)) {
                        if (world.getRegistryKey() == World.NETHER) name = name + "_nether";
                        if (world.getRegistryKey() == World.END) name = name + "_the_end";
                    }
                });
                mc.getWorldRegistryKeys().forEach(r -> {
                    String val = r.getValue().toString();
                    if (val.startsWith("multiworld:"))
                        val = val.replace("multiworld:", "");
                    names.add(val);
                 });
                for (String s : names) builder.suggest(s);
            }
            
            if (cmds[1].equalsIgnoreCase("create") && (ALL || Perm.has(plr, "multiworld.create"))) {
                builder.suggest("myid:myvalue");
                return builder.buildFuture();
            }
            
            if (cmds[1].equalsIgnoreCase("gamerule") && (ALL || Perm.has(plr, "multiworld.gamerule"))) {
                if (GameruleCommand.keys.size() == 0) {
                	GameruleCommand.setup();
                }
                
                System.out.println("INPUT: " + input);
                
                String last = input.substring(input.lastIndexOf(' ')).trim();

                for (String name : GameruleCommand.keys.keySet()) {
                	if (name.startsWith(last) || last.contains("gamerule") || name.toLowerCase().contains(last)) {
                		builder.suggest(name);
                	}
                }
                
            	//for (String name : GameruleCommand.keys.keySet()) {
            		// builder.suggest(name);
                //}
            	
            	//builder.suggest("myid:myvalue");
                return builder.buildFuture();
            }
        }

        if (cmds.length <= 3 || (cmds.length <= 4 && !input.endsWith(" "))) {
            if (cmds[1].equalsIgnoreCase("create") && (ALL || Perm.has(plr, "multiworld.create")) ) {
                builder.suggest("NORMAL");
                builder.suggest("NETHER");
                builder.suggest("END");
            }
            
            if (cmds[1].equalsIgnoreCase("gamerule") && (ALL || Perm.has(plr, "multiworld.gamerule")) ) {
                // TODO: IntRules
            	builder.suggest("true");
                builder.suggest("false");
            }
        }
        
        

        return builder.buildFuture();
    }

}