package com.rosetta.model.lib;

import com.rosetta.model.lib.meta.GlobalKeyFields;

public interface GlobalKey {
	
	GlobalKeyFields getMeta();
	
	interface GlobalKeyBuilder extends GlobalKey{

		GlobalKeyFields.GlobalKeyFieldsBuilder getMeta();

		GlobalKeyFields.GlobalKeyFieldsBuilder getOrCreateMeta();
	}
}
