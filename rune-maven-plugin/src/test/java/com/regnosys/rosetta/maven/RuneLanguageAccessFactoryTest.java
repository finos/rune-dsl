/*
 * Copyright 2026 REGnosys
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.eclipse.xtext.builder.standalone.ILanguageConfiguration;
import org.eclipse.xtext.generator.OutputConfiguration;
import org.junit.jupiter.api.Test;

import com.regnosys.rosetta.RosettaStandaloneSetup;

class RuneLanguageAccessFactoryTest {

    @Test
    void nullConfigIsExplicitlyForwardedToTheLanguageSetup() {
        assertConfigIsForwarded(null);
    }

    @Test
    void explicitConfigIsForwardedToTheLanguageSetup() {
        assertConfigIsForwarded("/project/rune-config.yml");
    }

    private void assertConfigIsForwarded(String configFile) {
        RecordingSetup.configFileSet = false;
        RecordingSetup.configFile = "not-set";

        ILanguageConfiguration languageConfiguration = new ILanguageConfiguration() {
            @Override
            public String getSetup() {
                return RecordingSetup.class.getName();
            }

            @Override
            public Set<OutputConfiguration> getOutputConfigurations() {
                return Set.of();
            }

            @Override
            public boolean isJavaSupport() {
                return false;
            }
        };

        new RuneLanguageAccessFactory().createLanguageAccess(
                languageConfiguration, configFile, getClass().getClassLoader(), null);

        assertTrue(RecordingSetup.configFileSet);
        assertEquals(configFile, RecordingSetup.configFile);
    }

    public static final class RecordingSetup extends RosettaStandaloneSetup {
        private static boolean configFileSet;
        private static String configFile;

        @Override
        public RosettaStandaloneSetup setConfigFile(String configFile) {
            RecordingSetup.configFileSet = true;
            RecordingSetup.configFile = configFile;
            return super.setConfigFile(configFile);
        }
    }
}
