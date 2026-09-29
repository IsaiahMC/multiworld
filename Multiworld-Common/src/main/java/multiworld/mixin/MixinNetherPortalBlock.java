package multiworld.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.isaiah.multiworld.I18n;
import me.isaiah.multiworld.MultiworldMod;
import me.isaiah.multiworld.command.PortalCommand;
import me.isaiah.multiworld.portal.Portal;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Note: Only applied for <=1.21.4
 */
@Mixin(NetherPortalBlock.class)
public class MixinNetherPortalBlock {

	/**
	 * TODO: check: 1.21.5 Changes this.
	 */
	@Inject(at = @At("HEAD"), method = "entityInside", cancellable = true)
	private void onEntityCollision( BlockState state, Level world, BlockPos pos, Entity entity, CallbackInfo ci) {
		
		
		if (!(entity instanceof ServerPlayer)) {
			return;
		}
		
		// Check if portal
		
		boolean is_our_portal = true;
		
		if (is_our_portal) {
			for (Portal p : PortalCommand.KNOWN_PORTALS.values()) {
				// boolean canPos = p.blocks.contains(pos);
				
				BlockPos min = p.getMinPos();
				BlockPos max = p.getMaxPos();

				boolean isInside = pos.getX() >= min.getX() && pos.getX() <= max.getX() &&
								pos.getY() >= min.getY() && pos.getY() <= max.getY() &&
								pos.getZ() >= min.getZ() && pos.getZ() <= max.getZ();

				
				if (isInside) {
					I18n.message((ServerPlayer) entity, I18n.TELEPORTING);
					
					BlockPos dest = p.getDestLocation();
					
					MultiworldMod.get_world_creator().teleleport(
							(ServerPlayer) entity,
							p.getDestWorld(),
							dest.getX(),
							dest.getY(),
							dest.getZ()
					);
					
					ci.cancel();
					return;
				}
			}
		}
		
	}

}