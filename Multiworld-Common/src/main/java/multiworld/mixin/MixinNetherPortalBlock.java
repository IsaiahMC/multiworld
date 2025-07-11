package multiworld.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.isaiah.multiworld.I18n;
import me.isaiah.multiworld.MultiworldMod;
import me.isaiah.multiworld.command.PortalCommand;
import me.isaiah.multiworld.portal.Portal;
import net.minecraft.block.BlockState;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(NetherPortalBlock.class)
public class MixinNetherPortalBlock {
	
	
	
	/**
	 * TODO: check: 1.21.5 Changes this.
	 */
	@Inject(at = @At("HEAD"), method = "onEntityCollision", cancellable = true)
	private void onEntityCollision( BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo ci) {
		
		
		if (!(entity instanceof ServerPlayerEntity)) {
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
					I18n.message((ServerPlayerEntity) entity, I18n.TELEPORTING);
					
					BlockPos dest = p.getDestLocation();
					
					MultiworldMod.get_world_creator().teleleport(
							(ServerPlayerEntity) entity,
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