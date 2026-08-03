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
import java.util.List;
import java.util.Properties;

/**
 * Writes the per-model marker file that {@code rune-testing} consults to determine whether this
 * model ships its own Rune configuration, instead of inferring it from classpath lookup order
 * (which can silently pick up an ancestor's configuration).
 * <p>
 * Besides the config-presence flag, the marker records the model's identity and its <em>direct</em>
 * model parents ({@link #MODEL_ID_KEY}, {@link #PARENT_MODELS_KEY}), from which {@code rune-testing}
 * elects the leaf model — the one no other marker on the test classpath claims as a parent —
 * independent of classpath order. Direct parents suffice because every non-root model jar carries a
 * marker contributing its own edges (children must track the chain's DSL version; only root models
 * may drift behind on pre-marker plugin versions, and a markerless root cannot win an election).
 * {@link #MODEL_SOURCE_GAV_KEY} is diagnostics only, so log and error messages can name a real
 * artifact instead of a URL.
 */
public class ModelPropertiesWriter {

    public static final String RELATIVE_PATH = "META-INF/rune/model.properties";

    public static final String RUNE_CONFIG_PRESENT_IN_MODEL_KEY = "runeConfigPresentInModel";
    public static final String RUNE_MAVEN_PLUGIN_VERSION_KEY = "runeMavenPluginVersion";
    public static final String MODEL_SOURCE_GAV_KEY = "modelSourceGav";
    public static final String MODEL_ID_KEY = "modelId";
    public static final String PARENT_MODELS_KEY = "parentModels";

    /**
     * Writes the marker. {@code modelSourceGav}/{@code modelId} may be {@code null} (their keys are
     * then omitted, producing a pre-ancestry marker that consumers handle via their compatibility
     * fallback), as may {@code parentModels}; a root model passes an empty list.
     */
    public void write(File outputDirectory, boolean runeConfigPresentInModel, String runeMavenPluginVersion,
            String modelSourceGav, String modelId, List<String> parentModels) throws IOException {
        File file = new File(outputDirectory, RELATIVE_PATH);
        File parent = file.getParentFile();
        if (!parent.isDirectory() && !parent.mkdirs()) {
            throw new IOException("Could not create directory " + parent);
        }
        Properties properties = new Properties();
        properties.setProperty(RUNE_CONFIG_PRESENT_IN_MODEL_KEY, String.valueOf(runeConfigPresentInModel));
        properties.setProperty(RUNE_MAVEN_PLUGIN_VERSION_KEY, runeMavenPluginVersion);
        if (modelSourceGav != null) {
            properties.setProperty(MODEL_SOURCE_GAV_KEY, modelSourceGav);
        }
        if (modelId != null) {
            properties.setProperty(MODEL_ID_KEY, modelId);
        }
        if (parentModels != null) {
            properties.setProperty(PARENT_MODELS_KEY, String.join(",", parentModels));
        }
        try (OutputStream out = new FileOutputStream(file)) {
            properties.store(out, null);
        }
    }
}
