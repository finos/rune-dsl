package com.regnosys.rosetta.interpreternew;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.tests.RosettaInjectorProvider;

import static org.junit.jupiter.api.Assertions.*;
import javax.inject.Inject;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaInjectorProvider.class)
public class RosettaInterpreterNewTest {
	
	@Inject
	RosettaInterpreterNew interpreter;
	
	@Test
	public void TestTest() {
		assertEquals(5, interpreter.Test());
	}
}
