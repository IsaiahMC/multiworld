package me.isaiah.multiworld.forge;

import net.minecraft.server.network.ServerPlayerEntity;
import me.isaiah.multiworld.perm.Perm;
import net.minecraftforge.fml.OptionalMod;

public class PermForge extends Perm {

    public static void init() {
        Perm.setPerm(new PermForge());
    }

    @Override
    public boolean has_impl(ServerPlayerEntity plr, String perm) {
        boolean cyber = OptionalMod.of("cyberpermissions").isPresent();
        
        boolean res = plr.hasPermissionLevel(2);

        if (cyber) {
            if (CyberHandler.hasPermission(plr, perm)) res = true;
        }

        return res;
    }

}