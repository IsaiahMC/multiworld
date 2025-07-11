package me.isaiah.multiworld.portal;

import java.util.StringJoiner;
import java.util.regex.Pattern;

import me.isaiah.multiworld.MultiworldMod;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class PortalUtil {

	public static String extractIdentifier(String input) {
	    if (!input.startsWith("e:")) return input;

	    String trimmed = input.substring(2); // remove "e:"
	    String[] parts = trimmed.split(":");

	    // Remove last 1 or 2 segments if they are comma-separated values
	    int removeCount = 0;
	    for (int i = parts.length - 1; i >= 0 && removeCount < 2; i--) {
	        if (parts[i].contains(",")) removeCount++;
	    }

	    // Build result string with remaining parts
	    StringJoiner joiner = new StringJoiner(":");
	    for (int i = 0; i < parts.length - removeCount; i++) {
	        joiner.add(parts[i]);
	    }

	    return joiner.toString();
	}
	
	public static BlockPos findSafeExit(ServerWorld world, BlockPos origin, int radius, int maxY) {
	    for (int y = origin.getY(); y < maxY; y++) {
	        for (int dx = -radius; dx <= radius; dx++) {
	            for (int dz = -radius; dz <= radius; dz++) {
	                BlockPos checkPos = origin.add(dx, y - origin.getY(), dz);

	                BlockState floor = world.getBlockState(checkPos);
	                BlockState head = world.getBlockState(checkPos.up());
	                BlockState aboveHead = world.getBlockState(checkPos.up(2));

	                boolean isSafe = floor.isSolidBlock(world, checkPos) &&
	                                 head.isAir() &&
	                                 aboveHead.isAir();

	                if (isSafe) {
	                    return checkPos.up(); // Return position where player's feet will land
	                }
	            }
	        }
	    }

	    // Fallback: return original position if no safe spot found
	    return origin;
	}
	
	public static BlockPos getCenterWithLowestY(BlockPos pos1, BlockPos pos2, int yOffset) {
	    int centerX = (pos1.getX() + pos2.getX()) / 2;
	    int centerZ = (pos1.getZ() + pos2.getZ()) / 2;
	    int lowestY = Math.min(pos1.getY(), pos2.getY()) + yOffset;

	    return new BlockPos(centerX, lowestY, centerZ);
	}

	
	public static BlockPos getMinPos(BlockPos a, BlockPos b) {
	    return new BlockPos(
	        Math.min(a.getX(), b.getX()),
	        Math.min(a.getY(), b.getY()),
	        Math.min(a.getZ(), b.getZ())
	    );
	}

	public static BlockPos getMaxPos(BlockPos a, BlockPos b) {
	    return new BlockPos(
	        Math.max(a.getX(), b.getX()),
	        Math.max(a.getY(), b.getY()),
	        Math.max(a.getZ(), b.getZ())
	    );
	}

	public static BlockPos blockPosFrom(String line) {
		String[] from = line.split(Pattern.quote(","));
		
		double x1 = Double.valueOf(from[0]);
		double y1 = Double.valueOf(from[1]);
		double z1 = Double.valueOf(from[2]);
		BlockPos a = MultiworldMod.get_world_creator().get_pos(x1, y1, z1);
		return a;
	}
	
}
