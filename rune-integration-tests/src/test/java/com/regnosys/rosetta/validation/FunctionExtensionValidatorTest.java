package com.regnosys.rosetta.validation;

import com.google.inject.Module;
import com.google.inject.util.Modules;
import com.regnosys.rosetta.config.file.RosettaConfigurationFileProvider;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;

import java.util.List;

@ExtendWith(InjectionExtension.class)
@InjectWith(FunctionExtensionValidatorTest.RosettaTestInjectorWithScopesEnabledProvider.class)
public class FunctionExtensionValidatorTest extends AbstractValidatorTest {
    // Enable scopes feature for this test
    public static class RosettaTestInjectorWithScopesEnabledProvider extends RosettaTestInjectorProvider {
        @Override
        protected Module createRuntimeModule() {
            Module base = super.createRuntimeModule();
            return Modules.override(base).with(
                    binder -> binder.bind(RosettaConfigurationFileProvider.class)
                            .toInstance(RosettaConfigurationFileProvider.createFromClasspath("rosetta-config-with-enabled-scopes.yml")));
        }
    }
    
    @Test
    void testCyclesResultInAnError() {
        assertIssues("""
				namespace test
				scope MyScope
				version "1"
				
				func Foo extends Bar:
					output:
						result int (1..1)
					set result: 0
				
				func Bar extends Foo:
					output:
						result int (1..1)
					set result: 42
				""", """
                ERROR (null) 'Cyclic extension: Foo extends Bar extends Foo' at 5:18, length 3, on Function
                ERROR (null) 'Cyclic extension: Bar extends Foo extends Bar' at 10:18, length 3, on Function
                """);
    }
    
	@Test
	void testFunctionExtensionOnlyAllowedInScopedFile() {
		assertIssues("""
				namespace test
				version "1"
				
				func Foo:
					output:
						result int (1..1)
					set result: 0
				
				func Bar extends Foo:
					output:
						result int (1..1)
					set result: 42
				""", """
				ERROR (null) 'You can only extend a function in a file with a scope' at 9:18, length 3, on Function
				""");
	}

    @Test
    void testMayOnlyExtendFunctionOnceInSingleScope() {
        assertIssues("""
				namespace test
				scope MyScope
				version "1"
				
				func Bar extends Foo:
					output:
						result int (1..1)
					set result: 42
				
				func Qux extends Foo:
					output:
						result int (1..1)
					set result: -1
				""",
                List.of("""
                    namespace test
                    version "1"
                    
                    func Foo:
                        output:
                            result int (1..1)
                        set result: 0
                    """
                ),"""
                ERROR (null) 'Function 'Foo' is extended multiple times in scope MyScope' at 5:18, length 3, on Function
                ERROR (null) 'Function 'Foo' is extended multiple times in scope MyScope' at 10:18, length 3, on Function
                """);
    }

    @Test
    void testScopeNameMustBeUnique() {
        assertIssues("""
				namespace test
				scope MyScope
				version "1"
				""", List.of("""
                    namespace test
                    scope MyScope
                    version "1"
                    """
                ), """
				ERROR (null) 'Duplicate scope 'MyScope' in namespace 'test'' at 2:7, length 7, on RosettaScope
				""");
    }
	
	@Test
	void testFunctionExtensionInputsAndOutputMustEqualOriginalInputsAndOutput() {
		assertIssues("""
				namespace test
				scope MyScope
				version "1"
				
				func Bar extends Foo:
				    inputs:
				        ab int (1..1)
				        b U (1..1)
				        c string (0..1)
				        d int (0..1)
					output:
						result int (1..1)
					set result: 42
				""",
                List.of("""
                    namespace test
                    version "1"
                    
                    type T:
                    type U extends T:
                    
                    func Foo:
                        inputs:
                            a int (0..1)
                            b T (1..1)
                            c string (0..1)
                                [metadata scheme]
                        output:
                            result number (1..1)
                        set result: 0
                    """
                ), """
                ERROR (RosettaIssueCodes.changedExtendedFunctionParameters) 'Input ab does not match the original input in Foo' at 7:9, length 13, on Function
                ERROR (RosettaIssueCodes.changedExtendedFunctionParameters) 'Input b does not match the original input in Foo' at 8:9, length 10, on Function
                ERROR (RosettaIssueCodes.changedExtendedFunctionParameters) 'Input c does not match the original input in Foo' at 9:9, length 15, on Function
                ERROR (RosettaIssueCodes.changedExtendedFunctionParameters) 'Too many inputs. The original function Foo only defines 3 inputs.' at 10:9, length 12, on Function
                ERROR (RosettaIssueCodes.changedExtendedFunctionParameters) 'Output result does not match the original output in Foo' at 12:3, length 17, on Function
                """);
	}

    @Test
    void testCannotCallSuperInRegularFunction() {
        assertIssues("""
				namespace test
				version "1"
				
				func Foo:
					output:
						result int (1..1)
					set result: super()
				""","""
				ERROR (null) 'Calling `super` is only allowed when extending a function' at 7:14, length 5, on RosettaSuperCall
				""");
    }

    @Test
    void testCanCallExtendedFunctionFromOutsideScope() {
        assertNoIssues("""
				namespace test
				version "1"
				
				func Foo:
				    output:
						result int (1..1)
					set result: 0
				
				func Qux:
					output:
						result int (1..1)
					set result: Bar()
				
				""", List.of("""
                    namespace test
                    scope MyScope
                    version "1"
                    
                    func Bar extends Foo:
                        output:
                            result int (1..1)
                        set result: super()
                    """
        ));
    }
}
