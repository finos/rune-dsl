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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

/**
 * Writes the per-model marker file that {@code rune-testing} consults to determine whether this
 * model ships its own Rune configuration, instead of inferring it from classpath lookup order
 * (which can silently pick up an ancestor's configuration).
 */
public class ModelPropertiesWriter {

    public static final String RELATIVE_PATH = "META-INF/rune/model.properties";

    public static final String RUNE_CONFIG_PRESENT_IN_MODEL_KEY = "runeConfigPresentInModel";
    public static final String RUNE_MAVEN_PLUGIN_VERSION_KEY = "runeMavenPluginVersion";

    public void write(File outputDirectory, boolean runeConfigPresentInModel, String runeMavenPluginVersion) throws IOException {
        File file = new File(outputDirectory, RELATIVE_PATH);
        File parent = file.getParentFile();
        if (!parent.isDirectory() && !parent.mkdirs()) {
            throw new IOException("Could not create directory " + parent);
        }
        Properties properties = new Properties();
        properties.setProperty(RUNE_CONFIG_PRESENT_IN_MODEL_KEY, String.valueOf(runeConfigPresentInModel));
        properties.setProperty(RUNE_MAVEN_PLUGIN_VERSION_KEY, runeMavenPluginVersion);
        try (OutputStream out = new FileOutputStream(file)) {
            properties.store(out, null);
        }
    }
}
