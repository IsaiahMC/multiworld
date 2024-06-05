package me.isaiah.multiworld.command;

import java.io.File;
import java.io.IOException;

import me.isaiah.multiworld.MultiworldMod;
import me.isaiah.multiworld.config.FileConfiguration;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class Util {

	
	// Dimension Ids
    public static final Identifier OVERWORLD_ID = id("overworld");
    public static final Identifier THE_NETHER_ID = id("the_nether");
    public static final Identifier THE_END_ID = id("the_end");

    public static Identifier id(String id) {
    	return MultiworldMod.new_id(id);
    }
    
    public static File get_platform_config_dir() {
    	// return FabricLoader.getInstance().getConfigDir().toFile();
    	return new File("config");
    }
    
    /**
     * 
     */
    public static FileConfiguration get_config(World w) throws IOException {
        File cf = new File(get_platform_config_dir(), "multiworld"); 
        cf.mkdirs();

        File worlds = new File(cf, "worlds");
        worlds.mkdirs();

        Identifier id = w.getRegistryKey().getValue();
        File namespace = new File(worlds, id.getNamespace());
        namespace.mkdirs();

        File wc = new File(namespace, id.getPath() + ".yml");
        wc.createNewFile();
        FileConfiguration config = new FileConfiguration(wc);
        return config;
    }
    
}