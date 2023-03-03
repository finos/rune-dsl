package com.regnosys.rosetta.generator.external;

public class ExternalOutputConfiguration {

	private final String name;
	private final String description;
	
	public ExternalOutputConfiguration(String name, String description) {
		this.name = name; 
		this.description = description;
	}

	public String getDirectory() {
		return name.toLowerCase();
	}
	
	public String getName() {
		return name.toUpperCase() + "_OUTPUT";
	}

	public String getDescription() {
		return description;
	}
	
}
