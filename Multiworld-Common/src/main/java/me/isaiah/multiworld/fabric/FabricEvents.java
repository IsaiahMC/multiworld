package me.isaiah.multiworld.fabric;

import me.isaiah.multiworld.portal.WandEventHandler;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;

public class FabricEvents {

    public static void register() {
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            return WandEventHandler.leftClickBlock(player, world, pos);
        });

        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            return WandEventHandler.rightClickBlock(player, world, hitResult);
        });
    }

}
