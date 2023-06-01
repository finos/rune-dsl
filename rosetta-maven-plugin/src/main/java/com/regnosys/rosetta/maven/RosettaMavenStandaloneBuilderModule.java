package com.regnosys.rosetta.maven;

import org.eclipse.xtext.builder.standalone.StandaloneBuilder;
import org.eclipse.xtext.maven.MavenStandaloneBuilderModule;

public class RosettaMavenStandaloneBuilderModule extends MavenStandaloneBuilderModule {
	@Override
	protected void configure() {
		super.configure();
		bind(StandaloneBuilder.class).to(RosettaStandaloneBuilder.class);
		
	}
}
