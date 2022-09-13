package com.regnosys.rosetta.ui.tests.validation

import com.google.inject.Inject
import com.regnosys.rosetta.rosetta.RosettaModel
import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IWorkspace
import org.eclipse.core.runtime.CoreException
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.jdt.core.JavaCore
import org.eclipse.xtext.ui.XtextProjectHelper
import org.eclipse.xtext.ui.resource.IResourceSetProvider
import org.eclipse.xtext.ui.testing.util.IResourcesSetupUtil
import org.eclipse.xtext.ui.util.ProjectFactory
import org.eclipse.xtext.util.StringInputStream
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.AfterAll

abstract class AbstractProjectAwareTest {

	@Inject IWorkspace workspace
	@Inject IResourceSetProvider resourceSetProvider
	@Inject ProjectFactory javaProjectFactory

	static val TESTPROJECT_NAME = "test-project"

	@BeforeEach
	def void setUpProject() throws Exception {
		createProject(TESTPROJECT_NAME)
		IResourcesSetupUtil.cleanBuild
	}

	@BeforeAll
	def static void freshUp() {
		IResourcesSetupUtil.cleanWorkspace
	}

	@AfterAll
	def static void cleanUp() {
		IResourcesSetupUtil.cleanWorkspace
	}

	def createRosettaTestFile(CharSequence content) {
		createRosettaFile(content, "test.rosetta")
	}

	def createRosettaFile(CharSequence content, String fileName) {
		val contantAsString = content.toString
		var IFile file = createFile(fileName, contantAsString)
		var Resource resource = getResourceSet().createResource(uri(file))
		resource.load(new StringInputStream(contantAsString), null)
		IResourcesSetupUtil.reallyWaitForAutoBuild

		return (resource.getContents().get(0) as RosettaModel)

	}

	def uri(IFile file) {
		URI.createPlatformResourceURI(file.getFullPath().toString(), true);
	}

	def IFile createFile(String fileName, String content) throws Exception {
		val fullName = getProject(true).getName() + "/" + fileName
		return IResourcesSetupUtil.createFile(fullName, content)
	}

	def protected IProject getProject(boolean createOnDemand) {
		var IProject project = workspace.getRoot().getProject(TESTPROJECT_NAME)
		if (createOnDemand && !project.exists()) {
			try {
				project = createProject(TESTPROJECT_NAME)
			} catch (CoreException e) {
				throw new RuntimeException(e)
			}

		}
		return project
	}

	def ResourceSet getResourceSet() {
		var ResourceSet resourceSet = resourceSetProvider.get(getProject(true))
		return resourceSet
	}

	def createProject(String name) {
		javaProjectFactory.projectName = name
		javaProjectFactory.addBuilderIds(JavaCore.BUILDER_ID, XtextProjectHelper.BUILDER_ID);
		javaProjectFactory.addProjectNatures(JavaCore.NATURE_ID, XtextProjectHelper.NATURE_ID);
		val proj = javaProjectFactory.createProject(null, null);
		return proj
	}
}
