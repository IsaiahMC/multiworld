package me.isaiah.multiworld.command;

import java.util.HashMap;

import dimapi.FabricDimensionInternals;
import net.minecraft.block.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import static me.isaiah.multiworld.MultiworldMod.text;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.Heightmap;
import java.io.File;

import me.isaiah.multiworld.MultiworldMod;
import me.isaiah.multiworld.config.*;

public class TpCommand {

    public static int run(MinecraftServer mc, ServerPlayerEntity plr, String[] args) {
        HashMap<String,ServerWorld> worlds = new HashMap<>();
        mc.getWorldRegistryKeys().forEach(r -> {
            ServerWorld world = mc.getWorld(r);
            worlds.put(r.getValue().toString(), world);
        });
        
        String arg1 = args[1];
        if (arg1.indexOf(':') == -1) arg1 = "multiworld:" + arg1;

        if (worlds.containsKey(arg1)) {
            ServerWorld w = worlds.get(arg1);
            // BlockPos sp = multiworld_method_43126(w);
            BlockPos sp = SpawnCommand.getSpawn(w);
			
			boolean isEnd = false;
			
			try {
				boolean is_the_end = MultiworldMod.get_world_creator().is_the_end(w);
				if (is_the_end) {
					isEnd = true;
				}
			} catch (NoSuchMethodError | Exception e) {
			}
			
			String env = read_env_from_config(arg1);
			if (null != env) {
				if (env.equalsIgnoreCase("END")) {
					isEnd = true;
				}
			}

			if (isEnd) {
				ServerWorld.createEndSpawnPlatform(w);
				sp = ServerWorld.END_SPAWN_POS;
			}
			
            if (null == sp) {
                plr.sendMessage(text("Error: null getSpawnPos", Formatting.RED), false);
                sp = new BlockPos(1, 40, 1);
            }
            plr.sendMessage(text("Telelporting...", Formatting.GOLD), false);

            sp = findSafePos(w, sp);

            TeleportTarget target = new TeleportTarget(new Vec3d(sp.getX(), sp.getY(), sp.getZ()), new Vec3d(1, 1, 1), 0f, 0f);
            FabricDimensionInternals.changeDimension(plr, w, target);
            return 1;
        }
        return 1;
    }

    private static BlockPos findSafePos(ServerWorld w, BlockPos sp) {
        BlockPos pos = sp;
        while (w.getBlockState(pos) != Blocks.AIR.getDefaultState()) {
            pos = pos.add(0, 1, 0);
        }
        return pos;
    }
	
	// getSpawnPos
	public static BlockPos multiworld_method_43126(ServerWorld world) {
        return SpawnCommand.multiworld_method_43126(world);
    }
	
	public static String read_env_from_config(String id) {
        File config_dir = new File("config");
        config_dir.mkdirs();
		
		String[] spl = id.split(":");
        
        File cf = new File(config_dir, "multiworld"); 
        cf.mkdirs();

        File worlds = new File(cf, "worlds");
        worlds.mkdirs();

        File namespace = new File(worlds, spl[0]);
        namespace.mkdirs();

        File wc = new File(namespace, spl[1] + ".yml");
        FileConfiguration config;
        try {
			if (!wc.exists()) {
				wc.createNewFile();
			}
            config = new FileConfiguration(wc);
			String env = config.getString("environment");
			return env;
        } catch (Exception e) {
            e.printStackTrace();
        }
		return "NORMAL";
    }

}
