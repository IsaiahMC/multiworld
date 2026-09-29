package me.isaiah.multiworld.fabric;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.level.ServerPlayer;

public class LuckHandler {

    public static boolean hasPermission(ServerPlayer plr, String perm) {
        return Permissions.check(plr, perm) || plr.canUseGameMasterBlocks();
    }

}
