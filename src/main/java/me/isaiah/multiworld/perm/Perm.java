package me.isaiah.multiworld.perm;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class Perm {

    public static boolean has(ServerPlayerEntity plr, String perm) {
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

    public static boolean has(ServerCommandSource s, String perm) {
        try {
            return has(s.getPlayer(), perm);
        } catch (CommandSyntaxException e) {
            return s.hasPermissionLevel(2);
        }
    }

}