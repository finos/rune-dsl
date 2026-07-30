package com.regnosys.rosetta.ide.performance;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.lsp4j.DiagnosticTag;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import com.regnosys.rosetta.ide.tests.AbstractRosettaLanguageServerTest;

/**
 * <b>TEMPORARY — DO NOT DELETE WITHOUT ASKING.</b> This harness is checked in so the phase-3 numbers can be
 * reproduced and re-measured. It is to be removed only when the repository owner says so, not as part of a
 * routine cleanup.
 *
 * <p>Measures how long the language server takes to respond to an edit in a real, large model, and how many
 * files it validates in doing so.
 *
 * <p>This exists because the "is never used" markers are cross-file by nature: a declaration's marker
 * depends on every other file, so both detection (a walk of the whole resource set, see
 * {@code UnusedElementHelper}) and invalidation (revalidating files whose incoming references changed, see
 * {@code RosettaStatefulIncrementalBuilder}) scale with model size in a way ordinary validation does not.
 * The numbers that matter are the marginal ones — an edit that changes no cross-file reference must stay
 * as cheap as it was, and an edit that toggles one must stay within a keystroke's budget.
 *
 * <p><b>Not a CI test.</b> The class name does not match surefire's {@code *Test} include patterns, so a
 * normal build never picks it up; the assumption below is a second line of defence for anyone who widens
 * those patterns. It needs a large model that is not part of this repo, so it skips unless pointed at one:
 *
 * <pre>
 * mvn -o test -pl rune-ide -Dtest=EditLatencyBenchmark \
 *     -Drune.benchmark.model.dir=/path/to/common-domain-model/rosetta-source/src/main/rosetta
 * </pre>
 *
 * <p>To A/B a change, run it on the branch and again with the change stashed, and compare. There is
 * deliberately no runtime switch for the revalidation pass: a production toggle that exists only to be
 * measured is worse than running the benchmark twice.
 *
 * <p>Every scenario is derived from the model's own contents rather than hardcoded line numbers, so it
 * survives the model being updated; if a scenario's anchor cannot be found, that scenario reports as
 * skipped rather than silently measuring nothing.
 */
public class EditLatencyBenchmark extends AbstractRosettaLanguageServerTest {
    private static final String MODEL_DIR_PROPERTY = "rune.benchmark.model.dir";
    private static final int ROUNDS = 3;

    /** `func Name:` / `type Name:` / `enum Name:` at the start of a line. */
    private static final Pattern DECLARATION =
            Pattern.compile("^(func|type|enum)\\s+([A-Za-z_][A-Za-z0-9_]*)\\s*[:(]", Pattern.MULTILINE);

    private final Map<String, String> originalContents = new LinkedHashMap<>();
    private final Map<String, Integer> versions = new LinkedHashMap<>();
    private final Set<String> opened = new LinkedHashSet<>();

    @Test
    void measureEditLatency() {
        Path modelDir = modelDir();
        Assumptions.assumeTrue(modelDir != null,
                "Set -D" + MODEL_DIR_PROPERTY + "=<dir of .rosetta files> to run this benchmark");

        long coldMillis = writeModelAndBuild(modelDir);
        System.out.printf("  %-52s %5d ms, %d files%n", "cold initial build", coldMillis, originalContents.size());

        reportMarkerCensus();

        String bulkTarget = pickBulkTarget();
        System.out.println("bulk-edit target: " + relativeName(bulkTarget));

        measure("edit changing no cross-file reference",
                Optional.of(new Edit(bulkTarget, appendBlankLine(bulkTarget))));
        measure("edit toggling a function call", callSiteEdit());
        measure("edit toggling a type or enum reference", typeReferenceEdit());
        measure("blanking out " + relativeName(bulkTarget),
                Optional.of(new Edit(bulkTarget, namespaceLineOf(bulkTarget))));
    }

