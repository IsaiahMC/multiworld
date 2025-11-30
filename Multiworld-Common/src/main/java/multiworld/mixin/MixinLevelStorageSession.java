package multiworld.mixin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.isaiah.multiworld.MultiworldMod;
import me.isaiah.multiworld.Utils;
import me.isaiah.multiworld.command.Util;
import me.isaiah.multiworld.config.FileConfiguration;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelStorage.Session;

/**
 * Multiworld Mixin for LevelStorage.Session
 */
@Mixin(Session.class)
public class MixinLevelStorageSession {

	@Inject(at = @At("HEAD"), method = "getWorldDirectory", cancellable = true)
	public void multiworld$getWorldDirectory(RegistryKey<World> key, CallbackInfoReturnable<Path> ci) {
		Identifier id = key.getValue();

		if (id.getNamespace().equalsIgnoreCase("minecraft")) {
			return;
		}

		// createConfigAndWorld creates the Config first
		if (Utils.shouldUseNewWorldFormat(MultiworldMod.mc, id)) {
			Identifier dim = Utils.getEnvironment(MultiworldMod.mc, id);

			if (null == dim) {
				System.out.println("Debug: Null DIM: " + id);
				return;
			}

			String dirName = ((Session) (Object) this).getDirectoryName();
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

	public Path mw$getStorageFolder(Path path, RegistryKey<DimensionOptions> dimensionType) {
		if (dimensionType == DimensionOptions.OVERWORLD) {
			return path;
		} else if (dimensionType == DimensionOptions.NETHER) {
			return path.resolve("DIM-1");
		} else {
			return dimensionType == DimensionOptions.END
					? path.resolve("DIM1")
							: path.resolve("dimensions").resolve(dimensionType.getValue().getNamespace()).resolve(dimensionType.getValue().getPath());
		}
	}


}
