package me.isaiah.multiworld.command;

import java.io.IOException;

import me.isaiah.multiworld.MultiworldMod;
import me.isaiah.multiworld.Utils;
import me.isaiah.multiworld.config.FileConfiguration;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.WorldProperties;

public class SpawnCommand implements Command {

    public static int run(MinecraftServer mc, ServerPlayerEntity plr, String[] args) {
        ServerWorld w = Command.getWorldFor(plr);
        BlockPos sp = getSpawn(w);

        // Don't use FabricDimensionInternals here as
        // we are teleporting to the same world.
        plr.teleport(sp.getX(), sp.getY(), sp.getZ(), true);

        // TeleportTarget target = new TeleportTarget(new Vec3d(sp.getX(), sp.getY(), sp.getZ()), new Vec3d(0, 0, 0), 0f, 0f);
        // ServerPlayerEntity teleported = FabricDimensionInternals.changeDimension(plr, w, target);
        return 1;
    }

    public static BlockPos getSpawn(ServerWorld w) {
        Utils.getConfigDir();

        Identifier id = w.getRegistryKey().getValue();
        FileConfiguration config;
        try {
            config = Utils.getConfigOrNull(id);
            if (null == config) {
            	return multiworld_method_43126(w);
            }
            if (config.is_set("spawnpos")) {
            	return BlockPos.fromLong(config.getLong("spawnpos"));
            } else {
            	return multiworld_method_43126(w);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return multiworld_method_43126(w);
        }
    }
	
	// getSpawnPos
	public static BlockPos multiworld_method_43126(ServerWorld world) {
		WorldProperties prop = world.getLevelProperties();
		
		BlockPos pos = MultiworldMod.get_world_creator().get_spawn(world);
        // BlockPos pos = new BlockPos(prop.getSpawnX(), prop.getSpawnY(), prop.getSpawnZ());
		
        if (!world.getWorldBorder().contains(pos)) {
        	BlockPos pp = MultiworldMod.get_world_creator().get_pos(world.getWorldBorder().getCenterX(), 0.0, world.getWorldBorder().getCenterZ());
            pos = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, new BlockPos(pp));
        }
        return pos;
    }

}
