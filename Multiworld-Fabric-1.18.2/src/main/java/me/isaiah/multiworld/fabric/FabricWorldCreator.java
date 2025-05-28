package me.isaiah.multiworld.fabric;

import java.util.HashMap;
import java.util.Optional;

import dimapi.FabricDimensionInternals;
import me.isaiah.multiworld.ICreator;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;
import net.minecraft.world.Difficulty;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureSet;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;
import xyz.nucleoid.fantasy.util.VoidChunkGenerator;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import me.isaiah.multiworld.MultiworldMod;
import net.minecraft.util.Formatting;
import net.minecraft.text.*;


public class FabricWorldCreator implements ICreator {
    
	public HashMap<String, RuntimeWorldConfig> worldConfigs;
	
	public FabricWorldCreator() {
		this.worldConfigs = new HashMap<>();
	}
	
    public static void init() {
        MultiworldMod.setICreator(new FabricWorldCreator());
    }

    public ServerWorld create_world(String id, Identifier dim, ChunkGenerator gen, Difficulty dif, long seed) {
        RuntimeWorldConfig config = new RuntimeWorldConfig()
                .setDimensionType(dim_of(dim))
                .setGenerator(gen)
                .setDifficulty(dif)
				.setSeed(seed)
				.setShouldTickTime(true)
                ;

        Fantasy fantasy = Fantasy.get(MultiworldMod.mc);
        RuntimeWorldHandle worldHandle = fantasy.getOrOpenPersistentWorld(new Identifier(id), config);
        this.worldConfigs.put(id, config);
        return worldHandle.asWorld();
    }
    
    @Override
    public void set_difficulty(String id, Difficulty dif) {
    	this.worldConfigs.get(id).setDifficulty(dif);
    }
    
    private static RegistryKey<DimensionType> dim_of(Identifier id) {
        return RegistryKey.of(Registry.DIMENSION_TYPE_KEY, id);
    }
    
    public void delete_world(String id) {
        Fantasy fantasy = Fantasy.get(MultiworldMod.mc);
        RuntimeWorldHandle worldHandle = fantasy.getOrOpenPersistentWorld(new Identifier(id), null);
        worldHandle.delete();
    }
	
	@Override
	public Text colored_literal(String txt, Formatting color) {
		try {
			return Text.of(txt).copy().formatted(color);
		} catch (Exception | IncompatibleClassChangeError e) {
			// Fallback
			return Text.of(txt);
		}
	}

	@Override
	public boolean is_the_end(ServerWorld world) {
		if (!world.getDimension().isBedWorking() && !world.getDimension().hasCeiling()) {
	        return true;
		}
		
		return false;
	}

	@Override
	public BlockPos get_pos(double x, double y, double z) {
		return new BlockPos(x, y, z);
	}

	@Override
	public BlockPos get_spawn(ServerWorld world) {
		WorldProperties prop = world.getLevelProperties();
		return new BlockPos(prop.getSpawnX(), prop.getSpawnY(), prop.getSpawnZ());
	}
	
	@Override
	public void teleleport(ServerPlayerEntity player, ServerWorld world, double x, double y, double z) {
        TeleportTarget target = new TeleportTarget(new Vec3d(x, y, z), new Vec3d(1, 1, 1), 0f, 0f);
        FabricDimensionInternals.changeDimension(player, world, target);
	}

	@Override
	public ChunkGenerator get_flat_chunk_gen(MinecraftServer mc) {
		Registry<StructureSet> str = mc.getRegistryManager().get(Registry.STRUCTURE_SET_KEY);
		Registry<Biome> biome = mc.getRegistryManager().get(Registry.BIOME_KEY); //.getEntry(0).get();
        FlatChunkGeneratorConfig flat = new FlatChunkGeneratorConfig(Optional.empty(), biome);
        flat.enableFeatures();
        flat.setBiome( biome.getEntry(0).get() );;
        FlatChunkGenerator generator = new FlatChunkGenerator(str, flat);
        return generator;
	}
	
	// Custom Flat Gen
	class CustomFlatChunkGenerator extends FlatChunkGenerator {
		public CustomFlatChunkGenerator(Registry<StructureSet> str, FlatChunkGeneratorConfig config) {
			super(str, config);
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
		Registry<Biome>  biome1 = mc.getRegistryManager().get(Registry.BIOME_KEY);
		RegistryEntry<Biome> biome = biome1.getEntry(BiomeKeys.THE_VOID).get();
		VoidChunkGenerator gen = new xyz.nucleoid.fantasy.util.VoidChunkGenerator(biome);
        return gen;
	}

}