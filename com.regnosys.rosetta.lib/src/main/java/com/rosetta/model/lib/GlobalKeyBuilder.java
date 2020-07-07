package com.rosetta.model.lib;

import com.rosetta.model.lib.meta.MetaFieldsI;

public interface GlobalKeyBuilder extends GlobalKey {

    MetaFieldsI.MetaFieldsBuilderI getMeta();

	MetaFieldsI.MetaFieldsBuilderI getOrCreateMeta();
}
