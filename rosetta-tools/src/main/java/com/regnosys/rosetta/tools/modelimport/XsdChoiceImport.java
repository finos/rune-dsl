package com.regnosys.rosetta.tools.modelimport;

import com.regnosys.rosetta.rosetta.ParametrizedRosettaType;
import com.regnosys.rosetta.rosetta.RosettaFactory;
import com.regnosys.rosetta.rosetta.RosettaTypeAlias;
import com.regnosys.rosetta.rosetta.TypeCall;
import com.regnosys.rosetta.rosetta.simple.Choice;
import com.regnosys.rosetta.rosetta.simple.ChoiceOption;
import com.regnosys.rosetta.rosetta.simple.SimpleFactory;
import jakarta.inject.Inject;
import org.xmlet.xsdparser.xsdelements.XsdAbstractElement;
import org.xmlet.xsdparser.xsdelements.XsdRestriction;
import org.xmlet.xsdparser.xsdelements.XsdSimpleType;

import java.util.List;
import java.util.stream.Collectors;

public class XsdChoiceImport extends AbstractXsdImport<XsdSimpleType, Choice> {

    private final XsdUtil util;

    @Inject
    public XsdChoiceImport(XsdUtil util) {
        super(XsdSimpleType.class);
        this.util = util;
    }

    @Override
    public List<XsdSimpleType> filterTypes(List<XsdAbstractElement> elements) {
        return super.filterTypes(elements).stream()
                .filter(util::isChoiceType)
                .collect(Collectors.toList());
    }

    @Override
    public Choice registerType(XsdSimpleType xsdType, RosettaXsdMapping typeMappings, ImportTargetConfig targetConfig) {
        Choice choiceType = SimpleFactory.eINSTANCE.createChoice();
        choiceType.setName(xsdType.getName());
        util.extractDocs(xsdType).ifPresent(choiceType::setDefinition);
        typeMappings.registerChoiceType(xsdType, choiceType);
        return choiceType;
    }

    @Override
    public void completeType(XsdSimpleType xsdType, RosettaXsdMapping typeMappings) {
        Choice choiceType = typeMappings.getRosettaTypeFromChoice(xsdType);

        for (XsdSimpleType subType : xsdType.getUnion().getUnionElements()) {
            TypeCall tc;
            XsdRestriction restriction = subType.getRestriction();
            if (subType.getRawName() == null) {
                String restrBase;
                if (subType.getCloneOf() == null) {
                    restrBase = restriction.getBaseAsBuiltInDataType().getName();
                } else {
                    restrBase = ((XsdSimpleType) subType.getCloneOf()).getRestriction().getBaseAsBuiltInDataType().getName();
                }

                tc = typeMappings.getRosettaTypeCallFromBuiltin(restrBase);

                ChoiceOption choiceOption = SimpleFactory.eINSTANCE.createChoiceOption();
                choiceOption.setTypeCall(tc);
                choiceOption.set_hardcodedName("ChoiceOption" + xsdType.getUnion().getUnionElements().indexOf(subType));
                choiceType.getAttributes().add(choiceOption);

            } else {
                RosettaTypeAlias t;
                if (subType.getCloneOf() == null) {
                    t = typeMappings.getRosettaTypeFromSimple(subType);
                } else {
                    t = typeMappings.getRosettaTypeFromSimple((XsdSimpleType) subType.getCloneOf());
                }

                tc = RosettaFactory.eINSTANCE.createTypeCall();
                tc.setType(t);

                ChoiceOption choiceOption = SimpleFactory.eINSTANCE.createChoiceOption();
                choiceOption.setTypeCall(tc);
                choiceOption.set_hardcodedName(t.getName());
                choiceType.getAttributes().add(choiceOption);
            }

            if (tc.getType() instanceof ParametrizedRosettaType) {
                util.addTypeArguments(tc, restriction);
            }
        }
    }
}
