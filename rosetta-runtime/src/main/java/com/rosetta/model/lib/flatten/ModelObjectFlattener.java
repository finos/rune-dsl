package com.rosetta.model.lib.flatten;

import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.model.lib.RosettaModelObjectBuilder;
import com.rosetta.model.lib.meta.FieldWithMeta;
import com.rosetta.model.lib.path.RosettaPath;
import com.rosetta.model.lib.path.RosettaPathValue;
import com.rosetta.model.lib.process.AttributeMeta;
import com.rosetta.model.lib.process.BuilderProcessor;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Flattens a {@link RosettaModelObject} into a list of {@link RosettaPathValue} objects.
 * This effectively transforms a nested object structure into a flat list of path-value pairs,
 * omitting metadata fields.
 */
public class ModelObjectFlattener {

    /**
     * Flattens the provided RosettaModelObject.
     *
     * @param modelObject The RosettaModelObject to flatten.
     * @return A list of RosettaPathValue objects representing the flattened object.
     */
    public List<RosettaPathValue> flatten(RosettaModelObject modelObject) {
        FlattenerBuilderProcessor processor = new FlattenerBuilderProcessor();
        modelObject.toBuilder().process(RosettaPath.valueOf(modelObject.getType().getSimpleName()), processor);

        List<RosettaPath> metaPaths = processor.getMetaPaths();
        List<RosettaPathValue> pathValues = processor.getRosettaPathValue();

        return removeAllMetaPaths(metaPaths, pathValues);
    }

    /**
     * Removes all metadata paths from the provided list of RosettaPathValues.
     *
     * @param metaPaths      The list of metadata paths to remove.
     * @param pathValues The list of RosettaPathValues to filter.
     * @return A new list of RosettaPathValues with metadata paths removed.
     */
    private List<RosettaPathValue> removeAllMetaPaths(List<RosettaPath> metaPaths, List<RosettaPathValue> pathValues) {
        return pathValues.stream()
                .map(pathValue -> new RosettaPathValue(removeAllMetaPaths(metaPaths, pathValue.getPath()), pathValue.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Removes all metadata path segments from the provided RosettaPath.
     *
     * @param metaPaths The list of metadata paths to use for filtering.
     * @param path      The RosettaPath to filter.
     * @return A new RosettaPath with metadata segments removed.
     */
    private RosettaPath removeAllMetaPaths(List<RosettaPath> metaPaths, RosettaPath path) {
        LinkedList<RosettaPath.Element> elements = path.allElements();
        metaPaths.stream()
                .filter(path::startsWith)
                .map(RosettaPath::allElements)
                .map(LinkedList::size)
                .map(i -> i - 1)
                .distinct()
                .sorted(Comparator.reverseOrder())
                .forEach(i -> elements.remove((int) i));

        RosettaPath newPath = RosettaPath.createPathFromElements(elements);
        return newPath.trimFirst();
    }

    /**
     * A {@link BuilderProcessor} implementation used to extract path-value pairs and metadata paths
     * during the flattening process.
     */
    private static class FlattenerBuilderProcessor implements BuilderProcessor {

        private final List<RosettaPathValue> rosettaPathValues = new ArrayList<>();
        private final List<RosettaPath> metaPaths = new ArrayList<>();

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