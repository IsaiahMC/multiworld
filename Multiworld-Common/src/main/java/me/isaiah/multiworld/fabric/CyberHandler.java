package me.isaiah.multiworld.fabric;

import me.isaiah.multiworld.perm.Perm;
import net.minecraft.server.level.ServerPlayer;

/**
 * CyberPermissions API
 * 
 * @deprecated Been superseded by fabric-permissions-api
 */
@Deprecated
public class CyberHandler {
    
    public static boolean hasPermission(ServerPlayer plr, String perm) {
        return Perm.permissionLevel(plr, 2);
    }

}