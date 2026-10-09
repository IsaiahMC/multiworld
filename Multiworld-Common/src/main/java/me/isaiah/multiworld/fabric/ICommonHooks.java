package me.isaiah.multiworld.fabric;

import me.isaiah.common.ICommonMod;
import me.isaiah.common.ICommonMod.SupportStatus;
import me.isaiah.common.event.EventHandler;
import me.isaiah.common.event.EventRegistery;
import me.isaiah.common.event.entity.EntityPortalCollideEvent;
import me.isaiah.multiworld.I18n;
import me.isaiah.multiworld.MultiworldMod;
import me.isaiah.multiworld.command.PortalCommand;
import me.isaiah.multiworld.portal.Portal;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ICommonHooks {
	
	/*
	@Deprecated
	public static boolean hasICommon() {
		Optional<ModContainer> container = FabricLoader.getInstance().getModContainer("icommon");
		
		if (!container.isPresent()) {
			return false;
		}

		// TODO: Check ICommonMod.API_VERSION

		return true;
	}
	*/
	
	public static Level getWorld(Player player) {
		try {
			// #if mc261 mc262 mc263
			// // empty 
			// #elif mc182
			// // empty
			// #else
			Level world = ((me.isaiah.common.cmixin.IMixinEntity) player).ic$getWorld();
			return world;
			// #endif
		} catch (Exception | NoSuchMethodError ex) {
			// Older version of iCommonLib
		}
		return null;
	}
	
	public static void register() {
		new ICommonHooks().registerThis();
	}
	
	public void registerThis() {
		if (!ICommonCheck.hasICommon()) {
			MultiworldMod.LOGGER.info("Note: iCommonLib is required for full functionality of mod");
			return;
		}
	
		boolean isLower = ICommonMod.checkVersion(0.6, false, "multiworld", SupportStatus.SUGGEST);
		
		if (isLower) {
			MultiworldMod.LOGGER.info("Note: iCommonLib #106 or higher is suggested.");
		}
		
		int r = EventRegistery.registerAll(this);
        MultiworldMod.LOGGER.info("Multiworld: Registered '" + r + "' iCommon events.");
	}
	
	// #if mc261 mc262 mc263
	// // TODO: 26.1: Update iCommon EntityPortalCollideEvent
	// #elif mc182
	// // TODO: 1.18.2 icommon portal enter event
	// #else
	@EventHandler
	public void onPortalEnter(EntityPortalCollideEvent ev) {
		if (!(ev.getEntity() instanceof ServerPlayer)) {
			return;
		}
		
		// // Check if portal
		
		boolean is_our_portal = true;
		
		Entity entity = ev.getEntity();
		BlockPos pos = ev.getBlockPos();

		if (is_our_portal) {
			for (Portal p : PortalCommand.KNOWN_PORTALS.values()) {
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
					
					ev.setCanceled(true);
					return;
				}
			}
		}
	}
	// #endif
	
}