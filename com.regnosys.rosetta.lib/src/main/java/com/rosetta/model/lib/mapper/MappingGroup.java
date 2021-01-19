package com.rosetta.model.lib.mapper;

import java.util.List;
import java.util.function.Function;

public class MappingGroup<A, B>{
	private final String identifier;
	private final String uri;
	
	// The in process eclipse compiler has a problem with the generated blueprints code that uses this type.
	// After investigating - we found that this is actually wrong and the normal eclipse compiler is lenient. 
	// This is actually a but and should be changed when we re-enable the RosettaBlueprintsTest.
    // TODO - This should be List<? extends Function<A, Mapper<B>>>
	private final List<Function<A, Mapper<B>>> functions;
	
	public MappingGroup(String identifier, String uri, List<Function<A, Mapper<B>>> functions) {
		super();
		this.identifier = identifier;
		this.uri = uri;
		this.functions = functions;
	}

	public String getIdentifier() {
		return identifier;
	}

	public List<Function<A, Mapper<B>>> getFunctions() {
		return functions;
	}

	public String getUri() {
		return uri;
	}
	
	 
}
