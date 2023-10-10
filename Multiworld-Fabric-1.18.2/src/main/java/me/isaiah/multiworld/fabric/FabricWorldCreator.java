package me.isaiah.multiworld.fabric;

import me.isaiah.multiworld.ICreator;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.Difficulty;
import net.minecraft.server.world.ServerWorld;

import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import me.isaiah.multiworld.MultiworldMod;
import net.minecraft.util.Formatting;
import net.minecraft.text.*;


public class FabricWorldCreator implements ICreator {
    
    public static void init() {
        MultiworldMod.setICreator(new FabricWorldCreator());
    }

    public ServerWorld create_world(String id, Identifier dim, ChunkGenerator gen, Difficulty dif, long seed) {
        RuntimeWorldConfig config = new RuntimeWorldConfig()
                .setDimensionType(dim_of(dim))
                .setGenerator(gen)
                .setDifficulty(Difficulty.NORMAL)
				.setSeed(seed)
				.setShouldTickTime(true)
                ;

        Fantasy fantasy = Fantasy.get(MultiworldMod.mc);
        RuntimeWorldHandle worldHandle = fantasy.getOrOpenPersistentWorld(new Identifier(id), config);
        return worldHandle.asWorld();
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

}