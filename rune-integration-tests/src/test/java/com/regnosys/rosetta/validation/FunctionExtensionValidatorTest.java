package com.regnosys.rosetta.validation;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;

import java.util.List;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class FunctionExtensionValidatorTest extends AbstractValidatorTest {
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
				ERROR Func extension not allowed
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
				ERROR Cannot extend Foo more than once in scope
				ERROR Cannot extend Foo more than once in scope
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
				ERROR MyScope already defined
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
                ),"""
				ERROR ab is not the same
				ERROR b is not the same
				ERROR c is not the same
				ERROR d is not the same
				ERROR result is not the same
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
				ERROR can only call `super` when extending
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

    @Test
    void testCannotCallExtendedFunctionFromInsideScope() {
        assertIssues("""
				namespace test
				version "1"
				
				func Foo1:
				    output:
						result int (1..1)
					set result: 0
				
				func Foo2:
				    output:
						result int (1..1)
					set result: 0
				
				""", List.of("""
                    namespace test
                    scope MyScope
                    version "1"
                    
                    func Bar1 extends Foo1:
                        output:
                            result int (1..1)
                        set result: super()
                    
                    func Bar2 extends Foo2:
                        output:
                            result int (1..1)
                        set result: Bar1()
                    """
                ), """
                ERROR cannot call extension Bar1 from within scope
                """);
    }
}