    /**
     * How many "is never used" markers the model attracts, by kind, against how many declarations of that
     * kind it has. This is the noise measurement: the marker is only worth having if the proportion is low
     * enough that a reader trusts it, and a library model — which publishes types for downstream consumers —
     * is the worst case.
     */
    private void reportMarkerCensus() {
        Map<String, Long> markersByKind = getDiagnostics().values().stream()
                .flatMap(List::stream)
                .filter(diagnostic -> diagnostic.getTags() != null
                        && diagnostic.getTags().contains(DiagnosticTag.Unnecessary))
                .collect(Collectors.groupingBy(
                        diagnostic -> diagnostic.getMessage().split(" ")[0], TreeMap::new, Collectors.counting()));
        Map<String, Integer> declarationsByKeyword = new TreeMap<>();
        Stream.of("func", "type", "enum").forEach(
                keyword -> declarationsByKeyword.put(keyword, declarations(keyword).size()));
        System.out.println("  markers: " + markersByKind + " against declarations " + declarationsByKeyword);
    }

    private record Edit(String uri, String newContent) {
    }

    /**
     * Applies the edit and reverts it {@link #ROUNDS} times, reporting the median of each direction
     * separately. Both directions are worth reporting: applying an edit that breaks a reference and undoing
     * it are not symmetric, since only one of them removes a declaration's last usage.
     */
    private void measure(String label, Optional<Edit> edit) {
        if (edit.isEmpty()) {
            System.out.printf("  %-52s SKIPPED (no anchor in this model)%n", label);
            return;
        }
        // `didChange` is only honoured for an open document, and `didOpen` is itself a build, so this has to
        // happen outside the timed section.
        openOnce(edit.get().uri());
        String original = originalContents.get(edit.get().uri());
        List<Long> applyMillis = new ArrayList<>();
        List<Integer> applyValidated = new ArrayList<>();
        List<Long> revertMillis = new ArrayList<>();
        List<Integer> revertValidated = new ArrayList<>();
        for (int round = 0; round < ROUNDS; round++) {
            Result applied = applyAndTime(edit.get().uri(), edit.get().newContent());
            applyMillis.add(applied.millis());
            applyValidated.add(applied.validated());
            Result reverted = applyAndTime(edit.get().uri(), original);
            revertMillis.add(reverted.millis());
            revertValidated.add(reverted.validated());
        }
        System.out.printf("  %-52s%n", label);
        report("apply", applyMillis, applyValidated);
        report("revert", revertMillis, revertValidated);
    }

    private void report(String direction, List<Long> millis, List<Integer> validated) {
        System.out.printf("    %-8s %5d ms (median of %d: %s), %2d files validated%n",
                direction, median(millis), ROUNDS, millis, median(validated));
    }

    private record Result(long millis, int validated) {
    }

    /** Replaces a document wholesale and returns how long the resulting build took, and its fan-out. */
    private Result applyAndTime(String uri, String newContent) {
        int notificationsBefore = notifications.size();
        int version = versions.merge(uri, 1, Integer::sum);
        long start = System.nanoTime();
        languageServer.didChange(new DidChangeTextDocumentParams(
                new VersionedTextDocumentIdentifier(uri, version),
                // A change event with no range replaces the whole document, which lets every scenario be
                // expressed as a string substitution instead of a line/column range.
                List.of(new TextDocumentContentChangeEvent(newContent))));
        // getDiagnostics() goes through the request manager, so it only returns once the build has run.
        getDiagnostics();
        long millis = (System.nanoTime() - start) / 1_000_000;
        return new Result(millis, publishedSince(notificationsBefore));
    }

    /** One {@code publishDiagnostics} is sent per validated resource, so counting them counts the fan-out. */
    private int publishedSince(int notificationsBefore) {
        return (int) notifications.subList(notificationsBefore, notifications.size()).stream()
                .filter(notification -> notification.getValue() instanceof PublishDiagnosticsParams)
                .count();
    }

