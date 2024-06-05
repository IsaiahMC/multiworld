package multiworld;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bundlemod
 */
public class MultiworldBundleMod implements ModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("MultiworldBundle");

	@Override
	public void onInitialize() {
		boolean has_fabric_api = FabricLoader.getInstance().getModContainer("fabric-api").isPresent();
		boolean has_common_lib = FabricLoader.getInstance().getModContainer("icommon").isPresent();

		// Print message if fabric api is not detected.
		if (!has_fabric_api) {
			LOGGER.info("===================================");
			LOGGER.info("ERROR: Fabric-API is not installed!");
			LOGGER.info("Multiworld requires Fabric-API to function properly");
			LOGGER.info("https://www.curseforge.com/minecraft/mc-mods/fabric-api");
			LOGGER.info("===================================");
			
			// Exit
			System.exit(0);
		}
		

		LOGGER.info("Hello Fabric world!");
	}

}