package com.regnosys.rosetta.generator.java.expression;

import com.regnosys.rosetta.generator.java.scoping.JavaIdentifierRepresentationService;
import com.regnosys.rosetta.generator.java.statement.JavaLocalVariableDeclarationStatement;
import com.regnosys.rosetta.generator.java.statement.JavaStatement;
import com.regnosys.rosetta.generator.java.statement.builder.JavaExpression;
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator;
import com.regnosys.rosetta.generator.java.types.JavaTypeUtil;
import com.regnosys.rosetta.generator.java.util.ImportManagerExtension;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.util.ExpressionParser;
import com.regnosys.rosetta.tests.util.ModelHelper;
import com.regnosys.rosetta.types.RObjectFactory;
import com.rosetta.util.DottedPath;
import com.rosetta.util.types.JavaType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.xtend2.lib.StringConcatenationClient;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class ExpressionGeneratorTest {

    @Inject
    private ExpressionGenerator expressionGenerator;

    @Inject
    private ExpressionParser expressionParser;

    @Inject
    private ImportManagerExtension importManagerExtension;

    @Inject
    private JavaTypeTranslator javaTypeTranslator;

    @Inject
    private RObjectFactory rObjectFactory;

    @Inject
    private ModelHelper modelHelper;

    @Inject
    private JavaDependencyProvider javaDependencyProvider;

    @Inject
    private JavaIdentifierRepresentationService javaIdentifierRepresentationService;

    @Inject
    private ExpressionScopeUtility scopeUtil;
    
    @Inject
    private JavaTypeUtil javaTypeUtil;

    private void assertJavaCode(String expectedCode, CharSequence expr, Class<?> expectedType) {
        assertJavaCode(expectedCode, expr, expectedType, List.of(), List.of());
    }

    private void assertJavaCode(String expectedCode, CharSequence expr, Class<?> expectedType, List<RosettaModel> context, Collection<? extends CharSequence> attrs) {
        assertJavaCode(expectedCode, expr, JavaType.from(expectedType), context, attrs);
    }

    private void assertJavaCode(String expectedCode, CharSequence expr, JavaType expectedType, List<RosettaModel> context, Collection<? extends CharSequence> attrs) {
        Attribute[] attributes = attrs.stream()
                .map(a -> expressionParser.parseAttribute(a, context))
                .toArray(Attribute[]::new);

        var parsedExpr = expressionParser.parseExpression(expr, context, attributes);
        var dependencies = javaDependencyProvider.javaDependencies(parsedExpr);

        var pkg = DottedPath.of("test", "ns");
        var scope = scopeUtil.createTestExpressionScope(pkg);
        var fileScope = scope.getFileScope();

        var runtimeScope = scope.createUniqueIdentifier("scope");
        List<JavaLocalVariableDeclarationStatement> dependencyStatements = dependencies.stream()
                .map(dep -> {
                    var identifier = scope.createIdentifier(javaIdentifierRepresentationService.toDependencyInstance(dep), StringUtils.uncapitalize(dep.getSimpleName()));
                    var expression = JavaExpression.from(new StringConcatenationClient() {
                    	@Override
                        protected void appendTo(TargetStringConcatenation target) {
                            target.append(runtimeScope);
                            target.append(".getInstance(");
                            target.append(dep);
                            target.append(".class)");
                        }
                    }, dep);
                    return new JavaLocalVariableDeclarationStatement(false, dep, identifier, expression);
                })
                .collect(Collectors.toList());

        List<JavaStatement> statements = new ArrayList<>();

        statements.addAll(
                Arrays.stream(attributes)
                        .map(attr -> rObjectFactory.buildRAttributeWithEnclosingType(null, attr))
                        .map(rAttr -> {
                            var metaType = javaTypeTranslator.toMetaJavaType(rAttr);
                            var identifier = scope.createIdentifier(rAttr, rAttr.getName());
                            return new JavaLocalVariableDeclarationStatement(false, metaType, identifier);
                        })
                        .toList()
        );
        var returnStmt = expressionGenerator.javaCode(parsedExpr, expectedType, null, scope).completeAsReturn();
        
        statements.add(returnStmt);
        
        var actualBody = statements.stream().reduce(JavaStatement::append).orElseThrow();

        var content = new StringConcatenationClient() {
            @Override
            protected void appendTo(TargetStringConcatenation sc) {
            	if (!dependencyStatements.isEmpty()) {
            		sc.append(new JavaLocalVariableDeclarationStatement(true, javaTypeUtil.RUNE_SCOPE, runtimeScope));
            		sc.append("\n");
	                for (int i = 0; i < dependencyStatements.size(); i++) {
	                    sc.append(dependencyStatements.get(i));
	                    if (i < dependencyStatements.size() - 1) {
	                        sc.append("\n");
	                    }
	                }
                    sc.append("\n\n");
                }
                actualBody.appendTo(sc);
            }
        };

        String actual = importManagerExtension
                                .buildClass(pkg, content, fileScope)
                                .replace("package test.ns;", "")
                                .replace("\r", "")
                                .replace("\t", "    ")
                                .trim() + "\n";

        assertEquals(expectedCode, actual);
    }

    @Test
    public void testDefaultWithMetaCoercion() {
        CharSequence expr = """
        foo default bar
        """;

        String expected = """
        import com.rosetta.model.lib.mapper.MapperS;
        import com.rosetta.model.metafields.FieldWithMetaString;
        
        
        {
            FieldWithMetaString foo;
            FieldWithMetaString bar;
            final FieldWithMetaString fieldWithMetaString = MapperS.of(foo).getOrDefault(bar);
            return fieldWithMetaString == null ? null : fieldWithMetaString.getValue();
        }
        """;

        assertJavaCode(
                expected,
                expr,
                String.class,
                List.of(),
                List.of("foo string (1..1) [metadata scheme]", "bar string (1..1) [metadata scheme]")
        );
    }

    @Test
    public void testFeatureCallToIncompatibleOverrideUsesCorrectGetter() {
        var context = modelHelper.parseRosettaWithNoIssues("""
        type Foo:
            attr number (1..1)
        
        type Bar extends Foo:
            override attr int (1..1)
        
        func Round:
          [codeImplementation]
            inputs:
                inp number (1..1)
            output:
                result int (1..1)
        """);

        String expected = """
        import com.rosetta.model.lib.expression.MapperMaths;
        import com.rosetta.model.lib.mapper.MapperS;
        import com.rosetta.test.model.Bar;
        import com.rosetta.test.model.functions.Round;
        import java.math.BigDecimal;
        import javax.inject.Inject;
        
        
        @Inject Round round;
        
        {
            Bar bar;
            return MapperMaths.<Integer, Integer, Integer>add(MapperS.of(bar).<Integer>map("getAttr", _bar -> _bar.getAttrOverriddenAsInteger()), MapperS.of(round.evaluate(MapperS.of(bar).<BigDecimal>map("getAttr", _bar -> _bar.getAttr()).get()))).get();
        }
        """;

        assertJavaCode(
                expected,
                "bar -> attr + Round(bar -> attr)",
                Integer.class,
                List.of(context),
                List.of("bar Bar (1..1)")
        );
    }

    @Test
    public void shouldEscapeStrings() {
        String expected = """
        return "Hello \\"world\\"!";
        """;
        assertJavaCode(expected, "\"Hello \\\"world\\\"!\"", String.class);
    }

    /**
     *  Foo -> attr1 > 5
     */
    @Test
    public void shouldGenerateGreaterThanExpression() {
        var context = modelHelper.parseRosettaWithNoIssues("""
        type Foo:
            attr1 number (1..1)
        """);

        String expected = """
        import com.rosetta.model.lib.expression.CardinalityOperator;
        import com.rosetta.model.lib.mapper.MapperS;
        import com.rosetta.test.model.Foo;
        import java.math.BigDecimal;
        
        import static com.rosetta.model.lib.expression.ExpressionOperators.*;
        
        {
            Foo foo;
            return greaterThan(MapperS.of(foo).<BigDecimal>map("getAttr1", _foo -> _foo.getAttr1()), MapperS.of(BigDecimal.valueOf(5)), CardinalityOperator.All);
        }
        """;

        assertJavaCode(
                expected,
                "foo -> attr1 > 5",
                javaTypeUtil.COMPARISON_RESULT,
                List.of(context),
                List.of("foo Foo (1..1)")
        );
    }

    /**
     *  Foo -> attr1 > 5 or Foo -> attr2 > 5
     */
    @Test
    public void shouldGenerateGreaterThanExpressionsWithOr1() {
        var context = modelHelper.parseRosettaWithNoIssues("""
        type Foo:
            attr1 number (1..1)
            attr2 number (1..1)
        """);

        String expected = """
        import com.rosetta.model.lib.expression.CardinalityOperator;
        import com.rosetta.model.lib.mapper.MapperS;
        import com.rosetta.test.model.Foo;
        import java.math.BigDecimal;
        
        import static com.rosetta.model.lib.expression.ExpressionOperators.*;
        
        {
            Foo foo;
            return greaterThan(MapperS.of(foo).<BigDecimal>map("getAttr1", _foo -> _foo.getAttr1()), MapperS.of(BigDecimal.valueOf(5)), CardinalityOperator.All).or(greaterThan(MapperS.of(foo).<BigDecimal>map("getAttr2", _foo -> _foo.getAttr2()), MapperS.of(BigDecimal.valueOf(5)), CardinalityOperator.All));
        }
        """;

        assertJavaCode(
                expected,
                "foo -> attr1 > 5 or foo -> attr2 > 5",
                javaTypeUtil.COMPARISON_RESULT,
                List.of(context),
                List.of("foo Foo (1..1)")
        );
    }

    /**
     *  Foo -> attr exists
     */
    @Test
    public void shouldGenerateExistsExpression() {
        var context = modelHelper.parseRosettaWithNoIssues("""
        type Foo:
            attr number (0..1)
        """);

        String expected = """
        import com.rosetta.model.lib.mapper.MapperS;
        import com.rosetta.test.model.Foo;
        import java.math.BigDecimal;
        
        import static com.rosetta.model.lib.expression.ExpressionOperators.*;
        
        {
            Foo foo;
            return exists(MapperS.of(foo).<BigDecimal>map("getAttr", _foo -> _foo.getAttr()));
        }
        """;

        assertJavaCode(
                expected,
                "foo -> attr exists",
                javaTypeUtil.COMPARISON_RESULT,
                List.of(context),
                List.of("foo Foo (1..1)")
        );
    }

    @Test
    public void shouldGenerateEnumValueRef() {
        var context = modelHelper.parseRosettaWithNoIssues("""
        type Foo:
            attr1 number (0..1)
            attr2 number (0..1)
            attr3 string (0..1)
            attr4 string (0..1)
        """);

        String expected = """
        import com.rosetta.model.lib.expression.CardinalityOperator;
        import com.rosetta.model.lib.mapper.MapperS;
        import com.rosetta.test.model.Foo;
        import java.math.BigDecimal;
        
        import static com.rosetta.model.lib.expression.ExpressionOperators.*;
        
        {
            Foo foo;
            return areEqual(MapperS.of(foo).<BigDecimal>map("getAttr1", _foo -> _foo.getAttr1()), MapperS.of(foo).<BigDecimal>map("getAttr2", _foo -> _foo.getAttr2()), CardinalityOperator.All).or(areEqual(MapperS.of(foo).<String>map("getAttr3", _foo -> _foo.getAttr3()), MapperS.of(foo).<String>map("getAttr4", _foo -> _foo.getAttr4()), CardinalityOperator.All));
        }
        """;

        assertJavaCode(
                expected,
                "(foo -> attr1 = foo -> attr2) or (foo -> attr3 = foo -> attr4)",
                javaTypeUtil.COMPARISON_RESULT,
                List.of(context),
                List.of("foo Foo (1..1)")
        );
    }
}