package com.regnosys.rosetta.codegen.api;

import java.util.Iterator;
import java.util.function.Consumer;

public interface CodeWriter {
    /* Core API */
    void write(Object object);

    void newline();

    void indent();

    void dedent();

    /* Convenience API */
    default void write(Object... objects) {
        for (Object object : objects) {
            write(object);
        }
    }

    default void writeln(Object object) {
        write(object);
        newline();
    }

    default void writeln(Object... objects) {
        write(objects);
        newline();
    }
    
    default void blank() {
        newline();
        newline();
    }

    default void indented(Runnable runnable) {
        indent();
        try {
            runnable.run();
        } finally {
            dedent();
        }
    }

    default void render(CodeRenderer renderer) {
        renderer.render(this);
    }

    default void join(Iterable<?> items, String separator) {
        join(items, separator, this::write);
    }

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
