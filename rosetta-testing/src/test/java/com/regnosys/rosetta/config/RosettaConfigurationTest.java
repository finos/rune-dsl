package com.regnosys.rosetta.config;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.function.Predicate;

import com.regnosys.rosetta.tests.RosettaInjectorProvider;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaInjectorProvider.class)
public class RosettaConfigurationTest {
	@Inject
	private RosettaConfiguration config;

	@Test
	public void testConfig() {
		assertNotNull(config.getModel());
		assertEquals("XYZ Model", config.getModel().getName());
		assertEquals(Collections.emptyList(), config.getDependencies());
		assertNotNull(config.getGenerators());
		
		Predicate<String> filter = config.getGenerators().getNamespaceFilter();
		assertNotNull(filter);
		assertTrue(filter.test("xyz"));
		assertTrue(filter.test("xyz.sub"));
		assertFalse(filter.test("xyzsub"));
		assertFalse(filter.test("foo"));
		assertTrue(filter.test("abc.def"));
		assertFalse(filter.test("abc.def.sub"));
	}
}
