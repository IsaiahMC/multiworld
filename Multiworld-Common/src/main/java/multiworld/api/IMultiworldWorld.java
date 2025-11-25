package multiworld.api;

import net.minecraft.util.Identifier;
import net.minecraft.world.level.storage.LevelStorage.Session;

/**
 * Interface for Multiworld added ServerWorld content.
 * Implemented by all ServerWorlds added by Multiworld.
 * 
 * {@link me.isaiah.multiworld.fabric.MultiworldWorld}
 */
public interface IMultiworldWorld {

	/**
	 * Retrieve the Identifer for this World
	 * @return Identifer
	 */
    public Identifier multiworld$getLevelId();

    /**
     * Retrieve the path of the Identifer of this world.
     */
    public String multiworld$getLevelName();

    /**
     * Saves the "level.dat" file for the MultiworldWorld
     * @see ServerWorld#save
     */
	void multiworld$saveLevelDatFile();

	/**
	 */
	Session multiworld$getLevelStorageSession();

}