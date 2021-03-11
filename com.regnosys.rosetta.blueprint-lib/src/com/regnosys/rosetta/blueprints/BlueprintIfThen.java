package com.regnosys.rosetta.blueprints;

public class BlueprintIfThen<I, O, K1, K2> {
	BlueprintBuilder<I, ?, K1, K1> ifInstance;
	BlueprintBuilder<I, O, K1, K2> thenInstance;
	
	public BlueprintIfThen(BlueprintBuilder<I, ?, K1, K1> ifInstance, BlueprintBuilder<I, O, K1, K2> thenInstance) {
		super();
		this.ifInstance = ifInstance;
		this.thenInstance = thenInstance;
	}

	public BlueprintIfThen(BlueprintBuilder<I, O, K1, K2> thenInstance) {
		super();
		this.thenInstance = thenInstance;
	}

}
