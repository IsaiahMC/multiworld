package me.isaiah.multiworld.fabric;

import net.minecraft.world.level.LevelProperties;

public class MySaveProperties extends LevelProperties {

	private String nameOverride;
	private LevelProperties original;
	
	public MySaveProperties(LevelProperties original) {
		// #if mc192
		// super(original.getLevelInfo(), original.getGeneratorOptions(), original.getLifecycle());
		// #elif mc182
		// super(original.getLevelInfo(), original.getGeneratorOptions(), original.getLifecycle());
		// #else
		super(original.getLevelInfo(), original.getGeneratorOptions(), getSpecialProperty(original), original.getLifecycle());
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
	private static SpecialProperty getSpecialProperty(LevelProperties input) {
		return input.isFlatWorld() ? SpecialProperty.FLAT : SpecialProperty.NONE;
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
	public long getTime() {
		return original.getTime();
	}

}
