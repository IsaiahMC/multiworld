package me.isaiah.multiworld.fabric;

import me.isaiah.multiworld.MultiworldMod;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class MultiworldModFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        PermFabric.init();
        FabricWorldCreator.init();

        ServerLifecycleEvents.SERVER_STARTED.register(mc -> {
            MultiworldMod.on_server_started(mc);
        });
        
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            MultiworldMod.register_commands(dispatcher);
        });

        MultiworldMod.init();
        
    }

}