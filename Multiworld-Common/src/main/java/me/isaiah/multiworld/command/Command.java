package me.isaiah.multiworld.command;

import me.isaiah.multiworld.MultiworldMod;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public interface Command {
	
	public static ServerWorld getWorldFor(ServerPlayerEntity plr) {
		 return MultiworldMod.getWorldFor(plr);
	}
	
}