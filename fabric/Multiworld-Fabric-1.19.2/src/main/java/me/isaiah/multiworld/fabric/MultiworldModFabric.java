package me.isaiah.multiworld.fabric;

import me.isaiah.multiworld.MultiworldMod;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class MultiworldModFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        PermFabric.init();
        FabricWorldCreator.init();

        ServerLifecycleEvents.SERVER_STARTED.register(mc -> {
            MultiworldMod.on_server_started(mc);
        });

        CommandRegistrationCallback.EVENT.register(
            (dispatcher, registryAccess, environment) -> {
                MultiworldMod.register_commands(dispatcher);
            }
        );

        MultiworldMod.init();
        versionSupportMessage();
    }
    
    private void versionSupportMessage() {
    	MultiworldMod.LOGGER.info("WARNING - Multiworld Fabric 1.19.2 support is limited. Please upgrade your server.");
    }
    
}
