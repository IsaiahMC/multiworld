package me.isaiah.multiworld.command;

import java.util.HashMap;

import me.isaiah.multiworld.MultiworldMod;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class TimeCommand implements Command {

    /**
     * "/mw time <set|add|query> <time> [world id]"
     *
     * Sets/adds/queries the time of day of the world the player is currently in, or of the named
     * world if a world id is supplied. Mirrors {@link DifficultyCommand}.
     *
     * Unlike vanilla {@code /time} (which applies to every world at once), this only affects the
     * targeted dimension.
     */
    public static int run(MinecraftServer mc, ServerPlayer plr, String[] args) {
        ServerLevel w = Command.getWorldFor(plr);

        if (args.length < 3) {
            MultiworldMod.message(plr, "&cUsage: /mw time <set|add|query> <time> [world id]");
            return 1;
        }

        String action = args[1];
        String value = args[2];

        // Optional world id (args[3]) — same resolution as DifficultyCommand.
        if (args.length >= 4) {
            String a3 = args[3];

            HashMap<String, ServerLevel> worlds = new HashMap<>();
            mc.levelKeys().forEach(r -> {
                ServerLevel world = mc.getLevel(r);
                worlds.put(r.identifier().toString(), world);
            });

            if (a3.indexOf(':') == -1) a3 = "multiworld:" + a3;

            if (worlds.containsKey(a3)) {
                w = worlds.get(a3);
            } else {
                MultiworldMod.message(plr, "&cWorld not found: " + a3);
                return 1;
            }
        }

        String id = w.dimension().identifier().toString();// w.getRegistryKey().getValue().toString();

        if (action.equalsIgnoreCase("query")) {
            long result;
            if (value.equalsIgnoreCase("daytime")) {
                result = getDayTime(w) % 24000L;
            } else if (value.equalsIgnoreCase("gametime")) {
                result = w.getGameTime();
            } else if (value.equalsIgnoreCase("day")) {
                result = (getDayTime(w) / 24000L) % 2147483647L;
            } else {
                MultiworldMod.message(plr, "&cInvalid query: " + value + " (daytime, gametime, day)");
                return 1;
            }
            MultiworldMod.message(plr, "[&cMultiworld&r]: Time (" + value + ") of world '" + id + "' is: " + result);
            return 1;
        }

        // set / add need a numeric (or named) tick value
        long ticks = parseTime(value);
        if (ticks < 0) {
            MultiworldMod.message(plr, "&cInvalid time: " + value + " (a number, or day/noon/night/midnight)");
            return 1;
        }

        if (action.equalsIgnoreCase("set")) {
            setDayTime(w, ticks);
            MultiworldMod.message(plr, "[&cMultiworld&r]: Time of world '" + id + "' set to: " + ticks);
        } else if (action.equalsIgnoreCase("add")) {
            setDayTime(w, getDayTime(w) + ticks);
            MultiworldMod.message(plr, "[&cMultiworld&r]: Added " + ticks + " ticks to world '" + id + "' (now " + getDayTime(w) + ")");
        } else {
            MultiworldMod.message(plr, "&cInvalid action: " + action + " (set, add, query)");
            return 1;
        }

        return 1;
    }
    
    // Helper Method
    public static long getDayTime(ServerLevel world) {
    	// #if mc261
    	// return world.registryAccess().get(net.minecraft.world.clock.WorldClocks.OVERWORLD).map( holder -> world.clockManager().getTotalTicks( holder ) ).orElse(0L);
    	// #else
		return world.getDayTime();
		// #endif
	}
    
    // Helper Method
    private static void setDayTime(ServerLevel world, long ticks) {
    	// #if mc261
    	// world.registryAccess().get(net.minecraft.world.clock.WorldClocks.OVERWORLD).ifPresent( holder -> world.clockManager().setTotalTicks( holder, ticks ) );
		// #else
    	world.setDayTime(ticks);
    	// #endif
	}

    /**
     * Parse a time value: a raw tick number, or one of the vanilla named times.
     * Returns -1 if the value cannot be parsed.
     */
    private static long parseTime(String value) {
        switch (value.toLowerCase()) {
            case "day":      return 1000L;
            case "noon":     return 6000L;
            case "night":    return 13000L;
            case "midnight": return 18000L;
            case "sunrise":  return 23000L;
            case "sunset":   return 12000L;
            default:
                try {
                    long t = Long.parseLong(value);
                    return t < 0 ? -1 : t;
                } catch (NumberFormatException e) {
                    return -1;
                }
        }
    }

}
