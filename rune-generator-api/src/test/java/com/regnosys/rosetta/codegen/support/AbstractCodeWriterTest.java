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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AbstractCodeWriterTest {
    private static final String NEWLINE = System.lineSeparator();

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
        assertEquals("Hello" + NEWLINE + "World", out.toString());
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
        assertEquals("Level 0" + NEWLINE + "    Level 1" + NEWLINE + "        Level 2", out.toString());
    }

    @Test
    void testDedent() {
        out.indent();
        out.write("Level 1");
        out.newline();
        out.dedent();
        out.write("Level 0");
        assertEquals("    Level 1" + NEWLINE + "Level 0", out.toString());
    }

    @Test
    void testWriteNullIsNoOp() {
        out.write("Hello");
        out.write((Object) null);
        assertEquals("Hello", out.toString());
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
        assertEquals(NEWLINE, out.toString());
    }
}
