package me.isaiah.multiworld.command;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;

import me.isaiah.multiworld.MultiworldMod;
import me.isaiah.multiworld.config.FileConfiguration;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class Util {

	public static Locale AMERICAN_STANDARD = Locale.ROOT; 
	
	// Dimension Ids
    public static final Identifier OVERWORLD_ID = id("overworld");
    public static final Identifier THE_NETHER_ID = id("the_nether");
    public static final Identifier THE_END_ID = id("the_end");
    
    // todo
    public static final Identifier VOID_ID = id("the_void");
    
   

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
    
    
    /**
     * 
     */
    public static FileConfiguration get_config(Identifier id) throws IOException {
        File cf = new File(get_platform_config_dir(), "multiworld"); 
        cf.mkdirs();

        File worlds = new File(cf, "worlds");
        worlds.mkdirs();

        File namespace = new File(worlds, id.getNamespace());
        namespace.mkdirs();

        File wc = new File(namespace, id.getPath() + ".yml");
        wc.createNewFile();
        FileConfiguration config = new FileConfiguration(wc);
        return config;
    }
    
    /**
     * 
     */
    public static File get_config_file(Identifier id) throws IOException {
        File cf = new File(get_platform_config_dir(), "multiworld"); 
        cf.mkdirs();

        File worlds = new File(cf, "worlds");
        worlds.mkdirs();

        File namespace = new File(worlds, id.getNamespace());
        namespace.mkdirs();

        File wc = new File(namespace, id.getPath() + ".yml");
        return wc;
    }
    
    private static boolean hasValueCached = false;
    private static boolean isForgeOrHasICommon;
    
	public static boolean isForgeOrHasICommon() {
		
		if (hasValueCached) {
			return isForgeOrHasICommon;
		}
		
		Class<?> hookClaz = null;
		
		try {
			hookClaz = Class.forName("me.isaiah.multiworld.fabric.ICommonHooks");
		} catch (ClassNotFoundException e) {
			// NeoForge
			isForgeOrHasICommon = true;
			hasValueCached = true;
			return true;
		}
		
		try {
			Method m = hookClaz.getDeclaredMethod("hasICommon");
			
			Object o = m.invoke(hookClaz, null);
			boolean b = (boolean) o;
			isForgeOrHasICommon = b;
			hasValueCached = true;
			return b;
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		isForgeOrHasICommon = false;
		hasValueCached = true;
		return false;
		
	}
    
}