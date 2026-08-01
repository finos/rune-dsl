package com.rosetta.model.lib.flatten;

import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.RosettaModelObjectBuilder;
import com.rosetta.model.lib.meta.FieldWithMeta;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.path.RosettaPathValue;
import com.rosetta.model.lib.process.AttributeMeta;
import com.rosetta.model.lib.process.BuilderProcessor;

import java.util.*;
import java.util.concurrent.CancellationException;

/**
 * Flattens a {@link RosettaModelObject} into a list of {@link RosettaPathValue} objects.
 * This effectively transforms a nested object structure into a flat list of path-value pairs,
 * omitting metadata fields.
 */
public class ModelObjectFlattener {

    /**
     * How many visited nodes to cover per interrupt check. Flattening is uninterruptible CPU work,
     * so callers that bound it with a timeout need it to observe interruption; polling in batches
     * keeps the cost of that immeasurably small relative to the work done per node.
     */
    private static final int INTERRUPT_CHECK_MASK = 1023;

    /**
     * Flattens the provided RosettaModelObject.
     * <p>
     * Large objects can take a long time to flatten, so this honours interruption of the calling
     * thread: if the thread's interrupt flag is set, flattening abandons its work and throws
     * {@link CancellationException}. The interrupt flag is left set for the caller to act on.
     *
     * @param modelObject The RosettaModelObject to flatten.
     * @return A list of RosettaPathValue objects representing the flattened object.
     * @throws CancellationException if the calling thread is interrupted while flattening.
     */
    public List<RosettaPathValue> flatten(RosettaModelObject modelObject) {
        FlattenerBuilderProcessor processor = new FlattenerBuilderProcessor();
        modelObject.toBuilder().process(RosettaPath.valueOf(modelObject.getType().getSimpleName()), processor);

        List<RosettaPath> metaPaths = processor.getMetaPaths();
        List<RosettaPathValue> pathValues = processor.getRosettaPathValue();

        return removeAllMetaPaths(metaPaths, pathValues);
    }

    /**
     * Aborts if the calling thread has been interrupted, checking only once every
     * {@link #INTERRUPT_CHECK_MASK}+1 calls.
     *
     * @param visitCount a monotonically increasing count of nodes visited so far.
     */
    private static void abortIfInterrupted(int visitCount) {
        if ((visitCount & INTERRUPT_CHECK_MASK) == 0 && Thread.currentThread().isInterrupted()) {
            throw new CancellationException("Interrupted while flattening a model object");
        }
    }

    /**
     * Removes all metadata paths from the provided list of RosettaPathValues.
     *
     * @param metaPaths      The list of metadata paths to remove.
     * @param pathValues The list of RosettaPathValues to filter.
     * @return A new list of RosettaPathValues with metadata paths removed.
     */
    private List<RosettaPathValue> removeAllMetaPaths(List<RosettaPath> metaPaths, List<RosettaPathValue> pathValues) {
        // A meta path is always a prefix of the value paths it applies to, so testing every value
        // path against every meta path is unnecessary: looking each ancestor up in a set is O(depth)
        // rather than O(metaPaths). This is the dominant cost when flattening large samples.
        Set<RosettaPath> metaPathSet = new HashSet<>(metaPaths);
        List<RosettaPathValue> result = new ArrayList<>(pathValues.size());
        int visited = 0;
        for (RosettaPathValue pathValue : pathValues) {
            abortIfInterrupted(visited++);
            result.add(new RosettaPathValue(removeAllMetaPaths(metaPathSet, pathValue.getPath()), pathValue.getValue()));
        }
        return result;
    }

    /**
     * Removes all metadata path segments from the provided RosettaPath.
     *
     * @param metaPaths The metadata paths to use for filtering.
     * @param path      The RosettaPath to filter.
     * @return A new RosettaPath with metadata segments removed, and its first element trimmed.
     */
    private RosettaPath removeAllMetaPaths(Set<RosettaPath> metaPaths, RosettaPath path) {
        int depth = path.depth();
        RosettaPath.Element[] elements = new RosettaPath.Element[depth];
        boolean[] isMetaSegment = new boolean[depth];

        int i = depth - 1;
        for (RosettaPath ancestor = path; ancestor != null; ancestor = ancestor.getParent(), i--) {
            elements[i] = ancestor.getElement();
            // an ancestor that is itself a meta path contributes the segment naming the metadata field
            isMetaSegment[i] = metaPaths.contains(ancestor);
        }

        RosettaPath result = null;
        boolean firstRemainingTrimmed = false;
        for (int j = 0; j < depth; j++) {
            if (isMetaSegment[j]) {
                continue;
            }
            if (!firstRemainingTrimmed) {
                firstRemainingTrimmed = true;
                continue;
            }
            result = result == null
                    ? RosettaPath.createPath(elements[j])
                    : result.newSubPath(elements[j]);
        }
        return result;
    }

    /**
     * A {@link BuilderProcessor} implementation used to extract path-value pairs and metadata paths
     * during the flattening process.
     */
    private static class FlattenerBuilderProcessor implements BuilderProcessor {

        private final List<RosettaPathValue> rosettaPathValues = new ArrayList<>();
        private final List<RosettaPath> metaPaths = new ArrayList<>();
        private int visited;

        /**
         * Returns the list of identified metadata paths.
         * @return The list of metadata paths.
         */
        public List<RosettaPath> getMetaPaths() {
            return metaPaths;
        }

        /**
         * Returns the list of RosettaPathValue objects, excluding metadata paths.
         * @return The list of RosettaPathValue objects.
         */
        public List<RosettaPathValue> getRosettaPathValue() {
            return rosettaPathValues;
        }

        @Override
        public <R extends RosettaModelObject> boolean processRosetta(RosettaPath path, Class<R> rosettaType, RosettaModelObjectBuilder builder, RosettaModelObjectBuilder parent, AttributeMeta... metas) {
            abortIfInterrupted(visited++);
            if (builder != null && parent instanceof FieldWithMeta) {
                metaPaths.add(path);
            }
            return true;
        }

        @Override
        public <R extends RosettaModelObject> boolean processRosetta(RosettaPath path, Class<R> rosettaType, List<? extends RosettaModelObjectBuilder> builders, RosettaModelObjectBuilder parent, AttributeMeta... metas) {
            int i = 0;
            for (RosettaModelObjectBuilder builder : builders) {
                processRosetta(path.withIndex(i++), rosettaType, builder, parent, metas);
            }
            return true;
        }

        @Override
        public <T> void processBasic(RosettaPath path, Class<T> rosettaType, T instance, RosettaModelObjectBuilder parent, AttributeMeta... metas) {
            abortIfInterrupted(visited++);
            if (instance != null) {
                if (parent instanceof FieldWithMeta) {
                    rosettaPathValues.add(new RosettaPathValue(path.getParent(), instance));
                } else {
                    rosettaPathValues.add(new RosettaPathValue(path, instance));
                }
            }
        }

        @Override
        public <T> void processBasic(RosettaPath path, Class<T> rosettaType, Collection<? extends T> instances, RosettaModelObjectBuilder parent, AttributeMeta... metas) {
            int i = 0;
            for (T instance : instances) {
                processBasic(path.withIndex(i++), rosettaType, instance, parent, metas);
            }
        }

        @Override
        public Report report() {
            return null;
        }
    }
}