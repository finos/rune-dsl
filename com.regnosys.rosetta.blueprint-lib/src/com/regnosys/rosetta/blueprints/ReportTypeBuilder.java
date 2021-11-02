package com.regnosys.rosetta.blueprints;

import java.util.Map;

import com.regnosys.rosetta.blueprints.runner.data.GroupableData;
import com.regnosys.rosetta.blueprints.runner.data.StringIdentifier;
import com.rosetta.model.lib.RosettaModelObject;

public interface ReportTypeBuilder {

	RosettaModelObject buildReport(Map<StringIdentifier, GroupableData<?, String>> reportData);
}
