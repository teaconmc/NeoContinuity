package me.pepperbell.ctm.client.resource;

import me.pepperbell.ctm.client.util.BooleanState;

public final class InvalidIdentifierStateHolder {
	private static final ThreadLocal<BooleanState> STATES = ThreadLocal.withInitial(BooleanState::new);

	public static BooleanState get() {
		return STATES.get();
	}
}
