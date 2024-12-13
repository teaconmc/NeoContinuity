package me.pepperbell.ctm.impl.client;

import me.pepperbell.ctm.api.client.ContinuityFeatureStates;
import me.pepperbell.ctm.client.model.ModelObjectsContainer;
import me.pepperbell.ctm.client.util.BooleanState;

public class ContinuityFeatureStatesImpl implements ContinuityFeatureStates {
	private final FeatureStateImpl connectedTexturesState = new FeatureStateImpl();
	private final FeatureStateImpl emissiveTexturesState = new FeatureStateImpl();
	{
		connectedTexturesState.enable();
		emissiveTexturesState.enable();
	}

	public static ContinuityFeatureStatesImpl get() {
		return ModelObjectsContainer.get().featureStates;
	}

	@Override
	public FeatureState getConnectedTexturesState() {
		return connectedTexturesState;
	}

	@Override
	public FeatureState getEmissiveTexturesState() {
		return emissiveTexturesState;
	}

	public static class FeatureStateImpl extends BooleanState implements FeatureState {
	}
}
