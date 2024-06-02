package me.isaiah.multiworld.fabric;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.network.ServerPlayerEntity;

public class LuckHandler {

    public static boolean hasPermission(ServerPlayerEntity plr, String perm) {
        return Permissions.check(plr, perm) || plr.isCreativeLevelTwoOp();
    }

}
