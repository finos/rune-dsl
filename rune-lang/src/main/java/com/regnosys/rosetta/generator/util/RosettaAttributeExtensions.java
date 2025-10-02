package com.regnosys.rosetta.generator.util;

import com.regnosys.rosetta.RosettaEcoreUtil;
import com.regnosys.rosetta.generator.object.ExpandedAttribute;
import com.regnosys.rosetta.generator.object.ExpandedSynonym;
import com.regnosys.rosetta.generator.object.ExpandedSynonymValue;
import com.regnosys.rosetta.generator.object.ExpandedType;
import com.regnosys.rosetta.rosetta.*;
import com.regnosys.rosetta.rosetta.simple.Attribute;
import com.regnosys.rosetta.rosetta.simple.Data;
import com.regnosys.rosetta.scoping.RosettaScopeProvider;

import java.util.*;

/**
 * Deprecated — Use RosettaExtensions instead.
 * <p>
 * Note that these methods will add a "meta" attribute if the data type has annotations.
 * "Can't inject as used in rosetta-translate and daml directly" — where noted, an instance
 * of RosettaEcoreUtil is created ad-hoc instead of DI.
 */
@Deprecated
public class RosettaAttributeExtensions {
    public static boolean cardinalityIsSingleValue(ExpandedAttribute attribute) {
        return attribute.getSup() == 1;
    }

    public static boolean cardinalityIsListValue(ExpandedAttribute attribute) {
        return !cardinalityIsSingleValue(attribute);
    }

    /**
     * Note that these methods will add a "meta" attribute if the data type has annotations
     */
    public static List<ExpandedAttribute> getExpandedAttributes(Data data) {
        List<ExpandedAttribute> result = new ArrayList<>();
        for (Attribute a : data.getAttributes()) {
            result.add(toExpandedAttribute(a));
        }
        result.addAll(additionalAttributes(data));
        return result;
    }

    public static List<ExpandedAttribute> expandedAttributesPlus(Data data) {
        List<ExpandedAttribute> atts = getExpandedAttributes(data);
        if (hasSuperType(data)) {
            List<ExpandedAttribute> attsWithSuper = expandedAttributesPlus(data.getSuperType());
            List<ExpandedAttribute> merged = new ArrayList<>();
            // Override by name if present on this type
            for (ExpandedAttribute s : attsWithSuper) {
                ExpandedAttribute overridden = atts.stream().filter(att -> Objects.equals(att.getName(), s.getName())).findFirst().orElse(null);
                merged.add(overridden != null ? overridden : s);
            }
            for (ExpandedAttribute a : atts) {
                if (!containsByIdentityOrName(merged, a)) merged.add(a);
            }
            return merged;
        }
        return atts;
    }

    private static boolean hasSuperType(Data d) {
        // In Xtend code this was a property "hasSuperType"; emulate it
        return d.getSuperType() != null;
    }

    private static boolean containsByIdentityOrName(List<ExpandedAttribute> list, ExpandedAttribute candidate) {
        for (ExpandedAttribute e : list) {
            if (e == candidate) return true;
            if (Objects.equals(e.getName(), candidate.getName())) return true;
        }
        return false;
    }

    private static List<ExpandedAttribute> additionalAttributes(Data data) {
        List<ExpandedAttribute> res = new ArrayList<>();
        // Can't inject as used in rosetta-translate and daml directly
        RosettaEcoreUtil rosExt = new RosettaEcoreUtil();
        if (rosExt.hasKeyedAnnotation(data)) {
            res.add(new ExpandedAttribute(
                    "meta",
                    data.getName(),
                    provideMetaFieldsType(data),
                    null,
                    false,
                    0,
                    1,
                    false,
                    Collections.emptyList(),
                    "",
                    Collections.emptyList(),
                    false,
                    Collections.emptyList()
            ));
        }
        return res;
    }

    public static String METAFIELDS_CLASS_NAME = "MetaFields";
    public static String META_AND_TEMPLATE_FIELDS_CLASS_NAME = "MetaAndTemplateFields";

