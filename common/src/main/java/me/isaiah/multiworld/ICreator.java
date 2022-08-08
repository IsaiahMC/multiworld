package me.isaiah.multiworld;

import net.minecraft.util.registry.Registry;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.Difficulty;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public interface ICreator {
    
    public ServerWorld create_world(String id, RegistryKey<DimensionType> dim, ChunkGenerator gen, Difficulty dif, long seed);
	
	public default Text colored_literal(String txt, Formatting color) {
		try {
			return Text.of(txt).copy().formatted(color);
		} catch (Exception | IncompatibleClassChangeError e) {
			// MutableText interface was changed to a class in 1.19;
			// Incase for 1.18:
			return Text.of(txt);
		}
	}

}