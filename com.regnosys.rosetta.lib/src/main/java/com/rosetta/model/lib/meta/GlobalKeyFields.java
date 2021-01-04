package com.rosetta.model.lib.meta;

import java.util.List;

public interface GlobalKeyFields {
	
	String getGlobalKey();
	
	String getExternalKey();
	
	List<? extends Key> getKeys();
	
	interface GlobalKeyFieldsBuilder extends GlobalKeyFields{
		
		GlobalKeyFieldsBuilder setGlobalKey(String globalKey);
		
		GlobalKeyFieldsBuilder setExternalKey(String ExternalKey);
		
		GlobalKeyFieldsBuilder setKeys(List<? extends Key> keys);
		GlobalKeyFieldsBuilder addKey(Key key);
		GlobalKeyFieldsBuilder addKey(Key key, int _idx);
		GlobalKeyFieldsBuilder addKey(List<? extends Key> keys);
		GlobalKeyFieldsBuilder GetOrCreateKey(int _idx);
	}
}
