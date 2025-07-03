package com.regnosys.rosetta.generator.java.scoping;

import com.regnosys.rosetta.generator.GeneratorScope;
import com.regnosys.rosetta.generator.TargetLanguageStringConcatenation;
import com.regnosys.rosetta.generator.java.types.RGeneratedJavaClass;
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.rosetta.util.DottedPath;
import com.rosetta.util.types.JavaClass;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtend2.lib.StringConcatenationClient;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@InjectWith(RosettaTestInjectorProvider.class)
@ExtendWith(InjectionExtension.class)
public class JavaScopingTest {
    @Test
    void testClassExtensionMethodClash() {
        JavaGlobalScope globalScope = new JavaGlobalScope();

        var foo = createClass("test.Foo");
        var fooScope = globalScope.createClassScopeAndRegisterIdentifier(foo);
        fooScope.createMethodScope("getType");
        
        var bar = createClass("test.Bar", foo);
        var barScope = globalScope.createClassScopeAndRegisterIdentifier(bar);
        barScope.createMethodScope("getType");

        // TODO Assertions.assertEquals("_getType", getActualName(barScope, barGetType));
    }
    
    private JavaClass<?> createClass(String fullyQualifiedName) {
        DottedPath fqn = DottedPath.splitOnDots(fullyQualifiedName);
        return RGeneratedJavaClass.create(JavaPackageName.escape(fqn.parent()), fqn.last(), Object.class);
    }
    private JavaClass<?> createClass(String fullyQualifiedName, JavaClass<?> superclass) {
        DottedPath fqn = DottedPath.splitOnDots(fullyQualifiedName);
        return RGeneratedJavaClass.create(JavaPackageName.escape(fqn.parent()), fqn.last(), superclass);
    }
    private String getActualName(GeneratorScope<?> scope, Object key) {
        var identifier = scope.getIdentifierOrThrow(key);
        var result = new TargetLanguageStringConcatenation();
        result.append(identifier);
        return result.toString();
    }
}
