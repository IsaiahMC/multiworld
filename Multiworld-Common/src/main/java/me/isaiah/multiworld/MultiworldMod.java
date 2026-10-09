/**
 * Multiworld Mod
 * Copyright (c) 2021-2025 by Isaiah.
 */
package me.isaiah.multiworld;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import me.isaiah.multiworld.command.CreateCommand;
import me.isaiah.multiworld.command.DifficultyCommand;

import me.isaiah.multiworld.command.IGameruleCommand;
import me.isaiah.multiworld.command.InfoCommand;

import me.isaiah.multiworld.command.PortalCommand;
import me.isaiah.multiworld.command.SetspawnCommand;
import me.isaiah.multiworld.command.SpawnCommand;
import me.isaiah.multiworld.command.TimeCommand;
import me.isaiah.multiworld.command.TpCommand;
import me.isaiah.multiworld.command.Util;
import me.isaiah.multiworld.perm.Perm;
import me.isaiah.multiworld.portal.Portal;
import multiworld.api.WorldFolderMode;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;

/**
 * Multiworld Mod
 */
public class MultiworldMod {
	
	public static final Logger LOGGER = LoggerFactory.getLogger("multiworld");

    public static final String MOD_ID = "multiworld";
    public static MinecraftServer mc;
    public static String CMD = "mw";
    public static ICreator world_creator;
    
    // Help Text For Command
    public static String[] COMMAND_HELP = {
    		"&4Multiworld Mod Commands:&r",
    		"&a/mw spawn&r - Teleport to current world spawn",
    		"&a/mw setspawn&r - Sets the current world spawn",
    		"&a/mw tp <id>&r - Teleport to a world",
    		"&a/mw list&r - List all worlds",
    		"&a/mw gamerule <rule> <value>&r - Change a worlds Gamerules",
    		"&a/mw create <id> <env> [-g=<generator> -s=<seed>]&r - create a new world",
    		"&a/mw difficulty <value> [world id] - Sets the difficulty of a world",
    		"&a/mw time <set|add|query> <time> [world id]&r - Change a world's time of day",
    		"&a/mw info [world id]&r - Show info about a world (players, time, weather, gamerules, spawn)"
    };

	// Multiworld Mod Version
	public static String VERSION = "VersionUnknown";

    public static void setICreator(ICreator ic) {
        world_creator = ic;
    }

    /**
     * Same as get_world_creator, as class also used for version support
     */
    public static ICreator versionSupport() {
    	return world_creator;
    }
    
    /**
     * Gets the Multiversion ICreator instance
     */
    public static ICreator get_world_creator() {
    	return world_creator;
    }

    public static ServerLevel createConfigAndWorld(String id, String dimStr, Identifier dimId, ChunkGenerator gen, Difficulty dif, long seed, String cgen, WorldFolderMode dirMode) {
    	// CreateCommand.make_config(new_id(id), dimStr, seed, cgen);
    	CreateCommand.makeConfigFile(new_id(id), dimStr, seed, cgen, dirMode);
    	return world_creator.create_world(id, dimId, gen, dif, seed);
    }
    
    public static ServerLevel create_world(String id, Identifier dim, ChunkGenerator gen, Difficulty dif, long seed) {
    	return world_creator.create_world(id, dim, gen, dif, seed);
    }

    /**
     * ModInitializer onInitialize
     * 
     * @see {@link me.isaiah.multiworld.fabric.MultiworldModFabric}
     */
    public static void init() {
    	// Load Translations
    	I18n.loadConfig();
    	try {
			I18n.save();
		} catch (IOException e) {
			e.printStackTrace();
		}

    	LOGGER.info("Multiworld Mod Init");

        //WandEventHandler.register();
    }

    public static Identifier new_id(String id) {
    	// tryParse works from 1.18 to 1.21+
    	return Identifier.tryParse(id);
    }

    // On server start
    public static void on_server_started(MinecraftServer mc) {
        MultiworldMod.mc = mc;
        
        // LOGGER.info("Registering events...");
        // WandEventHandler.register();
		
		File cfg_folder = new File("config");
		if (cfg_folder.exists()) {
			File folder = new File(cfg_folder, "multiworld");
			File worlds = new File(folder, "worlds");
			if (worlds.exists()) {
				for (File f : worlds.listFiles()) {
					if (f.getName().equals("minecraft")) {
						continue;
					}
					for (File fi : f.listFiles()) {
						String id = f.getName() + ":" + fi.getName().replace(".yml", "");
						LOGGER.info("Found legacy saved world " + id);
						CreateCommand.reinit_world_from_config(mc, id);
					}
				}
			}
		}
		
		List<Path> savedWorlds = Utils.searchForWorlds();
		for (Path dir : savedWorlds) {
			LOGGER.info("Loading a saved world from: " + dir.toString());
			Utils.loadSavedMultiworldWorld(mc, dir, Optional.empty());
		}
		
		if (cfg_folder.exists()) {
			int loaded = Portal.reinit_portals_from_config(mc);
			if (loaded > 0) {
				LOGGER.info("Found " + loaded + " saved world portals.");
			}

			// Verify each loaded portal's destination is synced; logs an ERROR per broken one.
			Portal.checkPortalsSync();
		}

    }

