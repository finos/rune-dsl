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

package com.regnosys.rosetta.codegen.support;

import com.regnosys.rosetta.codegen.api.CodeRenderer;
import com.regnosys.rosetta.codegen.api.CodeWriter;

/**
 * Base {@link CodeWriter} implementation handling indentation and line breaks.
 * Subclasses only need to implement {@link #writeString(String)}.
 *
 * <p>{@link CodeRenderer}s are rendered recursively; all other objects are
 * written using their {@code toString} representation.
 */
public abstract class AbstractCodeWriter implements CodeWriter {
    private static final String NEWLINE = System.lineSeparator();
    private static final String INDENT = "    ";

    private boolean atStartOfLine = true;
    private int indent = 0;

    protected abstract void writeString(String str);

    @Override
    public void write(Object object) {
        if (object == null) {
            return;
        }
        if (object instanceof CodeRenderer renderer) {
            renderer.render(this);
            return;
        }
        if (atStartOfLine) {
            writeString(INDENT.repeat(indent));
            atStartOfLine = false;
        }
        writeString(object.toString());
    }

    @Override
    public void newline() {
        writeString(NEWLINE);
        atStartOfLine = true;
    }

    @Override
    public void indent() {
        indent++;
    }

    @Override
    public void dedent() {
        if (indent == 0) {
            throw new IllegalStateException("Cannot dedent below zero");
        }
        indent--;
    }
}
