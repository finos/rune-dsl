package com.regnosys.rosetta.generator.resourcefsa;

import java.io.InputStream;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.generator.IFileSystemAccess2;
import org.eclipse.xtext.util.RuntimeIOException;

import com.regnosys.rosetta.generator.RosettaOutputConfigurationProvider;

public class TestResourceAwareFSAFactory implements ResourceAwareFSAFactory {

	@Override
	public IFileSystemAccess2 resourceAwareFSA(Resource resource, IFileSystemAccess2 fsa, boolean wholeProject) {
		return new TestFolderAwareFsa(resource, fsa, wholeProject);
	}

	public static class TestFolderAwareFsa implements IFileSystemAccess2 {
		private final IFileSystemAccess2 originalFsa;
		private final boolean testRes;

		public TestFolderAwareFsa(Resource resource, IFileSystemAccess2 originalFsa, boolean wholeProject) {
			this.originalFsa = originalFsa;
			this.testRes = !wholeProject && isTestResource(resource);
		}

		public static boolean isTestResource(Resource resource) {
			if (resource.getURI() != null) {
				// hardcode the folder for now
				return resource.getURI().toString().contains("src/test/resources/");
			}
			return false;
		}

		@Override
		public void generateFile(String fileName, CharSequence contents) {
			if (testRes) {
				originalFsa.generateFile(fileName, RosettaOutputConfigurationProvider.SRC_TEST_GEN_JAVA_OUTPUT,
						contents);
			} else {
				originalFsa.generateFile(fileName, contents);
			}
		}

		@Override
		public void deleteFile(String fileName) {
			originalFsa.deleteFile(fileName);
		}

		@Override
		public void deleteFile(String fileName, String outputConfigurationName) {
			originalFsa.deleteFile(fileName, outputConfigurationName);
		}

		@Override
		public void generateFile(String fileName, InputStream content) throws RuntimeIOException {
			originalFsa.generateFile(fileName, content);
		}

		@Override
		public void generateFile(String fileName, String outputConfigurationName, CharSequence contents) {
			originalFsa.generateFile(fileName, outputConfigurationName, contents);
		}

		@Override
		public void generateFile(String fileName, String outputConfigurationName, InputStream content)
				throws RuntimeIOException {
			originalFsa.generateFile(fileName, outputConfigurationName, content);
		}

		@Override
		public URI getURI(String fileName) {
			return originalFsa.getURI(fileName);
		}

		@Override
		public URI getURI(String fileName, String outputConfiguration) {
			return originalFsa.getURI(fileName, outputConfiguration);
		}

		@Override
		public boolean isFile(String path) throws RuntimeIOException {
			return originalFsa.isFile(path);
		}

		@Override
		public boolean isFile(String path, String outputConfigurationName) throws RuntimeIOException {
			return originalFsa.isFile(path, outputConfigurationName);
		}

		@Override
		public InputStream readBinaryFile(String fileName) throws RuntimeIOException {
			return originalFsa.readBinaryFile(fileName);
		}

		@Override
		public InputStream readBinaryFile(String fileName, String outputConfigurationName) throws RuntimeIOException {
			return originalFsa.readBinaryFile(fileName, outputConfigurationName);
		}

		@Override
		public CharSequence readTextFile(String fileName) throws RuntimeIOException {
			return originalFsa.readTextFile(fileName);
		}

		@Override
		public CharSequence readTextFile(String fileName, String outputConfigurationName) throws RuntimeIOException {
			return originalFsa.readTextFile(fileName, outputConfigurationName);
		}
	}

	@Override
	public void afterGenerate(Resource resource) {
	}

	@Override
	public void beforeGenerate(Resource resource) {
	}
}
