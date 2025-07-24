package com.regnosys.rosetta.generator.resourcefsa

import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtend.lib.annotations.Delegate
import com.regnosys.rosetta.generator.RosettaOutputConfigurationProvider

class TestResourceAwareFSAFactory implements ResourceAwareFSAFactory {
	override IFileSystemAccess2 resourceAwareFSA(Resource resource, IFileSystemAccess2 fsa, boolean wholeProject) {
		return new TestFolderAwareFsa(resource, fsa, wholeProject)
	}
	static class TestFolderAwareFsa implements IFileSystemAccess2 {
		@Delegate IFileSystemAccess2 originalFsa
		boolean testRes

		new(Resource resource, IFileSystemAccess2 originalFsa, boolean wholeProject) {
			this.originalFsa = originalFsa
			this.testRes = !wholeProject && isTestResource(resource)
		}

		def static boolean isTestResource(Resource resource) {
			if (resource.URI !== null) {
				// hardcode the folder for now
				return resource.URI.toString.contains('src/test/resources/')
			}
			false
		}

		override void generateFile(String fileName, CharSequence contents) {
			if (testRes) {
				originalFsa.generateFile(fileName, RosettaOutputConfigurationProvider.SRC_TEST_GEN_JAVA_OUTPUT,
					contents)
			} else {
				originalFsa.generateFile(fileName, contents)
			}
		}
	}
	
	override afterGenerate(Resource resource) {
	}
	
	override beforeGenerate(Resource resource) {
	}
	
}
