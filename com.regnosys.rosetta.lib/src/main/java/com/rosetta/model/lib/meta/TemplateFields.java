package com.rosetta.model.lib.meta;

/**
 * Templates can be used to reduce data duplication by providing a reference to a separate "template". 
 * Instances can then specify the reference to the template and the common data can be extracted there.  
 * The instance can be merged with the template to form one complete, valid instance.
 * 
 * TemplateFields extends MetaFields to provide methods to get and set a global reference to the template's global key.
 */
public interface TemplateFields {

	/**
	 * Gets the template global reference, which corresponds to the template's global key.
	 */
	String getTemplateGlobalReference();

	interface TemplateFieldsBuilder extends TemplateFields {
		
		/**
		 * Sets the template global reference, which corresponds to the template's global key.
		 */
		TemplateFieldsBuilder setTemplateGlobalReference(String globalKey);
	}
}
