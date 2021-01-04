package com.rosetta.model.lib.meta;

import com.rosetta.model.lib.meta.Keys.KeysBuilder;

public interface GlobalKeyFields {
	
	String getGlobalKey();
	
	String getExternalKey();
	
	Keys getKeys();
	
	interface GlobalKeyFieldsBuilder {
		
		String getGlobalKey();
		
		String getExternalKey();
		
		KeysBuilder getKeys();
		
		GlobalKeyFieldsBuilder setGlobalKey(String globalKey);
		
		GlobalKeyFieldsBuilder setExternalKey(String ExternalKey);
		
		GlobalKeyFieldsBuilder setKeys(KeysBuilder keys);
	}
}
