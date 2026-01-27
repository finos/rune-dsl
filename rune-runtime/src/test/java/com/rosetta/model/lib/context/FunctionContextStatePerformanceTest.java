package com.rosetta.model.lib.context;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.rosetta.model.lib.context.example.One;
import com.rosetta.model.lib.context.example.ScopeA;
import com.rosetta.model.lib.context.example.ScopeB;
import com.rosetta.model.lib.context.example.Two;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Performance tests to verify the O(1) optimization of getOverride.
 * <p>
 * These tests automatically run with JIT disabled (-Xint) via separate Surefire execution
 * to measure realistic first-run performance. They run automatically during {@code mvn test}
 * or {@code mvn clean install}.
 */
@Tag("performance")
public class FunctionContextStatePerformanceTest {
    @Inject
    private ScopeA scopeA;
    @Inject
    private ScopeB scopeB;

    @BeforeEach
    void setup() {
        Injector injector = Guice.createInjector();
        injector.injectMembers(this);
    }

    @Test
    void testPerformanceForADeepScopeStack() {
        // Test that deep scopes don't degrade performance significantly
        // This proves O(1) vs O(n) by comparing shallow vs deep stack performance
        // Tests first-run performance (realistic usage without JIT warmup)

        final int ITERATIONS = 10000;
        final int SHALLOW_DEPTH = 10;
        final int DEEP_DEPTH = 100;

        // Shallow stack
        FunctionContextState shallowContextState = createStateWithDepth(SHALLOW_DEPTH);

        // Deep stack
        FunctionContextState deepContextState = createStateWithDepth(DEEP_DEPTH);

        // Measure first-run performance (no JIT warmup - simulates real usage)
        long shallowMs = runBenchmark(shallowContextState, ITERATIONS);
        long deepMs = runBenchmark(deepContextState, ITERATIONS);

        System.out.println("Performance test (first-run, no JIT warmup):");
        System.out.println("  Shallow (depth " + SHALLOW_DEPTH + "): " + shallowMs + "ms for " + ITERATIONS + " lookups");
        System.out.println("  Deep (depth " + DEEP_DEPTH + "): " + deepMs + "ms for " + ITERATIONS + " lookups");
        double ratio = (double)deepMs / shallowMs;
        System.out.println("  Ratio: " + String.format("%.2f", ratio) + "x");

        // With O(1), ratio should be close to 1.0 even without JIT
        // With O(n), ratio would be ~10x (DEEP_DEPTH / SHALLOW_DEPTH)
        // Allow up to 3x difference for noise, cache effects, and first-run variance
        assert ratio < 3.0 : "Deep context is " + String.format("%.2f", ratio)
                            + "x slower than shallow - suggests O(n) behavior";
    }
    
    private FunctionContextState createStateWithDepth(int depth) {
        FunctionContextState state = FunctionContextState.empty();
        for (int i = 0; i < depth; i++) {
            state.pushScope(i % 2 == 0 ? scopeA : scopeB);
        }
        return state;
    }

    private long runBenchmark(FunctionContextState state, int iterations) {
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            Class<? extends One> one = state.getOverride(One.class);
            Class<? extends Two> two = state.getOverride(Two.class);
            assertNotNull(one);
            assertNotNull(two);
        }
        return (System.nanoTime() - start) / 1_000_000;
    }
}
