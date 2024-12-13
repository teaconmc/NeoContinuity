package me.pepperbell.ctm.impl.client;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import me.pepperbell.ctm.api.client.ProcessingDataKey;
import net.minecraft.resources.ResourceLocation;

public class ProcessingDataKeyImpl<T> implements ProcessingDataKey<T> {
	protected final ResourceLocation id;
	protected final int rawId;
	protected final Supplier<T> valueSupplier;
	protected final Consumer<T> valueResetAction;

	public ProcessingDataKeyImpl(ResourceLocation id, int rawId, Supplier<T> valueSupplier, Consumer<T> valueResetAction) {
		this.id = id;
		this.rawId = rawId;
		this.valueSupplier = valueSupplier;
		this.valueResetAction = valueResetAction;
	}

	@Override
	public ResourceLocation getId() {
		return id;
	}

	@Override
	public int getRawId() {
		return rawId;
	}

	@Override
	public Supplier<T> getValueSupplier() {
		return valueSupplier;
	}

	@Override
	@Nullable
	public Consumer<T> getValueResetAction() {
		return valueResetAction;
	}
}
