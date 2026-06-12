package com.regnosys.rosetta.codegen.support;

import com.regnosys.rosetta.codegen.api.CodeRenderer;
import com.regnosys.rosetta.codegen.api.CodeWriter;

public abstract class AbstractCodeWriter implements CodeWriter {
    private static final String NEWLINE = "\n";
    private static final String INDENT = "    ";

    private boolean atStartOfLine = true;
    private int indent = 0;

    protected abstract void writeString(String str);

    @Override
    public void write(Object object) {
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
