/*
 * Copyright 2024 REGnosys
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
public class RuneTestGenerateMojo extends AbstractRuneGeneratorMojo {

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
	
	@Override
	protected String getClassOutputDirectory() {
		return getProject().getBuild().getTestOutputDirectory();
	}
	
	@Override
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
