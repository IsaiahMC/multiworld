package me.isaiah.multiworld.fabric;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;

import me.isaiah.multiworld.ICreator;
import me.isaiah.multiworld.MultiworldMod;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleType;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.validation.ContentValidationException;
import net.minecraft.world.phys.Vec3;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeLevel;
import xyz.nucleoid.fantasy.RuntimeLevelConfig;
import xyz.nucleoid.fantasy.RuntimeLevelHandle;
import xyz.nucleoid.fantasy.util.VoidChunkGenerator;

public class FabricWorldCreator implements ICreator {

    public Identifier new_id(String id) {
    	return Identifier.parse(id);
    }

	public HashMap<String, RuntimeLevelConfig> worldConfigs;
	
	public FabricWorldCreator() {
		this.worldConfigs = new HashMap<>();
	}
	
    public static void init() {
        MultiworldMod.setICreator(new FabricWorldCreator());
    }
    
    private RuntimeLevel.Constructor worldConstructor = MultiworldWorld::new;

    public ServerLevel create_world(String id, Identifier dim, ChunkGenerator gen, Difficulty dif, long seed) {
        
    	Identifier idd = new_id(id);
    	GameRules rules = null;
		try {
			rules = readGameRules(idd);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
    	
    	RuntimeLevelConfig config = new RuntimeLevelConfig()
                .setDimensionType(dim_of(dim))
                .setGenerator(gen)
                .setDifficulty(dif)
				.setSeed(seed)
				.setShouldTickTime(true)
				.setLevelConstructor(MultiworldWorld::new)
                ;

        if (gen instanceof CustomFlatChunkGenerator) {
        	config.setFlat(true);
        }

        Fantasy fantasy = Fantasy.get(MultiworldMod.mc);
        RuntimeLevelHandle worldHandle = fantasy.getOrOpenPersistentLevel(idd, config);
        ServerLevel world = worldHandle.asLevel();
        
        if (null != rules) {
        	final GameRules rulesFinal = rules;
	        rules.availableRules().forEach(rule -> {
				if (rule.gameRuleType() == GameRuleType.BOOL) {
					world.getGameRules().set( (GameRule<Boolean>) rule, rulesFinal.get((GameRule<Boolean>) rule), null);
				}
	
				if (rule.gameRuleType() == GameRuleType.INT) {
					world.getGameRules().set( (GameRule<Integer>) rule, rulesFinal.get((GameRule<Integer>) rule), null);
				}
			});
        }
        
        this.worldConfigs.put(id, config);
        return world;
    }
    
    /**
     * Reads the gamerules from a level.dat file in the given world folder.
     * @param savesDir Path to the root saves directory (e.g. ./saves).
     * @param worldName Name of the world folder.
     * @param dataFixer The server's DataFixer instance.
     * @return A GameRules object containing the rules from level.dat.
     * @throws IOException if the file cannot be read.
     * @throws ContentValidationException 
     */
    public static GameRules readGameRules(Identifier id) throws IOException {

    	/*
        try (Session session = MultiworldWorld.mw$getSession(MultiworldMod.mc, id)) {
            Dynamic<?> dynamic = session.readLevelProperties();
            
            RegistryWrapper.WrapperLookup lookup = MultiworldMod.mc.getRegistryManager();
            
            Registry<DimensionOptions> dimensionRegistry = MultiworldMod.mc.getRegistryManager().getOrThrow(RegistryKeys.DIMENSION);
            
            DataConfiguration dataConfig = MultiworldMod.mc.getSaveProperties().getDataConfiguration();

            SaveProperties props = LevelStorage.parseSaveProperties(
                dynamic,
                dataConfig,
                dimensionRegistry,
                MultiworldMod.mc.getRegistryManager()
            ).properties();
            
            if (!(props instanceof LevelProperties levelProps)) {
                throw new IllegalStateException("SaveProperties is not a LevelProperties");
            }
            
            session.close();
            // Return the gamerules object
            return levelProps.getGameRules();
        }
        */
    	
    	/*
    	try {
			return MultiworldWorld.mw$readGameRules(MultiworldMod.mc, id);
		} catch (IOException | ContentValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new IOException(e);
		}
		*/
    	return null;
    }
    
    @Override
    public void set_difficulty(String id, Difficulty dif) {
    	this.worldConfigs.get(id).setDifficulty(dif);
    }
    
    private static ResourceKey<DimensionType> dim_of(Identifier id) {
        return ResourceKey.create(Registries.DIMENSION_TYPE, id);
    }
    
    public void delete_world(String id) {
        Fantasy fantasy = Fantasy.get(MultiworldMod.mc);
        RuntimeLevelHandle worldHandle = fantasy.getOrOpenPersistentLevel(new_id(id), null);
        worldHandle.delete();
    }

	@Override
	public boolean is_the_end(ServerLevel world) {
		// #if mc262 mc263
		// return ((ServerLevel)(Object)this).dimensionTypeRegistration().is(BuiltinDimensionTypes.END);
		// #else
		return world.dimensionTypeRegistration() == BuiltinDimensionTypes.END;
		// #endif
	}

	@Override
	public BlockPos get_pos(double x, double y, double z) {
		return BlockPos.containing(x, y, z);
	}
	
	@Override
	public BlockPos get_spawn(ServerLevel world) {
		
		return world.getRespawnData().pos();
		
		// return world.getLevelProperties().getSpawnPos();
	}

	@Override
	public void teleleport(ServerPlayer player, ServerLevel world, double x, double y, double z) {
        TeleportTransition target = new TeleportTransition(world, new Vec3(x, y, z), new Vec3(0, 0, 0), 0f, 0f, TeleportTransition.DO_NOTHING);
        
        // FabricDimensionInternals.changeDimension(player, world, target);
        
        // Per https://fabricmc.net/2024/05/31/121.html
        // for 1.21, FabricDimension API is replaced by teleportTo
        player.teleport(target);
	}
	
	@Override
	public ChunkGenerator get_void_chunk_gen(MinecraftServer mc) {
		var biome = mc.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.THE_VOID);
		VoidChunkGenerator gen = new xyz.nucleoid.fantasy.util.VoidChunkGenerator(biome);
        return gen;
	}

