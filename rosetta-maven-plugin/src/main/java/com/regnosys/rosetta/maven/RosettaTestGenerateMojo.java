package com.regnosys.rosetta.maven;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Sets.newLinkedHashSet;

import java.util.List;
import java.util.Set;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.eclipse.xtext.maven.Language;


// This class is identical to `XtextTestGenerateMojo`, except its superclass.
@Mojo(name = "testGenerate", defaultPhase = LifecyclePhase.GENERATE_TEST_SOURCES, requiresDependencyResolution = ResolutionScope.TEST, threadSafe = true)
public class RosettaTestGenerateMojo extends AbstractRosettaGeneratorMojo {

	/**
	 * Project classpath.
	 */
	@Parameter(defaultValue = "${project.testClasspathElements}", readonly = true, required = true)
	private List<String> classpathElements;

	@Override
	public Set<String> getClasspathElements() {
		Set<String> classpathElementSet = newLinkedHashSet();
		classpathElementSet.addAll(this.classpathElements);
		classpathElementSet.remove(getProject().getBuild().getTestOutputDirectory());
		return newLinkedHashSet(filter(classpathElementSet, emptyStringFilter()));
	}

	@Override
	protected void configureMavenOutputs() {
		for (Language language : getLanguages()) {
			addTestCompileSourceRoots(language);
		}
	}
	
	protected String tmpDirSuffix() {
		return "-test";
	}
	
	/**
	 * Project test source roots. List of folders, where the test source models are
	 * located.<br>
	 * The default value is a reference to the project's
	 * ${project.testCompileSourceRoots}.<br>
	 * When adding a new entry the default value will be overwritten not extended.
	 */
	@Parameter(defaultValue = "${project.testCompileSourceRoots}", required = true)
	private List<String> sourceRoots;

	@Override
	protected List<String> getSourceRoots() {
		return sourceRoots;
	}
}
