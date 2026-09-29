package me.isaiah.multiworld.fabric;

import xyz.nucleoid.fantasy.RuntimeLevel;
import xyz.nucleoid.fantasy.RuntimeLevelConfig;
import xyz.nucleoid.fantasy.RuntimeLevelData;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;

import me.isaiah.multiworld.Utils;
import multiworld.api.IMultiworldWorld;
import multiworld.api.WorldFolderMode;
import multiworld.mixin.MixinLevelInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ProgressListener;
import net.minecraft.util.Util;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.clock.ServerClockManager;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.WorldData;
import net.minecraft.world.level.validation.ContentValidationException;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.fantasy.mixin.MinecraftServerAccess;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;

public class MultiworldWorld extends RuntimeLevel implements IMultiworldWorld {

	public final Style style;
	public boolean flat;
	
	public final LevelStorageSource.LevelStorageAccess mw$levelStorageAccess;
	
	protected MultiworldWorld(MinecraftServer server, ResourceKey<Level> registryKey, RuntimeLevelConfig config, Style style) {
        
		GameRules gameRules = null;
        if (!config.shouldMirrorOverworldGameRules()) {
            gameRules = new GameRules(server.getWorldData().enabledFeatures());
            config.getGameRules().applyTo(gameRules, null);
        } else {
            gameRules = null;
        }
		
		this(
                server, Util.backgroundExecutor(), mw$session(server, registryKey.identifier()),
                new RuntimeLevelData(new MySaveProperties((PrimaryLevelData) server.getWorldData()).withName(registryKey.identifier().toDebugFileName().replace("multiworld_", "")), config),
                registryKey,
                config.createDimensionOptions(server),
                false,
                BiomeManager.obfuscateSeed(config.getSeed()),
                ImmutableList.of(),
                config.shouldTickTime(),
                style, gameRules, config.getClockManager(server, gameRules != null ? gameRules : server.getGameRules())
        );

        this.flat = config.isFlat().orElse(super.isFlat());
        // ((LevelProperties) this.properties).getLevelInfo().name = "";

        // ((MixinLevelInfo) (Object) info).setName(multiworld$getLevelName());
        
        // Save World
        this.save(null, true, false); 
    }

	protected MultiworldWorld(MinecraftServer server, Executor executor, LevelStorageSource.LevelStorageAccess levelStorage, ServerLevelData levelData,
			ResourceKey<Level> dimension,
            LevelStem levelStem, boolean isDebug, long biomeZoomSeed, List<CustomSpawner> customSpawners, boolean tickTime, Style style,
            @Nullable GameRules gameRules, @Nullable ServerClockManager clockManager) {

		super(server, executor, levelStorage, levelData, dimension, levelStem, isDebug, biomeZoomSeed, customSpawners, tickTime, style, gameRules, clockManager);
		this.mw$levelStorageAccess = levelStorage;
		this.style = style;
	}
	
	/*
    private MultiworldWorld(MinecraftServer server, Executor workerExecutor, LevelStorageSource.LevelStorageAccess session, ServerLevelData properties, ResourceKey<Level> worldKey, LevelStem dimensionOptions, boolean debugWorld, long seed, List<CustomSpawner> spawners, boolean shouldTickTime, @Nullable RandomSequences randomSequencesState, Style style,
    		 @Nullable GameRules gameRules, @Nullable ServerClockManager clockManager) {
        super(server, workerExecutor, session, properties, worldKey, dimensionOptions, debugWorld, seed, spawners, shouldTickTime, randomSequencesState, style, gameRules, clockManager);
        this.mw$levelStorageAccess = session;
        this.style = style;
    }
    */
    
    private static LevelStorageAccess mw$session(MinecraftServer server, Identifier id) {
    	return ((MinecraftServerAccess) server).getStorageSource();
    }
    
    /*
    private static LevelStorageAccess mw$session_old(MinecraftServer server, Identifier id) {
    	boolean useUs = Utils.shouldUseNewWorldFormat(server, id);
    	if (!useUs) { return ((MinecraftServerAccess) server).getSession(); }
    	
    	// ((MinecraftServerAccess) server).getStorageSource();
    	
    	return mw$getSession(server, id);
    }*/
    
