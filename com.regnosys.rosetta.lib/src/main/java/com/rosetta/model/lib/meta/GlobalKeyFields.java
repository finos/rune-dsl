package com.rosetta.model.lib.meta;

import java.util.List;

import com.rosetta.model.lib.meta.Key.KeyBuilder;

public interface GlobalKeyFields {
	
	String getGlobalKey();
	
	String getExternalKey();
	
	List<Key> getKey();
	
	interface GlobalKeyFieldsBuilder {
		
		String getGlobalKey();
		
		String getExternalKey();
		
		List<KeyBuilder> getKey();
		
		GlobalKeyFieldsBuilder setGlobalKey(String globalKey);
		
		GlobalKeyFieldsBuilder setExternalKey(String ExternalKey);
		
		GlobalKeyFieldsBuilder addKeyBuilder(List<KeyBuilder>  key);
	}
}
