package me.isaiah.multiworld;

import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.Difficulty;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public interface ICreator {

	public ServerWorld create_world(String id, Identifier dim, ChunkGenerator gen, Difficulty dif, long seed);

	public boolean is_the_end(ServerWorld world);

	public BlockPos get_pos(double x, double y, double z);

	public default Text colored_literal(String txt, Formatting color) {
		try {
			return Text.of(txt).copy().formatted(color);
		} catch (Exception | IncompatibleClassChangeError e) {
			// MutableText interface was changed to a class in 1.19;
			// Incase for 1.18:
			return Text.of(txt);
		}
	}

	public BlockPos get_spawn(ServerWorld world);

	void teleleport(ServerPlayerEntity player, ServerWorld world, double x, double y, double z);

	void set_difficulty(String id, Difficulty dif);

}