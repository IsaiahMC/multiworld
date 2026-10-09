package me.isaiah.multiworld.fabric;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

import me.isaiah.multiworld.ICreator;
import me.isaiah.multiworld.MultiworldMod;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.level.validation.ContentValidationException;
import net.minecraft.world.phys.Vec3;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;
import xyz.nucleoid.fantasy.util.VoidChunkGenerator;

public class FabricWorldCreator implements ICreator {

    public Identifier new_id(String id) {
    	return Identifier.parse(id);
    }

	public HashMap<String, RuntimeWorldConfig> worldConfigs;
	
	public FabricWorldCreator() {
		this.worldConfigs = new HashMap<>();
	}
	
    public static void init() {
        MultiworldMod.setICreator(new FabricWorldCreator());
    }

    public ServerLevel create_world(String id, Identifier dim, ChunkGenerator gen, Difficulty dif, long seed) {
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
				.setSunny(0)   // set clearWeatherTime to 0 for enable weather default minecraft behavior
				.setMirrorOverworldGameRules(false)   // per-dimension gamerules (don't share the overworld's)
				.setMirrorOverworldDifficulty(false)  // per-dimension difficulty (don't share the overworld's)
                ;

        Fantasy fantasy = Fantasy.get(MultiworldMod.mc);
        RuntimeWorldHandle worldHandle = fantasy.getOrOpenPersistentWorld(new_id(id), config);
        this.worldConfigs.put(id, config);
        ServerLevel world = worldHandle.asWorld();
        
        if (null != rules) {
        	world.getGameRules().assignFrom(rules, null);
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
    	try {
			return MultiworldWorld.mw$readGameRules(MultiworldMod.mc, id);
		} catch (IOException | ContentValidationException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			throw new IOException(e);
		}
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
        RuntimeWorldHandle worldHandle = fantasy.getOrOpenPersistentWorld(new_id(id), null);
        worldHandle.delete();
    }

	@Override
	public boolean is_the_end(ServerLevel world) {
		return world.dimensionTypeRegistration() == BuiltinDimensionTypes.END;
	}

	@Override
	public BlockPos get_pos(double x, double y, double z) {
		return BlockPos.containing(x, y, z);
	}
	
	@Override
	public BlockPos get_spawn(ServerLevel world) {
		return world.getLevelData().getSpawnPos();
	}
	
	@Override
	public void teleleport(ServerPlayer player, ServerLevel world, double x, double y, double z) {
        DimensionTransition target = new DimensionTransition(world, new Vec3(x, y, z), new Vec3(0, 0, 0), 0f, 0f, DimensionTransition.DO_NOTHING);
        
        // FabricDimensionInternals.changeDimension(player, world, target);
        
        // Per https://fabricmc.net/2024/05/31/121.html
        // for 1.21, FabricDimension API is replaced by teleportTo
        player.changeDimension(target);
	}
	
	@Override
	public ChunkGenerator get_flat_chunk_gen(MinecraftServer mc) {
		var biome = mc.registryAccess().registryOrThrow(Registries.BIOME).wrapAsHolder(mc.registryAccess().registryOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS));
        FlatLevelGeneratorSettings flat = new FlatLevelGeneratorSettings(Optional.empty(), biome, Collections.emptyList());
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
	
	@Override
	public ChunkGenerator get_void_chunk_gen(MinecraftServer mc) {
		// var biome = mc.getRegistryManager().getOrThrow(RegistryKeys.BIOME).getOrThrow(BiomeKeys.THE_VOID);
		VoidChunkGenerator gen = new xyz.nucleoid.fantasy.util.VoidChunkGenerator(mc);
        return gen;
	}
	
	@Override
	public boolean permissionLevel(CommandSourceStack source, int level) {
		return source.hasPermission(level);
	}

	@Override
	public boolean permissionLevel(ServerPlayer plr, int level) {
		return plr.hasPermissions(level);
	}

}