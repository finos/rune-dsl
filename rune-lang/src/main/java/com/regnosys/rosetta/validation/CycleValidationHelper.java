package com.regnosys.rosetta.validation;

import com.regnosys.rosetta.rosetta.RosettaNamed;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CycleValidationHelper {
    public <T extends RosettaNamed> void detectCycle(T object, Function<T, T> nextGetter, String delimiter, Consumer<String> onCycle) {
        doDetectCycles(object, wrapNull(nextGetter), next -> next, delimiter, (obj, path) -> onCycle.accept(path));
    }
    public <T extends RosettaNamed, U> void detectMultipleCycles(T object, Function<T, Iterable<U>> inclusionGetter, Function<U, T> nextGetter, String delimiter, BiConsumer<U, String> onCycle) {
        doDetectCycles(object, inclusionGetter, nextGetter, delimiter, onCycle);
    }
    private <T> Function<T, Iterable<T>> wrapNull(Function<T, T> nextGetter) {
        return t -> {
            T next = nextGetter.apply(t);
            if (next == null) {
                return Collections.emptyList();
            } else {
                return Collections.singletonList(next);
            }
        };
    }
    
    private <T extends RosettaNamed, U> void doDetectCycles(T object, Function<T, Iterable<U>> inclusionGetter, Function<U, T> nextGetter, String delimiter, BiConsumer<U, String> onCycle) {
        for (U inclusion : inclusionGetter.apply(object)) {
            T next = nextGetter.apply(inclusion);
            List<T> path = new ArrayList<>();
            path.add(object);
            Set<T> visited = new HashSet<>();
            visited.add(object);
            if (hasCycle(next, path, visited, inclusionGetter, nextGetter)) {
                String pathString = path.stream().map(RosettaNamed::getName).collect(Collectors.joining(" " + delimiter + " "));
                onCycle.accept(inclusion, pathString);
            }
        }
    }
    private <T, U> boolean hasCycle(T current, List<T> path, Set<T> visited, Function<T, Iterable<U>> inclusionGetter, Function<U, T> nextGetter) {
        path.add(current);
        if (visited.add(current)) {
            for (U inclusion : inclusionGetter.apply(current)) {
                T next = nextGetter.apply(inclusion);
                if (hasCycle(next, path, visited, inclusionGetter, nextGetter)) {
                    return true;
                }
            }
        } else {
            if (path.get(0).equals(path.get(path.size() - 1))) {
                return true;
            }
        }
        path.remove(path.size() - 1);
        return false;
    }
}
