package me.isaiah.multiworld.fabric;

import net.minecraft.util.Formatting;
import net.minecraft.text.*;

/**
 * Ensures compatiblity with colored chat in 1.18.2
 */
public class Fabric18Text {
    
	public static Text colored_literal_Depreated(String txt, Formatting color) {
		return Text.of(txt);
	}

}