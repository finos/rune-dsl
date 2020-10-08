package com.rosetta.model.lib;

import com.rosetta.model.lib.meta.GlobalKeyFields.GlobalKeyFieldsBuilder;

public interface GlobalKeyBuilder {

    GlobalKeyFieldsBuilder getMeta();

	GlobalKeyFieldsBuilder getOrCreateMeta();
}
