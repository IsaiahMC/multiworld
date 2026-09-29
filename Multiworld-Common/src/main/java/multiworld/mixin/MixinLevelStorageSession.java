package multiworld.mixin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.isaiah.multiworld.MultiworldMod;
import me.isaiah.multiworld.Utils;
import me.isaiah.multiworld.command.Util;
import me.isaiah.multiworld.config.FileConfiguration;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;

/**
 * Multiworld Mixin for LevelStorage.Session
 */
@Mixin(LevelStorageAccess.class)
public class MixinLevelStorageSession {

	@Shadow
	@Mutable
	private String levelId;
	
	@Inject(at = @At("HEAD"), method = "getDimensionPath", cancellable = true)
	public void multiworld$getWorldDirectory(ResourceKey<Level> key, CallbackInfoReturnable<Path> ci) {
		Identifier id = key.identifier();

		// System.out.println("Debug: " + id.toString());
		
		if (id.getNamespace().equalsIgnoreCase("minecraft")) {
			return;
		}
		
		if (Utils.isForge()) {
			// TODO: Add seperate directory support to NeoForge.
			return;
		}

		// createConfigAndWorld creates the Config first
		if (Utils.shouldUseNewWorldFormat(MultiworldMod.mc, id)) {
			Identifier dim = Utils.getEnvironment(MultiworldMod.mc, id);

			if (null == dim) {
				System.out.println("Debug: Null DIM: " + id);
				return;
			}

			String dirName = ((LevelStorageAccess) (Object) this).getLevelId();
			Path path = mw$getStorageFolder( Utils.getWorldStoragePath(MultiworldMod.mc).resolve( dirName ), id, dim);
			
			String s = Utils.getConfigValue(id, "worldDirectoryPath", "none");
			if (null != s && !s.equalsIgnoreCase("none")) {
				Path pat = new File(s).toPath();
				ci.setReturnValue(pat);
			}
			
			try {
				FileConfiguration config = Utils.getConfigOrNull(id);
				if (null != config) {
					config.set("worldDirectoryPath", path.toString());
					config.save();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// Path path = mw$getStorageFolder( ((Session) (Object) this).getDirectory().comp_732(), id, dim);
			ci.setReturnValue(path);
			return;
		}
	}

	/**
	 * 
	 */
	public Path mw$getStorageFolder(Path path, Identifier worldId, Identifier dimensionType) {
		
		LevelStorageAccess thiz = (LevelStorageAccess) (Object) this;
		String name = thiz.getLevelId();
		
		String name1 =Utils.getWorldName(worldId);
		
		if (!name.equalsIgnoreCase(name1)) {
			this.levelId = name1;
		}
		
		name = thiz.getLevelId();
		
		
		// System.out.println("mw$getStorageFolder: " + worldId.toString() + " / " + dimensionType.toString() + " / " + name );
		
		if (dimensionType == Util.OVERWORLD_ID) {
			return path;
		} else if (dimensionType == Util.THE_NETHER_ID) {
			return path.resolve("DIM-1");
		} else if (dimensionType == Util.THE_END_ID) {
			return path.resolve("DIM1");
		} else {

			// if (dimensionType.getNamespace())

			return path.resolve("dimensions").resolve(dimensionType.getNamespace()).resolve(dimensionType.getPath());
		}
	}

	public Path mw$getStorageFolder1(Path path, ResourceKey<LevelStem> dimensionType) {
		if (dimensionType == LevelStem.OVERWORLD) {
			return path;
		} else if (dimensionType == LevelStem.NETHER) {
			return path.resolve("DIM-1");
		} else {
			return dimensionType == LevelStem.END
					? path.resolve("DIM1")
							: path.resolve("dimensions").resolve(dimensionType.identifier().getNamespace()).resolve(dimensionType.identifier().getPath());
		}
	}


}
