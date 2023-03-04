package com.regnosys.rosetta.generator

import com.regnosys.rosetta.generator.java.RosettaJavaPackages
import org.eclipse.xtext.generator.IFileSystemAccess2
import java.util.List
import com.regnosys.rosetta.rosetta.RosettaRootElement

interface RosettaInternalGenerator {
	
	def void generate(RosettaJavaPackages packages, IFileSystemAccess2 fsa, List<RosettaRootElement> elements, String version)
	
}