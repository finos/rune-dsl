package com.regnosys.rosetta.generator.java.reports

import com.regnosys.rosetta.generator.java.RosettaJavaPackages.RootPackage
import com.regnosys.rosetta.rosetta.RosettaBlueprintReport
import org.eclipse.xtext.generator.IFileSystemAccess2
import javax.inject.Inject
import com.regnosys.rosetta.types.RObjectFactory

class ReportGenerator {
	@Inject extension RObjectFactory

	def generate(RootPackage root, IFileSystemAccess2 fsa, RosettaBlueprintReport report, String version) {
		
		val rFunction = buildRFunction(report, #[])
		

	}
	
	


}
