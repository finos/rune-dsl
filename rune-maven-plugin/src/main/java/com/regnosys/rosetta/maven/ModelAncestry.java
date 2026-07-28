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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Computes the model-ancestry facts recorded in the {@code META-INF/rune/model.properties} marker:
 * the model's repo identity ({@code modelId}) and its <em>direct</em> model parents
 * ({@code parentModels}), read off the {@code rosetta.parent.*} properties that model repos declare
 * in their top-level pom. {@code rune-testing} elects the leaf model (the one no other marker
 * claims as a parent) from these facts, independent of classpath order.
 * <p>
 * Direct parents suffice — no transitive closure is crawled — because every non-root model jar on a
 * test classpath carries a marker contributing its own parent edges: a model that declares a parent
 * must be built with a DSL/plugin version in step with its consumers (generated code must stay
 * compatible with the runtime), so intermediates cannot lag behind on pre-marker plugin versions.
 * Only <em>root</em> models may drift, and a markerless root is invisible to election in a way that
 * never produces a wrong or ambiguous winner.
 * <p>
 * All identities are {@code groupId:artifactId} pairs (no version): a model repo is identified by
 * its root/parent pom GA (e.g. {@code org.finos.cdm:cdm-parent}), which is both what a child
 * declares in {@code rosetta.parent.*} and what the generation module's own Maven {@code <parent>}
 * points at.
 * <p>
 * Everything here is a pure function over {@link Properties}/files, so the logic is unit-testable
 * without a Maven runtime.
 */
public final class ModelAncestry {

    public static final String PARENT_PROPERTY_PREFIX = "rosetta.parent.";

    private static final String GROUP_ID_SUFFIX = ".groupId";
    private static final String ARTIFACT_ID_SUFFIX = ".artifactId";
    private static final String VERSION_SUFFIX = ".version";

    private ModelAncestry() {
    }

    /** A direct model parent declared via {@code rosetta.parent.<name>.groupId/artifactId/version}. */
    public record ParentGav(String groupId, String artifactId, String version) {
        public String ga() {
            return groupId + ":" + artifactId;
        }

        public String gav() {
            return groupId + ":" + artifactId + ":" + version;
        }
    }

    /** A resolved dependency jar on the model's compile classpath, candidate for the cross-check. */
    public record ClasspathJar(String groupId, String artifactId, String version, File file) {
        public String gav() {
            return groupId + ":" + artifactId + ":" + version;
        }
    }

    /** The ancestry facts of a model jar's marker: its identity and its direct model parents. */
    public record JarMarker(String modelId, Set<String> parentModels) {
    }

    /**
     * The model's repo identity: its Maven {@code <parent>}'s GA, which is the coordinate children
     * declare for it in their {@code rosetta.parent.*} properties. A module without a parent pom
     * falls back to its own GA (children of such a model would declare that same GA).
     */
    public static String computeModelId(MavenProject project) {
        Parent parent = project.getModel().getParent();
        if (parent != null) {
            return parent.getGroupId() + ":" + parent.getArtifactId();
        }
        return project.getGroupId() + ":" + project.getArtifactId();
    }

