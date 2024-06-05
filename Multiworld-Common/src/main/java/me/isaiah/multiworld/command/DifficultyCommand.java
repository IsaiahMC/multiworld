package me.isaiah.multiworld.command;

import java.io.IOException;
import java.util.HashMap;

import me.isaiah.multiworld.MultiworldMod;
import me.isaiah.multiworld.config.FileConfiguration;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.Difficulty;

public class DifficultyCommand {

    public static int run(MinecraftServer mc, ServerPlayerEntity plr, String[] args) {
        ServerWorld w = (ServerWorld) plr.getWorld();

		if (args.length < 2) {
			MultiworldMod.message(plr, "[&4Multiworld&r] Usage: /mw difficulty <value> [world id]");
			return 1;
		}
		
        String a1 = args[1];
        // String a2 = args[2];
        
        if (args.length >= 3) {
        	String a2 = args[2];
        	
        	HashMap<String,ServerWorld> worlds = new HashMap<>();
            mc.getWorldRegistryKeys().forEach(r -> {
                ServerWorld world = mc.getWorld(r);
                worlds.put(r.getValue().toString(), world);
            });

            if (a2.indexOf(':') == -1) a2 = "multiworld:" + a2;

            if (worlds.containsKey(a2)) {
                w = worlds.get(a2);
            }
        }

		Difficulty d = Difficulty.NORMAL;

		// String to Difficulty
		if (a1.equalsIgnoreCase("EASY"))         { d = Difficulty.EASY; }
		else if (a1.equalsIgnoreCase("HARD"))    { d = Difficulty.HARD; }
		else if (a1.equalsIgnoreCase("NORMAL"))  { d = Difficulty.NORMAL; }
		else if (a1.equalsIgnoreCase("PEACEFUL")){ d = Difficulty.PEACEFUL; }
		else {
			MultiworldMod.message(plr, "Invalid difficulty: " + a1);
			return 1;
		}

        MultiworldMod.get_world_creator().set_difficulty(w.getRegistryKey().getValue().toString(), d);

        try {
			FileConfiguration config = Util.get_config(w);
			config.set("difficulty", a1);
			config.save();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        MultiworldMod.message(plr, "[&cMultiworld&r]: Difficulty of world '" + w.getRegistryKey().getValue().toString() + "' is now set to: " + a1);
        return 1;
    }

}