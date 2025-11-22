package me.isaiah.multiworld.neoforge;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import me.isaiah.multiworld.ICreator;
import me.isaiah.multiworld.MultiworldMod;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;

public class NeoForgeWorldCreator implements ICreator {
    
	public HashMap<String, RuntimeWorldConfig> worldConfigs;
	
	public NeoForgeWorldCreator() {
		this.worldConfigs = new HashMap<>();
	}
	
    public static void init() {
        MultiworldMod.setICreator(new NeoForgeWorldCreator());
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
        RuntimeWorldHandle worldHandle = fantasy.getOrOpenPersistentWorld(Identifier.of(id), config);
        this.worldConfigs.put(id, config);
        return worldHandle.asWorld();
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
        RuntimeWorldHandle worldHandle = fantasy.getOrOpenPersistentWorld(Identifier.of(id), null);
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
		return world.getLevelProperties().getSpawnPos();
	}

	@Override
	public void teleleport(ServerPlayerEntity player, ServerWorld world, double x, double y, double z) {
		TeleportTarget target = new TeleportTarget(world, new Vec3d(x, y, z), new Vec3d(0, 0, 0), 0f, 0f, TeleportTarget.NO_OP);
		// FabricDimensionInternals.changeDimension(player, world, target);

		// Per https://fabricmc.net/2024/05/31/121.html
		// for 1.21, FabricDimension API is replaced by teleportTo
		player.teleportTo(target);}
	
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
		return this.get_flat_chunk_gen(mc);
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