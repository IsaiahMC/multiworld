/**
 * Multiworld - Portals
 */
package me.isaiah.multiworld.portal;

import java.util.HashMap;
import java.util.UUID;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
    public static ActionResult leftClickBlock(PlayerEntity player, World world, BlockPos pos) {
    	 if (!world.isClient && isHoldingWand(player)) {
             setPosition(player, pos, 1);
             return ActionResult.PASS;
         }
         return ActionResult.PASS;
    }
    
    /**
     * Right-click = Position 2
     */
    public static ActionResult rightClickBlock(PlayerEntity player, World world, BlockHitResult hitResult) {
    	if (!world.isClient && isHoldingWand(player)) {
            setPosition(player, hitResult.getBlockPos(), 2);
            return ActionResult.PASS;
        }
        return ActionResult.PASS;
    }
    
    public static ItemStack getItemStack() {
    	return wand;
    }

    private static boolean isHoldingWand(PlayerEntity player) {
        ItemStack held = player.getMainHandStack();
        return held.getItem() == wand.getItem();
    }

    private static void setPosition(PlayerEntity player, BlockPos pos, int index) {
        UUID uuid = player.getUuid();
        Object[] positions = playerPositions.getOrDefault(uuid, new Object[3]);
        positions[index] = pos;
        playerPositions.put(uuid, positions);
        
        positions[0] = (ServerWorld) player.getWorld();

        message(player, "&9[MultiworldPortals]&a📍&r Position " + index + " set to: " + pos.toShortString());
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