    // A simple cache keyed by Data identity (these EObjects are identity-stable within a resource)
    private static Map<Data, ExpandedType> META_FIELDS_CACHE = Collections.synchronizedMap(new IdentityHashMap<>());

    private static ExpandedType provideMetaFieldsType(Data data) {
        ExpandedType cached = META_FIELDS_CACHE.get(data);
        if (cached != null) return cached;

        // Build a synthetic type in the lib namespace
        RosettaModel rosModel = RosettaFactory.eINSTANCE.createRosettaModel();
        rosModel.setName(RosettaScopeProvider.LIB_NAMESPACE);
        // Can't inject as used in rosetta-translate and daml directly
        RosettaEcoreUtil rosExt = new RosettaEcoreUtil();
        String name = rosExt.hasTemplateAnnotation(data) ? META_AND_TEMPLATE_FIELDS_CLASS_NAME : METAFIELDS_CLASS_NAME;

        ExpandedType computed = new ExpandedType(rosModel, name, true, false, false);
        META_FIELDS_CACHE.put(data, computed);
        return computed;
    }

    public static List<ExpandedAttribute> getExpandedAttributes(RosettaEnumeration rosettaEnum) {
        List<ExpandedAttribute> result = new ArrayList<>();
        for (RosettaEnumValue v : rosettaEnum.getEnumValues()) {
            result.add(expandedEnumAttribute(v));
        }
        return result;
    }

    public static ExpandedAttribute expandedEnumAttribute(RosettaEnumValue value) {
        return new ExpandedAttribute(
                value.getName(),
                value.getEnumeration().getName(),
                null,
                null,
                false,
                0,
                0,
                false,
                value.getEnumSynonyms().stream().map(RosettaAttributeExtensions::toExpandedSynonym).toList(),
                value.getDefinition(),
                value.getReferences(),
                true,
                Collections.emptyList()
        );
    }

    public static ExpandedSynonym toExpandedSynonym(RosettaEnumSynonym syn) {
        return new ExpandedSynonym(
                syn.getSources(),
                Collections.singletonList(new ExpandedSynonymValue(syn.getSynonymValue(), null, 0, false)),
                new ArrayList<>(),
                null,
                Collections.emptyList(),
                null,
                null,
                null,
                syn.getPatternMatch(),
                syn.getPatternReplace(),
                syn.isRemoveHtml()
        );
    }

    public static boolean isList(ExpandedAttribute a) {
        return cardinalityIsListValue(a);
    }

    public static List<ExpandedSynonym> toRosettaExpandedSynonym(Attribute attr, int index) {
        List<ExpandedSynonym> result = new ArrayList<>();
        attr.getSynonyms().stream()
                .filter(s -> s.getBody() != null && s.getBody().getMetaValues() != null && s.getBody().getMetaValues().size() > index)
                .forEach(s -> {
                    List<ExpandedSynonymValue> values = metaSynValue(
                            toArrayOrEmpty(s.getBody().getValues()),
                            s.getBody().getMetaValues().get(index)
                    );
                    List<ExpandedSynonymValue> metaValues = s.getBody().getMetaValues().stream()
                            .map(mv -> new ExpandedSynonymValue(mv, null, 1, true))
                            .toList();
                    result.add(new ExpandedSynonym(
                            s.getSources(),
                            values,
                            s.getBody().getHints(),
                            s.getBody().getMerge(),
                            metaValues,
                            s.getBody().getMappingLogic(),
                            s.getBody().getMapper(),
                            s.getBody().getFormat(),
                            s.getBody().getPatternMatch(),
                            s.getBody().getPatternReplace(),
                            s.getBody().isRemoveHtml()
                    ));
                });
        return result;
    }

