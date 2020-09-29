package com.rosetta.model.lib.meta;

public interface GlobalKeyFields {
	
	String getGlobalKey();
	
	String getExternalKey();
	
	interface GlobalKeyFieldsBuilder extends GlobalKeyFields {
		
		GlobalKeyFieldsBuilder setGlobalKey(String globalKey);
		
		GlobalKeyFieldsBuilder setExternalKey(String ExternalKey);
	}
}
