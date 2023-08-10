package me.isaiah.multiworld.forge;

import cyber.permissions.v1.CyberPermissions;
import cyber.permissions.v1.Permission;
import cyber.permissions.v1.PermissionDefaults;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 */
public class CyberHandler {
    
    public static boolean hasPermission(ServerPlayerEntity plr, String perm) {
        Permission p = new Permission(perm, "A permission for Multiworld", PermissionDefaults.OPERATOR);
        return plr.hasPermissionLevel(2) || CyberPermissions.getPlayerPermissible(plr).hasPermission(p);
    }

}