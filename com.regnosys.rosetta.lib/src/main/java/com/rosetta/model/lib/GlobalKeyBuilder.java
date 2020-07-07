package com.rosetta.model.lib;

import com.rosetta.model.lib.meta.MetaFieldsI;

//TODO remove this useless generic
public interface GlobalKeyBuilder<T> extends GlobalKey {

    MetaFieldsI.MetaFieldsBuilderI getMeta();

	MetaFieldsI.MetaFieldsBuilderI getOrCreateMeta();
}
