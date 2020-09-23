package com.rosetta.model.lib.process;

/**
 * @author TomForwood
 * Enum for meta information of attributes to be passed into process methods
 */
public enum AttributeMeta {
	/**
	 * Is this field defined as a meta field
	 */
	IS_META,
	/**
	 * Is this field defined as a global key, e.g. with "metadata id" annotation
	 */
	IS_GLOBAL_KEY_FIELD;

}
