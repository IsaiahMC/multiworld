package multiworld.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.ModifyReceiver;
// import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.server.command.GameRuleCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;

import static me.isaiah.multiworld.MultiworldMod.message;

@Mixin(GameRuleCommand.class)
public class MixinGameruleCommand {

	/**
	 static <T extends GameRules.Rule<T>> int executeSet(CommandContext<ServerCommandSource> context, GameRules.Key<T> key) {
        ServerCommandSource lv = context.getSource();
        Object lv2 = lv.getServer().getGameRules().get(key);
        ((GameRules.Rule)lv2).set(context, "value");
        lv.getServer().onGameRuleUpdated(key.getName(), (GameRules.Rule<?>)lv2);
        lv.sendFeedback(() -> Text.translatable("commands.gamerule.set", key.getName(), lv2.toString()), true);
        return ((GameRules.Rule)lv2).getCommandResult();
    }

    static <T extends GameRules.Rule<T>> int executeQuery(ServerCommandSource source, GameRules.Key<T> key) {
        Object lv = source.getServer().getGameRules().get(key);
        source.sendFeedback(() -> Text.translatable("commands.gamerule.query", key.getName(), lv.toString()), false);
        return ((GameRules.Rule)lv).getCommandResult();
    }
	 *
	 */
	
	private static final String mw$target = "Lnet/minecraft/world/GameRules;get(Lnet/minecraft/world/GameRules$Key;)Lnet/minecraft/world/GameRules$Rule;";
	

	@ModifyReceiver(at = @At(value = "INVOKE", target = mw$target), method = "executeSet") // , locals = LocalCapture.CAPTURE_FAILEXCEPTION)
	private static GameRules multiworld$executeSet(GameRules rules, GameRules.Key<?> key, @Local CommandContext<ServerCommandSource> context) {
		ServerWorld world = context.getSource().getWorld();

		if (null == world) {
			return rules; 
		}

		Identifier id = world.getRegistryKey().getValue();
		
		if (!id.getNamespace().equalsIgnoreCase("minecraft")) {
			message(context.getSource(), "&a[Multiworld]: &rGamerules for world \"" + id + "\"");
			return world.getGameRules();
		}
		
		return rules;
	}
	
	@ModifyReceiver(at = @At(value = "INVOKE", target = mw$target), method = "executeQuery") // , locals = LocalCapture.CAPTURE_FAILEXCEPTION)
	private static GameRules multiworld$executeQuery(GameRules rules, GameRules.Key<?> key, @Local ServerCommandSource source) {
		ServerWorld world = source.getWorld();

		if (null == world) {
			return rules; 
		}

		Identifier id = world.getRegistryKey().getValue();
		
		if (!id.getNamespace().equalsIgnoreCase("minecraft")) {
			message(source, "&a[Multiworld]: &rQuerying Gamerules for world \"" + id + "\"");
			return world.getGameRules();
		}
		
		return rules;
	}

	
	/*
	@Inject(at = @At(value = "INVOKE", target = mw$target), method = "executeSet", locals = LocalCapture.CAPTURE_FAILEXCEPTION)
	private static void multiworld$executeSet(GameRules rules, GameRules.Key<?> key, CommandContext<ServerCommandSource> context) {
		
	}
	
	/*
	@Redirect(at = @At(value = "INVOKE", target = mw$target), method = "executeQuery", locals = LocalCapture.CAPTURE_FAILEXCEPTION)
	private static void multiworld$executeQuery(GameRules rules, GameRules.Key<?> key, ServerCommandSource source) {
		ServerWorld world = source.getWorld();
	}
	*/
	
	/*
	@Inject(at = @At(value = "HEAD"), method = "executeQuery")
	private static int multiworld$executeQuery(GameRules rules, GameRules.Key<?> key, ServerCommandSource source) {
		Object lv = source.getServer().getGameRules().get(key);
        source.sendFeedback(() -> Text.translatable("commands.gamerule.query", key.getName(), lv.toString()), false);
        return ((GameRules.Rule)lv).getCommandResult();
	}
	*/
	
}
