package dimapi;

import com.google.common.base.Preconditions;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.TeleportTarget;

/**
 * For 1.18.2 - 1.20.6
 * 
 * @implNote Removed in Fabric API 1.21
 */
public final class FabricDimensionInternals {

	/**
	 * The target passed to the last call to {@link FabricDimensions#teleport(Entity, ServerWorld, TeleportTarget)}.
	 */
	private static TeleportTarget currentTarget;

	private FabricDimensionInternals() {
		throw new AssertionError();
	}

	/**
	 * Returns the last target set when a user of the API requested teleportation, or null.
	 */
	public static TeleportTarget getCustomTarget() {
		return currentTarget;
	}

	@SuppressWarnings("unchecked")
	public static <E extends Entity> E changeDimension(E teleported, ServerWorld dimension, TeleportTarget target) {
		Preconditions.checkArgument(!teleported.getWorld().isClient, "Entities can only be teleported on the server side");
		Preconditions.checkArgument(Thread.currentThread() == ((ServerWorld) teleported.getWorld()).getServer().getThread(), "Entities must be teleported from the main server thread");

		try {
			currentTarget = target;
			return (E) teleported.moveToWorld(dimension);
		} finally {
			currentTarget = null;
		}
	}

}
