package com.regnosys.rosetta.generator.util;

import com.regnosys.rosetta.rosetta.RosettaType;

import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Function;

/**
 * @deprecated Use newer utilities if available.
 */
@Deprecated
public class IterableUtil {

    public static <T> Iterable<T> distinct(Iterable<T> parentIterable) {
        return new DistinctByIterator<>(parentIterable, Function.identity());
    }

    public static <T, U> Iterable<T> distinctBy(Iterable<T> parentIterable, Function<T, U> extractFunction) {
        return new DistinctByIterator<>(parentIterable, extractFunction);
    }

    public static <T> boolean exists(Iterable<? super T> iter, Class<T> clazz) {
        for (Object o : iter) {
            if (clazz.isInstance(o)) return true;
        }
        return false;
    }

    private record DistinctByIterator<T, U>(Iterable<T> iterable,
                                            Function<T, U> extractFunction) implements Iterable<T> {

        @Override
        public Iterator<T> iterator() {
            Iterator<T> parentIterator = iterable.iterator();
            return new Iterator<T>() {
                private final Set<U> read = new HashSet<>();
                private T readNext;

                @Override
                public boolean hasNext() {
                    // by the time this method finishes readNext will contain the next readable element or this returns false
                    if (readNext != null) return true;
                    while (true) {
                        if (!parentIterator.hasNext()) return false;
                        readNext = parentIterator.next();
                        U compareVal = extractFunction.apply(readNext);
                        if (!read.contains(compareVal)) {
                            read.add(compareVal);
                            return true;
                        }
                    }
                }

                @Override
                public T next() {
                    if (hasNext()) {
                        T result = readNext;
                        readNext = null;
                        return result;
                    } else {
                        throw new NoSuchElementException("read past end of iterator");
                    }
                }
            };
        }
    }

    /**
     * @deprecated Inject ModelIdProvider instead
     */
    @Deprecated
    public static String fullname(RosettaType clazz) {
        return clazz.getModel().getName() + "." + clazz.getName();
    }

    /**
     * @deprecated Inject ModelIdProvider instead
     */
    @Deprecated
    public static String packageName(RosettaType clazz) {
        return clazz.getModel().getName();
    }
}