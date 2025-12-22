package multiworld.api;

/**
 * todo: improve class name?
 */
public enum WorldFolderMode {

	/**
	 * serverFolder/world/dimentions/dimensions/myid/myname
	 */
	VANILLA,
	
	/**
	 * serverFolder/myname
	 * (or serverFolder/myid_myname)
	 */
	BUKKIT;
	
	public static WorldFolderMode getDefault() {
		return VANILLA;
	}
	
}
