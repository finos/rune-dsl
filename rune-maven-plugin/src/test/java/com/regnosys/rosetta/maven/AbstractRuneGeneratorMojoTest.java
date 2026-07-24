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
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AbstractRuneGeneratorMojoTest {

    @Test
    void findConventionalConfigFile_returnsRuneConfigWhenPresent(@TempDir File basedir) throws IOException {
        File resources = createResourcesDir(basedir);
        File runeConfig = new File(resources, "rune-config.yml");
        Files.createFile(runeConfig.toPath());

        File found = AbstractRuneGeneratorMojo.findConventionalConfigFile(basedir);

        assertEquals(runeConfig.getAbsolutePath(), found.getAbsolutePath());
    }

    @Test
    void findConventionalConfigFile_fallsBackToLegacyName(@TempDir File basedir) throws IOException {
        File resources = createResourcesDir(basedir);
        File legacyConfig = new File(resources, "rosetta-config.yml");
        Files.createFile(legacyConfig.toPath());

        File found = AbstractRuneGeneratorMojo.findConventionalConfigFile(basedir);

        assertEquals(legacyConfig.getAbsolutePath(), found.getAbsolutePath());
    }

    @Test
    void findConventionalConfigFile_prefersRuneConfigOverLegacy(@TempDir File basedir) throws IOException {
        File resources = createResourcesDir(basedir);
        File runeConfig = new File(resources, "rune-config.yml");
        Files.createFile(runeConfig.toPath());
        Files.createFile(new File(resources, "rosetta-config.yml").toPath());

        File found = AbstractRuneGeneratorMojo.findConventionalConfigFile(basedir);

        assertEquals(runeConfig.getAbsolutePath(), found.getAbsolutePath());
    }

    @Test
    void findConventionalConfigFile_returnsNullWhenAbsent(@TempDir File basedir) {
        assertNull(AbstractRuneGeneratorMojo.findConventionalConfigFile(basedir));
    }

    @Test
    void resolveConfig_returnsConventionalRuneConfigWhenNoParameterSet(@TempDir File basedir) throws IOException {
        File resources = createResourcesDir(basedir);
        File runeConfig = new File(resources, "rune-config.yml");
        Files.createFile(runeConfig.toPath());
        List<String> warnings = new ArrayList<>();

        String resolved = AbstractRuneGeneratorMojo.resolveConfig(null, null, basedir, warnings::add);

        assertEquals(runeConfig.getAbsolutePath(), resolved);
        assertTrue(warnings.isEmpty());
    }

    @Test
    void resolveConfig_fallsBackToLegacyConventionalName(@TempDir File basedir) throws IOException {
        File resources = createResourcesDir(basedir);
        File legacyConfig = new File(resources, "rosetta-config.yml");
        Files.createFile(legacyConfig.toPath());
        List<String> warnings = new ArrayList<>();

        String resolved = AbstractRuneGeneratorMojo.resolveConfig(null, null, basedir, warnings::add);

        assertEquals(legacyConfig.getAbsolutePath(), resolved);
        assertTrue(warnings.isEmpty());
    }

    @Test
    void resolveConfig_returnsNullWhenNeitherParameterNorConventionalFileExist(@TempDir File basedir) {
        List<String> warnings = new ArrayList<>();

        String resolved = AbstractRuneGeneratorMojo.resolveConfig(null, null, basedir, warnings::add);

        assertNull(resolved);
        assertTrue(warnings.isEmpty());
    }

    @Test
    void resolveConfig_honoursRuneConfigParameterAndWarns(@TempDir File basedir) {
        List<String> warnings = new ArrayList<>();

        String resolved = AbstractRuneGeneratorMojo.resolveConfig("/explicit/rune-config.yml", null, basedir, warnings::add);

        assertEquals("/explicit/rune-config.yml", resolved);
        assertEquals(1, warnings.size());
    }

    @Test
    void resolveConfig_honoursRosettaConfigParameterAndWarns(@TempDir File basedir) {
        List<String> warnings = new ArrayList<>();

        String resolved = AbstractRuneGeneratorMojo.resolveConfig(null, "/explicit/rosetta-config.yml", basedir, warnings::add);

        assertEquals("/explicit/rosetta-config.yml", resolved);
        assertEquals(1, warnings.size());
    }

    @Test
    void resolveConfig_runeConfigParameterWinsWhenBothSet(@TempDir File basedir) {
        List<String> warnings = new ArrayList<>();

        String resolved = AbstractRuneGeneratorMojo.resolveConfig("/explicit/rune-config.yml", "/explicit/rosetta-config.yml", basedir, warnings::add);

        assertEquals("/explicit/rune-config.yml", resolved);
        assertEquals(1, warnings.size());
    }

    @Test
    void warnIfLegacyConfigFileName_warnsForLegacyName(@TempDir File basedir) throws IOException {
        File resources = createResourcesDir(basedir);
        File legacyConfig = new File(resources, "rosetta-config.yml");
        Files.createFile(legacyConfig.toPath());
        List<String> warnings = new ArrayList<>();

        AbstractRuneGeneratorMojo.warnIfLegacyConfigFileName(legacyConfig, warnings::add);

        assertEquals(1, warnings.size());
    }

    @Test
    void warnIfLegacyConfigFileName_doesNotWarnForCurrentName(@TempDir File basedir) throws IOException {
        File resources = createResourcesDir(basedir);
        File runeConfig = new File(resources, "rune-config.yml");
        Files.createFile(runeConfig.toPath());
        List<String> warnings = new ArrayList<>();

        AbstractRuneGeneratorMojo.warnIfLegacyConfigFileName(runeConfig, warnings::add);

        assertTrue(warnings.isEmpty());
    }

    @Test
    void warnIfLegacyConfigFileName_doesNotWarnWhenAbsent() {
        List<String> warnings = new ArrayList<>();

        AbstractRuneGeneratorMojo.warnIfLegacyConfigFileName(null, warnings::add);

        assertTrue(warnings.isEmpty());
    }

    private static File createResourcesDir(File basedir) throws IOException {
        File resources = new File(basedir, "src/main/resources");
        Files.createDirectories(resources.toPath());
        return resources;
    }
}
