package me.isaiah.multiworld;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import me.isaiah.multiworld.command.CreateCommand;
import me.isaiah.multiworld.command.Util;
import me.isaiah.multiworld.config.FileConfiguration;
import multiworld.api.WorldFolderMode;
// import net.minecraft.registry.RegistryKey;
// import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
// import net.minecraft.util.path.SymlinkValidationException;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelStorage.Session;
import xyz.nucleoid.fantasy.mixin.MinecraftServerAccess;

public class Utils {

	public static final String WORLD_YML_NAME = "multiworld-world.yml";
	
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
    
	public static boolean isForge() {
		try {
			Class.forName("net.neoforged.neoforge.common.NeoForge");
			return true;
		} catch (ClassNotFoundException e) {
			return isOldForge();
		}
	}
	
	public static boolean isOldForge() {
		try {
			Class.forName("net.minecraftforge.common.MinecraftForge");
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}
	
	public static Optional<WorldFolderMode> getFolderMode(Identifier id) {
		
		/*
		if (id.getNamespace().equalsIgnoreCase("minecraft")) {
			return Optional.of(WorldFolderMode.VANILLA);
		}
		
		File cf = new File(me.isaiah.multiworld.command.Util.get_platform_config_dir(), "multiworld"); 
        File worlds = new File(cf, "worlds");
        File namespace = new File(worlds, id.getNamespace());
        File val = new File(namespace, id.getPath() + ".yml");
        
        if (val.exists()) {
        	return Optional.of(WorldFolderMode.VANILLA);
        }
        
        Path buk = getWorldPath(id, WorldFolderMode.BUKKIT);
        Path van = getWorldPath(id, WorldFolderMode.VANILLA);
        
        if (buk.toFile().isDirectory()) {
        	return Optional.of(WorldFolderMode.BUKKIT);
        }

        if (van.toFile().isDirectory()) {
        	return Optional.of(WorldFolderMode.VANILLA);
        }
		
        return Optional.empty();
        */
		return Optional.of(WorldFolderMode.VANILLA);
	}
	
	
    public static boolean shouldUseNewWorldFormat(MinecraftServer server, Identifier id) {
    	
    	if (id.getNamespace().equalsIgnoreCase("minecraft")) {
			return false;
		}
    	
    	/*
    	Optional<WorldFolderMode> mode = Utils.getFolderMode(id);
    	if (mode.isPresent()) {
    		if (mode.get() == WorldFolderMode.VANILLA) {
    			return false;
    		}
    		if (mode.get() == WorldFolderMode.BUKKIT) {
    			return true;
    		}
    	}
    	
    	try {
			FileConfiguration config = getConfigOrNull(id);
			
			if (null == config) {
				// createConfigAndWorld should make the config first
				return false;
			}

			// TODO: Add world change directory support for NeoForge
			if (isForge()) {
				config.set("letForgeHandleWorldStorage", true);
				return false;
			}
			
			// Check if override set
			if (config.is_set("letVanillaHandleDirectory")) {
				if (config.getBoolean("letVanillaHandleDirectory")) {
					return false;
				}
			}
			
			// All newer Multiworld worlds
			if (config.is_set("worldFolderSaveMode")) {
				String str = config.getString("worldFolderSaveMode");
				try {
					WorldFolderMode m = WorldFolderMode.valueOf(str);
					if (m == WorldFolderMode.BUKKIT) {
						return true;
					}
					if (m == WorldFolderMode.VANILLA) {
						return false;
					}
				} catch (Exception e) {
					e.printStackTrace();
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
    	*/
    	
    	return false;
    }
    
    /**
     * @param <T>
     */
    public static <T> T getConfigValue(Identifier id, String key, T defaul) {
    	try {
			FileConfiguration config = getConfigOrNull(id);
			return config.getOrDefault(key, defaul);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
    }
    
    /**
     * 
     */
    public static FileConfiguration getConfigOrNull(Identifier id) throws IOException {
        File cf = new File(me.isaiah.multiworld.command.Util.get_platform_config_dir(), "multiworld"); 
        File worlds = new File(cf, "worlds");
        File namespace = new File(worlds, id.getNamespace());
        
        // Not Us.
        if (id.getNamespace().equalsIgnoreCase("minecraft")) {
        	return null;
        }
        
        cf.mkdirs();
        
        Optional<WorldFolderMode> mode = Utils.getFolderMode(id);
        
        Path pDir = Utils.getWorldDirectory(id);
    	Path pYml = pDir.resolve(WORLD_YML_NAME);
    	
    	if (pYml.toFile().exists()) {
    		FileConfiguration config = new FileConfiguration(pYml.toFile());
            return config;
    	}
    	
    	File wc = new File(namespace, id.getPath() + ".yml");
        if (!wc.exists()) {
        	
        	// pDir.toFile().mkdirs();
        	// pYml.toFile().createNewFile();
        	
        	return null;
        }

        worlds.mkdirs();
        namespace.mkdirs();
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

 	public static Path getWorldStoragePath() {
 		return getWorldStoragePath(MultiworldMod.mc);
 	}
 	
 	public static Path getWorldStoragePath(MinecraftServer server, WorldFolderMode mode) {
 		Path overworld = ((MinecraftServerAccess) server).getSession().getWorldDirectory(World.OVERWORLD); 
 		
 		// if (mode == WorldFolderMode.VANILLA) {
 			return overworld.resolve("dimensions");
 		// }
 		
 		// Client side
 		/*
 		if (!MultiworldMod.mc.isDedicated()) {
 			Path mw = overworld.resolve("multiworlds");
 			return mw;
 		}
 		
 		return overworld.getParent();
 		*/
 	}
 	
 	public static Path getWorldPath(Identifier id, WorldFolderMode mode) {
 		Path overworld = ((MinecraftServerAccess) MultiworldMod.mc).getSession().getWorldDirectory(World.OVERWORLD); 
 		
 		// if (mode == WorldFolderMode.VANILLA) {
 			return overworld.resolve("dimensions").resolve(id.getNamespace()).resolve(id.getPath());
 		//}
 		
 		// Client side
 		/*
 			if (!MultiworldMod.mc.isDedicated()) {
 			Path mw = overworld.resolve("multiworlds");
 			return mw.resolve(getWorldName(id));
 		}
 		
 		return overworld.getParent().resolve(getWorldName(id));
 		*/
 	}
 	
 	public static Path getWorldStoragePath(MinecraftServer server) {
 		Path overworld = ((MinecraftServerAccess) server).getSession().getWorldDirectory(World.OVERWORLD); 
 		
 		// Client side
 		if (!MultiworldMod.mc.isDedicated()) {
 			Path mw = overworld.resolve("multiworlds");
 			return mw;
 		}
 		
 		return overworld.getParent();
 	}
 	
 	public static Path getWorldDirectory(Identifier id) {
 		return getWorldPath(id, WorldFolderMode.VANILLA);
 	}
 	
 	public static Path getWorldDirectory(Identifier id, WorldFolderMode mode) {
 		return getWorldPath(id, WorldFolderMode.VANILLA);
 	}
 	
 	/*
 	public static Path getWorldDirectory(Identifier id) {
 		
 		/*
 		// TODO: Add proper Multiworld support to Forge
 		if (isForge()) {
 			System.out.println("Note: Using Vanilla Directory for World: " + id);
 			return ((MinecraftServerAccess) MultiworldMod.mc).getSession().getWorldDirectory(RegistryKey.of(RegistryKeys.WORLD, id));
 		}
 		
 		Path storage = getWorldStoragePath();
 		Path bukkit = storage.resolve( getWorldName(id) ); 
 		
 		return bukkit;
 		*
		return ((MinecraftServerAccess) MultiworldMod.mc).getSession().getWorldDirectory(RegistryKey.of(RegistryKeys.WORLD, id));

 	}
 	
 	public static Path getWorldDirectory(Identifier id, WorldFolderMode mode) {
 		
 		// TODO: Add proper Multiworld support to Forge
 		/*
 		if (isForge() || mode == WorldFolderMode.VANILLA) {
 			System.out.println("Note: Using Vanilla Directory for World: " + id);
 			return ((MinecraftServerAccess) MultiworldMod.mc).getSession().getWorldDirectory(RegistryKey.of(RegistryKeys.WORLD, id));
 		}
 		
 		Path storage = getWorldStoragePath();
 		Path bukkit = storage.resolve( getWorldName(id) ); 
 		
 		return bukkit;
 		*
 		return ((MinecraftServerAccess) MultiworldMod.mc).getSession().getWorldDirectory(RegistryKey.of(RegistryKeys.WORLD, id));
 	}
 	*/
 	
 	public static List<Path> searchForWorlds() {
 		Path storage = getWorldStoragePath();
 		return searchForWorlds(storage);
 	}

 	public static List<Path> searchForWorlds(Path storage) {
 		List<Path> worldPaths = new ArrayList<>(); 
 		
 		System.out.println("DEBUG: " + storage);
 		
 		File fold = storage.toFile();
 		
 		if (!fold.exists()) {
 			fold.mkdir();
 			return worldPaths;
 		}
 		
 		for (File f : fold.listFiles()) {
 			if (!f.isDirectory()) {
 				continue;
 			}
 			
 			Path dirPath = storage.resolve(f.getName());
 			
 			// File levelData = new File(f, "level.dat");
 			File multiworldConfig = new File(f, "multiworld-world.yml");
 			
 			if (/*levelData.exists() ||*/ multiworldConfig.exists()) {
 				worldPaths.add(dirPath);
 			}
 			
 		}
 		
 		return worldPaths;
 	}
 	
 	/**
     * Check if World (by ID) exists.
     * 
     * @param id - World Identifier
     */
    public static boolean checkIfWorldExists(String id) {
    	Path path = Utils.getWorldDirectory(MultiworldMod.new_id(id));
		
		if (!path.toFile().isDirectory()) {
			if (MultiworldMod.mc.isDedicated()) {
				MultiworldMod.LOGGER.info("Error loading World \"" + id + "\" could not find world folder: " + path);
			}
			// Singleplayer
			return false;
		}
    	return true;
    }
    
 	/**
     * Check if World (by Path) exists.
     * 
     * @param id - World Identifier
     */
    public static boolean checkIfWorldExists(Path path) {
    	// Path path = Utils.getWorldDirectory(MultiworldMod.new_id(id));
		
		if (!path.toFile().isDirectory()) {
			if (MultiworldMod.mc.isDedicated()) {
				MultiworldMod.LOGGER.info("Error loading Multiworld world, could not find world folder: " + path);
			}
			return false;
		}
    	return true;
    }
    
    /**
     * @return config/multiworld/
     */
    public static File getConfigDir() {
    	File config_dir = new File("config");
        config_dir.mkdirs();

        File cf = new File(config_dir, "multiworld"); 
        cf.mkdirs();
        return cf;
    }
 	
 	/**
     * Load an existing saved World from config (YAML) 
     */
	public static void loadSavedMultiworldWorld(MinecraftServer mc, Path worldPath, Optional<String> optId) {
        // File cf = getConfigDir();

        // String[] spl = id.split(":");

        /*
        File worlds = new File(cf, "worlds");
        worlds.mkdirs();

        File namespace = new File(worlds, spl[0]);
        namespace.mkdirs();
        */
        
        // Check if World data exists
        if (!checkIfWorldExists(worldPath)) {
        	return;
        }

        File wc = worldPath.resolve(WORLD_YML_NAME).toFile();// new File(namespace, spl[1] + ".yml");
        FileConfiguration config;
        try {
			if (!wc.exists()) {
				wc.createNewFile();
			}
            config = new FileConfiguration(wc);
			String env = config.getString("environment");
			long seed = 0;
			
			try {
				seed = config.getLong("seed");
			} catch (Exception e) {
				seed = config.getInt("seed");
			}

			ChunkGenerator gen = CreateCommand.get_chunk_gen(mc, env);
		    Identifier dim = CreateCommand.get_dim_id(env);
		    
		    if (null == dim) {
		    	dim = Util.OVERWORLD_ID;
		    }
			
			Difficulty d = Difficulty.NORMAL;

			// Set saved Difficulty
			if (config.is_set("difficulty")) {
				String di = config.getString("difficulty");
				d = getDifficultyFromName(di);
			}

			// Gen
			if (config.is_set("custom_generator")) {
				String cg = config.getString("custom_generator");
				
				ChunkGenerator gen1 = CreateCommand.get_chunk_gen(mc, cg);
        		if (null != gen1) {
        			gen = gen1;
        		} else {
        			System.out.println("Invalid ChunkGenerator: \"" + cg + "\"");
        		}
			}
			
			String id = optId.orElseGet(() -> {
				String namespace = config.getString("namespace");
				String path = config.getString("path");
				String savedId = namespace + ":" + path;
				return savedId;
			});

			ServerWorld world = MultiworldMod.create_world(id, dim, gen, d, seed);

			MultiworldMod.get_world_creator().set_difficulty(id, d);

			CreateCommand.reinitWorldGamerules(config, world);
			
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	/**
	 */
	private static Difficulty getDifficultyFromName(String di) {
		// String to Difficulty
		Difficulty d = Difficulty.NORMAL;
		if (di.equalsIgnoreCase("EASY"))     { d = Difficulty.EASY; }
		if (di.equalsIgnoreCase("HARD"))     { d = Difficulty.HARD; }
		if (di.equalsIgnoreCase("NORMAL"))   { d = Difficulty.NORMAL; }
		if (di.equalsIgnoreCase("PEACEFUL")) { d = Difficulty.PEACEFUL; }
		return d;
	}
	
}
