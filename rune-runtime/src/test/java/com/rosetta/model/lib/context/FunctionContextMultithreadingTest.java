package com.rosetta.model.lib.context;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.rosetta.model.lib.context.example.One;
import com.rosetta.model.lib.context.example.ScopeA;
import com.rosetta.model.lib.context.example.ScopeB;
import com.rosetta.model.lib.context.example.Two;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;

public class FunctionContextMultithreadingTest {
    @Inject
    private FunctionContext context;
    @Inject
    private ContextAwareProvider<One> oneProvider;
    @Inject
    private ContextAwareProvider<Two> twoProvider;
    
    @BeforeEach
    void setup() {
        Injector injector = Guice.createInjector();
        injector.injectMembers(this);
    }
    
    @Test
    void testStateIsCopiedCorrectlyToThreads() {
        context.runInScope(ScopeA.class, () -> {
            FunctionContextState state = context.copyStateOfCurrentThread();
            CompletableFuture<String> f1 = CompletableFuture.supplyAsync(() -> {
                context.setStateOfCurrentThread(state);
                return context.evaluateInScope(ScopeB.class, () -> getImplementationName(oneProvider) + getImplementationName(twoProvider));
            });
            CompletableFuture<String> f2 = CompletableFuture.supplyAsync(() -> {
                context.setStateOfCurrentThread(state);
                return getImplementationName(oneProvider) + getImplementationName(twoProvider);
            });
            CompletableFuture.allOf(f1, f2).join();
            
            String res1 = f1.join();
            String res2 = f2.join();
            String res3 = getImplementationName(oneProvider) + getImplementationName(twoProvider);

            Assertions.assertAll(
                    () -> Assertions.assertEquals("OneBTwoA", res1),
                    () -> Assertions.assertEquals("OneTwoA", res2),
                    () -> Assertions.assertEquals("OneTwoA", res3)
            );
        });
    }
    
    private String getImplementationName(ContextAwareProvider<?> provider) {
        return provider.get().getClass().getSimpleName();
    }
}