    public static ServerPlayer get_player(CommandSourceStack s) throws CommandSyntaxException {
    	ServerPlayer plr = s.getPlayer();
    	if (null == plr) {
    		// s.sendMessage(text_plain("Multiworld Mod for Minecraft " + mc.getVersion()));
    		// s.sendMessage(text_plain("These commands currently require a Player."));
    		
    		throw CommandSourceStack.ERROR_NOT_PLAYER.create();
    	}
    	return plr;
    }

   private static boolean isPlayer(CommandSourceStack s) {
    	try {
    		ServerPlayer plr = s.getPlayer();
    		if (null == plr) {
    			return false;
    		}
    	} catch (Exception ex) {
    		if (ex instanceof CommandSyntaxException) {
    			if (s.getTextName().equalsIgnoreCase("Server")) return false;
    		}
    	}
    	return true;
    }
    
    /**
     * <= >= 1.21.11 Compact.
     */
    public static boolean permissionLevel(CommandSourceStack source, int level) {
    	return Perm.permissionLevel(source, level);
    }

    private static String[] perms_list = {
    	"multiworld.cmd",
    	"multiworld.admin",
    	"multiworld.setspawn",
    	"multiworld.spawn",
    	"multiworld.gamerule",
    	"multiworld.difficulty",
    	"multiworld.time",
    	"multiworld.info",
    	"multiworld.tp",
    	"multiworld.create",
    	"multiworld.portal"
    };
    
    // On command register
    public static void register_commands(CommandDispatcher<CommandSourceStack> dispatcher) {
    	dispatcher.register(literal(CMD)
    			.requires(source -> {
    				// #if mc182
    				// if (net.fabricmc.loader.api.FabricLoader.getInstance().isDevelopmentEnvironment()) return true;
    				// #endif

    				try {
    					boolean has = Perm.has(get_player(source), "multiworld.cmd") ||
    							Perm.has(get_player(source), "multiworld.admin") || permissionLevel(source, 1);

    					if (has) {
    						return has;
    					}

    					for (String perm : perms_list) {
    						if (Perm.has(get_player(source), perm)) {
    							// Has Permission for at least one sub-command.
    							return true;
    						}
    					}
    					return has;
    				} catch (Exception e) {
    					e.printStackTrace();
    					return permissionLevel(source, 1);
    				}
    			}) 
    			.executes(ctx -> {
    				return broadcast(ctx.getSource(), ChatFormatting.AQUA, null);
    			})
    			.then(argument("message", greedyString()).suggests(new InfoSuggest())
    					.executes(ctx -> {
    						try {
    							return broadcast(ctx.getSource(), ChatFormatting.AQUA, getString(ctx, "message") );
    						} catch (Exception e) {
    							e.printStackTrace();
    							return 1;
    						}
    					}))); 
    }

    /**
     * Preprocessed method.
     */
    public static ServerLevel getWorldFor(Player plr) {
    	// #if mc218
    	// return (ServerWorld) plr.getWorld();
    	// #else
    	ServerLevel w = (ServerLevel) plr.level();
    	return w;
    	// #endif
    }

