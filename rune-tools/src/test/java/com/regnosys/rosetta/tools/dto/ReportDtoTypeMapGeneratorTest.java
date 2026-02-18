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

import jakarta.inject.Inject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(InjectionExtension.class)
@InjectWith(RosettaTestInjectorProvider.class)
class ReportDtoTypeMapGeneratorTest {

    @Inject
    private ReportDtoTypeMapGenerator generator;

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
                baseNestedField1 string (0..1)
                baseNestedField2 string (0..1)
            
            type ExtendedNestedType:
                extendedNestedField1 string (0..1)
                extendedNestedField2 string (0..1)
            
            type ExtendedNestedType2:
                extendedNested2Field1 string (0..1)
                extendedNested2Field2 string (0..1)
            
            type BaseTypeReport:
                baseFieldWithNoRuleReference string (0..1)
                baseFieldWithNoRuleReference2 string (0..1)
                baseFieldWithRuleReference string (0..1)
                 [ruleReference SomeRule]
                baseFieldWithRuleReference2 string (0..1)
                 [ruleReference SomeRule]
                baseNestedType BaseNestedType (0..1)
                  [ruleReference for baseNestedField1 SomeRule]
            
            type ExtendedTypeReport extends BaseTypeReport:
                override baseFieldWithNoRuleReference2 string (0..1)
                  [ruleReference SomeRule]
                override baseFieldWithRuleReference string (0..1)
                 [ruleReference empty]
                extendedFieldWithNoRuleReference string (0..1)
                extendedFieldWithRuleReference string (0..1)
                 [ruleReference SomeRule]
                extendedNestedType ExtendedNestedType (0..1)
                  [ruleReference for extendedNestedField2 SomeRule]
                extendedNestedType2 ExtendedNestedType2 (0..1)
                  [ruleReference SomeRule]
            """, resourceSet);

        Multimap<RType, RAttribute> dtoTypeMap = generator.generateReportDtoTypeMap(List.of(model), DottedPath.of("test"));

        assertFalse(dtoTypeMap.isEmpty());

        Set<RType> dtoTypes = dtoTypeMap.keySet();
        assertEquals(4, dtoTypes.size());

        List<String> dtoTypeNames = dtoTypes.stream()
                .map(RType::getName)
                .toList();

        assertThat(dtoTypeNames, hasItems("ExtendedTypeReport", "BaseNestedType", "ExtendedNestedType", "ExtendedNestedType2"));

        dtoTypeMap.keySet().forEach(type -> {
            if (type.getName().equals("BaseNestedType")) {
                Set<RAttribute> rAttributes = new HashSet<>(dtoTypeMap.get(type));
                assertEquals(1, rAttributes.size());
                assertThat(rAttributes.stream().map(RAttribute::getName).toList(), hasItems("baseNestedField1"));
            }

            if (type.getName().equals("ExtendedNestedType")) {
                Set<RAttribute> rAttributes = new HashSet<>(dtoTypeMap.get(type));
                assertEquals(1, rAttributes.size());
                assertThat(rAttributes.stream().map(RAttribute::getName).toList(), hasItems("extendedNestedField2"));
            }

            if (type.getName().equals("ExtendedTypeReport")) {
                Set<RAttribute> rAttributes = new HashSet<>(dtoTypeMap.get(type));
                assertEquals(6, rAttributes.size());
                assertThat(rAttributes.stream().map(RAttribute::getName).toList(), hasItems("baseFieldWithRuleReference2", "baseNestedType", "baseFieldWithNoRuleReference2", "extendedFieldWithRuleReference", "extendedNestedType", "extendedNestedType2"));
            }
        });
    }
}