package com.rosetta.model.lib.meta;

/**
 * Fields specified in [metadata] annotation excluding globalKey and template fields which have separate interfaces.
 * 
 * @see GlobalKeyFields
 * @see TemplateFields
 */
public interface MetaDataFields {

	String getScheme();

	interface MetaDataFieldsBuilder extends MetaDataFields {

		MetaDataFieldsBuilder setScheme(String scheme);
	}
}