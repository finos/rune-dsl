package com.rosetta.model.lib.meta;

public interface MetaFieldsI {
	
	public String getGlobalKey() ;
	public String getExternalKey() ;
	
	public interface MetaFieldsBuilderI extends MetaFieldsI{
		public MetaFieldsBuilderI setGlobalKey(String globalKey) ;
		public MetaFieldsBuilderI setExternalKey(String ExternalKey) ;
		
	}

}
