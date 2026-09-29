package me.isaiah.multiworld.fabric;

import net.minecraft.world.level.storage.PrimaryLevelData;

public class MySaveProperties extends PrimaryLevelData {

	private String nameOverride;
	private PrimaryLevelData original;
	
	public MySaveProperties(PrimaryLevelData original) {
		// #if mc261
		// super(original.getLevelSettings(), getSpecialProperty(original), original.worldGenSettingsLifecycle());
		// #elif mc192
		// super(original.getLevelInfo(), original.getGeneratorOptions(), original.getLifecycle());
		// #elif mc182
		// super(original.getLevelInfo(), original.getGeneratorOptions(), original.getLifecycle());
		// #else
		super(original.getLevelSettings(), original.worldGenOptions(), getSpecialProperty(original), original.worldGenSettingsLifecycle());
		// #endif
		this.original = original;
	}
	
	public MySaveProperties withName(String name) {
		this.nameOverride = name;
		return this;
	}
	
	// #if mc192
	// // Skip: getSpecialProperty
	// #elif mc182
	// // Skip: getSpecialProperty
	// #else
	private static SpecialWorldProperty getSpecialProperty(PrimaryLevelData input) {
		return input.isFlatWorld() ? SpecialWorldProperty.FLAT : SpecialWorldProperty.NONE;
	}
	// #endif
	
	@Override
	public String getLevelName() {
		return (null != nameOverride) ? nameOverride : super.getLevelName();
	}

	public void mw$setLevelName(String name) {
		this.nameOverride = name;
	}
	
	@Override
	public long getGameTime() {
		return original.getGameTime();
	}

}