    public static List<ExpandedSynonym> toRosettaExpandedSynonym(List<RosettaSynonymSource> sources,
                                                                 List<RosettaExternalSynonym> externalSynonyms,
                                                                 int index) {
        List<ExpandedSynonym> result = new ArrayList<>();
        for (RosettaExternalSynonym s : externalSynonyms) {
            if (s.getBody() == null || s.getBody().getMetaValues() == null) continue;
            if (s.getBody().getMetaValues().size() <= index) continue;

            List<ExpandedSynonymValue> values = metaSynValue(
                    toArrayOrEmpty(s.getBody().getValues()),
                    s.getBody().getMetaValues().get(index)
            );
            if (values.isEmpty()) continue;

            List<ExpandedSynonymValue> metaValues = s.getBody().getMetaValues().stream()
                    .map(mv -> new ExpandedSynonymValue(mv, null, 1, true))
                    .toList();

            result.add(new ExpandedSynonym(
                    sources,
                    values,
                    s.getBody().getHints(),
                    s.getBody().getMerge(),
                    metaValues,
                    s.getBody().getMappingLogic(),
                    s.getBody().getMapper(),
                    s.getBody().getFormat(),
                    s.getBody().getPatternMatch(),
                    s.getBody().getPatternReplace(),
                    s.getBody().isRemoveHtml()
            ));
        }
        return result;
    }

    public static ExpandedAttribute toExpandedAttribute(Attribute attr) {
        // Build meta attributes for annotation refs on this attribute
        List<ExpandedAttribute> metas = new ArrayList<>();
        List<com.regnosys.rosetta.rosetta.simple.AnnotationRef> annotations = attr.getAnnotations();
        for (int i = 0; i < annotations.size(); i++) {
            com.regnosys.rosetta.rosetta.simple.AnnotationRef annoRef = annotations.get(i);
            Attribute annoAttr = annoRef.getAttribute();
            if (annoAttr != null && annoAttr.getTypeCall() != null && annoAttr.getTypeCall().getType() != null) {
                metas.add(new ExpandedAttribute(
                        annoAttr.getName(),
                        annoRef.getAnnotation().getName(),
                        toExpandedType(annoAttr.getTypeCall().getType()),
                        annoAttr.getTypeCall(),
                        false,
                        0,
                        1,
                        false,
                        toRosettaExpandedSynonym(attr, i),
                        attr.getDefinition(),
                        attr.getReferences(),
                        false,
                        Collections.emptyList()
                ));
            }
        }

        return new ExpandedAttribute(
                attr.getName(),
                ((RosettaType) attr.eContainer()).getName(),
                attr.getTypeCall() != null && attr.getTypeCall().getType() != null ? toExpandedType(attr.getTypeCall().getType()) : null,
                attr.getTypeCall(),
                false,
                attr.getCard().getInf(),
                attr.getCard().getSup(),
                attr.getCard().isUnbounded(),
                toRosettaExpandedSynonyms(attr.getSynonyms(), -1),
                attr.getDefinition(),
                attr.getReferences(),
                isEnumeration(attr),
                metas
        );
    }

    public static ExpandedType toExpandedType(RosettaType type) {
        return new ExpandedType(
                type.getModel(),
                type.getName(),
                type instanceof Data,
                type instanceof RosettaEnumeration,
                type instanceof RosettaMetaType
        );
    }

