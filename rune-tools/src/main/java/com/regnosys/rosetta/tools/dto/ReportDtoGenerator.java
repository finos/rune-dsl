package com.regnosys.rosetta.tools.dto;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.regnosys.rosetta.generator.java.types.JavaTypeTranslator;
import com.regnosys.rosetta.rosetta.RosettaModel;
import com.regnosys.rosetta.rosetta.RosettaReport;
import com.regnosys.rosetta.transgest.ModelLoader;
import com.regnosys.rosetta.types.*;
import com.regnosys.rosetta.utils.ModelIdProvider;
import com.rosetta.util.DottedPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

public class ReportDtoGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReportDtoGenerator.class);

    private final ModelLoader modelLoader;
    private final RObjectFactory rObjectFactory;
    private final JavaTypeTranslator javaTypeTranslator;
    private final ModelIdProvider modelIdProvider;

    @Inject
    public ReportDtoGenerator(ModelLoader modelLoader, RObjectFactory rObjectFactory, JavaTypeTranslator javaTypeTranslator) {
        this.modelLoader = modelLoader;
        this.rObjectFactory = rObjectFactory;
        this.javaTypeTranslator = javaTypeTranslator;
        this.modelIdProvider = new ModelIdProvider();
    }

    public Multimap<RType, RAttribute> generateReportDtos(List<RosettaModel> models, DottedPath namespace) {
        List<Multimap<RType, RAttribute>> collectedReportAttributes = findReports(models, namespace)
                .stream()
                .map(report -> {
                    //Need to provide a unique visitor storage for each report otherwise the reports have a side effect on each other as some reports have fields labeled and some don't
                    Multimap<RType, RAttribute> collectedTypeAttributes = HashMultimap.create();
                    LOGGER.info("Collecting types and attributes for report {}", modelIdProvider.getReportId(report));
                    RDataType rReportType = rObjectFactory.buildRDataType(report.getReportType());
                    collectTypeAttributes(rReportType, rReportType, collectedTypeAttributes);
                    return collectedTypeAttributes;
                }).toList();

        Multimap<RType, RAttribute> aggregatedTypeAttributes = HashMultimap.create();
        for (Multimap<RType, RAttribute> multimap : collectedReportAttributes) {
            aggregatedTypeAttributes.putAll(multimap);
        }
        return aggregatedTypeAttributes;
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

    private void collectTypeAttributes(RDataType topLevelReportType, RDataType parentDataType, Multimap<RType, RAttribute> visitor) {

    }

}
