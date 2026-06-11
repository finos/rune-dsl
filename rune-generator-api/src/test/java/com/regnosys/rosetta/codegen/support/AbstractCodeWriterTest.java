package com.regnosys.rosetta.codegen.support;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AbstractCodeWriterTest {
    private StringCodeWriter out;

    @BeforeEach
    void setUp() {
        out = new StringCodeWriter();
    }

    @Test
    void testSimpleWrite() {
        out.write("Hello");
        assertEquals("Hello", out.toString());
    }

    @Test
    void testNewline() {
        out.write("Hello");
        out.newline();
        out.write("World");
        assertEquals("Hello\nWorld", out.toString());
    }

    @Test
    void testIndents() {
        out.write("Level 0");
        out.newline();
        out.indent();
        out.write("Level 1");
        out.newline();
        out.indent();
        out.write("Level 2");
        assertEquals("Level 0\n    Level 1\n        Level 2", out.toString());
    }

    @Test
    void testDedent() {
        out.indent();
        out.write("Level 1");
        out.newline();
        out.dedent();
        out.write("Level 0");
        assertEquals("    Level 1\nLevel 0", out.toString());
    }

    @Test
    void testDedentBelowZeroThrowsException() {
        assertThrows(IllegalStateException.class, () -> {
            out.dedent();
        });
    }

    @Test
    void testIndentOnlyAppliedAtStartOfLine() {
        out.indent();
        out.write("First");
        out.write(" Second");
        out.write(" Third");
        assertEquals("    First Second Third", out.toString());
    }

    @Test
    void testNewlineAtStartDoesNotAddIndent() {
        out.indent();
        out.newline();
        assertEquals("\n", out.toString());
    }
}
