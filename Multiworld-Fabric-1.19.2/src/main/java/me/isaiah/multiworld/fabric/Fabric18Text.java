package me.isaiah.multiworld.fabric;

import net.minecraft.util.Formatting;
import net.minecraft.text.*;

import net.minecraft.util.Identifier;
import me.isaiah.multiworld.MultiworldMod;

//import me.isaiah.lib.IText;

/**
 * Ensures compatiblity with colored chat in 1.18.2
 */
public class Fabric18Text {
    
	public static Text colored_literal(String txt, Formatting color) {
		return Text.of(txt);//IText.colored_literal(txt, color);
	}

}