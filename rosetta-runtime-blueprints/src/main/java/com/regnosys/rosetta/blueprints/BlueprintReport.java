package com.regnosys.rosetta.blueprints;

import java.util.Collection;

import com.regnosys.rosetta.blueprints.runner.data.GroupableData;

public class BlueprintReport {

    private final String blueprintName;
    private final Object reportData;
    private final Collection<? extends GroupableData<?, ?>> traceData;
    private final DataItemReportBuilder dataItemReportBuilder;

    public BlueprintReport(String blueprintName, 
    		Object reportData, 
    		Collection<? extends GroupableData<?, ?>> tradeData, 
			DataItemReportBuilder dataItemReportBuilder) {
        this.blueprintName = blueprintName;
        this.reportData = reportData;
        this.traceData = tradeData;
        this.dataItemReportBuilder = dataItemReportBuilder;
    }

    public String getBlueprintName() {
        return blueprintName;
    }
    
    public Object getReportData() {
        return reportData;
    }
    
    public Collection<? extends GroupableData<?, ?>> getTraceData() {
        return traceData;
    }
    
    public DataItemReportBuilder getDataItemReportBuilder() {
    	return dataItemReportBuilder;
    }
}
