package me.isaiah.multiworld.command;

import static me.isaiah.multiworld.MultiworldMod.message;

import java.io.File;
import java.io.IOException;

import me.isaiah.multiworld.Utils;
import me.isaiah.multiworld.config.FileConfiguration;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class SetspawnCommand implements Command {

    public static int run(MinecraftServer mc, ServerPlayer plr, String[] args) {
        Level w = Command.getWorldFor(plr);
        BlockPos pos = plr.blockPosition();
        try {
            setSpawn(w, pos);
			
			String txt = "Spawn for world \"" + w.dimension().identifier() + "\" changed to " + pos.toShortString();
            message(plr, "&6" + txt);
        } catch (IOException e) {
            message(plr, "Error: " + e.getMessage());
            e.printStackTrace();
        }
        return 1;
    }

    public static void setSpawn(Level w, BlockPos spawn) throws IOException {
        File cf = new File(Util.get_platform_config_dir(), "multiworld"); 
        cf.mkdirs();

        /*
        File worlds = new File(cf, "worlds");
        worlds.mkdirs();

        Identifier id = w.getRegistryKey().getValue();
        File namespace = new File(worlds, id.getNamespace());
        namespace.mkdirs();

        File wc = new File(namespace, id.getPath() + ".yml");
        wc.createNewFile();
        FileConfiguration config = new FileConfiguration(wc);
        */
        Identifier id = w.dimension().identifier();
        FileConfiguration config = Utils.getConfigOrNull(id);
        
        if (null == config) {
        	// TODO
        	return;
        }

        config.set("spawnpos", spawn.asLong());
        config.save();
    }


}