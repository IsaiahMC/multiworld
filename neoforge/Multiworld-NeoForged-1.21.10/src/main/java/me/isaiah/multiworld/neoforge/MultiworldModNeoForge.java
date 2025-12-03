/**
 * Multiworld Mod
 */
package me.isaiah.multiworld.neoforge;

import me.isaiah.multiworld.I18n;
import me.isaiah.multiworld.MultiworldMod;
import me.isaiah.multiworld.command.PortalCommand;
import me.isaiah.multiworld.portal.Portal;
import me.isaiah.multiworld.portal.WandEventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.TeleportTarget.PostDimensionTransition;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;


@Mod(MultiworldMod.MOD_ID)
public class MultiworldModNeoForge {

	public MultiworldModNeoForge(IEventBus modEventBus, ModContainer modContainer) {
		modEventBus.addListener(this::commonSetup);

		NeoForge.EVENT_BUS.register(this);

		new xyz.nucleoid.fantasy.FantasyInitializer(modEventBus);
		NeoForgeWorldCreator.init();
		PermForge.init();
		MultiworldMod.init();
	}

	public void commonSetup(final FMLCommonSetupEvent event) {
	}

	@SubscribeEvent
	public void onServerStarting(ServerStartingEvent event) {
		MultiworldMod.on_server_started(event.getServer());
	}

	@SubscribeEvent
	public void onCommandsRegister(RegisterCommandsEvent event) {
		MultiworldMod.register_commands(event.getDispatcher());
	}
	
	@SubscribeEvent
    public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        WandEventHandler.leftClickBlock(event.getEntity(), event.getLevel(), event.getPos());
    }

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        WandEventHandler.rightClickBlock(event.getEntity(), event.getLevel(), event.getHitVec());
    }
    
    @SubscribeEvent
    public void myEntityTravel(EntityTravelToDimensionEvent event) {
    	Entity entity = event.getEntity();

        if (!(entity instanceof ServerPlayerEntity player)) {
            return;
        }
    	
    	I18n.message((ServerPlayerEntity) (Object) event.getEntity(), "EntityTravelToDimensionEvent");
    	I18n.message((ServerPlayerEntity) (Object) event.getEntity(), "KP: " + PortalCommand.KNOWN_PORTALS.values());
    }
    
    // @SubscribeEvent
    public static void onEntityTravel(EntityTravelToDimensionEvent event, ServerPlayerEntity plr) {
        Entity entity = event.getEntity();

        if (!(entity instanceof ServerPlayerEntity player)) {
            return;
        }

        BlockPos pos = player.getBlockPos();
        final BlockPos currentPos = player.getBlockPos();

        for (Portal p : PortalCommand.KNOWN_PORTALS.values()) {
            BlockPos min = p.getMinPos();
            BlockPos max = p.getMaxPos();

            boolean isInside = pos.getX() >= min.getX() && pos.getX() <= max.getX() &&
                               pos.getY() >= min.getY() && pos.getY() <= max.getY() &&
                               pos.getZ() >= min.getZ() && pos.getZ() <= max.getZ();

            if (isInside) {
                I18n.message(player, I18n.TELEPORTING);

                BlockPos dest = p.getDestLocation();
                // RegistryKey<World> destWorldKey = p.getDestWorld().getRegistryKey();
                // double distSq = player.getPos().squaredDistanceTo(Vec3d.ofCenter(dest));
                // boolean isAlreadyThere = player.getWorld().getRegistryKey().equals(destWorldKey) && (distSq < 1.0 || currentPos.equals(dest));

                event.setCanceled(true);

                teleleport(
                    player,
                    p.getDestWorld(),
                    dest.getX(),
                    dest.getY(),
                    dest.getZ()
                );

                // event.setCanceled(true); // Prevent vanilla teleport
                return;
            }
        }
    }
    
    public static MyPostDimensionTransition NO_OP = new MyPostDimensionTransition();
    
    public static class MyPostDimensionTransition implements PostDimensionTransition {

		@Override
		public void onTransition(Entity entity) {
			TeleportTarget.NO_OP.onTransition(entity);
		}
    	
    }
    
	public static void teleleport(ServerPlayerEntity player, ServerWorld world, double x, double y, double z) {
		TeleportTarget target = new TeleportTarget(world, new Vec3d(x, y, z), new Vec3d(0, 0, 0), 0f, 0f, NO_OP);
		player.teleportTo(target);
	}


}
