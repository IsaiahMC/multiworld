package xyz.nucleoid.fantasy.mixin.multiworld;

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
	
	@Shadow
	@Mutable
	public LevelStorage.LevelSave directory;

	@Inject(at = @At("TAIL"), method = "Lnet/minecraft/world/level/storage/LevelStorage$Session;<init>(Lnet/minecraft/world/level/storage/LevelStorage;Ljava/lang/String;Ljava/nio/file/Path;)V")
	public void mw$init_test(LevelStorage s, String b, Path p, CallbackInfo ci) {
		System.out.println("DEBUG DIRNAME: " + b);
		System.out.println("DEBUG PATH: " + p.toString());
		
		Session thiz = (Session) (Object) this;
		
		System.out.println("LEVELSTORAGE.dat PATH: " + directory.getLevelDatPath() + " / "  + thiz.getDirectoryName() + " / " + thiz.getDirectory());
	}
	
	/*
	@Inject(at = @At("HEAD"), method = "getWorldDirectory", cancellable = true)
	public void multiworld$getWorldDirectory(RegistryKey<World> key, CallbackInfoReturnable<Path> ci) {
		
	}
	*/

	/**
	 * 
	 */
	public Path mw1$getStorageFolder(Path path, Identifier worldId, Identifier dimensionType) {
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

	public Path mw1$getStorageFolder(Path path, RegistryKey<DimensionOptions> dimensionType) {
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
