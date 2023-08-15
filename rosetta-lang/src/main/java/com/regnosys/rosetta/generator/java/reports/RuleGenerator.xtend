package com.regnosys.rosetta.generator.java.reports

import com.regnosys.rosetta.generator.java.RosettaJavaPackages.RootPackage
import com.regnosys.rosetta.generator.java.blueprints.BlueprintGenerator
import com.regnosys.rosetta.rosetta.RosettaBlueprint
import javax.inject.Inject
import org.eclipse.xtext.generator.IFileSystemAccess2

class RuleGenerator {
	@Inject BlueprintGenerator blueprintGenerator

	
	def generate(RootPackage root, IFileSystemAccess2 fsa, RosettaBlueprint rule, String version) {
		blueprintGenerator.generate(root, fsa, #[rule], version)
	}
	
	
}