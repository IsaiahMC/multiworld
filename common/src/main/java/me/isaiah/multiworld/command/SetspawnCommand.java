package me.isaiah.multiworld.command;

import java.io.File;
import java.io.IOException;

import me.isaiah.multiworld.config.FileConfiguration;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SetspawnCommand {

    public static int run(MinecraftServer mc, ServerPlayerEntity plr, String[] args) {
        World w = plr.getWorld();
        BlockPos pos = plr.getBlockPos();
        try {
            setSpawn(w, pos);
            plr.sendMessage(new LiteralText("Spawn for world \"" + w.getRegistryKey().getValue() + "\" changed to " 
                    + pos.toShortString()).formatted(Formatting.GOLD), false);
        } catch (IOException e) {
            plr.sendMessage(new LiteralText("Error: " + e.getMessage()), false);
            e.printStackTrace();
        }
        return 1;
    }

    public static void setSpawn(World w, BlockPos spawn) throws IOException {
        File cf = new File(FabricLoader.getInstance().getConfigDir().toFile(), "multiworld"); 
        cf.mkdirs();

        File worlds = new File(cf, "worlds");
        worlds.mkdirs();

        Identifier id = w.getRegistryKey().getValue();
        File namespace = new File(worlds, id.getNamespace());
        namespace.mkdirs();

        File wc = new File(namespace, id.getPath() + ".yml");
        wc.createNewFile();
        FileConfiguration config = new FileConfiguration(wc);

        config.set("spawnpos", spawn.asLong());
        config.save();
    }


}