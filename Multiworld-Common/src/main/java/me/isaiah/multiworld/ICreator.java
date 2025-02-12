package me.isaiah.multiworld;

import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public interface ICreator {

	/**
	 */
	public ServerWorld create_world(String id, Identifier dim, ChunkGenerator gen, Difficulty dif, long seed);

	/**
	 */
	public BlockPos get_pos(double x, double y, double z);

	/**
	 */
	public default Text colored_literal(String txt, Formatting color) {
		try {
			return Text.of(txt).copy().formatted(color);
		} catch (Exception | IncompatibleClassChangeError e) {
			// MutableText interface was changed to a class in 1.19;
			// Incase for 1.18:
			return Text.of(txt);
		}
	}

	/**
	 */
	void teleleport(ServerPlayerEntity player, ServerWorld world, double x, double y, double z);

	/**
	 */
	void set_difficulty(String id, Difficulty dif);

    /**
     * Return a {@link ChunkGenerator} for the given vanilla environment,
     * or NULL if the passed argument is not NORMAL / NETHER / END.
     */
    default ChunkGenerator get_chunk_gen(MinecraftServer mc, String env) {
    	ChunkGenerator gen = null;
    	if (env.contains("NORMAL")) {
			gen = mc.getWorld(World.OVERWORLD).getChunkManager().getChunkGenerator(); // .withSeed(seed);
		}

		if (env.contains("NETHER")) {
			gen = mc.getWorld(World.NETHER).getChunkManager().getChunkGenerator();
		}
		
		if (env.contains("END")) {
			gen = mc.getWorld(World.END).getChunkManager().getChunkGenerator(); // .withSeed(seed);
		}

		return gen;
    } 

	// TODO: move to icommonlib:
	public BlockPos get_spawn(ServerWorld world);
	public boolean is_the_end(ServerWorld world);
	
}