package xyz.nucleoid.fantasy.mixin.registry;

import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryInfo;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.nucleoid.fantasy.RemoveFromRegistry;

import java.util.Map;

@Mixin(SimpleRegistry.class)
public abstract class SimpleRegistryMixin<T> implements RemoveFromRegistry<T> {

    @Shadow @Final private Map<T, RegistryEntry.Reference<T>> valueToEntry;
    @Shadow @Final private Map<Identifier, RegistryEntry.Reference<T>> idToEntry;
    @Shadow @Final private Map<RegistryKey<T>, RegistryEntry.Reference<T>> keyToEntry;
    @Shadow @Final private Map<RegistryKey<T>, RegistryEntryInfo> keyToEntryInfo;
    @Shadow @Final private ObjectList<RegistryEntry.Reference<T>> rawIdToEntry;
    @Shadow @Final private Reference2IntMap<T> entryToRawId;
    @Shadow @Final RegistryKey<? extends Registry<T>> key;
    @Shadow private boolean frozen;

    @Override
    public boolean fantasy$remove(T entry) {
        var registryEntry = this.valueToEntry.get(entry);
        int rawId = this.entryToRawId.removeInt(entry);
        if (rawId == -1) {
            return false;
        }

        try {
            this.rawIdToEntry.set(rawId, null);
            this.idToEntry.remove(registryEntry.registryKey().getValue());
            this.keyToEntry.remove(registryEntry.registryKey());
            this.keyToEntryInfo.remove(entry);
            this.valueToEntry.remove(entry);
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean fantasy$remove(Identifier key) {
        var entry = this.idToEntry.get(key);
        return entry != null && entry.hasKeyAndValue() && this.fantasy$remove(entry.value());
    }

    @Override
    public void fantasy$setFrozen(boolean value) {
        this.frozen = value;
    }

    @Override
    public boolean fantasy$isFrozen() {
        return this.frozen;
    }
}