    public static List<ExpandedSynonym> toRosettaExpandedSynonyms(List<RosettaSynonym> synonyms, int meta) {
        List<ExpandedSynonym> result = new ArrayList<>();
        if (meta < 0) {
            for (RosettaSynonym s : synonyms) {
                List<ExpandedSynonymValue> values = s.getBody() == null || s.getBody().getValues() == null
                        ? null
                        : s.getBody().getValues().stream().map(v -> new ExpandedSynonymValue(v.getName(), v.getPath(), v.getMaps(), false)).toList();
                List<ExpandedSynonymValue> metaValues = s.getBody() == null ? Collections.emptyList()
                        : s.getBody().getMetaValues().stream().map(mv -> new ExpandedSynonymValue(mv, null, 1, true)).toList();

                result.add(new ExpandedSynonym(
                        s.getSources(),
                        values,
                        s.getBody() == null ? new ArrayList<>() : s.getBody().getHints(),
                        s.getBody() == null ? null : s.getBody().getMerge(),
                        metaValues,
                        s.getBody() == null ? null : s.getBody().getMappingLogic(),
                        s.getBody() == null ? null : s.getBody().getMapper(),
                        s.getBody() == null ? null : s.getBody().getFormat(),
                        s.getBody() == null ? null : s.getBody().getPatternMatch(),
                        s.getBody() == null ? null : s.getBody().getPatternReplace(),
                        s.getBody() != null && s.getBody().isRemoveHtml()
                ));
            }
        } else {
            for (RosettaSynonym s : synonyms) {
                if (s.getBody() == null || s.getBody().getMetaValues() == null || s.getBody().getMetaValues().size() <= meta)
                    continue;

                List<ExpandedSynonymValue> values = metaSynValue(
                        toArrayOrEmpty(s.getBody().getValues()),
                        s.getBody().getMetaValues().get(meta)
                );
                List<ExpandedSynonymValue> metaValues = s.getBody().getMetaValues().stream()
                        .map(mv -> new ExpandedSynonymValue(mv, null, 1, true))
                        .toList();

                result.add(new ExpandedSynonym(
                        s.getSources(),
                        values,
                        s.getBody().getHints(),
                        s.getBody().getMerge(),
                        metaValues,
                        s.getBody().getMappingLogic(),
                        s.getBody().getMapper(),
                        s.getBody().getFormat(),
                        s.getBody().getPatternMatch(),
                        s.getBody().getPatternReplace(),
                        s.getBody().isRemoveHtml()
                ));
            }
        }
        return result;
    }

    public static List<ExpandedSynonymValue> metaSynValue(RosettaSynonymValueBase[] values, String meta) {
        if (values == null || values.length == 0) {
            return List.of(new ExpandedSynonymValue(meta, null, 2, true));
        }
        List<ExpandedSynonymValue> result = new ArrayList<>();
        for (RosettaSynonymValueBase v : values) {
            String path = v.getPath() == null ? v.getName() : v.getPath() + "->" + v.getName();
            String name = meta;
            result.add(new ExpandedSynonymValue(name, path, v.getMaps(), true));
        }
        return result;
    }

    // Overload present in Xtend via dispatch with empty default
    public static ExpandedSynonym toRosettaExpandedSynonym(RosettaSynonymBase synonym) {
        return null;
    }

    public static ExpandedSynonym toRosettaExpandedSynonym(RosettaSynonym syn) {
        List<ExpandedSynonymValue> values = syn.getBody() == null || syn.getBody().getValues() == null
                ? null
                : syn.getBody().getValues().stream().map(v -> new ExpandedSynonymValue(v.getName(), v.getPath(), v.getMaps(), false)).toList();
        List<ExpandedSynonymValue> metaValues = syn.getBody() == null ? Collections.emptyList()
                : syn.getBody().getMetaValues().stream().map(mv -> new ExpandedSynonymValue(mv, null, 1, true)).toList();

        return new ExpandedSynonym(
                syn.getSources(),
                values,
                syn.getBody() == null ? new ArrayList<>() : syn.getBody().getHints(),
                syn.getBody() == null ? null : syn.getBody().getMerge(),
                metaValues,
                syn.getBody() == null ? null : syn.getBody().getMappingLogic(),
                syn.getBody() == null ? null : syn.getBody().getMapper(),
                syn.getBody() == null ? null : syn.getBody().getFormat(),
                syn.getBody() == null ? null : syn.getBody().getPatternMatch(),
                syn.getBody() == null ? null : syn.getBody().getPatternReplace(),
                syn.getBody() != null && syn.getBody().isRemoveHtml()
        );
    }

