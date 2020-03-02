package com.regnosys.rosetta.generator.java.util;

/**
 * POST namespace changes: MetaType now need to resolve not only themselves but the Type they belong 
 * to (as previously everything was org.isda.cdm). So this Type class is added so it resolves MetaType 
 * and the base JavaType represents the parent type of meta.  
 *
 */
public class MetaType extends JavaType {

	private String metaFieldName;
	private String metaFieldSimpleName;
	
	public MetaType(String parentTypeName, String metaFieldName) {
		super(parentTypeName);
		this.metaFieldName = metaFieldName;
		String[] split = metaFieldName.split("\\.");
		this.metaFieldSimpleName = split[split.length - 1];
	}

	public String getMetaFieldName() {
		return metaFieldName;
	}

	public void setMetaFieldName(String name) {
		this.metaFieldName = name;
	}

	public String getMetaFieldSimpleName() {
		return metaFieldSimpleName;
	}

	public void setMetaFieldSimpleName(String metaFieldSimpleName) {
		this.metaFieldSimpleName = metaFieldSimpleName;
	}

	static MetaType create(String parentQName, String qName) {
		return new MetaType(parentQName, qName);
	}
	
	@Override
	public String toString() {
		return getMetaFieldSimpleName();
	}
}
