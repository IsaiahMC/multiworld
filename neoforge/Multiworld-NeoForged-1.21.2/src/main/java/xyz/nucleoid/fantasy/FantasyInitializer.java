package xyz.nucleoid.fantasy;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.*;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

//@Mod("fantasy")
public final class FantasyInitializer {
   
	public static boolean after_tick_start = false;
	
    public MinecraftServer mc;
    public FantasyInitializer(IEventBus modEventBus) {
        NeoForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::commonSetup);
    }

    //@Override
    public void onInitialize() {
        // Registry.register(Registry.CHUNK_GENERATOR, Identifier.of(Fantasy.ID, "void"), VoidChunkGenerator.CODEC);
        // Registry.register(Registries.CHUNK_GENERATOR, Identifier.of(Fantasy.ID, "transient"), TransientChunkGenerator.CODEC);
    }
    
    public void commonSetup(FMLCommonSetupEvent event) {
            onInitialize();
    }
    
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
      // mc = event.getServer();
    }
    
    @SubscribeEvent
    public void onCommandsRegister(RegisterCommandsEvent event) {
       
    }
    
    @SubscribeEvent
    public void handleStart(ServerAboutToStartEvent event) {
        mc = event.getServer();
    }
    
    @SubscribeEvent
    public void handle_started(ServerStartedEvent event) {
    	after_tick_start = true;
    }
    
    @SubscribeEvent
    public void handleTickEvent(ServerTickEvent.Pre event) {
        Fantasy fantasy = Fantasy.get(mc);
        fantasy.tick();
        for (ServerWorld w : fantasy.worldManager.worldss.values()) {
            w.tick(() -> true);
        }
    }
    
    @SubscribeEvent
    public void handleServerStop(ServerStoppingEvent event) {
        Fantasy fantasy = Fantasy.get(event.getServer());
        fantasy.onServerStopping();
    }
     

}
/*
package xyz.nucleoid.fantasy;

import net.fabricmc.api.ModInitializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import xyz.nucleoid.fantasy.util.VoidChunkGenerator;

public final class FantasyInitializer implements ModInitializer {
    @Override
    public void onInitialize() {
        Registry.register(Registries.CHUNK_GENERATOR, new Identifier(Fantasy.ID, "void"), VoidChunkGenerator.CODEC);
    }
}*/
