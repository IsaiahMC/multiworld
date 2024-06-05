package xyz.nucleoid.fantasy;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public interface IMC {

	public void add_world(RegistryKey<World> key, ServerWorld value);
	
}
