package me.isaiah.multiworld.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.isaiah.multiworld.MultiworldMod;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

// #if mc2111 mc261 mc262 mc263
// import net.minecraft.world.level.gamerules.GameRules;
// #else
import net.minecraft.world.level.GameRules;
// #endif
import net.minecraft.world.level.storage.ServerLevelData;

// #if mc2101
// // Hello world!
// #endif

public class InfoCommand implements Command {

    /**
     * "/mw info [world id]"
     *
     * Read-only diagnostics for a dimension: players, time of day, weather (+ change timers),
     * gamerules differing from the overworld, and spawn position. Targets the player's current
     * dimension by default, or the named world if an id is supplied. Mirrors {@link DifficultyCommand}.
     */
    public static int run(MinecraftServer mc, ServerPlayer plr, String[] args) {
        ServerLevel w = Command.getWorldFor(plr);

        // Optional world id (args[1]) — same resolution as DifficultyCommand.
        if (args.length >= 2) {
            String a1 = args[1];

            HashMap<String, ServerLevel> worlds = new HashMap<>();
            mc.levelKeys().forEach(r -> {
                ServerLevel world = mc.getLevel(r);
                worlds.put(r.identifier().toString(), world);
            });

            if (a1.indexOf(':') == -1) a1 = "multiworld:" + a1;

            if (worlds.containsKey(a1)) {
                w = worlds.get(a1);
            } else {
                MultiworldMod.message(plr, "&cWorld not found: " + a1);
                return 1;
            }
        }

        String id = w.dimension().identifier().toString(); // w.getRegistryKey().getValue().toString();

        MultiworldMod.message(plr, "&b=== Multiworld Info: &r" + id + " &b===");

        // Players
        List<ServerPlayer> players = w.players();
        if (players.isEmpty()) {
            MultiworldMod.message(plr, "&aPlayers&r (0): (none)");
        } else {
            StringBuilder names = new StringBuilder();
            for (ServerPlayer p : players) {
                if (names.length() > 0) names.append(", ");
                names.append(p.getName().getString());
            }
            MultiworldMod.message(plr, "&aPlayers&r (" + players.size() + "): " + names);
        }

        // Time
        long timeOfDay = TimeCommand.getDayTime(w);
        long day = timeOfDay / 24000L;
        long dayTime = timeOfDay % 24000L;
        MultiworldMod.message(plr, "&aTime&r: " + timeOfDay + " (day " + day + ", daytime " + dayTime + ")");

        // Weather
        boolean raining = w.isRaining();
        boolean thundering = w.isThundering();
        String state = thundering ? "thunder" : (raining ? "rain" : "clear");
        StringBuilder weather = new StringBuilder("&aWeather&r: " + state);
        if (w.getLevelData() instanceof ServerLevelData props) {
        	// #if mc261 mc262 mc263 mc262 
        	// net.minecraft.world.level.saveddata.WeatherData wth = w.getWeatherData();
        	// int clear = wth.getClearWeatherTime();
            // int rain = wth.getRainTime();
            // int thunder = wth.getThunderTime();
        	// #else
            int clear = props.getClearWeatherTime();
            int rain = props.getRainTime();
            int thunder = props.getThunderTime();
            // #endif
            weather.append("(clear=").append(clear)
                   .append(", rain=").append(rain)
                   .append(", thunder=").append(thunder).append(")");
        }
        MultiworldMod.message(plr, weather.toString());

        // Gamerules differing from the overworld
        GameRules worldRules = w.getGameRules();
        GameRules overworldRules = mc.overworld().getGameRules();
        List<String> diffs = new ArrayList<>();
        // #if mc2111 mc261 mc262 mc263
        // // TODO: Implement gamerule comparison for 1.21.11+
        // #else
        worldRules.visitGameRuleTypes(new GameRules.GameRuleTypeVisitor() {
        	@Override
        	public <T extends GameRules.Value<T>> void visit(GameRules.Key<T> key, GameRules.Type<T> type) {
        	    String here = worldRules.getRule(key).serialize();
        	    String over = overworldRules.getRule(key).serialize();
        	    if (!here.equals(over)) {
        	        diffs.add(key.getId() + ": " + here + " (overworld: " + over + ")");
        	    }
        	}

        });
        // #endif
        if (diffs.isEmpty()) {
            MultiworldMod.message(plr, "&aGamerules&r: (same as overworld)");
        } else {
            MultiworldMod.message(plr, "&aGamerules differing from overworld&r (" + diffs.size() + "):");
            for (String d : diffs) {
                MultiworldMod.message(plr, "  &7- &r" + d);
            }
        }

        // Spawn (same source as /mw spawn: Multiworld config "spawnpos" set by /mw setspawn, with fallback)
        BlockPos spawn = SpawnCommand.getSpawn(w);
        MultiworldMod.message(plr, "&aSpawn&r: " + spawn.getX() + " / " + spawn.getY() + " / " + spawn.getZ());

        return 1;
    }

}
