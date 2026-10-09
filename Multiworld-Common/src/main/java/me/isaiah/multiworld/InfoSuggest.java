package me.isaiah.multiworld;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import me.isaiah.multiworld.command.CreateCommand;
import me.isaiah.multiworld.command.IGameruleCommand;
import me.isaiah.multiworld.command.PortalCommand;
import me.isaiah.multiworld.command.Util;
import me.isaiah.multiworld.perm.Perm;
import multiworld.api.WorldFolderMode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ServerLevelData;

/**
 * Our Implementation of a command SuggestionProvider.
 */
public class InfoSuggest implements SuggestionProvider<CommandSourceStack> {

	/**
	 * Valid Difficulty Arguments
	 */
	public static String[] diff_names = {
			"PEACEFUL", "EASY", "NORMAL", "HARD"
	};

	/**
	 * Valid Time Actions
	 */
	public static String[] time_actions = {
			"set", "add", "query"
	};

	/**
	 * Valid Subcommands
	 */
	private static String[] subcommands = {
			"tp", "list", "version", "create", "spawn", "setspawn", "gamerule", "help", "difficulty", "time", "info", "portal"
			// TODO: Add: delete, load, unload, info, clone, who, import
	};
	
	/**
	 * Build our Suggestion list
	 */
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        builder = builder.createOffset(builder.getInput().lastIndexOf(' ') + 1);

        String input = builder.getInput();
        String[] cmds = input.trim().split(" ");

