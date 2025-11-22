package me.isaiah.multiworld.perm;

import me.isaiah.multiworld.MultiworldMod;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class Perm {

    public static Perm INSTANCE;
    public static void setPerm(Perm p) {INSTANCE = p;}
    
    public boolean has_impl(ServerPlayerEntity plr, String perm) {
        System.out.println("Platform Permission Handler not found!");
        return false;
    }

    public static boolean has(ServerPlayerEntity plr, String perm) {
        if (null == INSTANCE) {
            System.out.println("Platform Permission Handler not found!");
            return permissionLevel(plr, 1);
        }
        return INSTANCE.has_impl(plr, perm) || plr.isCreativeLevelTwoOp();
    }

    public static boolean has(ServerCommandSource s, String perm) {
        try {
            return has(MultiworldMod.get_player(s), perm) || permissionLevel(s, 1);
        } catch (Exception e) {
            return permissionLevel(s, 1);
        }
    }
    
    /**
     * Check Vanilla Permission Level
     */
	public static boolean permissionLevel(ServerCommandSource source, int level) {
		return MultiworldMod.versionSupport().permissionLevel(source, level);
		// return source.hasPermissionLevel(level);
	}
    
    /**
     * Check Vanilla Permission Level
     */
	public static boolean permissionLevel(ServerPlayerEntity plr, int level) {
		return MultiworldMod.versionSupport().permissionLevel(plr, level);
		// return plr.hasPermissionLevel(level);
	}

}