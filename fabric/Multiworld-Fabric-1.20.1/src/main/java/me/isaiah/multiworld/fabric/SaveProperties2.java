package me.isaiah.multiworld.fabric;

import net.minecraft.world.level.LevelProperties;

public class SaveProperties2 extends LevelProperties {

	private String nameOverride;
	private LevelProperties original;
	
	public SaveProperties2(LevelProperties original) {
		super(original.getLevelInfo(), original.getGeneratorOptions(), getSpecialProperty(original), original.getLifecycle());
		this.original = original;
	}
	
	public SaveProperties2 withName(String name) {
		this.nameOverride = name;
		return this;
	}
	
	private static SpecialProperty getSpecialProperty(LevelProperties input) {
		return input.isFlatWorld() ? SpecialProperty.FLAT : SpecialProperty.NONE;
	}
	
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
