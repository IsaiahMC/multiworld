package net.fabricmc.loader.api;

import java.nio.file.Path;
import java.io.File;

@Deprecated
public class FabricLoader {

	public static FabricLoader INSTANCE = new FabricLoader();


	public static FabricLoader getInstance() {
		return INSTANCE;
	}

	/**
	 * Get the current directory for game configuration files.
	 *
	 * @return the configuration directory
	 */
	public Path getConfigDir() {
		File dir = new File("config");
		if (!dir.isDirectory()) {
			dir.mkdir();
		}
		return dir.toPath();
	}
	
	//public java.util.Optional<ModContainer> getModContainer(String id) {
	//	return java.util.Optional.empty();
	//}
	
}