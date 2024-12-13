package me.pepperbell.ctm.impl.client;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.pepperbell.ctm.api.client.ProcessingDataKey;
import me.pepperbell.ctm.api.client.ProcessingDataKeyRegistry;
import net.minecraft.resources.ResourceLocation;

public final class ProcessingDataKeyRegistryImpl implements ProcessingDataKeyRegistry {
	public static final ProcessingDataKeyRegistryImpl INSTANCE = new ProcessingDataKeyRegistryImpl();

	private final Map<ResourceLocation, ProcessingDataKey<?>> keyMap = new Object2ObjectOpenHashMap<>();
	private final List<ProcessingDataKey<?>> allResettable = new ObjectArrayList<>();
	private final List<ProcessingDataKey<?>> allResettableView = Collections.unmodifiableList(allResettable);

	private int registeredAmount = 0;
	private boolean frozen;

	@Override
	public <T> ProcessingDataKey<T> registerKey(ResourceLocation id, Supplier<T> valueSupplier, Consumer<T> valueResetAction) {
		if (frozen) {
			throw new IllegalArgumentException("Cannot register processing data key for ID '" + id + "' to frozen registry");
		}
		ProcessingDataKey<?> oldKey = keyMap.get(id);
		if (oldKey != null) {
			throw new IllegalArgumentException("Cannot override processing data key registration for ID '" + id + "'");
		}
		ProcessingDataKeyImpl<T> key = new ProcessingDataKeyImpl<>(id, registeredAmount, valueSupplier, valueResetAction);
		keyMap.put(id, key);
		if (valueResetAction != null) {
			allResettable.add(key);
		}
		registeredAmount++;
		return key;
	}

	@Override
	@Nullable
	public ProcessingDataKey<?> getKey(ResourceLocation id) {
		return keyMap.get(id);
	}

	@Override
	public int getRegisteredAmount() {
		return registeredAmount;
	}

	public void setFrozen() {
		frozen = true;
	}

	/*public void init() {
		ClientLifecycleEvents.CLIENT_STARTED.register(client -> );
	}*/

	public List<ProcessingDataKey<?>> getAllResettable() {
		return allResettableView;
	}
}
