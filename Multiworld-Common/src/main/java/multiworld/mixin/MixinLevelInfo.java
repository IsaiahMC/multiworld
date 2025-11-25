package multiworld.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.level.LevelInfo;

@Mixin(LevelInfo.class)
public interface MixinLevelInfo {

	@Accessor
	public void setName(String name);
	
}
