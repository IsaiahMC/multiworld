package xyz.nucleoid.fantasy;

//import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import xyz.nucleoid.fantasy.util.VoidChunkGenerator;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.*;
import net.minecraftforge.event.server.*;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraft.server.MinecraftServer;

//@Mod("fantasy")
public final class FantasyInitializer {
   
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
    public void handleTickEvent(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            //MinecraftServer server = event.getServer();
            Fantasy fantasy = Fantasy.get(mc);
            fantasy.tick();
        }
    }
    
    @SubscribeEvent
    public void handleServerStop(ServerLifecycleEvent event) {
        Fantasy fantasy = Fantasy.get(event.getServer());
        fantasy.onServerStopping();
    }
     

}
