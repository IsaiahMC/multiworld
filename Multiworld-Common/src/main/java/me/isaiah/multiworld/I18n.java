package me.isaiah.multiworld;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Set;

import me.isaiah.multiworld.command.PortalCommand;
import me.isaiah.multiworld.command.Util;
import me.isaiah.multiworld.config.FileConfiguration;
import me.isaiah.multiworld.portal.Portal;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * Messages for Internationalization (I18N)
 * 
 * @see {@link https://minecraft.wiki/w/Formatting_codes}
 */
public class I18n {

	public static void message(ServerPlayerEntity plr, String text) {
		MultiworldMod.message(plr, text);
	}
	
	// Default Language
	public static Locale WORLD_DEFAULT = Util.AMERICAN_STANDARD;
	public static String FILE_NAME = "i18n.yml";
	
	// Messages
	private static final String DEF_TELEPORTING = "&6Teleporting...";
	private static final String DEF_NULL_SPAWN = "&4Error: null getSpawnPos";
	private static final String DEF_USAGE_CREATE = "Usage: /mv create <id> <env> [-g=<gen> -s=<seed>]";
	private static final String DEF_CREATED_WORLD = "&aCreated world with id: ";
	private static final String DEF_CMD_PORTAL_USAGE = "&4Usage: /mw portal <subcommand> [arguments]";
	private static final String DEF_CMD_PORTAL_USAGE_CREATE = "&4Usage: /mw portal create <name> <destination>";
	private static final String DEF_CMD_PORTAL_NO_SELECTION = "You need to make an area selection first. (/mw portal wand)";
	private static final String DEF_CMD_DIFF_USAGE = "[&4Multiworld&r] Usage: /mw difficulty <value> [world id]";
	
	public static String TELEPORTING;
	public static String NULL_SPAWN;
	public static String USAGE_CREATE;
	public static String CREATED_WORLD;
	public static String CMD_PORTAL_USAGE;
	public static String CMD_PORTAL_USAGE_CREATE;
	public static String CMD_PORTAL_NO_SELECTION;
	public static String CMD_DIFF_USAGE;

	/**
     * Load an existing saved portals from config (YAML) 
     */
	public static void loadConfig() {
		File wc = new File(configDir(), FILE_NAME);
        FileConfiguration config;
        try {
			if (!wc.exists()) {
				wc.createNewFile();
			}
            config = new FileConfiguration(wc);
            
            TELEPORTING = config.getOrDefault("", DEF_TELEPORTING);
            
            TELEPORTING = config.getOrDefault("messages.teleport", DEF_TELEPORTING);
            NULL_SPAWN = config.getOrDefault("messages.nullSpawn", DEF_NULL_SPAWN);
            USAGE_CREATE = config.getOrDefault("messages.usageCreate", DEF_USAGE_CREATE);
            CREATED_WORLD = config.getOrDefault("messages.worldCreated", DEF_CREATED_WORLD);
            CMD_PORTAL_USAGE = config.getOrDefault("messages.usagePortal", DEF_CMD_PORTAL_USAGE);
            CMD_PORTAL_USAGE_CREATE = config.getOrDefault("messages.usagePortalCreate", DEF_CMD_PORTAL_USAGE_CREATE);
            CMD_PORTAL_NO_SELECTION = config.getOrDefault("messages.usagePortalNoSelect", DEF_CMD_PORTAL_NO_SELECTION);
            CMD_DIFF_USAGE = config.getOrDefault("messages.usageDifficulty", DEF_CMD_DIFF_USAGE);

        } catch (Exception e) {
            e.printStackTrace();
            // throw e;
        }
	}

	public static void save() throws IOException {
        File wc = new File(configDir(), FILE_NAME);
        FileConfiguration config;
        try {
			if (!wc.exists()) {
				wc.createNewFile();
			}
            config = new FileConfiguration(wc);

            config.set("messages.teleport", DEF_TELEPORTING);
            config.set("messages.nullSpawn", DEF_NULL_SPAWN);
            config.set("messages.usageCreate", DEF_USAGE_CREATE);
            config.set("messages.worldCreated", DEF_CREATED_WORLD);
            config.set("messages.usagePortal", DEF_CMD_PORTAL_USAGE);
            config.set("messages.usagePortalCreate", DEF_CMD_PORTAL_USAGE_CREATE);
            config.set("messages.usagePortalNoSelect", DEF_CMD_PORTAL_NO_SELECTION);
            config.set("messages.usageDifficulty", DEF_CMD_DIFF_USAGE);

			config.save();
        } catch (Exception e) {
            // e.printStackTrace();
            throw e;
        }
	}
	
	private static File configDir() {
		File config_dir = new File("config");
        config_dir.mkdirs();
        
        File cf = new File(config_dir, "multiworld"); 
        cf.mkdirs();
        return cf;
	}
	
}
