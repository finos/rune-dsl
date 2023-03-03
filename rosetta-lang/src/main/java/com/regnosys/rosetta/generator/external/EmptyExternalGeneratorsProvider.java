package com.regnosys.rosetta.generator.external;

import java.util.Collections;
import java.util.Iterator;

import com.google.inject.Provider;

/**
 * The default implementation of {@code IExternalGeneratorProvider} that returns no external generators. 
 * @author jimwang
 */
public class EmptyExternalGeneratorsProvider implements Provider<ExternalGenerators> {

	@Override
	public ExternalGenerators get() {
		return new ExternalGenerators() {
			@Override
			public Iterator<ExternalGenerator> iterator() {
				return Collections.emptyIterator();
			}
		};
	}

}
