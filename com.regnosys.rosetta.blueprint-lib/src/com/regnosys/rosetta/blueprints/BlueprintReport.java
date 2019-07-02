package com.regnosys.rosetta.blueprints;

import com.regnosys.rosetta.blueprints.runner.data.GroupableData;

import java.util.Collection;

public class BlueprintReport {

    private final String blueprintName;
    private final Object reportData;
    private final Collection<? extends GroupableData<?, ?>> traceData;

    public BlueprintReport(String blueprintName, Object reportData, Collection<? extends GroupableData<?, ?>> tradeData) {
        this.blueprintName = blueprintName;
        this.reportData = reportData;
        this.traceData = tradeData;
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
}
