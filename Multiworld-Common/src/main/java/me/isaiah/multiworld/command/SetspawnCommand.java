package me.isaiah.multiworld.command;

import java.io.File;
import java.io.IOException;

import me.isaiah.multiworld.config.FileConfiguration;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static me.isaiah.multiworld.MultiworldMod.text;
import static me.isaiah.multiworld.MultiworldMod.text_plain;

public class SetspawnCommand {

    public static int run(MinecraftServer mc, ServerPlayerEntity plr, String[] args) {
        World w = plr.getWorld();
        BlockPos pos = plr.getBlockPos();
        try {
            setSpawn(w, pos);
			
			String txt = "Spawn for world \"" + w.getRegistryKey().getValue() + "\" changed to " + pos.toShortString();
			
            plr.sendMessage(text(txt, Formatting.GOLD), false);
        } catch (IOException e) {
            plr.sendMessage(text_plain("Error: " + e.getMessage()), false);
            e.printStackTrace();
        }
        return 1;
    }

    public static void setSpawn(World w, BlockPos spawn) throws IOException {
        File cf = new File(Util.get_platform_config_dir(), "multiworld"); 
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