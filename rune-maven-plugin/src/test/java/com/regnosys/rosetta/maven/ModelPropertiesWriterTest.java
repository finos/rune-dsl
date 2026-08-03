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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelPropertiesWriterTest {

    private static final String SOURCE_GAV = "org.finos.cdm:cdm-java:6.23.0";
    private static final String MODEL_ID = "org.finos.cdm:cdm-parent";
    private static final List<String> PARENTS = List.of("com.regnosys.rune-fpml:parent");

    @Test
    void writesConfigPresentTrue(@TempDir File outputDirectory) throws IOException {
        new ModelPropertiesWriter().write(outputDirectory, true, "10.4.0", SOURCE_GAV, MODEL_ID, PARENTS);

        Properties properties = readMarker(outputDirectory);
        assertEquals("true", properties.getProperty(ModelPropertiesWriter.RUNE_CONFIG_PRESENT_IN_MODEL_KEY));
        assertEquals("10.4.0", properties.getProperty(ModelPropertiesWriter.RUNE_MAVEN_PLUGIN_VERSION_KEY));
    }

    @Test
    void writesConfigPresentFalse(@TempDir File outputDirectory) throws IOException {
        new ModelPropertiesWriter().write(outputDirectory, false, "10.4.0", SOURCE_GAV, MODEL_ID, PARENTS);

        Properties properties = readMarker(outputDirectory);
        assertEquals("false", properties.getProperty(ModelPropertiesWriter.RUNE_CONFIG_PRESENT_IN_MODEL_KEY));
    }

    @Test
    void writesIdentityAndAncestryKeysAlongsideV1Keys(@TempDir File outputDirectory) throws IOException {
        new ModelPropertiesWriter().write(outputDirectory, true, "10.4.0", SOURCE_GAV, MODEL_ID, PARENTS);

        Properties properties = readMarker(outputDirectory);
        assertEquals("true", properties.getProperty(ModelPropertiesWriter.RUNE_CONFIG_PRESENT_IN_MODEL_KEY));
        assertEquals("10.4.0", properties.getProperty(ModelPropertiesWriter.RUNE_MAVEN_PLUGIN_VERSION_KEY));
        assertEquals(SOURCE_GAV, properties.getProperty(ModelPropertiesWriter.MODEL_SOURCE_GAV_KEY));
        assertEquals(MODEL_ID, properties.getProperty(ModelPropertiesWriter.MODEL_ID_KEY));
        assertEquals("com.regnosys.rune-fpml:parent", properties.getProperty(ModelPropertiesWriter.PARENT_MODELS_KEY));
    }

    @Test
    void rootModelWritesEmptyParentModels(@TempDir File outputDirectory) throws IOException {
        new ModelPropertiesWriter().write(outputDirectory, true, "10.4.0",
                "com.regnosys.rune-fpml:rosetta-source:1.2.3", "com.regnosys.rune-fpml:parent", List.of());

        Properties properties = readMarker(outputDirectory);
        assertEquals("", properties.getProperty(ModelPropertiesWriter.PARENT_MODELS_KEY));
    }

    @Test
    void multipleParentsAreCommaSeparated(@TempDir File outputDirectory) throws IOException {
        new ModelPropertiesWriter().write(outputDirectory, true, "10.4.0", SOURCE_GAV, MODEL_ID,
                List.of("org.finos.cdm:cdm-parent", "org.iso20022:parent"));

        Properties properties = readMarker(outputDirectory);
        assertEquals("org.finos.cdm:cdm-parent,org.iso20022:parent",
                properties.getProperty(ModelPropertiesWriter.PARENT_MODELS_KEY));
    }

    @Test
    void nullIdentityAndAncestryOmitTheirKeys(@TempDir File outputDirectory) throws IOException {
        new ModelPropertiesWriter().write(outputDirectory, true, "10.4.0", null, null, null);

        Properties properties = readMarker(outputDirectory);
        assertEquals("true", properties.getProperty(ModelPropertiesWriter.RUNE_CONFIG_PRESENT_IN_MODEL_KEY));
        assertNull(properties.getProperty(ModelPropertiesWriter.MODEL_SOURCE_GAV_KEY));
        assertNull(properties.getProperty(ModelPropertiesWriter.MODEL_ID_KEY));
        assertNull(properties.getProperty(ModelPropertiesWriter.PARENT_MODELS_KEY));
    }

    @Test
    void landsUnderMetaInfRuneWithinTheGivenOutputDirectory(@TempDir File outputDirectory) throws IOException {
        new ModelPropertiesWriter().write(outputDirectory, true, "10.4.0", SOURCE_GAV, MODEL_ID, PARENTS);

        File marker = new File(outputDirectory, ModelPropertiesWriter.RELATIVE_PATH);
        assertTrue(marker.isFile());
        assertEquals(new File(outputDirectory, "META-INF/rune/model.properties").getAbsolutePath(), marker.getAbsolutePath());
    }

    @Test
    void repeatedWritesAreIdempotent(@TempDir File outputDirectory) throws IOException {
        new ModelPropertiesWriter().write(outputDirectory, false, "10.4.0", SOURCE_GAV, MODEL_ID, PARENTS);
        new ModelPropertiesWriter().write(outputDirectory, true, "10.5.0", SOURCE_GAV, MODEL_ID, PARENTS);

        Properties properties = readMarker(outputDirectory);
        assertEquals("true", properties.getProperty(ModelPropertiesWriter.RUNE_CONFIG_PRESENT_IN_MODEL_KEY));
        assertEquals("10.5.0", properties.getProperty(ModelPropertiesWriter.RUNE_MAVEN_PLUGIN_VERSION_KEY));
        assertEquals(MODEL_ID, properties.getProperty(ModelPropertiesWriter.MODEL_ID_KEY));

        File runeDir = new File(outputDirectory, "META-INF/rune");
        assertEquals(1, runeDir.listFiles().length);
    }

    @Test
    void doesNotWriteOutsideOfTheGivenOutputDirectory(@TempDir File outputDirectory) throws IOException {
        File testClasses = new File(outputDirectory.getParentFile(), "test-classes");
        testClasses.mkdirs();

        new ModelPropertiesWriter().write(outputDirectory, true, "10.4.0", SOURCE_GAV, MODEL_ID, PARENTS);

        assertFalse(new File(testClasses, ModelPropertiesWriter.RELATIVE_PATH).exists());
    }

    private static Properties readMarker(File outputDirectory) throws IOException {
        Properties properties = new Properties();
        try (InputStream in = new FileInputStream(new File(outputDirectory, ModelPropertiesWriter.RELATIVE_PATH))) {
            properties.load(in);
        }
        return properties;
    }
}
