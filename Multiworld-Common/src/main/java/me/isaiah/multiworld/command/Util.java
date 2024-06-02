package me.isaiah.multiworld.command;

import me.isaiah.multiworld.MultiworldMod;
import net.minecraft.util.Identifier;

public class Util {

	
	// Dimension Ids
    public static final Identifier OVERWORLD_ID = id("overworld");
    public static final Identifier THE_NETHER_ID = id("the_nether");
    public static final Identifier THE_END_ID = id("the_end");

    public static Identifier id(String id) {
    	return MultiworldMod.new_id(id);
    }
    
}