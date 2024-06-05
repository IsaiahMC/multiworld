package me.isaiah.multiworld.command;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import me.isaiah.multiworld.MultiworldMod;
import me.isaiah.multiworld.config.FileConfiguration;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.GameRules.BooleanRule;
import net.minecraft.world.GameRules.IntRule;
import net.minecraft.world.GameRules.Rule;

public class GameruleCommand {

	@SuppressWarnings("rawtypes")
	public static HashMap<String, GameRules.Key> keys = new HashMap<>();
	
    @SuppressWarnings("unchecked")
	public static int run(MinecraftServer mc, ServerPlayerEntity plr, String[] args) {
        ServerWorld w = (ServerWorld) plr.getWorld();
        
		if (keys.isEmpty()) {
			setup();
		}

        // GameRules rules = new GameRules();

		if (args.length < 3) {
			Rule<?> rule = w.getGameRules().get(keys.get(args[1]));
			MultiworldMod.message(plr, "[&4Multiworld&r] Value of " + args[1] + " is: " + rule);
			return 1;
		}
		
        String a1 = args[1];
        String a2 = args[2];
        
        /*if (a1.equalsIgnoreCase("difficulty")) {
        	// Test
        	
			Difficulty d = Difficulty.NORMAL;

			// String to Difficulty
			if (a2.equalsIgnoreCase("EASY"))     { d = Difficulty.EASY; }
			else if (a2.equalsIgnoreCase("HARD"))     { d = Difficulty.HARD; }
			else if (a2.equalsIgnoreCase("NORMAL"))   { d = Difficulty.NORMAL; }
			else if (a2.equalsIgnoreCase("PEACEFUL")) { d = Difficulty.PEACEFUL; }
			else {
				MultiworldMod.message(plr, "Invalid difficulty: " + a2);
				return 1;
			}
        	
        	MultiworldMod.get_world_creator().set_difficulty(w.getRegistryKey().getValue().toString(), d);
        	
        	try {
				FileConfiguration config = CreateCommand.get_config(w);
				config.set("difficulty", a2);
				config.save();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

        	return 1;
        }*/
        
        boolean is_bol = false;
        
        if (a2.equalsIgnoreCase("true") || a2.equalsIgnoreCase("false")) {
        	is_bol = true;
        }

        if (is_bol) {
        	// Boolean Rule
        	BooleanRule rule = (BooleanRule) w.getGameRules().get(keys.get(a1));
        	rule.set(Boolean.valueOf(a2), mc);
        } else {
        	// Int Rule
        	IntRule rule = (IntRule) w.getGameRules().get(keys.get(a1));
        	rule.set(Integer.valueOf(a2), mc);
        }

        // Save to world config
    	try {
			set_rule_cfg(w, a1, a2);
		} catch (IOException e) {
			e.printStackTrace();
		}

        MultiworldMod.message(plr, "[&cMultiworld&r]: Gamerule " + a1 + " is now set to: " + a2);
        
        return 1;
    }
    
    /**
     * Read the Gamerule names
     */
    public static void setup() {
        keys.clear();
    	GameRules.accept(new GameRules.Visitor(){
            @Override
            public <T extends GameRules.Rule<T>> void visit(GameRules.Key<T> key, GameRules.Type<T> type) {
                String name = key.getName();
            	keys.put(name, key);
            }
        });
    }
    
    /**
     * Save the Gamerule to our world config
     * 
     * @param w - The World to apply the Gamerule
     * @param a - The Gamerule name (ex: "doDaylightCycle")
     * @param b - The value for the Gamerule (ex: "true", or "100")
     */
    public static void set_rule_cfg(World w, String a, String b) throws IOException {
        File cf = new File(Util.get_platform_config_dir(), "multiworld"); 
        cf.mkdirs();

        File worlds = new File(cf, "worlds");
        worlds.mkdirs();

        Identifier id = w.getRegistryKey().getValue();
        File namespace = new File(worlds, id.getNamespace());
        namespace.mkdirs();

        File wc = new File(namespace, id.getPath() + ".yml");
        wc.createNewFile();
        FileConfiguration config = new FileConfiguration(wc);

        if (!config.is_set("gamerules")) {
        	config.set("gamerules", new ArrayList<String>());
        }

        config.set("gamerule_" + a, b);

        config.save();
    }

    /**
     * Load gamerule from config entry
     * 
     * @param world - The ServerWorld to apply the Gamerule
     * @param key - Config key for Gamerule (ex: "gamerule_doDaylightCycle")
     * @param val - The value for the Gamerule (ex: "true", or "100")
     * @see {@link CreateCommand#reinit_world_from_config(MinecraftServer, String)}
     */
	@SuppressWarnings("unchecked")
	public static void set_gamerule_from_cfg(ServerWorld world, String key, String val) {
		if (keys.isEmpty()) {
			setup();
		}

        String name = key.replace("gamerule_", "").trim();
        String a1 = val.trim();
		
		boolean is_bol = false;
        
        if (a1.equalsIgnoreCase("true") || a1.equalsIgnoreCase("false")) {
        	is_bol = true;
        }
		
        if (is_bol) {
        	// Boolean Rule
        	BooleanRule rule = (BooleanRule) world.getGameRules().get(keys.get(name));
        	rule.set(Boolean.valueOf(a1), MultiworldMod.mc);
        } else {
        	// Int Rule
        	IntRule rule = (IntRule) world.getGameRules().get(keys.get(name));
        	rule.set(Integer.valueOf(a1), MultiworldMod.mc);
        }
		
	}

}
