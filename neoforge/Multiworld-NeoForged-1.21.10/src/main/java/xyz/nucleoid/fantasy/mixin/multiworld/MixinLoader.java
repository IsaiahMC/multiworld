package xyz.nucleoid.fantasy.mixin.multiworld;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.SaveLoading;
import net.minecraft.server.world.ServerWorld;

@Mixin(MinecraftServer.class)
public class MixinLoader {

	/*
    @Inject(method = "createLevels", at = @At("HEAD"), cancellable = true)
    private void skipCustomWorld(MinecraftServer server, CallbackInfoReturnable<Map<ResourceKey<Level>, ServerLevel>> cir) {
        // Let NeoForge handle everything except our custom world
        Map<ResourceKey<Level>, ServerLevel> levels = new HashMap<>();

        for (ResourceKey<Level> key : server.registryAccess().registryOrThrow(Level.RESOURCE_KEY).keySet()) {
            if (key.equals(CustomWorldKeys.CUSTOM_WORLD)) {
                // Skip this one — we’ll load it ourselves later
                continue;
            }
            // Otherwise, let vanilla/NeoForge handle it
        }

        // If you want to fully override, you can set cir.setReturnValue(levels)
        // and build the map yourself.
    }
    */
	
	// @Inject()
	// private ServerWorld[] getWorldArray() {
}