    public static ExpandedSynonym toRosettaExpandedSynonym(RosettaExternalSynonym syn) {
        RosettaExternalRegularAttribute externalAttr = (RosettaExternalRegularAttribute) syn.eContainer();
        RosettaExternalClass externalClass = (RosettaExternalClass) externalAttr.eContainer();
        RosettaExternalSynonymSource externalSynonymSource = (RosettaExternalSynonymSource) externalClass.eContainer();
        List<RosettaSynonymSource> superSynonyms = externalSynonymSource.getSuperSynonymSources();

        List<RosettaSynonymSource> sources = new ArrayList<>();
        sources.add(externalSynonymSource);
        if (superSynonyms != null) {
            sources.addAll(superSynonyms);
        }

        List<ExpandedSynonymValue> values = syn.getBody() == null || syn.getBody().getValues() == null
                ? null
                : syn.getBody().getValues().stream().map(v -> new ExpandedSynonymValue(v.getName(), v.getPath(), v.getMaps(), false)).toList();
        List<ExpandedSynonymValue> metaValues = syn.getBody() == null ? Collections.emptyList()
                : syn.getBody().getMetaValues().stream().map(mv -> new ExpandedSynonymValue(mv, null, 1, true)).toList();

        return new ExpandedSynonym(
                sources,
                values,
                syn.getBody() == null ? new ArrayList<>() : syn.getBody().getHints(),
                syn.getBody() == null ? null : syn.getBody().getMerge(),
                metaValues,
                syn.getBody() == null ? null : syn.getBody().getMappingLogic(),
                syn.getBody() == null ? null : syn.getBody().getMapper(),
                syn.getBody() == null ? null : syn.getBody().getFormat(),
                syn.getBody() == null ? null : syn.getBody().getPatternMatch(),
                syn.getBody() == null ? null : syn.getBody().getPatternReplace(),
                syn.getBody() != null && syn.getBody().isRemoveHtml()
        );
    }

    public static ExpandedSynonym toRosettaExpandedSynonym(RosettaExternalClassSynonym syn) {
        List<ExpandedSynonymValue> synVals =
                syn.getValue() == null
                        ? Collections.emptyList()
                        : new ArrayList<>(List.of(new ExpandedSynonymValue(syn.getValue().getName(), syn.getValue().getPath(), syn.getValue().getMaps(), false)));
        List<ExpandedSynonymValue> synMetaVals =
                syn.getMetaValue() == null
                        ? Collections.emptyList()
                        : new ArrayList<>(List.of(new ExpandedSynonymValue(syn.getMetaValue().getName(), syn.getMetaValue().getPath(), syn.getMetaValue().getMaps(), true)));

        RosettaExternalClass externalClass = (RosettaExternalClass) syn.eContainer();
        RosettaExternalSynonymSource externalSynonymSource = (RosettaExternalSynonymSource) externalClass.eContainer();
        List<RosettaSynonymSource> superSynonyms = externalSynonymSource.getSuperSynonymSources();

        List<RosettaSynonymSource> sources = new ArrayList<>();
        sources.add(externalSynonymSource);
        if (superSynonyms != null) {
            sources.addAll(superSynonyms);
        }

        return new ExpandedSynonym(
                sources,
                synVals,
                new ArrayList<>(),
                null,
                synMetaVals,
                null,
                null,
                null,
                null,
                null,
                false
        );
    }

    public static ExpandedSynonym toRosettaExpandedSynonym(RosettaClassSynonym syn) {
        List<ExpandedSynonymValue> synVals =
                syn.getValue() == null
                        ? Collections.emptyList()
                        : new ArrayList<>(List.of(new ExpandedSynonymValue(syn.getValue().getName(), syn.getValue().getPath(), syn.getValue().getMaps(), false)));
        List<ExpandedSynonymValue> synMetaVals =
                syn.getMetaValue() == null
                        ? Collections.emptyList()
                        : new ArrayList<>(List.of(new ExpandedSynonymValue(syn.getMetaValue().getName(), syn.getMetaValue().getPath(), syn.getMetaValue().getMaps(), true)));

        return new ExpandedSynonym(
                syn.getSources(),
                synVals,
                new ArrayList<>(),
                null,
                synMetaVals,
                null,
                null,
                null,
                null,
                null,
                false
        );
    }

    private static boolean isEnumeration(RosettaTypedFeature a) {
        return a.getTypeCall() != null && a.getTypeCall().getType() instanceof RosettaEnumeration;
    }

    private static RosettaSynonymValueBase[] toArrayOrEmpty(List<RosettaSynonymValueBase> list) {
        if (list == null || list.isEmpty()) return null;
        return list.toArray(new RosettaSynonymValueBase[0]);
    }
}