    public static LevelStorageSource mw$getStorage() {
    	Path customWorldPath = Utils.getWorldStoragePath();
    	LevelStorageSource levelStorage = LevelStorageSource.createDefault(customWorldPath);
    	return levelStorage;
    }
    
    /*
    @Deprecated
    public static LevelStorageAccess mw$getSession(MinecraftServer server, Identifier id) {
    }
    */
    
    /**
     * Reads gamerules from a world's level.dat
     */
    /*
    public static GameRules mw$readGameRules(MinecraftServer server, Identifier worldId)
            throws IOException, ContentValidationException {

        String name = Utils.getWorldName(worldId);
        Path customWorldPath = Utils.getWorldStoragePath();

        Optional<WorldFolderMode> mode = Utils.getFolderMode(worldId);
        if (!mode.isEmpty()) {
        	customWorldPath = Utils.getWorldPath(worldId, mode.get()).getParent();
        
	        if (mode.get() == WorldFolderMode.VANILLA) {
	        	name = worldId.getPath();
	        }
        }
        
        LevelStorageSource storage = LevelStorageSource.createDefault(customWorldPath);

        try (LevelStorageAccess session = storage.validateAndCreateAccess(name)) {
            Dynamic<?> dynamic = session.getDataTag();

            Registry<LevelStem> dimensionRegistry = server.registryAccess().lookupOrThrow(Registries.LEVEL_STEM);
            WorldDataConfiguration dataConfig = server.getWorldData().getDataConfiguration();

            WorldData props = LevelStorageSource.getLevelDataAndDimensions(
                dynamic,
                dataConfig,
                dimensionRegistry,
                server.registryAccess()
            ).worldData();

            if (!(props instanceof PrimaryLevelData levelProps)) {
                throw new IllegalStateException("SaveProperties is not a LevelProperties");
            }

            // Return the gamerules directly
            return levelProps.getGameRules();
        }
    }
    */
    
    @Override
    public LevelStorageAccess multiworld$getLevelStorageSession() {
    	return this.mw$levelStorageAccess;
    }
    
    @Override
    public Identifier multiworld$getLevelId() {
    	return this.dimension().identifier();
    }
    
    @Override
    public String multiworld$getLevelName() {
    	return multiworld$getLevelId().getPath();
    }
    
    public WorldData getSaveProperties() {
    	WorldData serverSave = this.getServer().getWorldData();
    	MySaveProperties props = new MySaveProperties((PrimaryLevelData) serverSave).withName(
    			multiworld$getLevelName()
    			);

        LevelData worldProps = (RuntimeLevelData) this.getLevelData();

        props.setDifficulty(worldProps.getDifficulty());
        props.setSpawn(worldProps.getRespawnData());
        props.setGameTime(worldProps.getGameTime());
        // props.setDayTime(worldProps.getDayTime());
        props.setDifficultyLocked(worldProps.isDifficultyLocked());
        // props.setRaining(worldProps.isRaining());
        // props.setThundering(worldProps.isThundering());

        if (worldProps instanceof ServerLevelData swProps) {
        	// props.getGameRules().setAll(swProps.getGameRules(), null);
            // props.setClearWeatherTime(swProps.getClearWeatherTime());
            

            // props.setRainTime(swProps.getRainTime());
            // props.setThunderTime(swProps.getThunderTime());
            props.setGameType(swProps.getGameType());
            props.setInitialized(swProps.isInitialized());
            // props.setWanderingTraderId(swProps.getWanderingTraderId());
            // props.setWanderingTraderSpawnChance(swProps.getWanderingTraderSpawnChance());
            // props.setWanderingTraderSpawnDelay(swProps.getWanderingTraderSpawnDelay());
            // props.setLegacyWorldBorderSettings(swProps.getLegacyWorldBorderSettings());
        }
        
        props.setModdedInfo("fabric", true);
        props.setModdedInfo("multiworld", true);
        
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
        // this.mw$levelStorageAccess.saveDataTag(this.getServer().registryAccess(), getSaveProperties(), this.getServer().getPlayerList().getSingleplayerData());
    }

}
