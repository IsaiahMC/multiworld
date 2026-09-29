package multiworld.mixin;

import net.minecraft.world.level.LevelSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LevelSettings.class)
public interface MixinLevelInfo {

	@Accessor
	public void setLevelName(String name);
	
}
