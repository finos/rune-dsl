package com.regnosys.rosetta.blueprints;

import com.regnosys.rosetta.blueprints.runner.data.GroupableData;
import com.regnosys.rosetta.blueprints.runner.data.StringIdentifier;
import com.rosetta.model.lib.RosettaModelObject;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.rosetta.util.CollectionUtils.emptyIfNull;


public class BlueprintReport {

    private final String blueprintName;
    private final Object reportData;
    private final Collection<? extends GroupableData<?, ?>> traceData;
    private final ReportTypeBuilder reportTypeBuilder;

    public BlueprintReport(String blueprintName, Object reportData, Collection<? extends GroupableData<?, ?>> tradeData, ReportTypeBuilder reportTypeBuilder) {
        this.blueprintName = blueprintName;
        this.reportData = reportData;
        this.traceData = tradeData;
        this.reportTypeBuilder = reportTypeBuilder;
    }

    public Object getReportData() {
        return reportData;
    }

    public Collection<? extends GroupableData<?, ?>> getTraceData() {
        return traceData;
    }

    public String getBlueprintName() {
        return blueprintName;
    }
    
//    public Map<String, RosettaModelObject> getReport() {
//    	List<Map<StringIdentifier, GroupableData<?, String>>> useCaseReportData = (List<Map<StringIdentifier, GroupableData<?, String>>>) reportData;
//    	
//    	if (useCaseReportData != null) {
//    		useCaseReportData.stream()
//    			.collect(data -> data.getKey(), data -> Optional.ofNullable(reportTypeBuilder)
//        		.map(b -> b.buildReport((data)));
//
//    	}
//        return null;
//    }
}