    private long writeModelAndBuild(Path modelDir) {
        try (Stream<Path> files = Files.list(modelDir)) {
            files.filter(path -> path.getFileName().toString().endsWith(".rosetta"))
                    .sorted()
                    .forEach(path -> {
                        String content = read(path);
                        String uri = writeFile(path.getFileName().toString(), content);
                        originalContents.put(uri, content);
                        versions.put(uri, 1);
                    });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        long start = System.nanoTime();
        initialize();
        getDiagnostics();
        return (System.nanoTime() - start) / 1_000_000;
    }

    /**
     * An edit that makes a cross-file function call stop resolving: finds a function declared in one file
     * and called from another, and renames the call.
     */
    private Optional<Edit> callSiteEdit() {
        return declarations("func").stream()
                .flatMap(declaration -> referencingFile(declaration, name -> name + "(").stream())
                .findFirst()
                .map(reference -> new Edit(reference.uri(),
                        originalContents.get(reference.uri())
                                .replace(reference.name() + "(", reference.name() + "NoSuchThing(")));
    }

    /**
     * An edit that makes a cross-file type or enum reference stop resolving: finds a type or enum declared
     * in one file and used as an attribute type in another, and renames the use.
     */
    private Optional<Edit> typeReferenceEdit() {
        List<Declaration> candidates = new ArrayList<>(declarations("type"));
        candidates.addAll(declarations("enum"));
        return candidates.stream()
                .flatMap(declaration -> referencingFile(declaration, name -> " " + name + " (").stream())
                .findFirst()
                .map(reference -> new Edit(reference.uri(),
                        originalContents.get(reference.uri())
                                .replace(" " + reference.name() + " (", " " + reference.name() + "NoSuchThing (")));
    }

    private record Declaration(String uri, String name) {
    }

    private List<Declaration> declarations(String keyword) {
        List<Declaration> result = new ArrayList<>();
        originalContents.forEach((uri, content) -> {
            Matcher matcher = DECLARATION.matcher(content);
            while (matcher.find()) {
                if (keyword.equals(matcher.group(1))) {
                    result.add(new Declaration(uri, matcher.group(2)));
                }
            }
        });
        return result;
    }

    /**
     * The first file other than the declaring one that mentions the declaration in the given syntactic form,
     * and only if it mentions it exactly once — a unique occurrence is what makes a whole-document
     * substitution equivalent to a single keystroke.
     */
    private Optional<Declaration> referencingFile(Declaration declaration, UnaryOperator<String> form) {
        String needle = form.apply(declaration.name());
        return originalContents.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(declaration.uri()))
                .filter(entry -> occurrences(entry.getValue(), needle) == 1)
                .findFirst()
                .map(entry -> new Declaration(entry.getKey(), declaration.name()));
    }

    private static int occurrences(String haystack, String needle) {
        int count = 0;
        for (int index = haystack.indexOf(needle); index >= 0; index = haystack.indexOf(needle, index + 1)) {
            count++;
        }
        return count;
    }

    /**
     * The file the mass-edit scenarios blank out: the one with the widest fan-out, i.e. the most declarations
     * that other files mention. Fan-out rather than file size is the point — blanking out a large file that
     * nothing depends on rebuilds only itself and measures nothing.
     */
    private String pickBulkTarget() {
        Map<String, Integer> fanOut = new LinkedHashMap<>();
        originalContents.keySet().forEach(uri -> fanOut.put(uri, 0));
        Stream.of("func", "type", "enum")
                .flatMap(keyword -> declarations(keyword).stream())
                .forEach(declaration -> {
                    long referencingFiles = originalContents.entrySet().stream()
                            .filter(entry -> !entry.getKey().equals(declaration.uri()))
                            .filter(entry -> entry.getValue().contains(declaration.name()))
                            .count();
                    if (referencingFiles > 0) {
                        fanOut.merge(declaration.uri(), 1, Integer::sum);
                    }
                });
        return fanOut.entrySet().stream()
                .max(Comparator.comparingInt(Map.Entry::getValue))
                .orElseThrow()
                .getKey();
    }

    private void openOnce(String uri) {
        if (opened.add(uri)) {
            open(uri, originalContents.get(uri));
            getDiagnostics();
        }
    }

    private String appendBlankLine(String uri) {
        return originalContents.get(uri) + System.lineSeparator();
    }

    private String namespaceLineOf(String uri) {
        return originalContents.get(uri).lines()
                .filter(line -> line.startsWith("namespace "))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No namespace declaration in " + uri))
                + System.lineSeparator();
    }

    private static String relativeName(String uri) {
        return uri.substring(uri.lastIndexOf('/') + 1);
    }

    private static Path modelDir() {
        String configured = System.getProperty(MODEL_DIR_PROPERTY);
        if (configured == null || configured.isBlank()) {
            return null;
        }
        Path path = Path.of(configured);
        return Files.isDirectory(path) ? path : null;
    }

    private static String read(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static <T extends Comparable<T>> T median(List<T> values) {
        return values.stream().sorted().toList().get(values.size() / 2);
    }

}
