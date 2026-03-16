package com.rosetta.model.lib.context;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.rosetta.model.lib.context.example.One;
import com.rosetta.model.lib.context.example.ScopeA;
import com.rosetta.model.lib.context.example.ScopeB;
import com.rosetta.model.lib.context.example.Two;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FunctionContextStateTest {
    @Inject
    private ScopeA scopeA;
    @Inject
    private ScopeB scopeB;
    
    private FunctionContextState state;
    
    @BeforeEach
    void setup() {
        Injector injector = Guice.createInjector();
        injector.injectMembers(this);
        state = FunctionContextState.empty();
    }
    
    @Test
    void testScopeStackRestoration() {
        // Verify that scope stack is properly unwound
        assertEquals("One", getImplementationName(One.class));
        assertEquals("Two", getImplementationName(Two.class));
        state.pushScope(scopeA);
        state.pushScope(scopeB);
        assertEquals("OneB", getImplementationName(One.class));
        assertEquals("TwoA", getImplementationName(Two.class));
        state.popScope(); // pop scopeB
        assertEquals("One", getImplementationName(One.class));
        assertEquals("TwoA", getImplementationName(Two.class));
        state.popScope(); // pop scopeA
        assertEquals("One", getImplementationName(One.class));
        assertEquals("Two", getImplementationName(Two.class));
    }
    
    private String getImplementationName(Class<?> clazz) {
        return state.getOverride(clazz).getSimpleName();
    }
}
