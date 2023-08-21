package com.regnosys.rosetta.blueprints;

import java.util.Collection;
import java.util.List;

import com.regnosys.rosetta.blueprints.runner.data.GroupableData;
import com.rosetta.model.lib.RosettaModelObject;

public interface DataItemReportBuilder {

	<T> RosettaModelObject buildReport(Collection<GroupableData<?, T>> reportData);
	<T> List<? extends RosettaModelObject> buildReportList(Collection<GroupableData<?, T>> reportData);
}
