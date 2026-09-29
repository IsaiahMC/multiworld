package me.isaiah.multiworld.command;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.slf4j.Logger;

import me.isaiah.multiworld.ConsoleCommand;
import me.isaiah.multiworld.MultiworldMod;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;

public class DeleteCommand implements Command {

	public static Logger LOGGER = ConsoleCommand.LOGGER;

	private static HashMap<String, Long> map = new HashMap<>();
	
	/**
	 * Run Command
	 */
    public static int run(MinecraftServer mc, CommandSourceStack source, String[] args) {
        if (args.length == 1) {
        	LOGGER.error("Usage: /mw delete <id>");
            return 0;
        }

        String id = args[1];
        
        if (!map.containsKey(id)) {
        	map.put(id, System.currentTimeMillis() );
        	
        	LOGGER.info("NOTE: This command will destroy the life, universe and everything associated with the world.");
        	LOGGER.info("For this reason, Please run command again to confirm to delete \"" + id + "\".");
        	return 1;
        }
        
        long start = map.get(id);
        long now = System.currentTimeMillis();
        long TIMEOUT = 20_000;
        
        if (now - start > TIMEOUT) {
        	LOGGER.info("Delete request timed-out (>20s). Please try again.");
        	map.remove(id);
        	return 0;
        }

        LOGGER.info("Deleting multiworld config for \"" + id + "\"...");
        try {
			File config = Util.get_config_file(MultiworldMod.new_id(id));
			config.delete();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        LOGGER.info("Deleting world folder \"" + id + "\"...");
        MultiworldMod.get_world_creator().delete_world(id);

        return 1;
    }

}