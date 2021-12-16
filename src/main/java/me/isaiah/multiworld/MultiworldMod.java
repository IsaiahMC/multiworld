package me.isaiah.multiworld;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import me.isaiah.multiworld.command.CreateCommand;
import me.isaiah.multiworld.command.SetspawnCommand;
import me.isaiah.multiworld.command.SpawnCommand;
import me.isaiah.multiworld.command.TpCommand;
import me.isaiah.multiworld.perm.Perm;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.registry.Registry;

public class MultiworldMod implements ModInitializer {

    public static MinecraftServer mc;
    public static String CMD = "mw";

    @Override
    public void onInitialize() {
        System.out.println("Hello Fabric world!");
        ServerLifecycleEvents.SERVER_STARTED.register(mc -> {
            MultiworldMod.mc = mc;
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(literal(CMD)
                    .requires(source -> {
                        try {
                            return Perm.has(source.getPlayer(), "multiworld.cmd") ||
                                    Perm.has(source.getPlayer(), "multiworld.admin");
                        } catch (CommandSyntaxException e) {
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
        });
    }

    public static int broadcast(ServerCommandSource source, Formatting formatting, String message) throws CommandSyntaxException {
        final ServerPlayerEntity plr = source.getPlayer();

        if (null == message) {
            plr.sendMessage(new LiteralText("Usage:").formatted(Formatting.AQUA), false);
            return 1;
        }

        boolean ALL = Perm.has(plr, "multiworld.admin");
        String[] args = message.split(" ");

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
                plr.sendMessage(new LiteralText("Usage: /" + CMD + " tp <world>"), false);
                return 0;
            }
            return TpCommand.run(mc, plr, args);
        }

        if (args[0].equalsIgnoreCase("list") ) {
            if (!(ALL || Perm.has(plr, "multiworld.cmd"))) {
                plr.sendMessage(Text.of("No permission! Missing permission: multiworld.cmd"), false);
                return 1;
            }
            plr.sendMessage(new LiteralText("All Worlds:").formatted(Formatting.AQUA), false);
            mc.getWorlds().forEach(world -> {
                String name = world.getRegistryKey().getValue().toString();
                if (name.startsWith("multiworld:")) name = name.replace("multiworld:", "");

                plr.sendMessage(new LiteralText("- " + name), false);
            });
        }

        if (args[0].equalsIgnoreCase("version") && (ALL || Perm.has(plr, "multiworld.cmd")) ) {
            plr.sendMessage(new LiteralText("Mutliworld Mod (Fabric) version 1.0"), false);
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

}