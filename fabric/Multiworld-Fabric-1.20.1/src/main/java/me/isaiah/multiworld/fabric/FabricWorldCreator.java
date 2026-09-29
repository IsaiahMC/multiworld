package me.isaiah.multiworld.fabric;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import dimapi.FabricDimensionInternals;
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
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.Vec3;
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

    public ServerLevel create_world(String id, Identifier dim, ChunkGenerator gen, Difficulty dif, long seed) {
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
        return worldHandle.asWorld();
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
        RuntimeWorldHandle worldHandle = fantasy.getOrOpenPersistentWorld(new Identifier(id), null);
        worldHandle.delete();
    }

	@Override
	public boolean is_the_end(ServerLevel world) {
		return world.dimensionTypeId() == BuiltinDimensionTypes.END;
	}

	@Override
	public BlockPos get_pos(double x, double y, double z) {
		return BlockPos.containing(x, y, z);
	}
	
	@Override
	public BlockPos get_spawn(ServerLevel world) {
		LevelData prop = world.getLevelData();
		return new BlockPos(prop.getXSpawn(), prop.getYSpawn(), prop.getZSpawn());
	}
	
	@Override
	public void teleleport(ServerPlayer player, ServerLevel world, double x, double y, double z) {
        PortalInfo target = new PortalInfo(new Vec3(x, y, z), new Vec3(0, 0, 0), 0f, 0f);
        FabricDimensionInternals.changeDimension(player, world, target);
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
		var biome = mc.registryAccess().registryOrThrow(Registries.BIOME).wrapAsHolder(mc.registryAccess().registryOrThrow(Registries.BIOME).getOrThrow(Biomes.THE_VOID));
		VoidChunkGenerator gen = new xyz.nucleoid.fantasy.util.VoidChunkGenerator(biome);
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