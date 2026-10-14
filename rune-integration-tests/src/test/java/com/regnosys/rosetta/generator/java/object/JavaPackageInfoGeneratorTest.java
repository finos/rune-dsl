package com.regnosys.rosetta.generator.java.object;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;

import javax.inject.Inject;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class JavaPackageInfoGeneratorTest {

	@Inject
	private CodeGeneratorTestHelper generatorTestHelper;

	@ParameterizedTest(name = "{0}")
	@CsvSource({
			"demo.fpml.amounts, demo.fpml.amounts",
			"demo.fpml.^enum,  demo.fpml._enum",
			"demo.^class.fpml, demo._class.fpml",
			"demo.fpml.int,    demo.fpml._int",
			"demo.fpml.^true,  demo.fpml._true"
	})
	void shouldGeneratePackageInfoInEscapedPackage(String namespace, String javaPackage) {
		Map<String, String> code = generatorTestHelper.generateCode("""
				namespace %s : <"desc">

				enum AmountAdjustmentEnum:
					Increase
					Decrease
				""".formatted(namespace));

		String packageInfo = code.get(javaPackage + ".package-info");
		assertNotNull(packageInfo, () -> "No package-info generated for " + javaPackage + "; generated: " + code.keySet());
		assertThat(packageInfo, containsString("package " + javaPackage + ";"));
		assertNotNull(code.get(javaPackage + ".AmountAdjustmentEnum"));
		assertFalse(code.keySet().stream().anyMatch(k -> k.endsWith("package-info") && !k.equals(javaPackage + ".package-info")),
				() -> "Unexpected package-info generated: " + code.keySet());

		generatorTestHelper.compileToClasses(code);
	}
}
