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
