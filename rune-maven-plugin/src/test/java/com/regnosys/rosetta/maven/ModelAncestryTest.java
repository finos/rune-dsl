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

import org.apache.maven.model.Parent;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelAncestryTest {

    private final List<String> warnings = new ArrayList<>();

    // ---- parseDirectParents ----

    @Test
    void parseDirectParents_groupsTriplesByName() {
        Properties properties = new Properties();
        properties.setProperty("rosetta.parent.common-domain-model.groupId", "org.finos.cdm");
        properties.setProperty("rosetta.parent.common-domain-model.artifactId", "cdm-parent");
        properties.setProperty("rosetta.parent.common-domain-model.version", "6.23.0");
        properties.setProperty("rosetta.parent.iso-20022.groupId", "org.iso20022");
        properties.setProperty("rosetta.parent.iso-20022.artifactId", "parent");
        properties.setProperty("rosetta.parent.iso-20022.version", "1.0.0");
        properties.setProperty("unrelated.property", "value");

        List<ModelAncestry.ParentGav> parents = ModelAncestry.parseDirectParents(properties, warnings::add);

        assertEquals(2, parents.size());
        assertEquals(new ModelAncestry.ParentGav("org.finos.cdm", "cdm-parent", "6.23.0"), parents.get(0));
        assertEquals(new ModelAncestry.ParentGav("org.iso20022", "parent", "1.0.0"), parents.get(1));
        assertTrue(warnings.isEmpty());
    }

    @Test
    void parseDirectParents_noPropertiesYieldsEmptyList() {
        List<ModelAncestry.ParentGav> parents = ModelAncestry.parseDirectParents(new Properties(), warnings::add);

        assertTrue(parents.isEmpty());
        assertTrue(warnings.isEmpty());
    }

    @Test
    void parseDirectParents_warnsAndSkipsIncompleteGroup() {
        Properties properties = new Properties();
        properties.setProperty("rosetta.parent.broken.groupId", "org.example");
        properties.setProperty("rosetta.parent.broken.version", "1.0.0");
        properties.setProperty("rosetta.parent.ok.groupId", "org.finos.cdm");
        properties.setProperty("rosetta.parent.ok.artifactId", "cdm-parent");
        properties.setProperty("rosetta.parent.ok.version", "6.23.0");

        List<ModelAncestry.ParentGav> parents = ModelAncestry.parseDirectParents(properties, warnings::add);

        assertEquals(List.of(new ModelAncestry.ParentGav("org.finos.cdm", "cdm-parent", "6.23.0")), parents);
        assertEquals(1, warnings.size());
        assertTrue(warnings.get(0).contains("rosetta.parent.broken"));
    }

    @Test
    void parseDirectParents_usesInterpolatedValuesAsGiven() {
        // The Mojo hands over the *effective* model's properties, where ${...} references are
        // already interpolated; the parser must take values verbatim.
        Properties properties = new Properties();
        properties.setProperty("rosetta.parent.rune-fpml.groupId", "com.regnosys.rune-fpml");
        properties.setProperty("rosetta.parent.rune-fpml.artifactId", "parent");
        properties.setProperty("rosetta.parent.rune-fpml.version", "1.2.3");

        List<ModelAncestry.ParentGav> parents = ModelAncestry.parseDirectParents(properties, warnings::add);

        assertEquals("com.regnosys.rune-fpml:parent:1.2.3", parents.get(0).gav());
    }

    // ---- computeModelId ----

    @Test
    void modelId_isTheMavenParentGa() {
        MavenProject project = new MavenProject();
        project.setGroupId("org.finos.cdm");
        project.setArtifactId("cdm-java");
        Parent parent = new Parent();
        parent.setGroupId("org.finos.cdm");
        parent.setArtifactId("cdm-parent");
        project.getModel().setParent(parent);

        assertEquals("org.finos.cdm:cdm-parent", ModelAncestry.computeModelId(project));
    }

    @Test
    void modelId_fallsBackToOwnGaWithoutParentPom() {
        MavenProject project = new MavenProject();
        project.setGroupId("org.example");
        project.setArtifactId("standalone-model");

        assertEquals("org.example:standalone-model", ModelAncestry.computeModelId(project));
    }

    // ---- readJarMarker ----

    @Test
    void readJarMarker_readsIdentityAndParents(@TempDir Path dir) throws IOException {
        File jar = jarWithEntries(dir, Map.of(ModelPropertiesWriter.RELATIVE_PATH,
                "modelId=org.finos.cdm\\:cdm-parent\nparentModels=com.regnosys.rune-fpml\\:parent\n"));

        Optional<ModelAncestry.JarMarker> marker = ModelAncestry.readJarMarker(jar);

        assertEquals(Optional.of(new ModelAncestry.JarMarker("org.finos.cdm:cdm-parent",
                Set.of("com.regnosys.rune-fpml:parent"))), marker);
    }

    @Test
    void readJarMarker_rootModelHasEmptyParents(@TempDir Path dir) throws IOException {
        File jar = jarWithEntries(dir, Map.of(ModelPropertiesWriter.RELATIVE_PATH,
                "modelId=com.regnosys.rune-fpml\\:parent\nparentModels=\n"));

        Optional<ModelAncestry.JarMarker> marker = ModelAncestry.readJarMarker(jar);

        assertEquals(Optional.of(new ModelAncestry.JarMarker("com.regnosys.rune-fpml:parent", Set.of())), marker);
    }

    @Test
    void readJarMarker_absentForPreAncestryMarkerAndMarkerlessJar(@TempDir Path dir) throws IOException {
        File preAncestry = jarWithEntries(dir, Map.of(ModelPropertiesWriter.RELATIVE_PATH,
                "runeConfigPresentInModel=true\n"));
        File markerless = jarWithEntries(dir, Map.of("model/types.rosetta", ""));

        assertEquals(Optional.empty(), ModelAncestry.readJarMarker(preAncestry));
        assertEquals(Optional.empty(), ModelAncestry.readJarMarker(markerless));
    }

    // ---- cross-check ----

    @Test
    void crossCheck_warnsOnUndeclaredModelJar(@TempDir Path dir) throws IOException {
        File jar = jarWithEntries(dir, Map.of(ModelPropertiesWriter.RELATIVE_PATH,
                "modelId=org.finos.cdm\\:cdm-parent\nparentModels=\n"));
        ModelAncestry.ClasspathJar cdm = new ModelAncestry.ClasspathJar("org.finos.cdm", "cdm-java", "6.23.0", jar);

        ModelAncestry.crossCheckClasspathModels(List.of(cdm), List.of(), warnings::add);

        assertEquals(1, warnings.size());
        assertTrue(warnings.get(0).contains("org.finos.cdm:cdm-java:6.23.0"));
        assertTrue(warnings.get(0).contains("org.finos.cdm:cdm-parent"));
    }

    @Test
    void crossCheck_silentWhenModelJarIsDeclared(@TempDir Path dir) throws IOException {
        File jar = jarWithEntries(dir, Map.of(ModelPropertiesWriter.RELATIVE_PATH,
                "modelId=org.finos.cdm\\:cdm-parent\nparentModels=\n"));
        ModelAncestry.ClasspathJar cdm = new ModelAncestry.ClasspathJar("org.finos.cdm", "cdm-java", "6.23.0", jar);

        ModelAncestry.crossCheckClasspathModels(List.of(cdm), List.of("org.finos.cdm:cdm-parent"), warnings::add);

        assertTrue(warnings.isEmpty());
    }

    @Test
    void crossCheck_transitiveAncestorIsAccountedForByItsChildsMarker(@TempDir Path dir) throws IOException {
        // The DRR case: fpml is on the classpath transitively and is not among DRR's direct
        // parents, but CDM's marker claims it - no warning.
        File fpmlJar = jarWithEntries(dir, Map.of(ModelPropertiesWriter.RELATIVE_PATH,
                "modelId=com.regnosys.rune-fpml\\:parent\nparentModels=\n"));
        File cdmJar = jarWithEntries(dir, Map.of(ModelPropertiesWriter.RELATIVE_PATH,
                "modelId=org.finos.cdm\\:cdm-parent\nparentModels=com.regnosys.rune-fpml\\:parent\n"));
        ModelAncestry.ClasspathJar fpml = new ModelAncestry.ClasspathJar("com.regnosys.rune-fpml", "rosetta-source",
                "1.2.3", fpmlJar);
        ModelAncestry.ClasspathJar cdm = new ModelAncestry.ClasspathJar("org.finos.cdm", "cdm-java", "6.23.0", cdmJar);

        ModelAncestry.crossCheckClasspathModels(List.of(fpml, cdm), List.of("org.finos.cdm:cdm-parent"),
                warnings::add);

        assertTrue(warnings.isEmpty());
    }

    @Test
    void crossCheck_silentForParentlessModelWithNoModelJars(@TempDir Path dir) throws IOException {
        // The legitimate-root case: nothing declared, nothing found - no "you declared no parents" nag.
        File plainJar = jarWithEntries(dir, Map.of("com/example/Foo.class", ""));
        ModelAncestry.ClasspathJar library = new ModelAncestry.ClasspathJar("org.example", "library", "1.0", plainJar);

        ModelAncestry.crossCheckClasspathModels(List.of(library), List.of(), warnings::add);

        assertTrue(warnings.isEmpty());
    }

    @Test
    void crossCheck_skipsMarkerlessModelJarsSilently(@TempDir Path dir) throws IOException {
        // A model jar without a marker (a root model built before markers existed): its identity is
        // not knowable from the jar, and by the version-sync invariant only roots may lack markers -
        // skipped without noise.
        File jar = jarWithEntries(dir, Map.of("model/types.rosetta", ""));
        ModelAncestry.ClasspathJar model = new ModelAncestry.ClasspathJar("org.example", "old-root", "1.0", jar);

        ModelAncestry.crossCheckClasspathModels(List.of(model), List.of(), warnings::add);

        assertTrue(warnings.isEmpty());
    }

    // ---- fixtures ----

    private static File jarWithEntries(Path dir, Map<String, String> entries) throws IOException {
        File jar = File.createTempFile("fixture", ".jar", dir.toFile());
        try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(jar))) {
            for (Map.Entry<String, String> entry : entries.entrySet()) {
                out.putNextEntry(new ZipEntry(entry.getKey()));
                out.write(entry.getValue().getBytes(StandardCharsets.UTF_8));
                out.closeEntry();
            }
        }
        return jar;
    }
}
