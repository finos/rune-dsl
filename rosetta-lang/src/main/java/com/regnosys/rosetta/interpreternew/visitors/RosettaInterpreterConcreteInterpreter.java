package com.regnosys.rosetta.interpreternew.visitors;

import javax.inject.Inject;

import com.regnosys.rosetta.interpreternew.RosettaInterpreterVisitor;
import com.regnosys.rosetta.interpreternew.RosettaInterpreterVisitorBase;


public abstract class RosettaInterpreterConcreteInterpreter {
	@Inject
	protected RosettaInterpreterVisitor visitor = new RosettaInterpreterVisitor();
}