	@Override
	public ChunkGenerator get_flat_chunk_gen(MinecraftServer mc) {
		var biome = mc.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS);
        FlatLevelGeneratorSettings flat = new FlatLevelGeneratorSettings(Optional.empty(), biome, Collections.emptyList());

        flat.setDecoration();
        FlatLevelSource generator = new CustomFlatChunkGenerator(flat);

        return generator;
	}
	
	// Custom Flat Gen
	class CustomFlatChunkGenerator extends FlatLevelSource {
		public CustomFlatChunkGenerator(FlatLevelGeneratorSettings config) {
			super(config);
		}
		
		@Override
		public int getMinY() {
			return 0;
		}
		
		@Override
	    public int getSeaLevel() {
	        return 0;
	    }
	}
	
	/**
	 * 1.21.10/OP -> 1.21.11/Mojang-Permissions-API values: (from wiki)
	 * 
	 * Level 1 -> MOD
	 * Level 2 -> GAMEMASTERS
	 * Level 3 -> ADMIN
	 * Level 4 -> OWNER
	 */
	@Override
	public boolean permissionLevel(CommandSourceStack source, int level) {
		
		if (level == 0) {
			// Should not be 0
			return true;
		}
		
		Permission perm = Permissions.COMMANDS_MODERATOR;

		switch (level) {
			case 1:
				perm = Permissions.COMMANDS_MODERATOR;
				break;
			case 2:
				perm = Permissions.COMMANDS_GAMEMASTER;
				break;
			case 3:
				perm = Permissions.COMMANDS_ADMIN;
				break;
			case 4:
				perm = Permissions.COMMANDS_OWNER;
				break;
		}

		return source.permissions().hasPermission(perm);
	}

	@Override
	public boolean permissionLevel(ServerPlayer plr, int level) {

		if (level == 1) { return plr.permissions().hasPermission(Permissions.COMMANDS_MODERATOR); }
		if (level == 2) { return plr.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER); }
		if (level == 3) { return plr.permissions().hasPermission(Permissions.COMMANDS_ADMIN); }
		
		return plr.canUseGameMasterBlocks();
		
		//return plr.hasPermissionLevel(level);
	}

}