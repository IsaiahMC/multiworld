/**
 * Multiworld Mod
 * Copyright (c) 2021-2024 by Isaiah.
 */
package me.isaiah.multiworld;

import com.mojang.brigadier.CommandDispatcher;
import java.io.File;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import me.isaiah.multiworld.command.CreateCommand;
import me.isaiah.multiworld.command.DifficultyCommand;
import me.isaiah.multiworld.command.GameruleCommand;
import me.isaiah.multiworld.command.SetspawnCommand;
import me.isaiah.multiworld.command.SpawnCommand;
import me.isaiah.multiworld.command.TpCommand;
import me.isaiah.multiworld.perm.Perm;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;

/**
 * Multiworld Mod
 */
public class MultiworldMod {

    public static final String MOD_ID = "multiworld";
    public static MinecraftServer mc;
    public static String CMD = "mw";
    public static ICreator world_creator;

	// Mod Version
	public static final String VERSION = "1.8";

    public static void setICreator(ICreator ic) {
        world_creator = ic;
    }
    
    public static ICreator get_world_creator() {
    	return world_creator;
    }

    public static ServerWorld create_world(String id, Identifier dim, ChunkGenerator gen, Difficulty dif, long seed) {
    	return world_creator.create_world(id, dim, gen, dif, seed);
    }

    // On mod init
    public static void init() {
        System.out.println(" Multiworld init");
    }
    
    public static Identifier new_id(String id) {
    	/*if (id.indexOf(':') != -1) {
    		String[] spl = id.split(Pattern.quote(":"));
    		return Identifier.of(spl[0], spl[1]);
    	}
    	return Identifier.of("minecraft", id);*/
    	
    	// tryParse works from 1.18 to 1.21
    	return Identifier.tryParse(id);
    	
    	// return new Identifier(id);
    }

