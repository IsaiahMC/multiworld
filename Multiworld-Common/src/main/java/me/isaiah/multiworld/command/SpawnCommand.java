package me.isaiah.multiworld.command;

import java.io.IOException;

import me.isaiah.multiworld.MultiworldMod;
import me.isaiah.multiworld.Utils;
import me.isaiah.multiworld.config.FileConfiguration;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.LevelData;

public class SpawnCommand implements Command {

    public static int run(MinecraftServer mc, ServerPlayer plr, String[] args) {
        ServerLevel w = Command.getWorldFor(plr);
        BlockPos sp = getSpawn(w);

        // Don't use FabricDimensionInternals here as
        // we are teleporting to the same world.
        plr.randomTeleport(sp.getX(), sp.getY(), sp.getZ(), true);

        // TeleportTarget target = new TeleportTarget(new Vec3d(sp.getX(), sp.getY(), sp.getZ()), new Vec3d(0, 0, 0), 0f, 0f);
        // ServerPlayerEntity teleported = FabricDimensionInternals.changeDimension(plr, w, target);
        return 1;
    }

    public static BlockPos getSpawn(ServerLevel w) {
        Utils.getConfigDir();

        Identifier id = w.dimension().identifier();
        FileConfiguration config;
        try {
            config = Utils.getConfigOrNull(id);
            if (null == config) {
            	return multiworld_method_43126(w);
            }
            if (config.is_set("spawnpos")) {
            	return BlockPos.of(config.getLong("spawnpos"));
            } else {
            	return multiworld_method_43126(w);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return multiworld_method_43126(w);
        }
    }
	
	// getSpawnPos
	public static BlockPos multiworld_method_43126(ServerLevel world) {
		LevelData prop = world.getLevelData();
		
		BlockPos pos = MultiworldMod.get_world_creator().get_spawn(world);
        // BlockPos pos = new BlockPos(prop.getSpawnX(), prop.getSpawnY(), prop.getSpawnZ());
		
        if (!world.getWorldBorder().isWithinBounds(pos)) {
        	BlockPos pp = MultiworldMod.get_world_creator().get_pos(world.getWorldBorder().getCenterX(), 0.0, world.getWorldBorder().getCenterZ());
            pos = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, new BlockPos(pp));
        }
        return pos;
    }

}
