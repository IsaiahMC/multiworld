package me.isaiah.multiworld.command;

import java.util.HashMap;

import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;
import net.minecraft.block.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;

@SuppressWarnings("deprecation") // Fabric dimension API
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
            BlockPos sp = w.getSpawnPos();
            if (!w.getDimension().isBedWorking() && !w.getDimension().hasCeiling()) {
                ServerWorld.createEndSpawnPlatform(w);
                sp = ServerWorld.END_SPAWN_POS;
            }
            if (null == sp) {
                plr.sendMessage(new LiteralText("Error: null getSpawnPos").formatted(Formatting.RED), false);
                sp = new BlockPos(1, 40, 1);
            }
            plr.sendMessage(new LiteralText("Telelporting...").formatted(Formatting.GOLD), false);

            sp = findSafePos(w, sp);

            TeleportTarget target = new TeleportTarget(new Vec3d(sp.getX(), sp.getY(), sp.getZ()), new Vec3d(1, 1, 1), 0f, 0f);
            FabricDimensions.teleport(plr, w, target);
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

}
