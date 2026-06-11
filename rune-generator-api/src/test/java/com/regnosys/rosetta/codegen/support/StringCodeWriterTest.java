package com.regnosys.rosetta.codegen.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringCodeWriterTest {

    @Test
    void testToString() {
        StringCodeWriter out = new StringCodeWriter();
        out.write("Hello");
        assertEquals("Hello", out.toString());
    }

    @Test
    void testEmptyOutput() {
        assertEquals("", new StringCodeWriter().toString());
    }
}
