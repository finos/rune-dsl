package com.rosetta.model.lib.context;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.rosetta.model.lib.context.example.One;
import com.rosetta.model.lib.context.example.ScopeA;
import com.rosetta.model.lib.context.example.ScopeB;
import com.rosetta.model.lib.context.example.Two;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test to verify the O(1) performance optimization of getInstanceInScope.
 */
public class ScopedFunctionContextPerformanceTest {
    private ScopedFunctionContext context;

    @BeforeEach
    void setup() {
        Injector injector = Guice.createInjector();
        context = new ScopedFunctionContext(injector);
    }

    @Test
    void testPerformanceImprovement() {
        // Create a deeply nested scope stack
        final int DEPTH = 10000;
        ScopedFunctionContext currentContext = context;

        // Build a deep stack (alternating between ScopeA and ScopeB)
        for (int i = 0; i < DEPTH; i++) {
            currentContext = currentContext.inScope(i % 2 == 0 ? ScopeA.class : ScopeB.class);
        }

        // Perform many lookups - with O(1) cache this should be fast
        // With O(n) iteration this would be slow
        long startTime = System.nanoTime();
        final int ITERATIONS = 10000;
        for (int i = 0; i < ITERATIONS; i++) {
            One one = currentContext.getInstanceInScope(One.class);
            Two two = currentContext.getInstanceInScope(Two.class);
            assertNotNull(one);
            assertNotNull(two);
        }
        long endTime = System.nanoTime();
        long durationMs = (endTime - startTime) / 1_000_000;

        // With O(1) lookups, this should complete quickly (< 1 second)
        // With O(n) lookups at depth 10000, this would take much longer
        System.out.println("Performance test: " + ITERATIONS + " lookups at depth " + DEPTH + " took " + durationMs + "ms");

        // Assert it completes reasonably quickly - giving 1 second as generous upper bound
        // On a typical machine with O(1) lookup this should be < 100ms
        assert durationMs < 1000 : "Performance test took too long: " + durationMs + "ms";
    }

    @Test
    void testScopeStackRestoration() {
        // Verify that scope stack is properly unwound
        One one1 = context.getInstanceInScope(One.class);
        assertEquals("One", one1.getClass().getSimpleName());

        try (ScopedFunctionContext scopeA = context.inScope(ScopeA.class)) {
            try (ScopedFunctionContext scopeB = scopeA.inScope(ScopeB.class)) {
                One one2 = scopeB.getInstanceInScope(One.class);
                assertEquals("OneB", one2.getClass().getSimpleName());
            }

            One one3 = scopeA.getInstanceInScope(One.class);
            assertEquals("One", one3.getClass().getSimpleName());
        }

        One one4 = context.getInstanceInScope(One.class);
        assertEquals("One", one4.getClass().getSimpleName());
    }
}
