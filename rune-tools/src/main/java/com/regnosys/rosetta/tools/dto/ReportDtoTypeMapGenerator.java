package com.regnosys.rosetta.tools.dto;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.RosettaReport;
import com.regnosys.rosetta.rules.RuleReferenceService;
import com.regnosys.rosetta.types.RAttribute;
import com.regnosys.rosetta.types.RDataType;
import com.regnosys.rosetta.types.RObjectFactory;
import com.regnosys.rosetta.types.RType;
import com.regnosys.rosetta.utils.ModelIdProvider;
import com.rosetta.util.DottedPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ReportDtoTypeMapGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReportDtoTypeMapGenerator.class);

    private final RObjectFactory rObjectFactory;
    private final RuleReferenceService ruleService;
    private final ModelIdProvider modelIdProvider;

    @Inject
    public ReportDtoTypeMapGenerator(RObjectFactory rObjectFactory, RuleReferenceService ruleService) {
        this.rObjectFactory = rObjectFactory;
        this.ruleService = ruleService;
        this.modelIdProvider = new ModelIdProvider();
    }

    public Multimap<RType, RAttribute> generateReportDtoTypeMap(List<RosettaModel> models, DottedPath namespace) {
        List<Multimap<RType, RAttribute>> collectedReportAttributes = findReports(models, namespace)
                .stream()
                .map(report -> {
                    //Need to provide a unique visitor storage for each report otherwise the reports have a side effect on each other as some reports have fields labeled and some don't
                    LOGGER.info("Collecting types and attributes for report {}", modelIdProvider.getReportId(report));
                    RDataType rReportType = rObjectFactory.buildRDataType(report.getReportType());

                    Set<RAttribute> includedAttributes = traverse(report, rReportType);
                    Multimap<RType, RAttribute> collectedTypeAttributes = HashMultimap.create();
                    collectTypeAttributes(rReportType, includedAttributes, collectedTypeAttributes);
                    return collectedTypeAttributes;
                }).toList();

        Multimap<RType, RAttribute> aggregatedTypeAttributes = HashMultimap.create();
        for (Multimap<RType, RAttribute> multimap : collectedReportAttributes) {
            aggregatedTypeAttributes.putAll(multimap);
        }
        return aggregatedTypeAttributes;
    }

    private Set<RAttribute> traverse(RosettaReport report, RDataType rReportType) {
        return ruleService.<Set<RAttribute>>traverse(
                report.getRuleSource(),
                rReportType,
                new HashSet<>(),
                (acc, context) -> {
                    if (!context.isExplicitlyEmpty()) {
                        acc.addAll(context.getPath());
                    }
                    return acc;
                }
        );
    }

    private Set<RosettaReport> findReports(List<RosettaModel> models, DottedPath namespace) {
        return models.stream()
                .map(RosettaModel::getElements)
                .flatMap(Collection::stream)
                .filter(RosettaReport.class::isInstance)
                .map(RosettaReport.class::cast)
                .filter(r -> modelIdProvider.toDottedPath(r.getModel()).startsWith(namespace))
                .collect(Collectors.toSet());
    }

    private void collectTypeAttributes(RDataType parentDataType, Set<RAttribute> includedAttributes,  Multimap<RType, RAttribute> visitor) {
        parentDataType.getAllAttributes()
                .forEach(attribute -> {
                    RType attributeType = attribute.getRMetaAnnotatedType().getRType();
                    if (attributeType instanceof RDataType childDataType) {
                        // collect attributes for child type
                        collectTypeAttributes(childDataType, includedAttributes, visitor);
                        // if the child has reported attributes, then add parent attribute
                        Collection<RAttribute> collectedChildAttributes = visitor.get(childDataType);
                        if (!collectedChildAttributes.isEmpty()) {
                            visitor.put(parentDataType, attribute);
                        }
                    } else {
                        if (canAddAttribute(includedAttributes, attribute)) {
                            visitor.put(parentDataType, attribute);
                        }
                    }
                });
    }

    private boolean canAddAttribute(Set<RAttribute> includedAttributes, RAttribute attribute) {
        return includedAttributes.contains(attribute);
    }

}
