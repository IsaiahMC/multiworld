package me.isaiah.multiworld;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import me.isaiah.multiworld.command.CreateCommand;
import me.isaiah.multiworld.command.GameruleCommand;
import me.isaiah.multiworld.perm.Perm;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.world.World;
import net.minecraft.world.level.ServerWorldProperties;

/**
 * Our Implementation of a command SuggestionProvider.
 */
public class InfoSuggest implements SuggestionProvider<ServerCommandSource> {

	/**
	 * Valid Difficulty Arguments
	 */
	public static String[] diff_names = {
			"PEACEFUL", "EASY", "NORMAL", "HARD"
	};

	/**
	 * Valid Subcommands
	 */
	private static String[] subcommands = {
			"tp", "list", "version", "create", "spawn", "setspawn", "gamerule", "help", "difficulty",
			// TODO: Add: delete, load, unload, info, clone, who, import
	};
	
	/**
	 * Build our Suggestion list
	 */
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        builder = builder.createOffset(builder.getInput().lastIndexOf(' ') + 1);

        String input = builder.getInput();
        String[] cmds = input.split(" ");

        ServerCommandSource plr = context.getSource();
        boolean ALL = Perm.has(plr, "multiworld.admin");

        if (cmds.length <= 1 || (cmds.length <= 2 && !input.endsWith(" "))) {
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
                	GameruleCommand.setupServer(MultiworldMod.mc);
                }
                
                String last = input.substring(input.lastIndexOf(' ')).trim();

                for (String name : GameruleCommand.keys.keySet()) {
                	if (name.startsWith(last) || last.contains("gamerule") || name.toLowerCase().contains(last)) {
                		builder.suggest(name);
                	}
                }
                return builder.buildFuture();
            }
            
            if (cmds[1].equalsIgnoreCase("difficulty") && (ALL || Perm.has(plr, "multiworld.difficulty"))) {
            	String last = input.substring(input.lastIndexOf(' ')).trim();
            	for (String name : diff_names) {
                 	if (name.startsWith(last) || last.contains("difficulty") || name.toLowerCase().contains(last)) {
                 		builder.suggest(name);
                 	}
                }
                return builder.buildFuture();
            }
        }

        if (cmds.length <= 3 || (cmds.length <= 4 && !input.endsWith(" "))) {
            if (cmds[1].equalsIgnoreCase("create") && (ALL || Perm.has(plr, "multiworld.create")) ) {
                builder.suggest("NORMAL");
                builder.suggest("NETHER");
                builder.suggest("END");
                return builder.buildFuture();
            }
            
            if (cmds[1].equalsIgnoreCase("gamerule") && (ALL || Perm.has(plr, "multiworld.gamerule")) ) {
                // TODO: IntRules
            	builder.suggest("true");
                builder.suggest("false");
            }
            
            if (cmds[1].equalsIgnoreCase("difficulty") && (ALL || Perm.has(plr, "multiworld.difficulty")) ) {
                // TODO: IntRules
            	ArrayList<String> names = new ArrayList<>();
            	MultiworldMod.mc.getWorldRegistryKeys().forEach(r -> {
                    String val = r.getValue().toString();
                    if (val.startsWith("multiworld:")) {
                        val = val.replace("multiworld:", "");
                    }
                    names.add(val);
                    //builder.suggest(val);
                 });
                for (String s : names) builder.suggest(s);
            }
        }

        // Create command optional arguments
        // -g=GENERATOR
        // -s=SEED
        
        int ccoA = 4;
        int ccoB = 5;

        if (cmds.length <= ccoA || (cmds.length <= ccoB && !input.endsWith(" "))) {
        	if (cmds[1].equalsIgnoreCase("create") && (ALL || Perm.has(plr, "multiworld.create")) ) {
        		if (cmds.length <= ccoA) {
        			builder.suggest("-g=<GENERATOR>");
        			builder.suggest("-s=<SEED>");
        			return builder.buildFuture();
        		}

        		if (cmds.length == ccoB) {
        			if (cmds[ccoA].startsWith("-s=")) {
        				builder.suggest("-s=1234");
        				builder.suggest("-s=RANDOM");
        			} else if (cmds[ccoA].startsWith("-g=")) {
	        			builder.suggest("-g=NORMAL");
	        			builder.suggest("-g=FLAT");
	        			builder.suggest("-g=VOID");

	        			for (String key : CreateCommand.customs.keySet()) {
	        				builder.suggest("-g=" + key.toUpperCase(Locale.ROOT));
	        			}
	        		} else {
	        			if (cmds[4].startsWith("-")) {
	        				builder.suggest("-g=<GENERATOR>");
	        				builder.suggest("-s=<SEED>");
	        			}
	        		}
        		}
        	}
        }
        

        return builder.buildFuture();
    }

}