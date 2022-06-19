/**
 * Multiworld Mod
 * Copyright (c) 2021-2022 by Isaiah.
 */
package me.isaiah.multiworld;

import java.util.function.Supplier;
import com.mojang.brigadier.CommandDispatcher;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.io.IOException;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import me.isaiah.multiworld.command.CreateCommand;
import me.isaiah.multiworld.command.SetspawnCommand;
import me.isaiah.multiworld.command.SpawnCommand;
import me.isaiah.multiworld.command.TpCommand;
import me.isaiah.multiworld.perm.Perm;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.block.Block;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.Difficulty;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;

/**
 * Multiworld version 1.3
 */
public class MultiworldMod {

    public static final String MOD_ID = "multiworld";
    public static MinecraftServer mc;
    public static String CMD = "mw";
    public static ICreator world_creator;
    
    public static void setICreator(ICreator ic) {
        world_creator = ic;
    }

    public static ServerWorld create_world(String id, RegistryKey<DimensionType> dim, ChunkGenerator gen, Difficulty dif, long seed) {
        return world_creator.create_world(id, dim,gen,dif, seed);
    }

    // On mod init
    public static void init() {
        System.out.println(" Multiworld init");
    }

    // On server start
    public static void on_server_started(MinecraftServer mc) {
        MultiworldMod.mc = mc;
    }
    
    // On command register
    public static void register_commands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal(CMD)
                    .requires(source -> {
                        try {
                            return Perm.has(source.getPlayer(), "multiworld.cmd") ||
                                    Perm.has(source.getPlayer(), "multiworld.admin");
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
        final ServerPlayerEntity plr = source.getPlayer();

        if (null == message) {
            plr.sendMessage(text("Usage:", Formatting.AQUA), false);
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

        if (args[0].equalsIgnoreCase("setspawn") && (ALL || Perm.has(plr, "multiworld.setspawn") )) {
            return SetspawnCommand.run(mc, plr, args);
        }

        if (args[0].equalsIgnoreCase("spawn") && (ALL || Perm.has(plr, "multiworld.spawn")) ) {
            return SpawnCommand.run(mc, plr, args);
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
            plr.sendMessage(text_plain("Mutliworld Mod (Fabric) version 1.2"), false);
            return 1;
        }

        if (args[0].equalsIgnoreCase("create") ) {
            if (!(ALL || Perm.has(plr, "multiworld.create"))) {
                plr.sendMessage(Text.of("No permission! Missing permission: multiworld.create"), false);
                return 1;
            }
            return CreateCommand.run(mc, plr, args);
        }

        return Command.SINGLE_SUCCESS; // Success
    }
    
    
	// TODO: this could be better
	public static Text text(String txt, Formatting color) {
		return Text.of(txt).copy().formatted(color);
	}
	
	public static Text text_plain(String txt) {
		return Text.of(txt);
	}
	
}