    /**
     * Extracts the direct model parents from {@code rosetta.parent.*} properties. Pom inheritance
     * flattens the top-level pom's declarations into the module's effective properties, already
     * interpolated, so this reads them off the given {@link Properties} directly. Groups missing
     * any of the three coordinates are warned about and skipped. Results are ordered by group name
     * for determinism ({@link Properties} has no declaration order).
     */
    public static List<ParentGav> parseDirectParents(Properties properties, Consumer<String> warningSink) {
        Map<String, Map<String, String>> groups = new TreeMap<>();
        for (String key : properties.stringPropertyNames()) {
            if (!key.startsWith(PARENT_PROPERTY_PREFIX)) {
                continue;
            }
            String rest = key.substring(PARENT_PROPERTY_PREFIX.length());
            String component;
            if (rest.endsWith(GROUP_ID_SUFFIX)) {
                component = "groupId";
            } else if (rest.endsWith(ARTIFACT_ID_SUFFIX)) {
                component = "artifactId";
            } else if (rest.endsWith(VERSION_SUFFIX)) {
                component = "version";
            } else {
                continue;
            }
            String name = rest.substring(0, rest.lastIndexOf('.'));
            if (name.isEmpty()) {
                continue;
            }
            groups.computeIfAbsent(name, k -> new TreeMap<>()).put(component, properties.getProperty(key));
        }
        List<ParentGav> parents = new ArrayList<>();
        groups.forEach((name, coordinates) -> {
            String groupId = coordinates.get("groupId");
            String artifactId = coordinates.get("artifactId");
            String version = coordinates.get("version");
            if (isBlank(groupId) || isBlank(artifactId) || isBlank(version)) {
                warningSink.accept("Ignoring incomplete model parent declaration '" + PARENT_PROPERTY_PREFIX + name
                        + ".*': all three of groupId, artifactId and version must be set.");
                return;
            }
            parents.add(new ParentGav(groupId.trim(), artifactId.trim(), version.trim()));
        });
        return parents;
    }

    /**
     * The ancestry facts recorded in the jar's model marker, if the jar has one that declares an
     * identity. A marker without {@code modelId} (pre-ancestry) reads as absent, like no marker.
     */
    public static Optional<JarMarker> readJarMarker(File jarFile) {
        try (ZipFile zip = new ZipFile(jarFile)) {
            ZipEntry entry = zip.getEntry(ModelPropertiesWriter.RELATIVE_PATH);
            if (entry == null) {
                return Optional.empty();
            }
            Properties marker = new Properties();
            try (InputStream in = zip.getInputStream(entry)) {
                marker.load(in);
            }
            String modelId = marker.getProperty(ModelPropertiesWriter.MODEL_ID_KEY);
            if (modelId == null || modelId.isBlank()) {
                return Optional.empty();
            }
            String parentModels = marker.getProperty(ModelPropertiesWriter.PARENT_MODELS_KEY, "");
            Set<String> parents = Arrays.stream(parentModels.split(","))
                    .map(String::trim)
                    .filter(parent -> !parent.isEmpty())
                    .collect(Collectors.toSet());
            return Optional.of(new JarMarker(modelId.trim(), parents));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    /**
     * Cross-checks the declared parents against the classpath, which is ground truth: every model
     * jar among {@code classpathJars} whose marker declares an identity must be accounted for —
     * either declared as one of this model's {@code declaredParents}, or claimed as a parent by
     * some other model jar's marker (a transitive ancestor, e.g. fpml under DRR, is accounted for
     * by CDM's marker rather than DRR's declarations). Warns otherwise: a sign the
     * {@code rosetta.parent.*} declarations have rotted.
     * <p>
     * Markerless model jars are skipped silently: their repo identity is not knowable from the jar,
     * and by the version-sync invariant only root models legitimately lack markers.
     * Presence-driven only: a parentless model with no model jars on its classpath stays silent —
     * there is no "you declared no parents" warning. Never fails the build.
     */
    public static void crossCheckClasspathModels(Collection<ClasspathJar> classpathJars,
            Collection<String> declaredParents, Consumer<String> warningSink) {
        Map<ClasspathJar, JarMarker> markers = classpathJars.stream()
                .filter(jar -> jar.file() != null && jar.file().isFile())
                .flatMap(jar -> readJarMarker(jar.file()).map(marker -> Map.entry(jar, marker)).stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        Set<String> accountedFor = new HashSet<>(declaredParents);
        markers.values().forEach(marker -> accountedFor.addAll(marker.parentModels()));
        markers.forEach((jar, marker) -> {
            if (!accountedFor.contains(marker.modelId())) {
                warningSink.accept("Model jar " + jar.gav() + " (model repo " + marker.modelId() + ") is on the "
                        + "compile classpath but is not declared as a model parent via " + PARENT_PROPERTY_PREFIX
                        + "* properties in the top-level pom, nor claimed as a parent by any other model jar's "
                        + "marker. The parentModels written to the model marker will not account for it, which "
                        + "can make rune-testing's model election ambiguous. Please declare it.");
            }
        });
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
