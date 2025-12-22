package me.isaiah.multiworld.fabric;

import xyz.nucleoid.fantasy.RuntimeWorld;
import xyz.nucleoid.fantasy.RuntimeWorldConfig;
import xyz.nucleoid.fantasy.RuntimeWorldProperties;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;

import me.isaiah.multiworld.Utils;
import multiworld.api.IMultiworldWorld;
import multiworld.api.WorldFolderMode;
import multiworld.mixin.MixinLevelInfo;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.DataConfiguration;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.Util;
import net.minecraft.util.math.random.RandomSequencesState;
import net.minecraft.util.path.SymlinkValidationException;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.World;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelStorage.Session;
import net.minecraft.world.rule.GameRules;
import net.minecraft.world.spawner.SpecialSpawner;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.fantasy.mixin.MinecraftServerAccess;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;

public class MultiworldWorld extends RuntimeWorld implements IMultiworldWorld {

	public final Style style;
	public boolean flat;
	
	public final LevelStorage.Session mw$levelStorageAccess;
	
	protected MultiworldWorld(MinecraftServer server, RegistryKey<World> registryKey, RuntimeWorldConfig config, Style style) {
        this(
                server, Util.getMainWorkerExecutor(), mw$session(server, registryKey.getValue()),
                new RuntimeWorldProperties(new SaveProperties2((LevelProperties) server.getSaveProperties()).withName(registryKey.getValue().toUnderscoreSeparatedString().replace("multiworld_", "")), config),
                registryKey,
                config.createDimensionOptions(server),
                false,
                BiomeAccess.hashSeed(config.getSeed()),
                ImmutableList.of(),
                config.shouldTickTime(),
                null, style
        );

        this.flat = config.isFlat().orElse(super.isFlat());
        // ((LevelProperties) this.properties).getLevelInfo().name = "";

        // ((MixinLevelInfo) (Object) info).setName(multiworld$getLevelName());
        
        // Save World
        this.save(null, true, false); 
    }

    private MultiworldWorld(MinecraftServer server, Executor workerExecutor, LevelStorage.Session session, ServerWorldProperties properties, RegistryKey<World> worldKey, DimensionOptions dimensionOptions, boolean debugWorld, long seed, List<SpecialSpawner> spawners, boolean shouldTickTime, @Nullable RandomSequencesState randomSequencesState, Style style) {
        super(server, workerExecutor, session, properties, worldKey, dimensionOptions, debugWorld, seed, spawners, shouldTickTime, randomSequencesState, style);
        this.mw$levelStorageAccess = session;
        this.style = style;
    }
    
    private static Session mw$session(MinecraftServer server, Identifier id) {
    	boolean useUs = Utils.shouldUseNewWorldFormat(server, id);
    	if (!useUs) { return ((MinecraftServerAccess) server).getSession(); }
    	
    	return mw$getSession(server, id);
    }
    
    public static LevelStorage mw$getStorage() {
    	Path customWorldPath = Utils.getWorldStoragePath();
    	LevelStorage levelStorage = LevelStorage.create(customWorldPath);
    	return levelStorage;
    }
    
    public static Session mw$getSession(MinecraftServer server, Identifier id) {
    	String name = Utils.getWorldName(id);
    	Path customWorldPath = Utils.getWorldStoragePath();
    	LevelStorage levelStorage = LevelStorage.create(customWorldPath);
    	try (LevelStorage.Session session = levelStorage.createSession( name )) {
			return session;
		} catch (IOException | SymlinkValidationException e) {
			e.printStackTrace();
			return ((MinecraftServerAccess) server).getSession();
		}
    }
    
    /**
     * Reads gamerules from a world's level.dat
     */
    public static GameRules mw$readGameRules(MinecraftServer server, Identifier worldId)
            throws IOException, SymlinkValidationException {

        String name = Utils.getWorldName(worldId);
        Path customWorldPath = Utils.getWorldStoragePath();

        Optional<WorldFolderMode> mode = Utils.getFolderMode(worldId);
        if (!mode.isEmpty()) {
        	customWorldPath = Utils.getWorldPath(worldId, mode.get()).getParent();
        
	        if (mode.get() == WorldFolderMode.VANILLA) {
	        	name = worldId.getPath();
	        }
        }
        
        LevelStorage storage = LevelStorage.create(customWorldPath);

        try (Session session = storage.createSession(name)) {
            Dynamic<?> dynamic = session.readLevelProperties();

            Registry<DimensionOptions> dimensionRegistry = server.getRegistryManager().getOrThrow(RegistryKeys.DIMENSION);
            DataConfiguration dataConfig = server.getSaveProperties().getDataConfiguration();

            SaveProperties props = LevelStorage.parseSaveProperties(
                dynamic,
                dataConfig,
                dimensionRegistry,
                server.getRegistryManager()
            ).properties();

            if (!(props instanceof LevelProperties levelProps)) {
                throw new IllegalStateException("SaveProperties is not a LevelProperties");
            }

            // Return the gamerules directly
            return levelProps.getGameRules();
        }
    }
    
    @Override
    public Session multiworld$getLevelStorageSession() {
    	return this.mw$levelStorageAccess;
    }
    
    @Override
    public Identifier multiworld$getLevelId() {
    	return this.getRegistryKey().getValue();
    }
    
    @Override
    public String multiworld$getLevelName() {
    	return multiworld$getLevelId().getPath();
    }
    
    public SaveProperties getSaveProperties() {
    	SaveProperties serverSave = this.getServer().getSaveProperties();
    	SaveProperties2 props = new SaveProperties2((LevelProperties) serverSave).withName(
    			multiworld$getLevelName()
    			);

        WorldProperties worldProps = (RuntimeWorldProperties) this.getLevelProperties();

        props.setDifficulty(worldProps.getDifficulty());
        props.setSpawnPoint(worldProps.getSpawnPoint());
        props.setTime(worldProps.getTime());
        props.setTimeOfDay(worldProps.getTimeOfDay());
        props.setDifficultyLocked(worldProps.isDifficultyLocked());
        props.setRaining(worldProps.isRaining());
        props.setThundering(worldProps.isThundering());

        if (worldProps instanceof ServerWorldProperties swProps) {
        	props.getGameRules().copyFrom(swProps.getGameRules(), null);
            props.setClearWeatherTime(swProps.getClearWeatherTime());
            props.setRainTime(swProps.getRainTime());
            props.setThunderTime(swProps.getThunderTime());
            props.setGameMode(swProps.getGameMode());
            props.setInitialized(swProps.isInitialized());
            props.setWanderingTraderId(swProps.getWanderingTraderId());
            props.setWanderingTraderSpawnChance(swProps.getWanderingTraderSpawnChance());
            props.setWanderingTraderSpawnDelay(swProps.getWanderingTraderSpawnDelay());
            props.setWorldBorder(swProps.getWorldBorder());
        }
        
        props.addServerBrand("fabric", true);
        props.addServerBrand("multiworld", true);
        
        props.mw$setLevelName(this.multiworld$getLevelName());

    	return props;
    }

    // @Override
    public String toString2() {
    	return "ServerLevel[" + multiworld$getLevelName() + "]";
    }
    
    @Override
    public void save(@Nullable ProgressListener progressListener, boolean flush, boolean savingDisabled) {
    	super.save(progressListener, flush, savingDisabled);
    	this.multiworld$saveLevelDatFile();
    }
    
    @Override
    public void multiworld$saveLevelDatFile() {
        this.mw$levelStorageAccess.backupLevelDataFile(this.getServer().getRegistryManager(), getSaveProperties(), this.getServer().getPlayerManager().getUserData());
    }

}
