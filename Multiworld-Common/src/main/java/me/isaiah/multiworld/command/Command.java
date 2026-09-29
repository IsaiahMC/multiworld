package me.isaiah.multiworld.command;

import me.isaiah.multiworld.MultiworldMod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public interface Command {
	
	public static ServerLevel getWorldFor(ServerPlayer plr) {
		 return MultiworldMod.getWorldFor(plr);
	}
	
}