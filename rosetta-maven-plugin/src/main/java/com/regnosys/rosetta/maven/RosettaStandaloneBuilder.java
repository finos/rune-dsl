package com.regnosys.rosetta.maven;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.builder.standalone.LanguageAccess;
import org.eclipse.xtext.builder.standalone.StandaloneBuilder;
import org.eclipse.xtext.generator.GeneratorContext;
import org.eclipse.xtext.generator.GeneratorDelegate;
import org.eclipse.xtext.generator.JavaIoFileSystemAccess;
import org.eclipse.xtext.util.CancelIndicator;

import com.regnosys.rosetta.generator.RosettaGenerator;

public class RosettaStandaloneBuilder extends StandaloneBuilder {
	private static final Logger LOG = Logger.getLogger(RosettaStandaloneBuilder.class);
	
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
	private ResourceSet currentResourceSet;
	private JavaIoFileSystemAccess currentFileSystemAccess;
	@Override
	public boolean launch() {
		needsBeforeAllCall = true;
		boolean success = super.launch();
		
		LOG.info("Starting after all generation");
		GeneratorContext context = new GeneratorContext();
		context.setCancelIndicator(CancelIndicator.NullImpl);
		getRosettaGenerator().afterAllGenerate(currentResourceSet, currentFileSystemAccess, context);
		
		return success;
	}

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
