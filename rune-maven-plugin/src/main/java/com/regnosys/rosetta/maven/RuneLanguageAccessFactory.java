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

import com.google.inject.Injector;
import com.regnosys.rosetta.RosettaStandaloneSetup;
import org.eclipse.xtext.builder.standalone.ILanguageConfiguration;
import org.eclipse.xtext.builder.standalone.LanguageAccess;
import org.eclipse.xtext.resource.FileExtensionProvider;
import org.eclipse.xtext.resource.IResourceServiceProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * This will setup language access with the `SingletonGeneratorResourceServiceProvider`
 * instead of the default `IResourceServiceProvider`.
 */
public class RuneLanguageAccessFactory {

    public Map<String, LanguageAccess> createLanguageAccess(ILanguageConfiguration languageGenConf, String rosettaConfig, ClassLoader compilerClassLoder) {
        Map<String, LanguageAccess> result = new HashMap<String, LanguageAccess>();

        try {
            Class<?> loadClass = compilerClassLoder.loadClass(languageGenConf.getSetup());
            if (!RosettaStandaloneSetup.class.isAssignableFrom(loadClass)) {
                throw new IllegalArgumentException("Language setup class " + languageGenConf.getSetup()
                        + " must implement " + RosettaStandaloneSetup.class.getName());
            }
            RosettaStandaloneSetup setup = (RosettaStandaloneSetup) loadClass.getDeclaredConstructor().newInstance();
            if (rosettaConfig != null) {
                setup.setConfigFile(rosettaConfig);
            }
            Injector injector = setup.createInjectorAndDoEMFRegistration();

            IResourceServiceProvider singletonServiceProvider = injector.getInstance(SingletonGeneratorResourceServiceProvider.class);
            FileExtensionProvider fileExtensionProvider = injector.getInstance(FileExtensionProvider.class);
            LanguageAccess languageAccess = new LanguageAccess(languageGenConf.getOutputConfigurations(), singletonServiceProvider, languageGenConf.isJavaSupport());
            for (String extension : fileExtensionProvider.getFileExtensions()) {
                result.put(extension, languageAccess);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load language setup for class '" + rosettaConfig + "'.", e);
        }
        return result;
    }

}
