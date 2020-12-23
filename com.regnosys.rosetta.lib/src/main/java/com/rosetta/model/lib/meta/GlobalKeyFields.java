package com.rosetta.model.lib.meta;

import java.util.List;

public interface GlobalKeyFields {
	
	String getGlobalKey();
	
	String getExternalKey();
	
	List<Key> getKeys();
	
	interface GlobalKeyFieldsBuilder extends GlobalKeyFields{
		
		GlobalKeyFieldsBuilder setGlobalKey(String globalKey);
		
		GlobalKeyFieldsBuilder setExternalKey(String ExternalKey);
		
		GlobalKeyFieldsBuilder setKeys(List<Key> keys);
	}
}
