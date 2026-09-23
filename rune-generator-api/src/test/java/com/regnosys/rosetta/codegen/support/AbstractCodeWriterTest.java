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

import com.regnosys.rosetta.codegen.api.CodeWriterConfig;
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
        assertEquals("\n", out.toString());
    }

    @Test
    void testCustomIndent() {
        StringCodeWriter tabbed = new StringCodeWriter(CodeWriterConfig.builder().indent("\t").build());
        tabbed.writeln("{");
        tabbed.indented(() -> tabbed.writeln("Indented"));
        tabbed.write("}");
        assertEquals("{\n\tIndented\n}", tabbed.toString());
    }

    @Test
    void testCustomNewline() {
        StringCodeWriter crlf = new StringCodeWriter(CodeWriterConfig.builder().newline("\r\n").build());
        crlf.writeln("Hello");
        crlf.write("World");
        assertEquals("Hello\r\nWorld", crlf.toString());
    }

    @Test
    void testMultiLineTextWithoutIndent() {
        out.write("First\nSecond\nThird");
        assertEquals("First\nSecond\nThird", out.toString());
    }

    @Test
    void testMultiLineTextIsIndentedOnEveryLine() {
        out.indent();
        out.indent();
        out.write("/**\n * Doc\n */\n");
        assertEquals("        /**\n         * Doc\n         */\n", out.toString());
    }

    @Test
    void testEmptyLinesInMultiLineTextStayEmpty() {
        out.indent();
        out.write("First\n\nSecond\n\n");
        assertEquals("    First\n\n    Second\n\n", out.toString());
    }

    @Test
    void testCarriageReturnBeforeLineFeedIsDropped() {
        out.indent();
        out.write("First\r\nSecond\r\n\r\nThird");
        assertEquals("    First\n    Second\n\n    Third", out.toString());
    }

    @Test
    void testLineBreaksUseConfiguredNewline() {
        StringCodeWriter crlf = new StringCodeWriter(CodeWriterConfig.builder().newline("\r\n").build());
        crlf.indent();
        crlf.write("First\nSecond\r\nThird");
        assertEquals("    First\r\n    Second\r\n    Third", crlf.toString());
    }

    @Test
    void testTextEndingWithLineFeedEndsTheLine() {
        out.indent();
        out.write("First\n");
        out.write("Second");
        assertEquals("    First\n    Second", out.toString());
    }

    @Test
    void testTextStartingWithLineFeedEndsTheCurrentLine() {
        out.indent();
        out.write("First");
        out.write("\nSecond");
        assertEquals("    First\n    Second", out.toString());
    }

    @Test
    void testTextAfterLastLineFeedLeavesWriterMidLine() {
        out.indent();
        out.write("First\nSecond");
        out.write(" continued");
        out.newline();
        out.write("Third");
        assertEquals("    First\n    Second continued\n    Third", out.toString());
    }

    @Test
    void testMultiLineTextAfterMidLineWriteContinuesTheLine() {
        out.indent();
        out.write("int x = ");
        out.write("1 +\n2;");
        assertEquals("    int x = 1 +\n    2;", out.toString());
    }

    @Test
    void testTextOfOnlyLineFeeds() {
        out.indent();
        out.write("\n\n");
        assertEquals("\n\n", out.toString());
    }

    @Test
    void testMultiLineToStringOfNonStringObject() {
        out.indent();
        out.write(new StringBuilder("First\nSecond"));
        assertEquals("    First\n    Second", out.toString());
    }
}
