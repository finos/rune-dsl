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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Computes the model-ancestry facts recorded in the {@code META-INF/rune/model.properties} marker:
 * the model's repo identity ({@code modelId}) and the recursive closure of its ancestor model repo
 * identities ({@code ancestorModels}), derived from the {@code rosetta.parent.*} properties that
 * model repos declare in their top-level pom. {@code rune-testing} elects the leaf model (the one
 * no other marker claims as an ancestor) from these facts, independent of classpath order.
 * <p>
 * All identities are {@code groupId:artifactId} pairs (no version): a model repo is identified by
 * its root/parent pom GA (e.g. {@code org.finos.cdm:cdm-parent}), which is both what a child
 * declares in {@code rosetta.parent.*} and what the generation module's own Maven {@code <parent>}
 * points at. Versions from the properties are used only to resolve parent poms during the crawl.
 * <p>
 * Everything here is a pure function over {@link Properties}/files plus the {@link ParentPomLoader}
 * seam, so the logic is unit-testable without a Maven runtime; the Mojo supplies a
 * {@code ProjectBuilder}-backed loader.
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

        public String ga() {
            return groupId + ":" + artifactId;
        }
    }

    /** What the crawl needs from an effective pom: its properties and its Maven parent's GA. */
    public record EffectivePom(Properties properties, String parentGa) {
    }

    /**
     * Builds the <em>effective</em> pom of the given (parent-pom) GAV. Effective-model building is
     * required because declared {@code rosetta.parent.*} versions may themselves be property
     * references (e.g. {@code ${rune-fpml.version}}) that only interpolate in the built model.
     * Implementations may throw on an unresolvable pom; the crawl warns and skips rather than
     * failing the build.
     */
    @FunctionalInterface
    public interface ParentPomLoader {
        EffectivePom load(String groupId, String artifactId, String version) throws Exception;
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
     * The full recursive closure of ancestor model repo identities (GA only), crawled from the
     * module's effective properties via {@code loader}: each direct parent's effective pom is built
     * and its own {@code rosetta.parent.*} declarations followed. A visited set on GA guards
     * against cycles; an unresolvable parent pom is warned about and skipped, degrading the closure
     * rather than failing the build.
     */
    public static List<String> computeAncestorClosure(Properties properties, ParentPomLoader loader,
            Consumer<String> warningSink) {
        LinkedHashSet<String> closure = new LinkedHashSet<>();
        Deque<ParentGav> queue = new ArrayDeque<>(parseDirectParents(properties, warningSink));
        while (!queue.isEmpty()) {
            ParentGav parent = queue.removeFirst();
            if (!closure.add(parent.ga())) {
                continue;
            }
            EffectivePom parentPom;
            try {
                parentPom = loader.load(parent.groupId(), parent.artifactId(), parent.version());
            } catch (Exception e) {
                warningSink.accept("Could not build the effective pom of ancestor model " + parent.gav() + " ("
                        + e.getMessage() + "); the ancestorModels closure written to the model marker may be "
                        + "incomplete.");
                continue;
            }
            queue.addAll(parseDirectParents(parentPom.properties(), warningSink));
        }
        return new ArrayList<>(closure);
    }

    /**
     * Whether the file looks like a Rune model jar: it carries a model marker or any
     * {@code *.rosetta} source entry. Unreadable or missing files are simply not model jars.
     */
    public static boolean isModelJar(File file) {
        if (file == null || !file.isFile()) {
            return false;
        }
        try (ZipFile zip = new ZipFile(file)) {
            if (zip.getEntry(ModelPropertiesWriter.RELATIVE_PATH) != null) {
                return true;
            }
            return zip.stream().anyMatch(entry -> !entry.isDirectory() && entry.getName().endsWith(".rosetta"));
        } catch (IOException e) {
            return false;
        }
    }

    /** The {@code modelId} recorded in the jar's model marker, if the jar has one that declares it. */
    public static Optional<String> readMarkerModelId(File jarFile) {
        try (ZipFile zip = new ZipFile(jarFile)) {
            ZipEntry entry = zip.getEntry(ModelPropertiesWriter.RELATIVE_PATH);
            if (entry == null) {
                return Optional.empty();
            }
            Properties marker = new Properties();
            try (InputStream in = zip.getInputStream(entry)) {
                marker.load(in);
            }
            return Optional.ofNullable(marker.getProperty(ModelPropertiesWriter.MODEL_ID_KEY)).filter(id -> !id.isBlank());
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    /**
     * Cross-checks the declared ancestor closure against the classpath, which is ground truth: for
     * every model jar among {@code classpathJars}, resolves its repo identity (its marker's
     * {@code modelId} when present, otherwise its pom's Maven parent GA via {@code loader}, falling
     * back to its own GA) and warns when that identity is missing from {@code declaredAncestors} —
     * a sign the {@code rosetta.parent.*} declarations have rotted. Presence-driven only: a
     * parentless model with no model jars on its classpath stays silent. Never fails the build.
     */
    public static void crossCheckClasspathModels(Collection<ClasspathJar> classpathJars,
            Collection<String> declaredAncestors, ParentPomLoader loader, Consumer<String> warningSink) {
        Set<String> declared = new HashSet<>(declaredAncestors);
        for (ClasspathJar jar : classpathJars) {
            if (!isModelJar(jar.file())) {
                continue;
            }
            String identity = readMarkerModelId(jar.file()).orElseGet(() -> {
                try {
                    EffectivePom pom = loader.load(jar.groupId(), jar.artifactId(), jar.version());
                    return pom.parentGa() != null ? pom.parentGa() : jar.ga();
                } catch (Exception e) {
                    return null;
                }
            });
            if (identity == null) {
                warningSink.accept("Could not determine the model repo identity of model jar " + jar.gav()
                        + "; skipping the ancestor cross-check for it.");
                continue;
            }
            if (!declared.contains(identity)) {
                warningSink.accept("Model jar " + jar.gav() + " (model repo " + identity + ") is on the compile "
                        + "classpath but is not declared as a model parent via " + PARENT_PROPERTY_PREFIX + "* "
                        + "properties in the top-level pom. The ancestorModels closure written to the model marker "
                        + "will not include it, which can make rune-testing's model election ambiguous. Please "
                        + "declare it.");
            }
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
