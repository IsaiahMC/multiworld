package me.isaiah.multiworld.fabric;

import net.minecraft.server.network.ServerPlayerEntity;
import me.isaiah.multiworld.perm.Perm;
import net.fabricmc.loader.api.FabricLoader;

public class PermFabric extends Perm {
    
    public static void init() {
        Perm.setPerm(new PermFabric());
    }

    @Override
    public boolean has_impl(ServerPlayerEntity plr, String perm) {
        boolean cyber = FabricLoader.getInstance().getModContainer("cyber-permissions").isPresent();
        boolean luck =  FabricLoader.getInstance().getModContainer("fabric-permissions-api-v0").isPresent();
        
        boolean res = plr.hasPermissionLevel(2);

        if (cyber) {
            if (CyberHandler.hasPermission(plr, perm)) res = true;
        }

        if (luck) {
            if (LuckHandler.hasPermission(plr, perm)) res = true;
        }

        return res;
    }

}