    // On server start
    public static void on_server_started(MinecraftServer mc) {
        MultiworldMod.mc = mc;
		
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
						System.out.println("Found saved world " + id);
						CreateCommand.reinit_world_from_config(mc, id);
					}
				}
			}
		}
    }
    
    public static ServerPlayerEntity get_player(ServerCommandSource s) throws CommandSyntaxException {
    	ServerPlayerEntity plr = s.getPlayer();
    	if (null == plr) {
    		// s.sendMessage(text_plain("Multiworld Mod for Minecraft " + mc.getVersion()));
    		// s.sendMessage(text_plain("These commands currently require a Player."));
    		
    		throw ServerCommandSource.REQUIRES_PLAYER_EXCEPTION.create();
    	}
    	return plr;
    }

    // On command register
    public static void register_commands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal(CMD)
                    .requires(source -> {
                        try {
                            return source.hasPermissionLevel(1) || Perm.has(get_player(source), "multiworld.cmd") ||
                                    Perm.has(get_player(source), "multiworld.admin");
                        } catch (Exception e) {
                            return source.hasPermissionLevel(1);
                        }
                    }) 
                        .executes(ctx -> {
                            return broadcast(ctx.getSource(), Formatting.AQUA, null);
                        })
                        .then(argument("message", greedyString()).suggests(new InfoSuggest())
                                .executes(ctx -> {
                                    try {
                                        return broadcast(ctx.getSource(), Formatting.AQUA, getString(ctx, "message") );
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        return 1;
                                    }
                                 }))); 
    }
    
    public static int broadcast(ServerCommandSource source, Formatting formatting, String message) throws CommandSyntaxException {
        final ServerPlayerEntity plr = get_player(source); // source.getPlayerOrThrow();

        if (null == message) {
            plr.sendMessage(text("Multiworld Mod for Minecraft " + mc.getVersion(), Formatting.AQUA), false);
            
            World world = plr.getWorld();
            Identifier id = world.getRegistryKey().getValue();
            
            message(plr, "Currently in: " + id.toString());
            
            return 1;
        }

        boolean ALL = Perm.has(plr, "multiworld.admin");
        String[] args = message.split(" ");
        
        /*if (args[0].equalsIgnoreCase("portaltest")) {
            BlockPos pos = plr.getBlockPos();
            pos = pos.add(2, 0, 2);
            ServerWorld w = plr.getWorld();

            Portal p = new Portal();
            for (int x = 0; x < 4; x++) {
                for (int y = 0; y < 5; y++) {
                    BlockPos pos2 = pos.add(x, y, 0);
                    if ((x > 0 && x < 3) && (y > 0 && y < 4)) {
                        p.blocks.add(pos2);
                        w.setBlockState(pos2, Blocks.NETHER_PORTAL.getDefaultState());
                    } else
                    w.setBlockState(pos2, Blocks.STONE.getDefaultState());
                }
            }
            p.addToMap();
            try {
                p.save();
            } catch (IOException e) {
                plr.sendMessage(text("Failed saving portal data. Check console for details.", Formatting.RED), false);
                e.printStackTrace();
            }
        }*/
        
        if (args[0].equalsIgnoreCase("help")) {
            String[] lines = {
            		"&4Multiworld Mod Commands:&r",
            		"&a/mw spawn&r - Teleport to current world spawn",
            		"&a/mw setspawn&r - Sets the current world spawn",
            		"&a/mw tp <id>&r - Teleport to a world",
            		"&a/mw list&r - List all worlds",
            		"&a/mw gamerule <rule> <value>&r - Change a worlds Gamerules",
            		"&a/mw create <id> <env>&r - create a new world",
            		"&a/mw difficulty <value> [world id] - Sets the difficulty of a world"
            };
            
            for (String s : lines) {
            	message(plr, s);
            }
            
        }
        
        if (args[0].equalsIgnoreCase("debugtick")) {
        	ServerWorld w = (ServerWorld) plr.getWorld();
        	Identifier id = w.getRegistryKey().getValue();
        	message(plr, "World ID: " + id.toString());
        	message(plr, "Players : " + w.getPlayers().size());
        	w.tick(() -> true);
        }

        if (args[0].equalsIgnoreCase("setspawn") && (ALL || Perm.has(plr, "multiworld.setspawn") )) {
            return SetspawnCommand.run(mc, plr, args);
        }

        if (args[0].equalsIgnoreCase("spawn") && (ALL || Perm.has(plr, "multiworld.spawn")) ) {
            return SpawnCommand.run(mc, plr, args);
        }
        
        if (args[0].equalsIgnoreCase("gamerule") && (ALL || Perm.has(plr, "multiworld.gamerule"))) {
        	return GameruleCommand.run(mc, plr, args);
        }
        
        if (args[0].equalsIgnoreCase("difficulty") && (ALL || Perm.has(plr, "multiworld.difficulty"))) {
        	return DifficultyCommand.run(mc, plr, args);
        }

        if (args[0].equalsIgnoreCase("tp") ) {
            if (!(ALL || Perm.has(plr, "multiworld.tp"))) {
                plr.sendMessage(Text.of("No permission! Missing permission: multiworld.tp"), false);
                return 1;
            }
            if (args.length == 1) {
                plr.sendMessage(text_plain("Usage: /" + CMD + " tp <world>"), false);
                return 0;
            }
            return TpCommand.run(mc, plr, args);
        }

        if (args[0].equalsIgnoreCase("list") ) {
            if (!(ALL || Perm.has(plr, "multiworld.cmd"))) {
                plr.sendMessage(Text.of("No permission! Missing permission: multiworld.cmd"), false);
                return 1;
            }
            plr.sendMessage(text("All Worlds:", Formatting.AQUA), false);
            mc.getWorlds().forEach(world -> {
                String name = world.getRegistryKey().getValue().toString();
                if (name.startsWith("multiworld:")) name = name.replace("multiworld:", "");

                plr.sendMessage(text_plain("- " + name), false);
            });
        }

        if (args[0].equalsIgnoreCase("version") && (ALL || Perm.has(plr, "multiworld.cmd")) ) {
            message(plr, "Multiworld Mod version " + VERSION);
            return 1;
        }

        if (args[0].equalsIgnoreCase("create") ) {
            if (!(ALL || Perm.has(plr, "multiworld.create"))) {
                message(plr, "No permission! Missing permission: multiworld.create");
                return 1;
            }
            return CreateCommand.run(mc, plr, args);
        }

        return Command.SINGLE_SUCCESS; // Success
    }

    @Deprecated
	public static Text text(String txt, Formatting color) {
		return world_creator.colored_literal(txt, color);
	}
	
	public static void message(PlayerEntity player, String message) {
		try {
			player.sendMessage(Text.of(translate_alternate_color_codes('&', message)), false);
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

	
	public static Text text_plain(String txt) {
		return Text.of(txt);
	}
	
}
