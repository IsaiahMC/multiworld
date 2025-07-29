package me.isaiah.multiworld.fabric;

import java.util.Optional;

import me.isaiah.common.event.EventHandler;
import me.isaiah.common.event.EventRegistery;
import me.isaiah.common.event.entity.EntityPortalCollideEvent;
import me.isaiah.multiworld.I18n;
import me.isaiah.multiworld.MultiworldMod;
import me.isaiah.multiworld.command.PortalCommand;
import me.isaiah.multiworld.portal.Portal;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

public class ICommonHooks {
	
	public static boolean hasICommon() {
		Optional<ModContainer> container = FabricLoader.getInstance().getModContainer("icommon");
		
		if (!container.isPresent()) {
			return false;
		}

		// TODO: Check ICommonMod.API_VERSION

		return true;
	}
	
	public static void register() {
		new ICommonHooks().registerThis();
	}
	
	public void registerThis() {
		if (!hasICommon()) {
			MultiworldMod.LOGGER.info("Note: iCommonLib is required for full functionality of mod");
			return;
		}
		
		int r = EventRegistery.registerAll(this);
        MultiworldMod.LOGGER.info("Multiworld: Registered '" + r + "' iCommon events.");
	}
	
	@EventHandler
	public void onPortalEnter(EntityPortalCollideEvent ev) {
		if (!(ev.getEntity() instanceof ServerPlayerEntity)) {
			return;
		}
		
		// Check if portal
		
		boolean is_our_portal = true;
		
		Entity entity = ev.getEntity();
		BlockPos pos = ev.getBlockPos();

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
					
					ev.setCanceled(true);
					return;
				}
			}
		}
	}
	
}