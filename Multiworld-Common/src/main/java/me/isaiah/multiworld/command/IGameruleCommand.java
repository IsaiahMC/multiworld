package me.isaiah.multiworld.command;

import java.util.Set;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public interface IGameruleCommand {

	public Set<String> getKeys();
	
	public void initRulesMapIfNeeded(MinecraftServer server);

	public void set_gamerule_from_cfg(ServerWorld world, String key, String val);
	
	public int run(MinecraftServer mc, ServerPlayerEntity plr, String[] args);

}
