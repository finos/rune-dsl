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

package com.regnosys.rosetta.codegen.api;

import java.util.Iterator;
import java.util.function.Consumer;

/**
 * A sink for generated code. Keeps track of the current indentation level,
 * which is applied to the start of each non-empty line.
 *
 * <p>Implementations decide how written objects are converted to text, e.g.,
 * by rendering {@link CodeRenderer}s recursively or by resolving objects
 * to target-language identifiers.
 */
public interface CodeWriter {
    /* Core API */

    /**
     * Writes a single object. Writing {@code null} is a no-op.
     */
    void write(Object object);

    /**
     * Ends the current line.
     */
    void newline();

    /**
     * Increases the indentation level for subsequent lines.
     */
    void indent();

    /**
     * Decreases the indentation level for subsequent lines.
     *
     * @throws IllegalStateException if the indentation level is already zero
     */
    void dedent();

    /* Convenience API */

    /**
     * Writes the given objects in order.
     */
    default void write(Object... objects) {
        for (Object object : objects) {
            write(object);
        }
    }

    /**
     * Writes the given object and ends the line.
     */
    default void writeln(Object object) {
        write(object);
        newline();
    }

    /**
     * Writes the given objects in order and ends the line.
     */
    default void writeln(Object... objects) {
        write(objects);
        newline();
    }

    /**
     * Ends the current line and adds an empty line.
     */
    default void blank() {
        newline();
        newline();
    }

    /**
     * Runs the given code with the indentation level increased by one.
     */
    default void indented(Runnable runnable) {
        indent();
        try {
            runnable.run();
        } finally {
            dedent();
        }
    }

    /**
     * Renders the given renderer into this writer.
     */
    default void render(CodeRenderer renderer) {
        renderer.render(this);
    }

    /**
     * Writes the given items, separated by the given separator.
     */
    default void join(Iterable<?> items, String separator) {
        join(items, separator, this::write);
    }

    /**
     * Writes the given items using {@code renderItem}, separated by the given separator.
     */
    default <T> void join(Iterable<T> items, String separator, Consumer<T> renderItem) {
        Iterator<T> it = items.iterator();
        if (!it.hasNext()) return;

        renderItem.accept(it.next());
        while (it.hasNext()) {
            write(separator);
            renderItem.accept(it.next());
        }
    }
}
