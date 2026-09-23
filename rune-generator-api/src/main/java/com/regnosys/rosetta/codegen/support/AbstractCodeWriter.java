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

import java.util.Objects;

import com.regnosys.rosetta.codegen.api.CodeRenderer;
import com.regnosys.rosetta.codegen.api.CodeWriter;
import com.regnosys.rosetta.codegen.api.CodeWriterConfig;

/**
 * Base {@link CodeWriter} implementation handling indentation and line breaks,
 * as configured by a {@link CodeWriterConfig}. Objects other than
 * {@link CodeRenderer}s are written using their {@code toString} representation,
 * with line breaks in it handled as described in {@link CodeWriter#write(Object)}.
 *
 * <p>Subclasses only need to implement {@link #writeString(String)}, which
 * determines where the code is written to.
 */
public abstract class AbstractCodeWriter implements CodeWriter {
    private final CodeWriterConfig config;

    private boolean atStartOfLine = true;
    private int indent = 0;

    protected AbstractCodeWriter() {
        this(CodeWriterConfig.DEFAULT);
    }

    protected AbstractCodeWriter(CodeWriterConfig config) {
        this.config = Objects.requireNonNull(config);
    }

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
        String text = object.toString();
        int lineStart = 0;
        int lineEnd;
        while ((lineEnd = text.indexOf('\n', lineStart)) >= 0) {
            int contentEnd = lineEnd > lineStart && text.charAt(lineEnd - 1) == '\r' ? lineEnd - 1 : lineEnd;
            writeLineContent(text, lineStart, contentEnd);
            newline();
            lineStart = lineEnd + 1;
        }
        writeLineContent(text, lineStart, text.length());
    }

    private void writeLineContent(String text, int start, int end) {
        if (start == end) {
            return;
        }
        if (atStartOfLine) {
            writeString(config.getIndent().repeat(indent));
            atStartOfLine = false;
        }
        writeString(text.substring(start, end));
    }

    @Override
    public void newline() {
        writeString(config.getNewline());
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
