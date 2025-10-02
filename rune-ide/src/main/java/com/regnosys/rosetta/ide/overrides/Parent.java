package com.regnosys.rosetta.ide.overrides;

import org.eclipse.lsp4j.Location;

public class Parent {
	private final String title;
	private final Location location;
	
	public Parent(String title, Location location) {
		this.title = title;
		this.location = location;
	}

	public String getTitle() {
		return title;
	}

	public Location getLocation() {
		return location;
	}
}
