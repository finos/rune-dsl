package com.rosetta.model.lib.meta;

public interface TemplateFields extends MetaFieldsI {

	public String getTemplateGlobalReference();
	
	public interface TemplateFieldsBuilder extends TemplateFields, MetaFieldsBuilderI {
		
		public TemplateFieldsBuilder setTemplateGlobalReference(String globalKey);
	}
}
