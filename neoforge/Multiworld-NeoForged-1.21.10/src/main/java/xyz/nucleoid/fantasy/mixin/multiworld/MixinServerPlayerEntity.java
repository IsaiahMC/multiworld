package xyz.nucleoid.fantasy.mixin.multiworld;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import me.isaiah.multiworld.I18n;
import me.isaiah.multiworld.MultiworldMod;
import me.isaiah.multiworld.command.PortalCommand;
import me.isaiah.multiworld.neoforge.MultiworldModNeoForge;
import me.isaiah.multiworld.portal.Portal;
import net.minecraft.block.BlockState;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;

@Mixin(ServerPlayerEntity.class)
public class MixinServerPlayerEntity {

    @Inject(
        method = "Lnet/minecraft/server/network/ServerPlayerEntity;teleportTo(Lnet/minecraft/world/TeleportTarget;)Lnet/minecraft/server/network/ServerPlayerEntity;",
        at = @At("HEAD"),
        cancellable = true
    )
    /**
     * Avoid recalling NeoForge's Event.
     */
    private void onTeleportToTarget(TeleportTarget target, CallbackInfoReturnable<ServerPlayerEntity> ci) {

    	
    	if (target.postTeleportTransition() instanceof MultiworldModNeoForge.MyPostDimensionTransition) {
    		I18n.message((ServerPlayerEntity) (Object) this, "is MyPostDimensionTransition");
    		return;
    	}
    	
    	I18n.message((ServerPlayerEntity) (Object) this, "TELEPORTTO debug");

    	RegistryKey<World> dimension = target.world().getRegistryKey();
    	EntityTravelToDimensionEvent event = new EntityTravelToDimensionEvent((ServerPlayerEntity) (Object) this, dimension);
    	MultiworldModNeoForge.onEntityTravel(event, (ServerPlayerEntity) (Object) this);
    	
    	if (event.isCanceled()) {
    		ci.setReturnValue(null);
    		ci.cancel();
    		return;
    	}
    }

}