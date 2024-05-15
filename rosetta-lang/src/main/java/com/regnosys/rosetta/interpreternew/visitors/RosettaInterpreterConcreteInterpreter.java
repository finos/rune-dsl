package com.regnosys.rosetta.interpreternew.visitors;

import javax.inject.Inject;

import com.regnosys.rosetta.interpreternew.RosettaInterpreterVisitor;

public abstract class RosettaInterpreterConcreteInterpreter {
	@Inject
	protected RosettaInterpreterVisitor visitor;
}
