package me.isaiah.multiworld;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import me.isaiah.multiworld.config.FileConfiguration;
import me.isaiah.multiworld.perm.Perm;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.dimension.v1.FabricDimensions;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.Difficulty;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.TheEndBiomeSource;
import net.minecraft.world.biome.source.VanillaLayeredBiomeSource;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldHandle;

public class MultiworldMod implements ModInitializer {

    public static MinecraftServer mc;
    public static String CMD = "mw";

    @Override
    public void onInitialize() {
        System.out.println("Hello Fabric world!");
        ServerLifecycleEvents.SERVER_STARTED.register(mc -> {
            MultiworldMod.mc = mc;
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(literal(CMD)
                    .requires(source -> {
                        try {
                            return Perm.has(source.getPlayer(), "multiworld.cmd") ||
                                    Perm.has(source.getPlayer(), "multiworld.admin");
                        } catch (CommandSyntaxException e) {
                            return source.hasPermissionLevel(2);
                        }
                        //return source.hasPermissionLevel(2);
                    }) 
                        .executes(ctx -> {
                            return broadcast(ctx.getSource(), Formatting.AQUA, null);
                        })
                        .then(argument("message", greedyString()).suggests(new InfoSuggest())
                                .executes(ctx -> {
                                    try {
                                        return broadcast(ctx.getSource(), Formatting.AQUA, getString(ctx, "message") );
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        return 1;
                                    }
                                 }))); 
        });
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

    public static BlockPos getSpawn(ServerWorld w) {
        File cf = new File(FabricLoader.getInstance().getConfigDir().toFile(), "multiworld"); 
        cf.mkdirs();

        File worlds = new File(cf, "worlds");
        worlds.mkdirs();

        Identifier id = w.getRegistryKey().getValue();
        File namespace = new File(worlds, id.getNamespace());
        namespace.mkdirs();

        File wc = new File(namespace, id.getPath() + ".yml");
        if (!wc.exists()) {
            return w.getSpawnPos();
        }
        FileConfiguration config;
        try {
            config = new FileConfiguration(wc);
            return BlockPos.fromLong(config.getLong("spawnpos"));
        } catch (IOException e) {
            e.printStackTrace();
            return w.getSpawnPos();
        }
    }

    public static int broadcast(ServerCommandSource source, Formatting formatting, String message) throws CommandSyntaxException {
        final ServerPlayerEntity plr = source.getPlayer();

        if (null == message) {
            plr.sendMessage(new LiteralText("Usage:").formatted(Formatting.AQUA), false);
            return 1;
        }

        boolean ALL = Perm.has(plr, "multiworld.admin");
        String[] args = message.split(" ");

        if (args[0].equalsIgnoreCase("setspawn") && (ALL || Perm.has(plr, "multiworld.setspawn") )) {
            World w = plr.getServerWorld();
            BlockPos pos = plr.getBlockPos();
            try {
                setSpawn(w, pos);
                plr.sendMessage(new LiteralText("Spawn for world \"" + w.getRegistryKey().getValue() + "\" changed to " 
                        + pos.toShortString()).formatted(Formatting.GOLD), false);
            } catch (IOException e) {
                plr.sendMessage(new LiteralText("Error: " + e.getMessage()), false);
                e.printStackTrace();
            }
        }

        if (args[0].equalsIgnoreCase("spawn") && (ALL || Perm.has(plr, "multiworld.spawn")) ) {
            ServerWorld w = plr.getServerWorld();
            BlockPos sp = getSpawn(w);
            TeleportTarget target = new TeleportTarget(new Vec3d(sp.getX(), sp.getY(), sp.getZ()), new Vec3d(1, 1, 1), 0f, 0f);
            ServerPlayerEntity teleported = FabricDimensions.teleport(plr, w, target);
            return 1;
            //plr = teleported;
        }

        if (args[0].equalsIgnoreCase("tp") ) {
            if (!(ALL || Perm.has(plr, "multiworld.tp"))) {
                plr.sendMessage(Text.of("No permission! Missing permission: multiworld.tp"), false);
                return 1;
            }
            if (args.length == 1) {
                plr.sendMessage(new LiteralText("Usage: /" + CMD + " tp <world>"), false);
                return 0;
            }
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
                ServerPlayerEntity teleported = FabricDimensions.teleport(plr, w, target);
                return 1; //plr = teleported;
            }
        }

        if (args[0].equalsIgnoreCase("list") ) {
            if (!(ALL || Perm.has(plr, "multiworld.cmd"))) {
                plr.sendMessage(Text.of("No permission! Missing permission: multiworld.cmd"), false);
                return 1;
            }
            plr.sendMessage(new LiteralText("All Worlds:").formatted(Formatting.AQUA), false);
            mc.getWorlds().forEach(world -> {
                String name = world.getRegistryKey().getValue().toString();
                if (name.startsWith("multiworld:")) name = name.replace("multiworld:", "");

                plr.sendMessage(new LiteralText("- " + name), false);
            });
        }
        
        if (args[0].equalsIgnoreCase("version") && (ALL || Perm.has(plr, "multiworld.cmd")) ) {
            plr.sendMessage(new LiteralText("Mutliworld Mod (Fabric) version 1.0"), false);
            return 1;
        }

        if (args[0].equalsIgnoreCase("create") ) {
            if (!(ALL || Perm.has(plr, "multiworld.create"))) {
                plr.sendMessage(Text.of("No permission! Missing permission: multiworld.create"), false);
                return 1;
            }
            if (args.length == 1 && args.length == 2) {
                plr.sendMessage(new LiteralText("Usage: /mv create <id> <env>"), false);
                return 0;
            }

            Registry<Biome> biomeRegistry = mc.getRegistryManager().get(SimpleRegistry.BIOME_KEY);
            Registry<ChunkGeneratorSettings> chunkGeneratorSettingsRegistry = mc.getRegistryManager().get(SimpleRegistry.CHUNK_GENERATOR_SETTINGS_KEY);
            RegistryKey<DimensionType> dim = null;
            Random r = new Random();
            long seed = r.nextInt();

            ChunkGenerator gen = null;
            if (args[2].contains("NORMAL")) {
                gen = new NoiseChunkGenerator(new VanillaLayeredBiomeSource(seed, false, false, biomeRegistry),
                        seed, () -> chunkGeneratorSettingsRegistry.getOrThrow(ChunkGeneratorSettings.OVERWORLD));
                dim = DimensionType.OVERWORLD_REGISTRY_KEY;
            }

            if (args[2].contains("NETHER")) {
                gen = mc.getWorld(World.NETHER).getChunkManager().getChunkGenerator();
                dim = DimensionType.THE_NETHER_REGISTRY_KEY;
            }
            
            if (args[2].contains("END")) {
                gen = new NoiseChunkGenerator(new TheEndBiomeSource(biomeRegistry, seed),
                        seed, () -> chunkGeneratorSettingsRegistry.getOrThrow(ChunkGeneratorSettings.END));
                dim = DimensionType.THE_END_REGISTRY_KEY;
            }

            RuntimeWorldConfig config = new RuntimeWorldConfig()
                    .setDimensionType(dim)
                    .setGenerator(gen)
                    .setDifficulty(Difficulty.NORMAL)
                    ;

            String arg1 = args[1];
            if (arg1.indexOf(':') == -1) arg1 = "multiworld:" + arg1;

            Fantasy fantasy = Fantasy.get(mc);
            RuntimeWorldHandle worldHandle = fantasy.getOrOpenPersistentWorld(new Identifier(arg1), config);
            worldHandle.asWorld();
            
            plr.sendMessage(new LiteralText("Created world with id: " + args[1]).formatted(Formatting.GREEN), false);
        }

        return Command.SINGLE_SUCCESS; // Success
    }

    private static BlockPos findSafePos(ServerWorld w, BlockPos sp) {
        BlockPos pos = sp;
        while (w.getBlockState(pos) != Blocks.AIR.getDefaultState()) {
            pos = pos.add(0, 1, 0);
        }
        return pos;
    }
}