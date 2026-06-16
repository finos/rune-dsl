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

import com.regnosys.rosetta.codegen.support.StringCodeWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CodeWriterTest {
    private StringCodeWriter out;

    @BeforeEach
    void setUp() {
        out = new StringCodeWriter();
    }

    @Test
    void testMultiWrite() {
        out.write("Hello", " ", "World");
        assertEquals("Hello World", out.toString());
    }

    @Test
    void testWriteln() {
        out.writeln("Hello", " ", "World");
        assertEquals("Hello World\n", out.toString());
    }

    @Test
    void testIndented() {
        out.writeln("{");
        out.indented(() -> {
            out.writeln("Indented");
            out.writeln("More indented");
        });
        out.writeln("}");
        assertEquals("""
                {
                    Indented
                    More indented
                }
                """, out.toString());
    }

    @Test
    void testJoin() {
        out.join(List.of("a", "b", "c"), ", ");
        assertEquals("a, b, c", out.toString());
    }

    @Test
    void testBlank() {
        out.write("Line 1");
        out.blank();
        out.write("Line 2");
        assertEquals("Line 1\n\nLine 2", out.toString());
    }
}
