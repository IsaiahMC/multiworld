package me.isaiah.multiworld;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import me.isaiah.multiworld.command.CreateCommand;
import me.isaiah.multiworld.config.FileConfiguration;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.path.SymlinkValidationException;
import net.minecraft.world.World;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelStorage.Session;
import xyz.nucleoid.fantasy.mixin.MinecraftServerAccess;

public class Utils {

	public static Identifier getEnvironment(MinecraftServer server, Identifier id) {
    	try {
			FileConfiguration config = getConfigOrNull(id);
			
			if (null == config) {
				return null;
			}

			if (config.is_set("environment")) {
				String ev = config.getString("environment");
				Identifier did = CreateCommand.get_dim_id(ev);
				if (null == did) {
					did = me.isaiah.multiworld.command.Util.OVERWORLD_ID;
				}
				return did;
    		}


		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

    	// Not on Minecraft worlds!
    	if (id.getNamespace().contains("minecraft")) {
    		return null;
    	}
    	
    	return null;
    }
    
    public static boolean shouldUseNewWorldFormat(MinecraftServer server, Identifier id) {
    	
    	if (id.getNamespace().equalsIgnoreCase("minecraft")) {
			return false;
		}
    	
    	try {
			FileConfiguration config = getConfigOrNull(id);
			
			if (null == config) {
				// createConfigAndWorld should make the config first
				return false;
			}

			// Check if override set
			if (config.is_set("letVanillaHandleDirectory")) {
				if (config.getBoolean("letVanillaHandleDirectory")) {
					return false;
				}
			}

			// All newer Multiworld worlds
			if (config.is_set("isMultiworldWorld")) {
				if (config.getBoolean("isMultiworldWorld")) {
					return true;
				}
    		}
	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

    	// Not on Minecraft worlds!
    	if (id.getNamespace().contains("minecraft")) {
    		return false;
    	}
    	
    	return false;
    }
    
    /**
     * 
     */
    public static FileConfiguration getConfigOrNull(Identifier id) throws IOException {
        File cf = new File(me.isaiah.multiworld.command.Util.get_platform_config_dir(), "multiworld"); 
        File worlds = new File(cf, "worlds");
        File namespace = new File(worlds, id.getNamespace());
        
        cf.mkdirs();
        worlds.mkdirs();
        namespace.mkdirs();

        File wc = new File(namespace, id.getPath() + ".yml");
        if (!wc.exists()) {
        	return null;
        }
        
        wc.createNewFile();
        FileConfiguration config = new FileConfiguration(wc);
        return config;
    }
    
    /**
     */
    public static String getWorldName(Identifier id) {
		if (id.getNamespace().equalsIgnoreCase("multiworld")) {
			return id.getPath();
		}
		return id.toUnderscoreSeparatedString();
	}
    
    private static Session mw$session(MinecraftServer server, Identifier id) {
    	boolean useUs = Utils.shouldUseNewWorldFormat(server, id);
    	if (!useUs) { return ((MinecraftServerAccess) server).getSession(); }
    	
    	String name = Utils.getWorldName(id);
    	Path customWorldPath = getWorldContainer(server).toPath();
    	LevelStorage levelStorage = LevelStorage.create(customWorldPath);
    	try {
			LevelStorage.Session session = levelStorage.createSession( name );
			return session;
		} catch (IOException | SymlinkValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return ((MinecraftServerAccess) server).getSession();
		}

    }
    
    // Exerpt from CraftServer: getWorldContainer
 	public static File getWorldContainer(MinecraftServer server) {
 		return ((MinecraftServerAccess) server).getSession().getWorldDirectory(World.OVERWORLD).getParent().toFile();
 	}
 	
 	public static Path getWorldStoragePath(MinecraftServer server) {
 		return ((MinecraftServerAccess) server).getSession().getWorldDirectory(World.OVERWORLD).getParent();
 	}
	
}