        CommandSourceStack plr = context.getSource();
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
                mc.getAllLevels().forEach(world -> {
                    String name = ((ServerLevelData) world.getLevelData()).getLevelName();
                    if (names.contains(name)) {
                        if (world.dimension() == Level.NETHER) name = name + "_nether";
                        if (world.dimension() == Level.END) name = name + "_the_end";
                    }
                });
                mc.levelKeys().forEach(r -> {
                    String val = r.identifier().toString();
                    if (val.startsWith("multiworld:"))
                        val = val.replace("multiworld:", "");
                    names.add(val);
                 });
                for (String s : names) builder.suggest(s);
            }

            if (cmds[1].equalsIgnoreCase("gamerule") && (ALL || Perm.has(plr, "multiworld.gamerule"))) {
                
            	IGameruleCommand gameruleCommand = Util.getGameruleCommand();

            	if (gameruleCommand == null) {
            		builder.suggest("IGameruleCommand implementation does not exist");
            		return builder.buildFuture();
            	}

            	gameruleCommand.initRulesMapIfNeeded(MultiworldMod.mc);
                
                String last = input.substring(input.lastIndexOf(' ')).trim();

                for (String name : gameruleCommand.getKeys()) {
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
            
            if (cmds[1].equalsIgnoreCase("time") && (ALL || Perm.has(plr, "multiworld.time"))) {
            	String last = input.substring(input.lastIndexOf(' ')).trim();
            	for (String name : time_actions) {
                 	if (name.startsWith(last) || last.contains("time") || name.toLowerCase().contains(last)) {
                 		builder.suggest(name);
                 	}
                }
                return builder.buildFuture();
            }

            if (cmds[1].equalsIgnoreCase("info") && (ALL || Perm.has(plr, "multiworld.info"))) {
            	for (String s : getWorldNames()) builder.suggest(s);
            	return builder.buildFuture();
            }

            if (cmds[1].equalsIgnoreCase("portal")) {
            	for (String s : PortalCommand.SUBCOMMANDS) {
                    builder.suggest(s);
                }
            	return builder.buildFuture();
            }
        }

        if (cmds.length <= 3 || (cmds.length <= 4 && !input.endsWith(" "))) {
            if (cmds[1].equalsIgnoreCase("gamerule") && (ALL || Perm.has(plr, "multiworld.gamerule")) ) {
                // TODO: IntRules
            	builder.suggest("true");
                builder.suggest("false");
            }
            
            if (cmds[1].equalsIgnoreCase("difficulty") && (ALL || Perm.has(plr, "multiworld.difficulty")) ) {
            	ArrayList<String> names = new ArrayList<>();
            	MultiworldMod.mc.levelKeys().forEach(r -> {
                    String val = r.identifier().toString();
                    if (val.startsWith("multiworld:")) {
                        val = val.replace("multiworld:", "");
                    }
                    names.add(val);
                 });
                for (String s : names) builder.suggest(s);
            }

            // Time value (argument 3): query types or named times.
            if (cmds[1].equalsIgnoreCase("time") && (ALL || Perm.has(plr, "multiworld.time")) ) {
            	if (cmds[2].equalsIgnoreCase("query")) {
            		builder.suggest("daytime");
            		builder.suggest("gametime");
            		builder.suggest("day");
            	} else {
            		builder.suggest("day");
            		builder.suggest("noon");
            		builder.suggest("night");
            		builder.suggest("midnight");
            	}
            }
        }

        // Time world id (argument 4)
        if (cmds[1].equalsIgnoreCase("time") && (ALL || Perm.has(plr, "multiworld.time"))
        		&& (cmds.length <= 4 || (cmds.length <= 5 && !input.endsWith(" "))) && cmds.length >= 4) {
        	for (String s : getWorldNames()) builder.suggest(s);
        }

        // Create Command
        if (cmds[1].equalsIgnoreCase("create") && (ALL || Perm.has(plr, "multiworld.create")) ) {
        	getSuggestions_CreateCommand(builder, input, cmds, plr, ALL);
        }
        
        // Portal Command
        if (cmds[1].equalsIgnoreCase("portal")) {
        	PortalCommand.getSuggestions_PortalCommand(builder, input, cmds, plr, ALL);
        }

        return builder.buildFuture();
    }
    
    public static List<String> getWorldNames() {
    	 MinecraftServer mc = MultiworldMod.mc;
         List<String> names = new ArrayList<>();
         mc.getAllLevels().forEach(world -> {
             String name = ((ServerLevelData) world.getLevelData()).getLevelName();
             if (names.contains(name)) {
                 if (world.dimension() == Level.NETHER) name = name + "_nether";
                 if (world.dimension() == Level.END) name = name + "_the_end";
             }
         });
         mc.levelKeys().forEach(r -> {
             String val = r.identifier().toString();
             if (val.startsWith("multiworld:"))
                 val = val.replace("multiworld:", "");
             names.add(val);
          });
         return names;
    }

    /**
     * Create Command, "/mw Create"
     * 
     * "/mw create <id> <env> [-g=<generator> -s=<seed>]"
     */
    public void getSuggestions_CreateCommand(SuggestionsBuilder builder, String input, String[] cmds, CommandSourceStack plr, boolean ALL) {
    	if ( !(ALL || Perm.has(plr, "multiworld.create")) ) return; // No Permission

    	// Argument 1: <id>
    	if (cmds.length <= 2 || (cmds.length <= 3 && !input.endsWith(" "))) {
    		builder.suggest("myworldname");
    		return;
    	}
    	
    	// Argument 2: <env>
    	if (cmds.length <= 3 || (cmds.length <= 4 && !input.endsWith(" ")) ) {
    		builder.suggest("NORMAL");
    		builder.suggest("NETHER");
    		builder.suggest("END");
    		return;
    	}

    	// Optional Arguments
    	int maxDebug = 7;
        if (cmds.length <= 4 || (cmds.length <= maxDebug && !input.endsWith(" "))) {
        	if (cmds.length <= 4) {
        		builder.suggest("-g=<GENERATOR>");
        		builder.suggest("-s=<SEED>");
        		builder.suggest("-m=<WorldSaveMode>");
        		return;
        	}

        	int n = 4 - 1;
        	String current = cmds[cmds.length - 1];
        	String[] beforeCurrent = Arrays.copyOfRange(cmds, n + 1, cmds.length - 1);
        	String beforeStr = String.join(" " , beforeCurrent);

        	if (current.startsWith("-s=")) {
        		builder.suggest("-s=1234");
        		builder.suggest("-s=RANDOM");
        	} else if (current.startsWith("-g=")) {
	        	builder.suggest("-g=NORMAL");
	        	builder.suggest("-g=FLAT");
	        	builder.suggest("-g=VOID");

	        	for (String key : CreateCommand.customs.keySet()) {
	        		builder.suggest("-g=" + key.toUpperCase(Locale.ROOT));
	        	}
        	} else if (current.startsWith("-m=")) {
        		// builder.suggest("-m=VANILLA");
        		
        		for (WorldFolderMode mode : WorldFolderMode.values()) {
        			builder.suggest("-m=" + mode);
        		}
        		
	        } else {
	        	if (current.startsWith("-")) {
	        		if (!beforeStr.contains("-g=")) builder.suggest("-g=<GENERATOR>");
	        		if (!beforeStr.contains("-s=")) builder.suggest("-s=<SEED>");
	        		if (!beforeStr.contains("-m=")) builder.suggest("-m=<WorldSaveMode>");
	        	}
	        }
        }
    }

}