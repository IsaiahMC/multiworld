package me.isaiah.multiworld.command;

import java.util.Random;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
//import xyz.nucleoid.fantasy.Fantasy;
//import xyz.nucleoid.fantasy.RuntimeWorldConfig;
//import xyz.nucleoid.fantasy.RuntimeWorldHandle;
import me.isaiah.multiworld.MultiworldMod;

public class CreateCommand {

    public static int run(MinecraftServer mc, ServerPlayerEntity plr, String[] args) {
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
            gen = mc.getWorld(World.OVERWORLD).getChunkManager().getChunkGenerator().withSeed(seed);
            dim = DimensionType.OVERWORLD_REGISTRY_KEY;
        }

        if (args[2].contains("NETHER")) {
            gen = mc.getWorld(World.NETHER).getChunkManager().getChunkGenerator();
            dim = DimensionType.THE_NETHER_REGISTRY_KEY;
        }
        
        if (args[2].contains("END")) {
            gen = mc.getWorld(World.END).getChunkManager().getChunkGenerator().withSeed(seed);
            dim = DimensionType.THE_END_REGISTRY_KEY;
        }
        
        String arg1 = args[1];
        if (arg1.indexOf(':') == -1) arg1 = "multiworld:" + arg1;
        
        MultiworldMod.create_world(arg1, dim, gen, Difficulty.NORMAL);

        /*RuntimeWorldConfig config = new RuntimeWorldConfig()
                .setDimensionType(dim)
                .setGenerator(gen)
                .setDifficulty(Difficulty.NORMAL)
                ;*/

        // TODO
        //Fantasy fantasy = Fantasy.get(mc);
        //RuntimeWorldHandle worldHandle = fantasy.getOrOpenPersistentWorld(new Identifier(arg1), config);
        //worldHandle.asWorld();
        
        plr.sendMessage(new LiteralText("Created world with id: " + args[1]).formatted(Formatting.GREEN), false);
        
        return 1;
    }

}