package me.isaiah.multiworld.command;

import java.util.Set;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public interface IGameruleCommand {

	public Set<String> getKeys();
	
	public void initRulesMapIfNeeded(MinecraftServer server);

	public void set_gamerule_from_cfg(ServerLevel world, String key, String val);
	
	public int run(MinecraftServer mc, ServerPlayer plr, String[] args);

}
