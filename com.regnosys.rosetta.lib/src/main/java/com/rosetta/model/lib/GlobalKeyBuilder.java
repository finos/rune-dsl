package com.rosetta.model.lib;

import com.rosetta.model.lib.meta.MetaFieldsI;

//TODO remove this useless generic - but it will create a backwards incompatibility so I will
//remove all the usages as a preliminary step
public interface GlobalKeyBuilder extends GlobalKey {

    MetaFieldsI.MetaFieldsBuilderI getMeta();

	MetaFieldsI.MetaFieldsBuilderI getOrCreateMeta();
}
