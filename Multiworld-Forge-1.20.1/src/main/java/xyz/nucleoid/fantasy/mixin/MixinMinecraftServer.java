// (c) 2023 Isaiah
package xyz.nucleoid.fantasy.mixin;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.function.BooleanSupplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import xyz.nucleoid.fantasy.Fantasy;
import xyz.nucleoid.fantasy.IMC;
import xyz.nucleoid.fantasy.util.SafeIterator;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer implements IMC {

	// The locals you have to manage for an inject are insane. And do it twice. A redirect is much cleaner.
		// Here is what it looks like with an inject: https://gist.github.com/i509VCB/f80077cc536eb4dba62b794eba5611c1
	@Redirect(method = "createWorlds", at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"))
	private <K, V> V onLoadWorld(Map<K, V> worlds, K registryKey, V serverWorld) {
		final V result = worlds.put(registryKey, serverWorld);
		// ServerWorldEvents.LOAD.invoker().onWorldLoad((MinecraftServer) (Object) this, (ServerWorld) serverWorld);
		
		// System.out.println("LOADING WORLD!: " + registryKey);

		return result;
	}
	
	@Redirect(method = "tickWorlds", at = @At(value = "INVOKE", target = "Ljava/lang/Iterable;iterator()Ljava/util/Iterator;", ordinal = 0), require = 0)
	private Iterator<ServerWorld> fantasy$copyBeforeTicking(Iterable<ServerWorld> instance) {
		return new SafeIterator<>((Collection<ServerWorld>) instance);
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;tickWorlds(Ljava/util/function/BooleanSupplier;)V"), method = "tick")
	private void on_start_tick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
		Fantasy fantasy = Fantasy.get((MinecraftServer) (Object) this);
        fantasy.tick();
	}

	@Inject(at = @At("HEAD"), method = "shutdown")
	private void before_shutdown_server(CallbackInfo info) {
		Fantasy fantasy = Fantasy.get((MinecraftServer) (Object) this);
        fantasy.onServerStopping();
	}
	
	@Shadow
    private Map<RegistryKey<World>, ServerWorld> worlds;

	@Override
	public void add_world(RegistryKey<World> key, ServerWorld value) {
		// TODO Auto-generated method stub
		worlds.put(key, value);
	}

}