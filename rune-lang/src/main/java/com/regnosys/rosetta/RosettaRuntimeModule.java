package com.regnosys.rosetta;

import com.regnosys.rosetta.scoping.RosettaLinkingService;
import com.regnosys.rosetta.validation.names.RosettaNamesAreUniqueValidationHelper;
import com.regnosys.rosetta.validation.names.RosettaUniqueNamesContextProvider;
import jakarta.inject.Singleton;
import org.eclipse.xtext.conversion.IValueConverterService;
import org.eclipse.xtext.formatting2.FormatterRequest;
import org.eclipse.xtext.generator.GeneratorDelegate;
import org.eclipse.xtext.generator.IOutputConfigurationProvider;
import org.eclipse.xtext.linking.ILinkingService;
import org.eclipse.xtext.naming.IQualifiedNameProvider;
import org.eclipse.xtext.parser.IEncodingProvider;
import org.eclipse.xtext.parsetree.reconstr.ITransientValueService;
import org.eclipse.xtext.resource.DerivedStateAwareResourceDescriptionManager;
import org.eclipse.xtext.resource.IDerivedStateComputer;
import org.eclipse.xtext.resource.IResourceDescription;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.impl.DefaultResourceDescriptionStrategy;
import org.eclipse.xtext.service.DispatchingProvider;
import org.eclipse.xtext.validation.INamesAreUniqueValidationHelper;
import org.eclipse.xtext.validation.IResourceValidator;

import com.google.inject.Binder;
import com.regnosys.rosetta.cache.RequestScopedCacheModule;
import com.regnosys.rosetta.config.RosettaConfiguration;
import com.regnosys.rosetta.config.file.FileBasedRosettaConfigurationProvider;
import com.regnosys.rosetta.derivedstate.RosettaDerivedStateComputer;
import com.regnosys.rosetta.formatting2.FormatterRequestWithDefaultPreferencesProvider;
import com.regnosys.rosetta.formatting2.ResourceFormatterService;
import com.regnosys.rosetta.formatting2.XtextResourceFormatter;
import com.regnosys.rosetta.generator.RosettaOutputConfigurationProvider;
import com.regnosys.rosetta.generator.external.EmptyExternalGeneratorsProvider;
import com.regnosys.rosetta.generator.external.ExternalGenerators;
import com.regnosys.rosetta.generator.resourcefsa.ResourceAwareFSAFactory;
import com.regnosys.rosetta.generator.resourcefsa.TestResourceAwareFSAFactory;
import com.regnosys.rosetta.parsing.RosettaValueConverterService;
import com.regnosys.rosetta.resource.RosettaResource;
import com.regnosys.rosetta.resource.RosettaResourceDescriptionStrategy;
import com.regnosys.rosetta.scoping.RosettaQualifiedNameProvider;
import com.regnosys.rosetta.serialization.RosettaTransientValueService;
import com.regnosys.rosetta.transgest.ModelLoader;
import com.regnosys.rosetta.transgest.ModelLoaderImpl;
import com.regnosys.rosetta.validation.CachingResourceValidator;

import jakarta.inject.Provider;

public class RosettaRuntimeModule extends AbstractRosettaRuntimeModule {
	
	public void configureRequestScopedCache(Binder binder) {
		binder.install(new RequestScopedCacheModule());
	}
	
	public Class<? extends ResourceAwareFSAFactory> bindResourceAwareFSAFactory() {
		return TestResourceAwareFSAFactory.class;
	}
	
	public Class<? extends DefaultResourceDescriptionStrategy> bindDefaultResourceDescriptionStrategy() {
		return RosettaResourceDescriptionStrategy.class;
	}
	
	@Override
	public Class<? extends IQualifiedNameProvider> bindIQualifiedNameProvider() {
		return RosettaQualifiedNameProvider.class;
	}
	
	public Class<? extends IOutputConfigurationProvider> bindIOutputConfigurationProvider() {
		return RosettaOutputConfigurationProvider.class;
	}
	
	public Class<? extends IResourceDescription.Manager> bindIResourceDescriptionManager() {
		return DerivedStateAwareResourceDescriptionManager.class;
	}
	
	public Class<? extends Provider<ExternalGenerators>> provideExternalGenerators() {
		return EmptyExternalGeneratorsProvider.class;
	}
	
	public Class<? extends Provider<FormatterRequest>> provideFormatterRequest() {
		return FormatterRequestWithDefaultPreferencesProvider.class;
	}
	
	public void configureGeneratorDelegate(Binder binder) {
		binder.bind(GeneratorDelegate.class).in(Singleton.class);
	}
	
	@Override
	public Class<? extends ITransientValueService> bindITransientValueService() {
		return RosettaTransientValueService.class;
	}
	
    @Override
	public void configureRuntimeEncodingProvider(Binder binder) {
        binder.bind(IEncodingProvider.class)
        	.annotatedWith(DispatchingProvider.Runtime.class)
        	.to(UTF8EncodingProvider.class);
    }
    
    @Override
	public Class<? extends IValueConverterService> bindIValueConverterService() {
    	return RosettaValueConverterService.class;
    }
	
	@Override
	public Class<? extends XtextResource> bindXtextResource() {
		return RosettaResource.class;
	}
	public Class<? extends IDerivedStateComputer> bindIDerivedStateComputer() {
		return RosettaDerivedStateComputer.class;
	}
	
	public Class<? extends ModelLoader> bindModelLoader() {
		return ModelLoaderImpl.class;
	}
	
	public Class<? extends IResourceValidator> bindIResourceValidator() {
		return CachingResourceValidator.class;
	}
	
	public Class<? extends Provider<? extends RosettaConfiguration>> provideRosettaConfiguration() {
		return FileBasedRosettaConfigurationProvider.class;
	}
	
	public Class<? extends ResourceFormatterService> bindResourceFormatterService() {
		return XtextResourceFormatter.class;
	}
	
	@Override
	public Class<? extends ILinkingService> bindILinkingService() {
		return RosettaLinkingService.class;
	}
    
    public Class<? extends INamesAreUniqueValidationHelper> bindINamesAreUniqueValidationHelper() {
    	return RosettaNamesAreUniqueValidationHelper.class;
    }
    public Class<? extends RosettaNamesAreUniqueValidationHelper.ContextProvider> bindINamesAreUniqueValidationHelperContextProvider() {
        return RosettaUniqueNamesContextProvider.class;
    }
}
