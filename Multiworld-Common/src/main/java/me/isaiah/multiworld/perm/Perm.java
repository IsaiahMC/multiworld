package me.isaiah.multiworld.perm;

import me.isaiah.multiworld.MultiworldMod;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public class Perm {

    public static Perm INSTANCE;
    public static void setPerm(Perm p) {INSTANCE = p;}
    
    public boolean has_impl(ServerPlayer plr, String perm) {
        System.out.println("Platform Permission Handler not found!");
        return false;
    }
    
    public static boolean check(ServerPlayer plr, String perm) {
        if (null == INSTANCE) {
            System.out.println("Platform Permission Handler not found!");
            return permissionLevel(plr, 1);
        }
        boolean ALL = INSTANCE.has_impl(plr, "multiworld.admin");
        return ALL || INSTANCE.has_impl(plr, perm) || plr.canUseGameMasterBlocks();
    }

    public static boolean has(ServerPlayer plr, String perm) {
        if (null == INSTANCE) {
            System.out.println("Platform Permission Handler not found!");
            return permissionLevel(plr, 1);
        }
        return INSTANCE.has_impl(plr, perm) || plr.canUseGameMasterBlocks();
    }

    public static boolean has(CommandSourceStack s, String perm) {
        try {
            return has(MultiworldMod.get_player(s), perm) || permissionLevel(s, 1);
        } catch (Exception e) {
            return permissionLevel(s, 1);
        }
    }
    
    /**
     * Check Vanilla Permission Level
     */
	public static boolean permissionLevel(CommandSourceStack source, int level) {
		return MultiworldMod.versionSupport().permissionLevel(source, level);
		// return source.hasPermissionLevel(level);
	}
    
    /**
     * Check Vanilla Permission Level
     */
	public static boolean permissionLevel(ServerPlayer plr, int level) {
		return MultiworldMod.versionSupport().permissionLevel(plr, level);
		// return plr.hasPermissionLevel(level);
	}

}