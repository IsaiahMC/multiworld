package me.isaiah.multiworld.neoforge;

import net.minecraft.server.network.ServerPlayerEntity;
import me.isaiah.multiworld.perm.Perm;
import net.neoforged.fml.ModList;

public class PermForge extends Perm {

    public static void init() {
        Perm.setPerm(new PermForge());
    }

    @Override
    public boolean has_impl(ServerPlayerEntity plr, String perm) {
        boolean cyber = ModList.get().getModContainerById("cyberpermissions").isPresent();
        
        boolean res = plr.hasPermissionLevel(2);

        if (cyber) {
            if (CyberHandler.hasPermission(plr, perm)) res = true;
        }

        return res;
    }

}