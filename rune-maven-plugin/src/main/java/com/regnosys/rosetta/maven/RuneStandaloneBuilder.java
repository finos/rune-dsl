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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.builder.standalone.LanguageAccess;
import org.eclipse.xtext.builder.standalone.StandaloneBuilder;
import org.eclipse.xtext.generator.GeneratorContext;
import org.eclipse.xtext.generator.GeneratorDelegate;
import org.eclipse.xtext.generator.JavaIoFileSystemAccess;
import org.eclipse.xtext.util.CancelIndicator;

import com.regnosys.rosetta.generator.RosettaGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RuneStandaloneBuilder extends StandaloneBuilder {
	private static final Logger LOG = LoggerFactory.getLogger(RuneStandaloneBuilder.class);
	
	private LanguageAccess rosettaLanguageAccess = null;
	// TODO: patch Xtext to make `languages` available
	@SuppressWarnings("unchecked")
	private LanguageAccess getRosettaLanguageAccess() {
		if (rosettaLanguageAccess == null) {
			Map<String, LanguageAccess> languages;
	        try {
	        	Field f = StandaloneBuilder.class.getDeclaredField("languages");
		        f.setAccessible(true);
				languages = (Map<String, LanguageAccess>) f.get(this);
			} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
				throw new RuntimeException(e);
			}
	        rosettaLanguageAccess = languages.get("rosetta");
		}
		return rosettaLanguageAccess;
	}
	
	private RosettaGenerator rosettaGenerator = null;
	private RosettaGenerator getRosettaGenerator() {
		if (rosettaGenerator == null) {
			LanguageAccess access = getRosettaLanguageAccess();
	        GeneratorDelegate delegate = access.getGenerator();
	        try {
	        	Field f = GeneratorDelegate.class.getDeclaredField("generator");
		        f.setAccessible(true);
		        rosettaGenerator = (RosettaGenerator) f.get(delegate);
			} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
				throw new RuntimeException(e);
			}
		}
		return rosettaGenerator;
	}

	private boolean needsBeforeAllCall = false;
	private ResourceSet currentResourceSet = null;
	private JavaIoFileSystemAccess currentFileSystemAccess;
	@Override
	public boolean launch() {
		needsBeforeAllCall = true;
		currentResourceSet = null;
		
		boolean success = super.launch();
		
		if (success && currentResourceSet != null) {
			LOG.info("Starting after all generation");
			GeneratorContext context = new GeneratorContext();
			context.setCancelIndicator(CancelIndicator.NullImpl);
			getRosettaGenerator().afterAllGenerate(currentResourceSet, currentFileSystemAccess, context);
		}
		
		return success;
	}

	@Override
	protected void generate(List<Resource> sourceResources) {
		if (needsBeforeAllCall) {
			LOG.info("Starting before all generation");
			currentResourceSet = sourceResources.get(0).getResourceSet();
			LanguageAccess access = getRosettaLanguageAccess();
			currentFileSystemAccess = getFileSystemAccess(access);
			GeneratorContext context = new GeneratorContext();
			context.setCancelIndicator(CancelIndicator.NullImpl);
			getRosettaGenerator().beforeAllGenerate(currentResourceSet, currentFileSystemAccess, context);
			needsBeforeAllCall = false;
		}
		
		super.generate(sourceResources);
	}
	
	private JavaIoFileSystemAccess getFileSystemAccess(LanguageAccess language) {
		try {
			Method m = StandaloneBuilder.class.getDeclaredMethod("getFileSystemAccess", LanguageAccess.class);
			m.setAccessible(true);
			return (JavaIoFileSystemAccess) m.invoke(this, language);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}
}
