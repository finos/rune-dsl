package com.regnosys.rosetta.utils;

import com.google.common.collect.Lists;
import com.regnosys.rosetta.RosettaEcoreUtil;
import com.regnosys.rosetta.rosetta.RosettaFeature;
import com.regnosys.rosetta.rosetta.expression.ConstructorKeyValuePair;
import com.regnosys.rosetta.rosetta.expression.ExpressionFactory;
import com.regnosys.rosetta.rosetta.expression.RosettaConstructorExpression;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.types.RMetaAnnotatedType;
import com.regnosys.rosetta.types.RosettaTypeProvider;

import jakarta.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

public class ConstructorManagementService {
    @Inject
    private RosettaTypeProvider types;
    @Inject
    private RosettaEcoreUtil extensions;

    public void modifyConstructorWithAllAttributes(RosettaConstructorExpression constructor) {
        RosettaFeatureGroup rosettaFeatureGroup = groupConstructorFeatures(constructor);
        List<RosettaFeature> absentAttributes = rosettaFeatureGroup.getAbsentAttributes();

        absentAttributes.forEach(attr -> {
            ConstructorKeyValuePair constructorKeyValuePair = ExpressionFactory.eINSTANCE.createConstructorKeyValuePair();
            constructorKeyValuePair.setKey(attr);
            constructorKeyValuePair.setValue(ExpressionFactory.eINSTANCE.createListLiteral());
            constructor.getValues().add(constructorKeyValuePair);
        });
    }


    public void modifyConstructorWithRequiredAttributes(RosettaConstructorExpression constructor) {
        RosettaFeatureGroup rosettaFeatureGroup = groupConstructorFeatures(constructor);
        List<RosettaFeature> requiredAbsentAttributes = rosettaFeatureGroup.getAbsentRequiredAttributes();
        List<RosettaFeature> optionalAbsentAttributes = rosettaFeatureGroup.getAbsentOptionalAttributes();

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

    public RosettaFeatureGroup groupConstructorFeatures(RosettaConstructorExpression constructor) {
        if (constructor != null) {
            RMetaAnnotatedType metaAnnotatedType = types.getRMetaAnnotatedType(constructor);
            List<RosettaFeature> populatedFeatures = populatedFeaturesInConstructor(constructor);
            List<RosettaFeature> allFeatures = Lists.newArrayList(extensions.allFeatures(metaAnnotatedType.getRType(), constructor));
            return new RosettaFeatureGroup(populatedFeatures, allFeatures);
        }
        return new RosettaFeatureGroup();
    }

    private List<RosettaFeature> populatedFeaturesInConstructor(RosettaConstructorExpression constructor) {
        return constructor.getValues().stream()
                .map(ConstructorKeyValuePair::getKey)
                .collect(Collectors.toList());
    }

    public static class RosettaFeatureGroup {
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

        public List<RosettaFeature> getAbsentAttributes() {
            return all.stream().filter(x -> !populated.contains(x)).collect(Collectors.toList());
        }

        public List<RosettaFeature> getAbsentRequiredAttributes() {
            return all.stream()
                    .filter(x -> !populated.contains(x))
                    .filter(RosettaFeatureGroup::isRequired)
                    .collect(Collectors.toList());
        }

        public List<RosettaFeature> getAbsentOptionalAttributes() {
            return all.stream()
                    .filter(x -> !populated.contains(x))
                    .filter(not(RosettaFeatureGroup::isRequired))
                    .collect(Collectors.toList());
        }

        private static boolean isRequired(RosettaFeature it) {
            return !(it instanceof Attribute) || ((Attribute) it).getCard().getInf() != 0;
        }
    }
}