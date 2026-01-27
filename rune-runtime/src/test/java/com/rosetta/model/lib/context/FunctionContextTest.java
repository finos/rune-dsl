package com.rosetta.model.lib.context;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.rosetta.model.lib.context.example.Three;
import com.rosetta.model.lib.context.example.ThreeInScopeA;
import com.rosetta.model.lib.context.example.ThreeInScopeB;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FunctionContextTest {
    @Inject
    private Three three;
    @Inject
    private ThreeInScopeA threeInScopeA;
    @Inject
    private ThreeInScopeB threeInScopeB;
    
    @BeforeEach
    void setup() {
        Injector injector = Guice.createInjector();
        injector.injectMembers(this);
    }
    
    @Test
    void testThreeInDefaultScope() {
        assertEquals(3, three.evaluate());
    }
    
    @Test
    void testThreeInScopeA() {
        assertEquals(5, threeInScopeA.evaluate());
    }
    
    @Test
    void testThreeInScopeB() {
        assertEquals(15, threeInScopeB.evaluate());
    }
}
