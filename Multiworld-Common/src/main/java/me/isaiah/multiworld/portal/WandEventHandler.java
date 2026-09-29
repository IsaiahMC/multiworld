/**
 * Multiworld - Portals
 */
package me.isaiah.multiworld.portal;

import java.util.HashMap;
import java.util.UUID;

import me.isaiah.multiworld.MultiworldMod;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

import static me.isaiah.multiworld.MultiworldMod.message;

public class WandEventHandler {
	
	/**
	 * Values:
	 * Object[0] = ServerWorld
	 * Object[1] = BlockPos 1
	 * Object[2] = BlockPos 2
	 */
    private static final HashMap<UUID, Object[]> playerPositions = new HashMap<>();

	private static final ItemStack wand = new ItemStack(Items.WOODEN_AXE);
    
    /**
     * Left-click = Position 1
     */
    public static InteractionResult leftClickBlock(Player player, Level world, BlockPos pos) {
    	 if (!world.isClientSide() && isHoldingWand(player)) {
             setPosition(player, pos, 1);
             return InteractionResult.PASS;
         }
         return InteractionResult.PASS;
    }
    
    /**
     * Right-click = Position 2
     */
    public static InteractionResult rightClickBlock(Player player, Level world, BlockHitResult hitResult) {
    	if (!world.isClientSide() && isHoldingWand(player)) {
            setPosition(player, hitResult.getBlockPos(), 2);
            return InteractionResult.PASS;
        }
        return InteractionResult.PASS;
    }
    
    public static ItemStack getItemStack() {
    	return wand;
    }

    private static boolean isHoldingWand(Player player) {
        ItemStack held = player.getMainHandItem();
        return held.getItem() == wand.getItem();
    }

    private static void setPosition(Player player, BlockPos pos, int index) {
        UUID uuid = player.getUUID();
        Object[] positions = playerPositions.getOrDefault(uuid, new Object[3]);
        positions[index] = pos;
        playerPositions.put(uuid, positions);
        
        try {
        	positions[0] = MultiworldMod.getWorldFor(player);
        } catch (NoSuchMethodError err) {
        	
        	// 1.21.8:
        	try {
        		positions[0] = (ServerLevel) me.isaiah.multiworld.fabric.ICommonHooks.getWorld(player);
        	} catch (Exception e) {
        		err.printStackTrace();
        		e.printStackTrace();
        	}
        }

        // TODO: Make less annoying
        // message(player, "&9[MultiworldPortals]&a📍&r Position " + index + " set to: " + pos.toShortString());
    }

    public static Object[] getWandPositions(UUID playerId) {
        return playerPositions.getOrDefault(playerId, new Object[3]);
    }
    
    public static Object[] getWandPositionsOrNull(UUID playerId) {
    	
    	if (playerPositions.containsKey(playerId)) {
    		return playerPositions.get(playerId);
    	}
    	
    	return null;
    }
    
}
