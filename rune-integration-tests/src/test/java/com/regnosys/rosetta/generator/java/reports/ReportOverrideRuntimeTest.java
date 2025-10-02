package com.regnosys.rosetta.generator.java.reports;

import com.google.common.collect.Maps;
import com.regnosys.rosetta.generator.java.function.FunctionGeneratorHelper;
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.tests.util.CodeGeneratorTestHelper;
import com.rosetta.model.lib.RosettaModelObject;
import com.rosetta.util.DottedPath;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import static java.nio.file.Files.readString;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
public class ReportOverrideRuntimeTest {

    @Inject
    FunctionGeneratorHelper functionGeneratorHelper;
    @Inject
    CodeGeneratorTestHelper generatorTestHelper;

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void replicateOverrideBug() throws IOException {
        Path resourceDir = Path.of("src/test/resources/report-override-runtime-test");
        var code = generatorTestHelper.generateCode(
                readString(resourceDir.resolve("base.layer.rosetta")),
                readString(resourceDir.resolve("cde.layer.price.rosetta")),
                readString(resourceDir.resolve("cde.layer.rosetta")),
                readString(resourceDir.resolve("common.layer.rosetta")),
                readString(resourceDir.resolve("reg.rosetta")),
                readString(resourceDir.resolve("ext.rosetta")));

        // not sure why I need to do this
        Map<String, String> code2 = Maps.filterKeys(code, k -> !k.endsWith("package-info"));

        var classes = generatorTestHelper.compileToClasses(code2);

        Class<Enum> tradeEnum = (Class<Enum>) classes.get("base.layer.TradeEnum");
        Class<Enum> notationEnum = (Class<Enum>) classes.get("cde.layer.price.NotationEnum");
        RosettaModelObject modelObject = generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("base.layer"), "Instruction",
                Map.of("id", 999, "trade", Enum.valueOf(tradeEnum, "A")));

        var expectedRegTradeReportFuncResult = generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("reg"), "RegReport", Map.of("notation", Enum.valueOf(notationEnum, "X")));

        var regTradeReportFunc = functionGeneratorHelper.createFunc(classes, "RegTradeReportFunction", DottedPath.of("reg.reports"));
        var regTradeReportFuncResult = functionGeneratorHelper.invokeFunc(regTradeReportFunc, modelObject.getType(), modelObject);
        assertEquals(expectedRegTradeReportFuncResult, regTradeReportFuncResult);

        var expectedExtRegTradeReportFuncResult = generatorTestHelper.createInstanceUsingBuilder(classes, DottedPath.splitOnDots("ext"), "ExtRegReport", Map.of("notation", Enum.valueOf(notationEnum, "X")));

        var extRegTradeReportFunc = functionGeneratorHelper.createFunc(classes, "RegTradeExtReportFunction", DottedPath.of("ext.reports"));
        var extRegTradeReportFuncResult = functionGeneratorHelper.invokeFunc(extRegTradeReportFunc, modelObject.getType(), modelObject);
        assertEquals(expectedExtRegTradeReportFuncResult, extRegTradeReportFuncResult);
    }
}
