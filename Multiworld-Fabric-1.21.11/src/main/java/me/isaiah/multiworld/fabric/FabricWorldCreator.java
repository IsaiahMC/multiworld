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
import net.minecraft.command.DefaultPermissions;
import net.minecraft.command.permission.Permission;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.RegistryWrapper.Impl;
import net.minecraft.resource.DataConfiguration;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.path.SymlinkFinder;
import net.minecraft.util.path.SymlinkValidationException;
import net.minecraft.world.Difficulty;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelStorage.Session;
import net.minecraft.world.rule.GameRule;
import net.minecraft.world.rule.GameRuleType;
import net.minecraft.world.rule.GameRules;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeWorld;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;
import xyz.nucleoid.fantasy.util.VoidChunkGenerator;

public class FabricWorldCreator implements ICreator {

    public Identifier new_id(String id) {
    	return Identifier.of(id);
    }

	public HashMap<String, RuntimeWorldConfig> worldConfigs;
	
	public FabricWorldCreator() {
		this.worldConfigs = new HashMap<>();
	}
	
    public static void init() {
        MultiworldMod.setICreator(new FabricWorldCreator());
    }
    
    private RuntimeWorld.Constructor worldConstructor = MultiworldWorld::new;

    public ServerWorld create_world(String id, Identifier dim, ChunkGenerator gen, Difficulty dif, long seed) {
        
    	Identifier idd = new_id(id);
    	GameRules rules = null;
		try {
			rules = readGameRules(idd);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
    	
    	RuntimeWorldConfig config = new RuntimeWorldConfig()
                .setDimensionType(dim_of(dim))
                .setGenerator(gen)
                .setDifficulty(dif)
				.setSeed(seed)
				.setShouldTickTime(true)
				.setWorldConstructor(MultiworldWorld::new)
                ;

        if (gen instanceof CustomFlatChunkGenerator) {
        	config.setFlat(true);
        }

        Fantasy fantasy = Fantasy.get(MultiworldMod.mc);
        RuntimeWorldHandle worldHandle = fantasy.getOrOpenPersistentWorld(idd, config);
        ServerWorld world = worldHandle.asWorld();
        
        if (null != rules) {
        	final GameRules rulesFinal = rules;
	        rules.streamRules().forEach(rule -> {
				if (rule.getType() == GameRuleType.BOOL) {
					world.getGameRules().setValue( (GameRule<Boolean>) rule, rulesFinal.getValue((GameRule<Boolean>) rule), null);
				}
	
				if (rule.getType() == GameRuleType.INT) {
					world.getGameRules().setValue( (GameRule<Integer>) rule, rulesFinal.getValue((GameRule<Integer>) rule), null);
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
     * @throws SymlinkValidationException 
     */
    public static GameRules readGameRules(Identifier id) throws IOException {

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
    }
    
    @Override
    public void set_difficulty(String id, Difficulty dif) {
    	this.worldConfigs.get(id).setDifficulty(dif);
    }
    
    private static RegistryKey<DimensionType> dim_of(Identifier id) {
        return RegistryKey.of(RegistryKeys.DIMENSION_TYPE, id);
    }
    
    public void delete_world(String id) {
        Fantasy fantasy = Fantasy.get(MultiworldMod.mc);
        RuntimeWorldHandle worldHandle = fantasy.getOrOpenPersistentWorld(new_id(id), null);
        worldHandle.delete();
    }

	@Override
	public boolean is_the_end(ServerWorld world) {
		return world.getDimensionEntry() == DimensionTypes.THE_END;
	}

	@Override
	public BlockPos get_pos(double x, double y, double z) {
		return BlockPos.ofFloored(x, y, z);
	}
	
	@Override
	public BlockPos get_spawn(ServerWorld world) {
		
		return world.getSpawnPoint().getPos();
		
		// return world.getLevelProperties().getSpawnPos();
	}

	@Override
	public void teleleport(ServerPlayerEntity player, ServerWorld world, double x, double y, double z) {
        TeleportTarget target = new TeleportTarget(world, new Vec3d(x, y, z), new Vec3d(0, 0, 0), 0f, 0f, TeleportTarget.NO_OP);
        
        // FabricDimensionInternals.changeDimension(player, world, target);
        
        // Per https://fabricmc.net/2024/05/31/121.html
        // for 1.21, FabricDimension API is replaced by teleportTo
        player.teleportTo(target);
	}
	
	@Override
	public ChunkGenerator get_void_chunk_gen(MinecraftServer mc) {
		var biome = mc.getRegistryManager().getOrThrow(RegistryKeys.BIOME).getOrThrow(BiomeKeys.THE_VOID);
		VoidChunkGenerator gen = new xyz.nucleoid.fantasy.util.VoidChunkGenerator(biome);
        return gen;
	}

	@Override
	public ChunkGenerator get_flat_chunk_gen(MinecraftServer mc) {
		var biome = mc.getRegistryManager().getOrThrow(RegistryKeys.BIOME).getOrThrow(BiomeKeys.PLAINS);
        FlatChunkGeneratorConfig flat = new FlatChunkGeneratorConfig(Optional.empty(), biome, Collections.emptyList());

        flat.enableFeatures();
        FlatChunkGenerator generator = new CustomFlatChunkGenerator(flat);

        return generator;
	}
	
	// Custom Flat Gen
	class CustomFlatChunkGenerator extends FlatChunkGenerator {
		public CustomFlatChunkGenerator(FlatChunkGeneratorConfig config) {
			super(config);
		}
		
		@Override
		public int getMinimumY() {
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
	public boolean permissionLevel(ServerCommandSource source, int level) {
		
		if (level == 0) {
			// Should not be 0
			return true;
		}
		
		Permission perm = DefaultPermissions.MODERATORS;

		switch (level) {
			case 1:
				perm = DefaultPermissions.MODERATORS;
				break;
			case 2:
				perm = DefaultPermissions.GAMEMASTERS;
				break;
			case 3:
				perm = DefaultPermissions.ADMINS;
				break;
			case 4:
				perm = DefaultPermissions.OWNERS;
				break;
		}

		return source.getPermissions().hasPermission(perm);
	}

	@Override
	public boolean permissionLevel(ServerPlayerEntity plr, int level) {

		if (level == 1) { return plr.getPermissions().hasPermission(DefaultPermissions.MODERATORS); }
		if (level == 2) { return plr.getPermissions().hasPermission(DefaultPermissions.GAMEMASTERS); }
		if (level == 3) { return plr.getPermissions().hasPermission(DefaultPermissions.ADMINS); }
		
		return plr.isCreativeLevelTwoOp();
		
		//return plr.hasPermissionLevel(level);
	}

}