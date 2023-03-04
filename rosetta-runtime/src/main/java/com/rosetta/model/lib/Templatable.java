package com.rosetta.model.lib;

import com.rosetta.model.lib.meta.TemplateFields;
import com.rosetta.model.lib.meta.TemplateFields.TemplateFieldsBuilder;

/**
 * Templates can be used to reduce data duplication by providing a reference to a separate "template". 
 * Instances can then specify the reference to the template and the common data can be extracted there.  
 * The instance can be merged with the template to form one complete, valid instance.
 * 
 * RosettaModelObjects specified as Templatable provide methods to get and set a global reference to the template's global key.
 */
public interface Templatable {
	
	/**
	 * Get TemplateFields, which contains a global reference to the template.
	 */
	TemplateFields getMeta();


	interface TemplatableBuilder {
	
		/**
		 * Get TemplateFieldsBuilder, which contains a global reference to the template.
		 */
	    TemplateFieldsBuilder getMeta();

	    /**
		 * Get or create TemplateFieldsBuilder, which contains a global reference to the template.
		 */
	    TemplateFieldsBuilder getOrCreateMeta();
	}
}
