package me.isaiah.multiworld.fabric;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Dynamic;

import dimapi.FabricDimensionInternals;
import me.isaiah.multiworld.ICreator;
import me.isaiah.multiworld.MultiworldMod;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.DataConfiguration;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.path.SymlinkValidationException;
import net.minecraft.world.Difficulty;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.WorldProperties;
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
import net.minecraft.world.GameRules;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;
import xyz.nucleoid.fantasy.util.VoidChunkGenerator;

public class FabricWorldCreator implements ICreator {
    
	public HashMap<String, RuntimeWorldConfig> worldConfigs;
	
	public FabricWorldCreator() {
		this.worldConfigs = new HashMap<>();
	}
	
    public static void init() {
        MultiworldMod.setICreator(new FabricWorldCreator());
    }

    public ServerWorld create_world(String id, Identifier dim, ChunkGenerator gen, Difficulty dif, long seed) {
        
    	Identifier idd = new Identifier(id);
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

        Fantasy fantasy = Fantasy.get(MultiworldMod.mc);
        RuntimeWorldHandle worldHandle = fantasy.getOrOpenPersistentWorld(new Identifier(id), config);
        this.worldConfigs.put(id, config);
        ServerWorld world = worldHandle.asWorld();
        
        if (null != rules) {
        	world.getGameRules().setAllValues(rules, null);
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
            
            Registry<DimensionOptions> dimensionRegistry = MultiworldMod.mc.getRegistryManager().get(RegistryKeys.DIMENSION);
            
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
        RuntimeWorldHandle worldHandle = fantasy.getOrOpenPersistentWorld(new Identifier(id), null);
        worldHandle.delete();
    }

	@Override
	public boolean is_the_end(ServerWorld world) {
		return world.getDimensionKey() == DimensionTypes.THE_END;
	}

	@Override
	public BlockPos get_pos(double x, double y, double z) {
		return BlockPos.ofFloored(x, y, z);
	}
	
	@Override
	public BlockPos get_spawn(ServerWorld world) {
		WorldProperties prop = world.getLevelProperties();
		return new BlockPos(prop.getSpawnX(), prop.getSpawnY(), prop.getSpawnZ());
	}
	
	@Override
	public void teleleport(ServerPlayerEntity player, ServerWorld world, double x, double y, double z) {
        TeleportTarget target = new TeleportTarget(new Vec3d(x, y, z), new Vec3d(0, 0, 0), 0f, 0f);
        FabricDimensionInternals.changeDimension(player, world, target);
	}
	
	@Override
	public ChunkGenerator get_flat_chunk_gen(MinecraftServer mc) {
		var biome = mc.getRegistryManager().get(RegistryKeys.BIOME).getEntry(mc.getRegistryManager().get(RegistryKeys.BIOME).getOrThrow(BiomeKeys.PLAINS));
        FlatChunkGeneratorConfig flat = new FlatChunkGeneratorConfig(Optional.empty(), biome, Collections.emptyList());
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

	@Override
	public ChunkGenerator get_void_chunk_gen(MinecraftServer mc) {
		VoidChunkGenerator gen = new xyz.nucleoid.fantasy.util.VoidChunkGenerator(mc);
        return gen;
	}
	
	@Override
	public boolean permissionLevel(ServerCommandSource source, int level) {
		return source.hasPermissionLevel(level);
	}

	@Override
	public boolean permissionLevel(ServerPlayerEntity plr, int level) {
		return plr.hasPermissionLevel(level);
	}

}