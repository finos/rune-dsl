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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

    // ---- computeAncestorClosure ----

    @Test
    void closure_followsTheFullChain() {
        // DRR -> {CDM, ISO}; CDM -> fpml; ISO and fpml are roots.
        Properties drr = withParents(Map.of(
                "common-domain-model", new ModelAncestry.ParentGav("org.finos.cdm", "cdm-parent", "6.23.0"),
                "iso-20022", new ModelAncestry.ParentGav("org.iso20022", "parent", "1.0.0")));
        FixtureLoader loader = new FixtureLoader();
        loader.register("org.finos.cdm:cdm-parent:6.23.0", withParents(Map.of(
                "rune-fpml", new ModelAncestry.ParentGav("com.regnosys.rune-fpml", "parent", "1.2.3"))));
        loader.register("org.iso20022:parent:1.0.0", new Properties());
        loader.register("com.regnosys.rune-fpml:parent:1.2.3", new Properties());

        List<String> closure = ModelAncestry.computeAncestorClosure(drr, loader, warnings::add);

        assertEquals(List.of("org.finos.cdm:cdm-parent", "org.iso20022:parent", "com.regnosys.rune-fpml:parent"),
                closure);
        assertTrue(warnings.isEmpty());
    }

    @Test
    void closure_rootModelWithNoParentsIsEmpty() {
        List<String> closure = ModelAncestry.computeAncestorClosure(new Properties(), new FixtureLoader(),
                warnings::add);

        assertTrue(closure.isEmpty());
        assertTrue(warnings.isEmpty());
    }

    @Test
    void closure_terminatesOnCycles() {
        Properties child = withParents(Map.of("a", new ModelAncestry.ParentGav("org.example", "a", "1")));
        FixtureLoader loader = new FixtureLoader();
        loader.register("org.example:a:1", withParents(Map.of("b", new ModelAncestry.ParentGav("org.example", "b", "1"))));
        loader.register("org.example:b:1", withParents(Map.of("a", new ModelAncestry.ParentGav("org.example", "a", "1"))));

        List<String> closure = ModelAncestry.computeAncestorClosure(child, loader, warnings::add);

        assertEquals(List.of("org.example:a", "org.example:b"), closure);
    }

    @Test
    void closure_warnsAndContinuesOnUnresolvableParentPom() {
        Properties child = withParents(Map.of(
                "resolvable", new ModelAncestry.ParentGav("org.example", "resolvable", "1"),
                "unresolvable", new ModelAncestry.ParentGav("org.example", "unresolvable", "1")));
        FixtureLoader loader = new FixtureLoader();
        loader.register("org.example:resolvable:1", new Properties());

        List<String> closure = ModelAncestry.computeAncestorClosure(child, loader, warnings::add);

        // Both direct parents stay in the closure; only the crawl *through* the broken pom is lost.
        assertEquals(List.of("org.example:resolvable", "org.example:unresolvable"), closure);
        assertEquals(1, warnings.size());
        assertTrue(warnings.get(0).contains("org.example:unresolvable:1"));
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

    // ---- model jar detection ----

    @Test
    void isModelJar_trueForJarWithMarker(@TempDir Path dir) throws IOException {
        File jar = jarWithEntries(dir, Map.of(ModelPropertiesWriter.RELATIVE_PATH, "runeConfigPresentInModel=true\n"));

        assertTrue(ModelAncestry.isModelJar(jar));
    }

    @Test
    void isModelJar_trueForJarWithRosettaSources(@TempDir Path dir) throws IOException {
        File jar = jarWithEntries(dir, Map.of("model/types.rosetta", "namespace test\n"));

        assertTrue(ModelAncestry.isModelJar(jar));
    }

    @Test
    void isModelJar_falseForPlainJarAndMissingFile(@TempDir Path dir) throws IOException {
        File jar = jarWithEntries(dir, Map.of("com/example/Foo.class", ""));

        assertFalse(ModelAncestry.isModelJar(jar));
        assertFalse(ModelAncestry.isModelJar(new File(dir.toFile(), "missing.jar")));
        assertFalse(ModelAncestry.isModelJar(null));
    }

    @Test
    void readMarkerModelId_readsFromMarkerAndHandlesAbsence(@TempDir Path dir) throws IOException {
        File withId = jarWithEntries(dir, Map.of(ModelPropertiesWriter.RELATIVE_PATH,
                "modelId=org.finos.cdm\\:cdm-parent\n"));
        File withoutId = jarWithEntries(dir, Map.of(ModelPropertiesWriter.RELATIVE_PATH,
                "runeConfigPresentInModel=true\n"));
        File withoutMarker = jarWithEntries(dir, Map.of("model/types.rosetta", ""));

        assertEquals(Optional.of("org.finos.cdm:cdm-parent"), ModelAncestry.readMarkerModelId(withId));
        assertEquals(Optional.empty(), ModelAncestry.readMarkerModelId(withoutId));
        assertEquals(Optional.empty(), ModelAncestry.readMarkerModelId(withoutMarker));
    }

    // ---- cross-check ----

    @Test
    void crossCheck_warnsOnUndeclaredModelJar(@TempDir Path dir) throws IOException {
        File jar = jarWithEntries(dir, Map.of(ModelPropertiesWriter.RELATIVE_PATH,
                "modelId=org.finos.cdm\\:cdm-parent\n"));
        ModelAncestry.ClasspathJar cdm = new ModelAncestry.ClasspathJar("org.finos.cdm", "cdm-java", "6.23.0", jar);

        ModelAncestry.crossCheckClasspathModels(List.of(cdm), List.of(), new FixtureLoader(), warnings::add);

        assertEquals(1, warnings.size());
        assertTrue(warnings.get(0).contains("org.finos.cdm:cdm-java:6.23.0"));
        assertTrue(warnings.get(0).contains("org.finos.cdm:cdm-parent"));
    }

    @Test
    void crossCheck_silentWhenModelJarIsDeclared(@TempDir Path dir) throws IOException {
        File jar = jarWithEntries(dir, Map.of(ModelPropertiesWriter.RELATIVE_PATH,
                "modelId=org.finos.cdm\\:cdm-parent\n"));
        ModelAncestry.ClasspathJar cdm = new ModelAncestry.ClasspathJar("org.finos.cdm", "cdm-java", "6.23.0", jar);

        ModelAncestry.crossCheckClasspathModels(List.of(cdm), List.of("org.finos.cdm:cdm-parent"),
                new FixtureLoader(), warnings::add);

        assertTrue(warnings.isEmpty());
    }

    @Test
    void crossCheck_silentForParentlessModelWithNoModelJars(@TempDir Path dir) throws IOException {
        // The legitimate-root case: nothing declared, nothing found - no "you declared no parents" nag.
        File plainJar = jarWithEntries(dir, Map.of("com/example/Foo.class", ""));
        ModelAncestry.ClasspathJar library = new ModelAncestry.ClasspathJar("org.example", "library", "1.0", plainJar);

        ModelAncestry.crossCheckClasspathModels(List.of(library), List.of(), new FixtureLoader(), warnings::add);

        assertTrue(warnings.isEmpty());
    }

    @Test
    void crossCheck_markerlessModelJarIdentityComesFromItsPomParent(@TempDir Path dir) throws IOException {
        // A model jar built before markers existed: identity falls back to its pom's Maven parent GA.
        File jar = jarWithEntries(dir, Map.of("model/types.rosetta", ""));
        ModelAncestry.ClasspathJar cdm = new ModelAncestry.ClasspathJar("org.finos.cdm", "cdm-java", "6.23.0", jar);
        FixtureLoader loader = new FixtureLoader();
        loader.register("org.finos.cdm:cdm-java:6.23.0", new Properties(), "org.finos.cdm:cdm-parent");

        ModelAncestry.crossCheckClasspathModels(List.of(cdm), List.of("org.finos.cdm:cdm-parent"), loader,
                warnings::add);
        assertTrue(warnings.isEmpty());

        ModelAncestry.crossCheckClasspathModels(List.of(cdm), List.of(), loader, warnings::add);
        assertEquals(1, warnings.size());
    }

    @Test
    void crossCheck_markerlessModelJarWithoutPomParentUsesItsOwnGa(@TempDir Path dir) throws IOException {
        File jar = jarWithEntries(dir, Map.of("model/types.rosetta", ""));
        ModelAncestry.ClasspathJar model = new ModelAncestry.ClasspathJar("org.example", "standalone", "1.0", jar);
        FixtureLoader loader = new FixtureLoader();
        loader.register("org.example:standalone:1.0", new Properties());

        ModelAncestry.crossCheckClasspathModels(List.of(model), List.of("org.example:standalone"), loader,
                warnings::add);

        assertTrue(warnings.isEmpty());
    }

    @Test
    void crossCheck_warnsButNeverFailsWhenIdentityCannotBeDetermined(@TempDir Path dir) throws IOException {
        File jar = jarWithEntries(dir, Map.of("model/types.rosetta", ""));
        ModelAncestry.ClasspathJar model = new ModelAncestry.ClasspathJar("org.example", "broken", "1.0", jar);

        // The loader knows nothing about the jar's pom, so identity resolution fails.
        ModelAncestry.crossCheckClasspathModels(List.of(model), List.of(), new FixtureLoader(), warnings::add);

        assertEquals(1, warnings.size());
        assertTrue(warnings.get(0).contains("org.example:broken:1.0"));
    }

    // ---- fixtures ----

    private static Properties withParents(Map<String, ModelAncestry.ParentGav> parentsByName) {
        Properties properties = new Properties();
        parentsByName.forEach((name, gav) -> {
            properties.setProperty(ModelAncestry.PARENT_PROPERTY_PREFIX + name + ".groupId", gav.groupId());
            properties.setProperty(ModelAncestry.PARENT_PROPERTY_PREFIX + name + ".artifactId", gav.artifactId());
            properties.setProperty(ModelAncestry.PARENT_PROPERTY_PREFIX + name + ".version", gav.version());
        });
        return properties;
    }

    /** In-memory {@link ModelAncestry.ParentPomLoader}; unknown GAVs throw like an unresolvable pom. */
    private static final class FixtureLoader implements ModelAncestry.ParentPomLoader {
        private final Map<String, ModelAncestry.EffectivePom> poms = new HashMap<>();

        void register(String gav, Properties properties) {
            register(gav, properties, null);
        }

        void register(String gav, Properties properties, String parentGa) {
            poms.put(gav, new ModelAncestry.EffectivePom(properties, parentGa));
        }

        @Override
        public ModelAncestry.EffectivePom load(String groupId, String artifactId, String version) throws Exception {
            ModelAncestry.EffectivePom pom = poms.get(groupId + ":" + artifactId + ":" + version);
            if (pom == null) {
                throw new Exception("could not resolve pom " + groupId + ":" + artifactId + ":" + version);
            }
            return pom;
        }
    }

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
