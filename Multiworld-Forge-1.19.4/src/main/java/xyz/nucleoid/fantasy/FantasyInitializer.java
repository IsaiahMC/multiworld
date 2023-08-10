package xyz.nucleoid.fantasy;

//import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registry;
import xyz.nucleoid.fantasy.util.VoidChunkGenerator;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.common.MinecraftForge;
// import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.*;
import net.minecraftforge.event.server.*;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;

//@Mod("fantasy")
public final class FantasyInitializer {
   
	public static boolean after_tick_start = false;
	
    public MinecraftServer mc;
    public FantasyInitializer() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    //@Override
    public void onInitialize() {
        //Registry.register(Registry.CHUNK_GENERATOR, new Identifier(Fantasy.ID, "void"), VoidChunkGenerator.CODEC);
    }
    
    @SubscribeEvent
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
    public void handleTickEvent(TickEvent.ServerTickEvent event) {
       if (event.phase == TickEvent.Phase.START) {
            //MinecraftServer server = event.getServer();
            Fantasy fantasy = Fantasy.get(mc);
            fantasy.tick();
            for (ServerWorld w : fantasy.worldManager.worldss.values()) {
            	w.tick(() -> true);
            }
        }
    }
    
    @SubscribeEvent
    public void handleServerStop(ServerLifecycleEvent event) {
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
