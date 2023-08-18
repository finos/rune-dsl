package com.regnosys.rosetta.generator.java.reports

import com.regnosys.rosetta.generator.java.RosettaJavaPackages.RootPackage
import com.regnosys.rosetta.rosetta.RosettaBlueprintReport
import com.regnosys.rosetta.types.RObjectFactory
import javax.inject.Inject
import org.eclipse.xtext.generator.IFileSystemAccess2

class ReportGenerator {
	@Inject extension RObjectFactory

	def generate(RootPackage root, IFileSystemAccess2 fsa, RosettaBlueprintReport report, String version) {
		
		val rFunction = buildRFunction(report)
		

	}


}
