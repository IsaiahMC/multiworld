package me.isaiah.multiworld;

import java.util.Locale;

import me.isaiah.multiworld.command.Util;
import net.minecraft.server.network.ServerPlayerEntity;

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
	
	// Messages
	public static final String TELEPORTING = "&6Teleporting...";
	public static final String NULL_SPAWN = "&4Error: null getSpawnPos";
	public static final String USAGE_CREATE = "Usage: /mv create <id> <env> [-g=<gen> -s=<seed>]";
	public static final String CREATED_WORLD = "&aCreated world with id: ";
	public static final String CMD_PORTAL_USAGE = "&4Usage: /mw portal <subcommand> [arguments]";
	public static final String CMD_PORTAL_USAGE_CREATE = "&4Usage: /mw portal create <name> <destination>";
	public static final String CMD_PORTAL_NO_SELECTION = "You need to make an area selection first. (/mw portal wand)";

}
