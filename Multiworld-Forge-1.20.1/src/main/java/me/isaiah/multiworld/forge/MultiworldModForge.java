/**
 * Multiworld Mod
 */
package me.isaiah.multiworld.forge;

import me.isaiah.multiworld.MultiworldMod;
import me.isaiah.multiworld.portal.WandEventHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod(MultiworldMod.MOD_ID)
public class MultiworldModForge {

	public MultiworldModForge() {
		// Register ourselves for server and other game events we are interested in
		MinecraftForge.EVENT_BUS.register(this);
		new xyz.nucleoid.fantasy.FantasyInitializer();
		ForgeWorldCreator.init();
		PermForge.init();
		MultiworldMod.init();
	}

	@SubscribeEvent
	public void commonSetup(FMLCommonSetupEvent event) {
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

}
