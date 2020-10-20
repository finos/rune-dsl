package com.rosetta.model.lib.process;

/**
 * @author TomForwood
 * Enum for meta information of attributes to be passed into process methods
 */
public enum AttributeMeta {
	/**
	 * Is this field defined as a meta field
	 */
	META,
	/**
	 * Is this external key, e.g. with "metadata key" annotation on a type
	 */
	EXTERNAL_KEY,
	/**
	 * Is this global key, e.g. with "metadata key" annotation on a type
	 */
	GLOBAL_KEY,
	/**
	 * Is this field defined as a global key, e.g. with "metadata id" annotation
	 */
	GLOBAL_KEY_FIELD;

}
