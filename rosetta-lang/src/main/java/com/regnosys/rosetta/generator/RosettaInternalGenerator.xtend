package com.regnosys.rosetta.generator

import org.eclipse.xtext.generator.IFileSystemAccess2
import java.util.List
import com.regnosys.rosetta.rosetta.RosettaRootElement
import com.regnosys.rosetta.generator.java.RosettaJavaPackages.RootPackage

interface RosettaInternalGenerator {
	
	def void generate(RootPackage root, IFileSystemAccess2 fsa, List<RosettaRootElement> elements, String version)
	
}