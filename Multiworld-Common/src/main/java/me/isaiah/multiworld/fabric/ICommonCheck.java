package me.isaiah.multiworld.fabric;

import java.util.Optional;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

public class ICommonCheck {

	public static boolean hasICommon() {
		Optional<ModContainer> container = FabricLoader.getInstance().getModContainer("icommon");
		
		if (!container.isPresent()) {
			return false;
		}

		// TODO: Check ICommonMod.API_VERSION

		return true;
	}
	
}
