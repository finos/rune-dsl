package com.regnosys.rosetta.ide.quickfix;

import com.google.common.collect.Lists;
import com.regnosys.rosetta.RosettaEcoreUtil;
import com.regnosys.rosetta.rosetta.RosettaFeature;
import com.regnosys.rosetta.rosetta.expression.ConstructorKeyValuePair;
import com.regnosys.rosetta.rosetta.expression.ExpressionFactory;
import com.regnosys.rosetta.rosetta.expression.RosettaConstructorExpression;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.simple.ChoiceOption;
import com.regnosys.rosetta.types.RMetaAnnotatedType;
import com.regnosys.rosetta.types.RosettaTypeProvider;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

public class ConstructorQuickFix {
    @Inject
    private RosettaTypeProvider types;
    @Inject
    private RosettaEcoreUtil extensions;

    public void modifyConstructorWithAllAttributes(RosettaConstructorExpression constructor) {
        RosettaFeatureGroup rosettaFeatureGroup = groupConstructorFeatures(constructor);
        List<RosettaFeature> allAttributes = rosettaFeatureGroup.allAttributes();

        allAttributes.forEach(attr -> {
            ConstructorKeyValuePair constructorKeyValuePair = ExpressionFactory.eINSTANCE.createConstructorKeyValuePair();
            constructorKeyValuePair.setKey(attr);
            constructorKeyValuePair.setValue(ExpressionFactory.eINSTANCE.createListLiteral());
            constructor.getValues().add(constructorKeyValuePair);
        });
    }


    public void modifyConstructorWithMandatoryAttributes(RosettaConstructorExpression constructor) {
        RosettaFeatureGroup rosettaFeatureGroup = groupConstructorFeatures(constructor);
        List<RosettaFeature> requiredAbsentAttributes = rosettaFeatureGroup.requiredAbsentAttributes();
        List<RosettaFeature> optionalAbsentAttributes = rosettaFeatureGroup.optionalAbsentAttributes();

        requiredAbsentAttributes.forEach(attr -> {
            ConstructorKeyValuePair constructorKeyValuePair = ExpressionFactory.eINSTANCE.createConstructorKeyValuePair();
            constructorKeyValuePair.setKey(attr);
            constructorKeyValuePair.setValue(ExpressionFactory.eINSTANCE.createListLiteral());
            constructor.getValues().add(constructorKeyValuePair);
        });
        if (!optionalAbsentAttributes.isEmpty()) {
            constructor.setImplicitEmpty(true);
        }
    }

    private RosettaFeatureGroup groupConstructorFeatures(RosettaConstructorExpression constructor) {
        if (constructor != null) {
            RMetaAnnotatedType metaAnnotatedType = types.getRMetaAnnotatedType(constructor);
            if (metaAnnotatedType != null && metaAnnotatedType.getRType() != null) {
                List<RosettaFeature> populatedFeatures = populatedFeaturesInConstructor(constructor);
                List<RosettaFeature> allFeatures = Lists.newArrayList(extensions.allFeatures(metaAnnotatedType.getRType(), constructor));
                return new RosettaFeatureGroup(populatedFeatures, allFeatures);
            }
        }
        return new RosettaFeatureGroup();
    }

    private List<RosettaFeature> populatedFeaturesInConstructor(RosettaConstructorExpression constructor) {
        return constructor.getValues().stream()
                .map(ConstructorKeyValuePair::getKey)
                .collect(Collectors.toList());
    }

    private static class RosettaFeatureGroup {
        private final List<? extends RosettaFeature> populated;
        private final List<? extends RosettaFeature> all;

        private RosettaFeatureGroup() {
            this.populated = Collections.emptyList();
            this.all = Collections.emptyList();
        }

        private RosettaFeatureGroup(List<RosettaFeature> populated, List<RosettaFeature> all) {
            this.populated = populated;
            this.all = all;
        }

        private List<RosettaFeature> allAttributes() {
            return all.stream().filter(x -> !populated.contains(x)).collect(Collectors.toList());
        }

        private List<RosettaFeature> requiredAbsentAttributes() {
            return all.stream()
                    .filter(x -> !populated.contains(x))
                    .filter(RosettaFeatureGroup::isRequired)
                    .collect(Collectors.toList());
        }

        private List<RosettaFeature> optionalAbsentAttributes() {
            return all.stream()
                    .filter(x -> !populated.contains(x))
                    .filter(not(RosettaFeatureGroup::isRequired))
                    .collect(Collectors.toList());
        }

        private static boolean isRequired(RosettaFeature it) {
            return !(it instanceof Attribute) || ((Attribute) it).getCard().getInf() != 0 || (it instanceof ChoiceOption);
        }
    }
}
