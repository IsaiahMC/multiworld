package me.isaiah.multiworld;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import me.isaiah.multiworld.command.DeleteCommand;
import me.isaiah.multiworld.command.TpCommand;
import me.isaiah.multiworld.perm.Perm;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class ConsoleCommand {
	
	public static final Logger LOGGER = LoggerFactory.getLogger("multiworld");

	public static int broadcast_console(MinecraftServer mc, ServerCommandSource source, String message) throws CommandSyntaxException {
		if (null == message) {
			LOGGER.info("Multiworld Mod for Minecraft " + mc.getVersion());
			LOGGER.info("(Console Commands are experimental)");
			return 1;
		}

		String[] args = message.split(" ");
		if (args[0].equalsIgnoreCase("help")) {
			for (String s : MultiworldMod.COMMAND_HELP) LOGGER.info(s);
			
			return 1;
		}

		// Delete Command (Console Only)
		if (args[0].equalsIgnoreCase("delete")) {
			DeleteCommand.run(mc, source, args);
			return 1;
		}

		// TP Command
		if (args[0].equalsIgnoreCase("tp") ) {
			if (args.length <= 2) {
				LOGGER.info("Usage: /mw tp <world> <player>");
				return 0;
			}
			return TpCommand.run(mc, null, args);
		}

		// List Command
        if (args[0].equalsIgnoreCase("list") ) {
            LOGGER.info("All Worlds:");
            mc.getWorlds().forEach(world -> LOGGER.info("- " + world.getRegistryKey().getValue().toString()));
            return 1;
        }

        // Version Command
        if (args[0].equalsIgnoreCase("version") ) {
            LOGGER.info("Multiworld Mod version " + MultiworldMod.VERSION);
            return 1;
        }

		throw ServerCommandSource.REQUIRES_PLAYER_EXCEPTION.create();
	}
}