    public static int broadcast(CommandSourceStack source, ChatFormatting formatting, String message) throws CommandSyntaxException {
    	if (!isPlayer(source)) {
    		ConsoleCommand.broadcast_console(mc, source, message);
    		return 1;
    	}
    	
    	final ServerPlayer plr = get_player(source); // source.getPlayerOrThrow();

        if (null == message) {
            message(plr, "&bMultiworld Mod for Minecraft " + mc.getServerVersion());

            Level world = getWorldFor(plr);
            Identifier id = world.dimension().identifier();
            
            message(plr, "Currently in: " + id.toString());
            
            return 1;
        }

        boolean ALL = Perm.has(plr, "multiworld.admin");
        String[] args = message.split(" ");

        // Help Command
        if (args[0].equalsIgnoreCase("help")) {
            for (String s : COMMAND_HELP) {
            	message(plr, s);
            }
        }
        
        // Debug
        if (args[0].equalsIgnoreCase("debugtick")) {
        	ServerLevel w = getWorldFor(plr);
        	Identifier id = w.dimension().identifier();
        	message(plr, "World ID: " + id.toString());
        	message(plr, "Players : " + w.players().size());
        	w.tick(() -> true);
        }

        // SetSpawn Command
        if (args[0].equalsIgnoreCase("setspawn") && Perm.check(plr, "multiworld.setspawn")) {
            return SetspawnCommand.run(mc, plr, args);
        }

        // Spawn Command
        if (args[0].equalsIgnoreCase("spawn") && Perm.check(plr, "multiworld.spawn") ) {
            return SpawnCommand.run(mc, plr, args);
        }
        
        // Gamerule Command
        if (args[0].equalsIgnoreCase("gamerule") && Perm.check(plr, "multiworld.gamerule")) {
        	return Util.getGameruleCommand().run(mc, plr, args);
        }
        
        // Difficulty Command
        if (args[0].equalsIgnoreCase("difficulty") && Perm.check(plr, "multiworld.difficulty")) {
        	return DifficultyCommand.run(mc, plr, args);
        }

        // Time Command
        if (args[0].equalsIgnoreCase("time") && Perm.check(plr, "multiworld.time")) {
        	return TimeCommand.run(mc, plr, args);
        }

        // Info Command
        if (args[0].equalsIgnoreCase("info") && Perm.check(plr, "multiworld.info")) {
        	return InfoCommand.run(mc, plr, args);
        }

        // TP Command
        if (args[0].equalsIgnoreCase("tp") ) {
            if (!(ALL || Perm.has(plr, "multiworld.tp"))) {
                message(plr, "No permission! Missing permission: multiworld.tp");
                return 1;
            }
            if (args.length == 1) {
                message(plr, "Usage: /" + CMD + " tp <world>");
                return 0;
            }
            return TpCommand.run(mc, plr, args);
        }

        // List Command
        if (args[0].equalsIgnoreCase("list") ) {
            if (!(ALL || Perm.has(plr, "multiworld.cmd"))) {
                message(plr, "No permission! Missing permission: multiworld.cmd");
                return 1;
            }

            message(plr, "&bAll Worlds:");
            
            Level pworld = getWorldFor(plr);
            Identifier pwid = pworld.dimension().identifier();
            
            mc.getAllLevels().forEach(world -> {
            	Identifier id = world.dimension().identifier();
                String name = id.toString();
                if (name.startsWith("multiworld:")) name = name.replace("multiworld:", "");

                if (id.equals(pwid)) {
                	message(plr, "- " + name + " &a(Currently in)");
                } else {
                	message(plr, "- " + name);
                }
            });
        }

        // Version Command
        if (args[0].equalsIgnoreCase("version") && (ALL || Perm.has(plr, "multiworld.cmd")) ) {
            message(plr, "Multiworld Mod version " + VERSION);
            return 1;
        }

        // Create Command
        if (args[0].equalsIgnoreCase("create") ) {
            if (!(ALL || Perm.has(plr, "multiworld.create"))) {
                message(plr, "No permission! Missing permission: multiworld.create");
                return 1;
            }
            return CreateCommand.run(mc, plr, args);
        }
        
        // Delete Command
        if (args[0].equalsIgnoreCase("delete")) {
        	if (!ALL) {
                message(plr, "No permission! Missing permission: multiworld.admin");
                return 1;
            }
        	message(plr, "Delete Command is Console-only for security.");
        }
        
        // Help Command
        if (args[0].equalsIgnoreCase("portal")) {
        	if (!(ALL || Perm.has(plr, "multiworld.portal"))) {
                message(plr, "No permission! Missing permission: multiworld.portal");
                return 1;
            }
        	
        	PortalCommand.run(mc, plr, args);
        }

        return Command.SINGLE_SUCCESS; // Success
    }

	public static void message(Player player, String message) {
		try {
			// #if mc261 mc262 mc263
			// player.sendSystemMessage(Component.nullToEmpty(translate_alternate_color_codes('&', message)));
			// #else
			player.displayClientMessage(Component.nullToEmpty(translate_alternate_color_codes('&', message)), false);
			// #endif
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
	
	public static void message(CommandSourceStack s, String message) {
		try {
			ServerPlayer player = s.getPlayer();
			// #if mc261 mc262 mc263
			// player.sendSystemMessage(Component.nullToEmpty(translate_alternate_color_codes('&', message)), false);
			// #else
			player.displayClientMessage(Component.nullToEmpty(translate_alternate_color_codes('&', message)), false);
			// #endif
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

    private static final char COLOR_CHAR = '\u00A7';
    private static String translate_alternate_color_codes(char altColorChar, String textToTranslate) {
        char[] b = textToTranslate.toCharArray();
        for (int i = 0; i < b.length - 1; i++) {
            if (b[i] == altColorChar && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i+1]) > -1) {
                b[i] = COLOR_CHAR;
                b[i+1] = Character.toLowerCase(b[i+1]);
            }
        }
        return new String(b);
    }

	public static String text(String message) {
		return translate_alternate_color_codes('&', message);
	}

}