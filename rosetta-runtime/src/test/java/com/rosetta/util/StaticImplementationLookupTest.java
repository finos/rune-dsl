package com.rosetta.util;

import com.google.inject.ImplementedBy;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.rosetta.model.lib.functions.RosettaFunction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StaticImplementationLookupTest {
    private Injector injector;
    private StaticImplementationLookup staticImplementationLookup;

    @BeforeEach
    void setUp() {
        injector = mock(Injector.class);
        staticImplementationLookup = new StaticImplementationLookup.Default(injector);
    }

    @Test
    void testNoImplementationReturnsFalse() {
        when(injector.getInstance(TestFunction.class))
                .thenReturn(new TestFunction.Default());

        boolean result = staticImplementationLookup.hasStaticImplementation(TestFunction.class);
        assertFalse(result);
    }
    
    @Test
    void testHasStaticImplementationReturnsTrue() {
        when(injector.getInstance(TestFunction.class))
        .thenReturn(new TestFunctionImpl());

        boolean result = staticImplementationLookup.hasStaticImplementation(TestFunction.class);
        assertTrue(result);
    }

    @ImplementedBy(TestFunction.Default.class)
    static abstract class TestFunction implements RosettaFunction {
        public String evaluate() {
            String testFunction = doEvaluete();
            return testFunction;
        }
        
        protected abstract String doEvaluete();
        
        static class Default extends TestFunction {

            @Override
            protected String doEvaluete() {
                String testFunction = null;
                return assignOutput(testFunction);
            }
            
            protected String assignOutput(String testFunction) {
                return testFunction;
            }
            
        }
    }

    static class TestFunctionImpl extends TestFunction {

        @Override
        protected String doEvaluete() {
            return "realOutput";
        }
    }
}
