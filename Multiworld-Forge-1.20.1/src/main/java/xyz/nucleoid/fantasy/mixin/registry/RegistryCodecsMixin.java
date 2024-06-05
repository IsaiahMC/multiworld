package xyz.nucleoid.fantasy.mixin.registry;

import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.registry.RegistryCodecs;

@Mixin(RegistryCodecs.class)
public class RegistryCodecsMixin {
	/**
	 * Fix the issue that cannot load world after uninstalling a dimension mod/datapack.
	 * After uninstalling a dimension mod/datapack, the dimension config in `level.dat` file cannot be deserialized.
	 * The solution is to make it fail-soft.
	 * Currently (1.19.3), `createKeyedRegistryCodec` is only used in dimension codec.
	 */
	/*@ModifyVariable(
			method = "createKeyedRegistryCodec",
			at = @At(
					value = "INVOKE_ASSIGN",
					target = "Lcom/mojang/serialization/Codec;unboundedMap(Lcom/mojang/serialization/Codec;Lcom/mojang/serialization/Codec;)Lcom/mojang/serialization/codecs/UnboundedMapCodec;",
					remap = false
			),
			ordinal = 1 // there are two local variables of `Codec` type. Modify the second.
	)
	private static <E> Codec<Map<RegistryKey<E>, E>> modifyCodecLocalVariable(
			Codec<Map<RegistryKey<E>, E>> originalVariable,
			RegistryKey<? extends Registry<E>> registryRef,
			Lifecycle lifecycle, Codec<E> elementCodec
	) {
		// make sure that it's not modifying the wrong variable
		// Validate.isTrue(originalVariable instanceof UnboundedMapCodec<?, ?>);

		return new FailSoftMapCodec<>(RegistryKey.createCodec(registryRef), elementCodec);
	}*/
}