package me.isaiah.multiworld.command;

import static me.isaiah.multiworld.MultiworldMod.text_plain;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;

public class DeleteCommand implements Command {

	/**
	 * Run Command
	 */
    public static int run(MinecraftServer mc, ServerCommandSource source, String[] args) {
        if (args.length == 1) {
        	// source.sendMessage(text_plain("Usage: /mw delete <id>"));
            return 0;
        }
        
        // source.sendMessage(text_plain("Command is work in progress"));
        
        return 1;
    }

}
