package com.regnosys.rosetta.tools.dto;

import com.google.common.collect.Multimap;
import com.regnosys.rosetta.builtin.RosettaBuiltinsService;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.tests.RosettaTestInjectorProvider;
import com.regnosys.rosetta.types.RAttribute;
import com.regnosys.rosetta.types.RType;
import com.rosetta.util.DottedPath;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.testing.util.ParseHelper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.inject.Inject;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
class ReportDtoGeneratorTest {

    @Inject
    private ReportDtoGenerator generator;

    @Inject
    private XtextResourceSet resourceSet;

    @Inject
    private RosettaBuiltinsService builtins;

    @Inject
    private ParseHelper<RosettaModel> parseHelper;

    @Test
    void testDtoGeneration() throws Exception{
        resourceSet.getResource(builtins.basicTypesURI, true);
        resourceSet.getResource(builtins.annotationsURI, true);

        RosettaModel model = parseHelper.parse("""
            namespace test
            
            body Authority ReportBody
            corpus Regullation ReportCorpus1
            corpus Regullation ReportCorpus2
        
            report ReportBody ReportCorpus1 ReportCorpus2 in T+1
                from ReportInstruction
                when IsValid
                with type ExtendedTypeReport
        
            eligibility rule IsValid from ReportInstruction:
                filter True
        
            type ReportInstruction:
                inputField string (1..1)
        
            reporting rule SomeRule from ReportInstruction:
                extract inputField
        
            type BaseNestedType:
                baseField1 string (0..1)
                baseField2 string (0..1)
        
            type ExtendedNestedType:
                extendedField1 string (0..1)
                extendedField2 string (0..1)
        
            type BaseTypeReport:
                baseFieldWithNoRuleReference string (0..1)
                baseFieldWithNoRuleReference2 string (0..1)
                baseFieldWithRuleReference string (0..1)
                 [ruleReference SomeRule]
                baseFieldWithRuleReference2 string (0..1)
                 [ruleReference SomeRule]
                baseNestedType BaseNestedType (0..1)
                  [ruleReference for baseField1 SomeRule]
        
            type ExtendedTypeReport extends BaseTypeReport:
                override baseFieldWithNoRuleReference2 string (0..1)
                  [ruleReference SomeRule]
                override baseFieldWithRuleReference string (0..1)
                 [ruleReference empty]
                extendedFieldWithNoRuleReference string (0..1)
                extendedFieldWithRuleReference string (0..1)
                 [ruleReference SomeRule]
                extendedNestedType ExtendedNestedType (0..1)
                  [ruleReference for extendedField2 SomeRule]
            """, resourceSet);

        Multimap<RType, RAttribute> test = generator.generateReportDtos(List.of(model), DottedPath.of("test"));

        assertFalse(test.isEmpty());
    }
}