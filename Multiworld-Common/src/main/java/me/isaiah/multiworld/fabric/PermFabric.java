package me.isaiah.multiworld.fabric;

import java.util.NoSuchElementException;

import me.isaiah.multiworld.MultiworldMod;
import me.isaiah.multiworld.perm.Perm;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;

public class PermFabric extends Perm {
    
    public static void init() {
        Perm.setPerm(new PermFabric());
        
        try {
        	String version = FabricLoader.getInstance().getModContainer("multiworld").get().getMetadata().getVersion().getFriendlyString();
        	MultiworldMod.VERSION = version;
        } catch (NoSuchElementException e) {
        	// Error while getting version
        }
        
        // TODO: move this
        ICommonHooks.register();
        FabricEvents.register();
    }

    @Override
    public boolean has_impl(ServerPlayer plr, String perm) {
        
        // #if mc182
        // if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
        //	return true;
        // }
        // #endif
    	
    	boolean cyber = FabricLoader.getInstance().getModContainer("cyber-permissions").isPresent();
        boolean luck =  FabricLoader.getInstance().getModContainer("fabric-permissions-api-v0").isPresent();
        
        boolean res = Perm.permissionLevel(plr, 2); // plr.hasPermissionLevel(2);

        if (cyber) {
        	// if (CyberHandler.hasPermission(plr, perm)) res = true;
        }

        if (luck) {
            if (LuckHandler.hasPermission(plr, perm)) res = true;
        }

        return res;
    }

}