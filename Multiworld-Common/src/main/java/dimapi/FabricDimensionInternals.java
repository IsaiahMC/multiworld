package dimapi;

import com.google.common.base.Preconditions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.PortalInfo;

/**
 * For 1.18.2 - 1.20.6
 * 
 * @implNote Removed in Fabric API 1.21
 */
public final class FabricDimensionInternals {

	/**
	 * The target passed to the last call to {@link FabricDimensions#teleport(Entity, ServerWorld, TeleportTarget)}.
	 */
	private static PortalInfo currentTarget;

	private FabricDimensionInternals() {
		throw new AssertionError();
	}

	/**
	 * Returns the last target set when a user of the API requested teleportation, or null.
	 */
	public static PortalInfo getCustomTarget() {
		return currentTarget;
	}

	@SuppressWarnings("unchecked")
	public static <E extends Entity> E changeDimension(E teleported, ServerLevel dimension, PortalInfo target) {
		Preconditions.checkArgument(!teleported.level().isClientSide, "Entities can only be teleported on the server side");
		Preconditions.checkArgument(Thread.currentThread() == ((ServerLevel) teleported.level()).getServer().getRunningThread(), "Entities must be teleported from the main server thread");

		try {
			currentTarget = target;
			return (E) teleported.changeDimension(dimension);
		} finally {
			currentTarget = null;
		}
	}

}
